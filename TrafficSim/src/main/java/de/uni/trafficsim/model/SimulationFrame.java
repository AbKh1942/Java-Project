package de.uni.trafficsim.model;

import de.uni.trafficsim.manager.VehicleManager;
import org.eclipse.sumo.libtraci.TraCIPosition;

import java.util.*;

public class SimulationFrame {
    public List<TrafficLightWrapper> trafficLights = new ArrayList<>();
    public VehicleManager vehicleManager = new VehicleManager();

    // Cache for UI dropdowns
    public List<String> availableRoutes = new ArrayList<>();
    public List<String> availableTypes = new ArrayList<>();
}
