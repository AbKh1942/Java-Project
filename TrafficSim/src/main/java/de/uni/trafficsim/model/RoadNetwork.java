package de.uni.trafficsim.model;

import org.eclipse.sumo.libtraci.*;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoadNetwork {
    // Map of Lane ID -> Java Shape object
    private final Map<String, Shape> laneShapes = new HashMap<>();
    // Map: TLS_ID -> List of Stop Line Positions (one per controlled lane index)
    private final Map<String, List<TraCIPosition>> tlsStopLines = new HashMap<>();

    public Map<String, Shape> getLaneShapes() {
        return laneShapes;
    }
    public Map<String, List<TraCIPosition>> getTlsStopLines() {
        return tlsStopLines;
    }

    public void loadFromSumo() {
        // 1. Load Lane Geometries
        loadLanes();

        // 2. Load Traffic Light Geometries
        loadTrafficLights();
    }

    private void loadLanes() {
        System.out.println("Loading static road network from SUMO...");

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
        System.out.println("Loaded " + laneShapes.size() + " lanes.");
    }


    private void loadTrafficLights() {
        System.out.println("Loading traffic light positions...");
        StringVector tlsIds = TrafficLight.getIDList();

        for (String tid : tlsIds) {
            // Get all lanes controlled by this TLS. The order corresponds to the state string indices.
            StringVector controlledLanes = TrafficLight.getControlledLanes(tid);
            List<TraCIPosition> positions = new ArrayList<>();

            if (!controlledLanes.isEmpty()) {
                for (String laneId : controlledLanes) {
                    TraCIPositionVector shape = Lane.getShape(laneId);

                    // The traffic light is typically at the end of the lane (stop line)
                    if (!shape.getValue().isEmpty()) {
                        // Directly adding shape.get() stores a reference to memory that gets freed
                        // when 'shape' (the vector) goes out of scope, causing positions to become (0,0).
                        TraCIPosition rawPos = shape.getValue().get(shape.getValue().size() - 1);
                        TraCIPosition pos = new TraCIPosition();
                        pos.setX(rawPos.getX());
                        pos.setY(rawPos.getY());
                        positions.add(pos);
                    } else {
                        TraCIPosition position = new TraCIPosition();
                        position.setX(0);
                        position.setY(0);
                        // Fallback if lane has no shape
                        positions.add(position);
                    }
                }
                tlsStopLines.put(tid, positions);
            }
        }
        System.out.println("Loaded positions for " + tlsStopLines.size() + " traffic light systems.");
    }
}