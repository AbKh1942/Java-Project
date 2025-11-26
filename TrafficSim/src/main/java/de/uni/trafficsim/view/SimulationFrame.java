package de.uni.trafficsim.view;

import org.eclipse.sumo.libtraci.TraCIPosition;

import java.util.HashMap;
import java.util.Map;

public class SimulationFrame {
    public Map<String, TraCIPosition> vehiclePositions = new HashMap<>();
    public Map<String, Double> vehicleAngles = new HashMap<>();
    public Map<String, String> tlsStates = new HashMap<>();
    public Map<String, TraCIPosition> tlsPositions = new HashMap<>();
}
