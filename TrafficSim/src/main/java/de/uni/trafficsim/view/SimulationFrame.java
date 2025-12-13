package de.uni.trafficsim.view;

import de.uni.trafficsim.model.TrafficLightWrapper;
import org.eclipse.sumo.libtraci.TraCIPosition;

import java.util.*;

public class SimulationFrame {
    public Map<String, TraCIPosition> vehiclePositions = new HashMap<>();
    public Map<String, Double> vehicleAngles = new HashMap<>();
    public List<TrafficLightWrapper> trafficLights = new ArrayList<>();
}
