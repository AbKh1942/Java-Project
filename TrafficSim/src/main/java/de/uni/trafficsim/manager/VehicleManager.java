package de.uni.trafficsim.manager;

import de.uni.trafficsim.App;
import de.uni.trafficsim.model.VehicleWrapper;         //import Vehicle class
import org.eclipse.sumo.libtraci.Vehicle;

import java.util.ArrayList;
import java.util.List;


/*
Changes from original UML Design:
move getSpeed, getPosition, getColor into vehicle class
only addVehicle, removeVehicle, getVehiclebyfilter in manager
add method getAllVehicles, that returns the whole map of ids and vehicle object
*/


/**
 * Manages vehicle wrappers for the current simulation frame.
 * Holds all vehicle wrappers that are currently relevant for the simulation.
 * Manager itself does not talk to TraCI, that work is handled by the vehicle instances.
 * <p>
 * Provides methods to store vehicles and inject new ones into SUMO.
 */
public class VehicleManager {
    
    // List with all Vehicles in simulation
    private final List<VehicleWrapper> vehicles = new ArrayList<>();

    /**
     * Injects a vehicle into the SUMO simulation.
     *
     * @param vehicle vehicle wrapper to add
     * @param typeId SUMO vehicle type ID
     * @throws IllegalArgumentException if vehicle is null
     */
    public void addVehicleToSimulation(VehicleWrapper vehicle, String typeId) {
        // makes sure input can`t be NULL
        if (vehicle == null) {
            throw new IllegalArgumentException("vehicle must not be null");
        }
        // libtraci Vehicle`s methods called
        Vehicle.add(vehicle.getId(), vehicle.getRoute(), typeId, "now", "first", "base", "0");
        Vehicle.setColor(vehicle.getId(), vehicle.getTraCIColor());
        App.logger.info("Injected vehicle {} on route {}", vehicle.getId(), vehicle.getRoute());
    }

    /**
     * Adds a vehicle wrapper to the local list.
     *
     * @param vehicle vehicle to store
     */
    public void addVehicle(VehicleWrapper vehicle) {
        vehicles.add(vehicle);
    }

    public List<VehicleWrapper> getVehicles() {
        return vehicles;
    }

    /**
     * Removes a vehicle wrapper from the local list.
     *
     * @param vehicle vehicle to remove
     * @return true if the vehicle was present and removed
     */
    public boolean removeVehicle(VehicleWrapper vehicle) {
        //calls Java-Map method vehicles.remove() to remove entry from the list
        return vehicles.remove(vehicle);
    }

}
