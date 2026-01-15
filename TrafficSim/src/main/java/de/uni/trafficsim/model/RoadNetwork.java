package de.uni.trafficsim.model;

import de.uni.trafficsim.App;
import org.eclipse.sumo.libtraci.*;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoadNetwork {
    public static class SignalData {
        public TraCIPosition pos;
        public double angle;
        public SignalData(TraCIPosition p, double a) { pos=p; angle=a; }
    }

    // Map of Lane ID -> Java Shape object
    private final Map<String, Shape> laneShapes = new HashMap<>();
    // Map: TLS_ID -> List of Stop Line Positions (one per controlled lane index)
    public Map<String, List<SignalData>> tlsStopLines = new HashMap<>();

    public Map<String, Shape> getLaneShapes() {
        return laneShapes;
    }

    public Map<String, List<SignalData>> getTlsStopLines() {
        return tlsStopLines;
    }

    public void loadFromSumo() {
        // 1. Load Lane Geometries
        loadLanes();

        // 2. Load Traffic Light Geometries
        loadTrafficLights();
    }

    private void loadLanes() {
        App.logger.info("Loading static road network from SUMO...");

        // 1. Get all Lane IDs from SUMO
        StringVector laneIds = Lane.getIDList();
        for (String id : laneIds) {
            // 2. Get the shape (geometry) of the lane
            TraCIPositionVector shapeVector = Lane.getShape(id);
            double width = Lane.getWidth(id);

            // 3. Convert SUMO coordinates to a Java Path
            Path2D path = new Path2D.Double();
            if (!shapeVector.getValue().isEmpty()) {
                TraCIPosition start = shapeVector.getValue().get(0);
                path.moveTo(start.getX(), start.getY());

                for (int j = 1; j < shapeVector.getValue().size(); j++) {
                    TraCIPosition p = shapeVector.getValue().get(j);
                    path.lineTo(p.getX(), p.getY());
                }
            }

            // Create a stroke based on lane width (approximate scaling)
            // We use a stroke width of 'width' assuming 1 pixel = 1 meter for simplicity
            // or we just draw the centerline. Here we create a stroked shape for thickness.
            laneShapes.put(id, new BasicStroke((float) width).createStrokedShape(path));
        }
        App.logger.info("Loaded {} lanes.", laneShapes.size());
    }


    private void loadTrafficLights() {
        App.logger.info("Loading traffic light positions...");
        StringVector tlsIds = TrafficLight.getIDList();

        for (String tid : tlsIds) {
            // Get all lanes controlled by this TLS. The order corresponds to the state string indices.
            StringVector controlledLanes = TrafficLight.getControlledLanes(tid);
            List<SignalData> positions = new ArrayList<>();

            // Initialize a list with placeholders
            List<String> laneIdsForIndex = new ArrayList<>();
            for (String controlledLane : controlledLanes) {
                laneIdsForIndex.add(controlledLane);
                TraCIPosition newPos = new TraCIPosition();
                newPos.setX(0);
                newPos.setY(0);
                positions.add(new SignalData(newPos, 0));
            }

            // Group signal indices by their Lane ID
            Map<String, List<Integer>> laneToIndices = new HashMap<>();
            for (int j = 0; j < laneIdsForIndex.size(); j++) {
                String laneId = laneIdsForIndex.get(j);
                laneToIndices.computeIfAbsent(laneId, k -> new ArrayList<>()).add(j);
            }

            // Calculate positions with offsets for overlapping signals
            for (Map.Entry<String, List<Integer>> entry : laneToIndices.entrySet()) {
                String laneId = entry.getKey();
                List<Integer> indices = entry.getValue();

                TraCIPositionVector shape = Lane.getShape(laneId);
                if (shape.getValue().size() < 2) continue;

                TraCIPosition end = shape.getValue().get(shape.getValue().size() - 1);
                TraCIPosition beforeEnd = shape.getValue().get(shape.getValue().size() - 2);

                // Calculate Lane Direction
                double dx = end.getX() - beforeEnd.getX();
                double dy = end.getY() - beforeEnd.getY();
                double len = Math.sqrt(dx * dx + dy * dy);

                // Perpendicular vector (Right side relative to a driving direction)
                double perpX = -dy / len;
                double perpY = dx / len;
                double angle = Math.toDegrees(Math.atan2(perpY, perpX));

                // Spread signals out: if multiple signals on one lane, shift them
                // e.g., spacing 1.5 m apart centered on the lane end
                double spacing = 1.5;
                double startOffset = -(indices.size() - 1) * spacing / 2.0;

                for (int k = 0; k < indices.size(); k++) {
                    int globalIndex = indices.get(k);
                    double offset = startOffset + k * spacing;

                    double finalX = end.getX() + perpX * offset;
                    double finalY = end.getY() + perpY * offset;
                    TraCIPosition newPos = new TraCIPosition();
                    newPos.setX(finalX);
                    newPos.setY(finalY);
                    positions.set(globalIndex, new SignalData(newPos, angle));
                }
            }
            tlsStopLines.put(tid, positions);
        }
        App.logger.info("Loaded positions for {} traffic light systems.", tlsStopLines.size());
    }
}