package de.uni.trafficsim.model;

import de.uni.trafficsim.manager.VehicleManager;
import de.uni.trafficsim.model.TrafficLight.TrafficLightWrapper;

import java.util.*;

/**
 * Snapshot of simulation data for a single time step.
 * <p>
 * Holds the current traffic lights, vehicles, and cached route/type IDs
 * used by the UI.
 */
public class SimulationFrame {
    public List<TrafficLightWrapper> trafficLights = new ArrayList<>();
    public VehicleManager vehicleManager = new VehicleManager();

    // Cache for UI dropdowns
    public List<String> availableRoutes = new ArrayList<>();
    public List<String> availableTypes = new ArrayList<>();
}
