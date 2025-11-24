package de.uni.trafficsim.model;

import org.eclipse.sumo.libtraci.Lane;
import org.eclipse.sumo.libtraci.StringVector;
import org.eclipse.sumo.libtraci.TraCIPosition;
import org.eclipse.sumo.libtraci.TraCIPositionVector;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.Map;

public class RoadNetwork {
    // Map of Lane ID -> Java Shape object
    public Map<String, Shape> laneShapes = new HashMap<>();

    public void loadFromSumo() {
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
}