package de.uni.trafficsim.manager;

import de.uni.trafficsim.model.Vehicle;         //import Vehicle class

import java.util.Collections;
import java.util.Map;                           //importing Map Data structure  
import java.util.concurrent.ConcurrentHashMap;  //concurrentHashMap is thread safe, so GUI and simulation can work parallel, while SUMO works stepwise


/*
Changes from original UML Design:
move getSpeed, getPosition, getColor into vehicle class
only addVehicle, removeVehicle, getVehiclebyfilter in manager
add method getAllVehicles, that returns the whole map of ids and vehicle object
*/

/*
Holds all vehicle wrappers that are currently relevant for the simulation.
The manager itself does not talk to TraCI. That work is handled by the
Vehicle instances.
*/

public class VehicleManager {                                                   //class for handling all vehicle entities in our simulation, used by SimulationController
    private final Map<String, Vehicle> vehicles = new ConcurrentHashMap<>();    //Map: Key = id (String), value = Vehicle-Object. Final: Map-reference cannot be changed, but the map-contents can be changed (add/remove)

    public void addVehicle(Vehicle vehicle, String routeId) {                   //injecting new vehicle
        if (vehicle == null) {                                                  //makes sure input cant be NULL
            throw new IllegalArgumentException("vehicle must not be null");
        }
        vehicle.spawnVehicle(routeId);
        vehicles.put(vehicle.getId(), vehicle);                                 //calls vehicles.put() method from JAva-Map to add a new entry(id, vehicle) to the Hashmap
    }

    public Vehicle removeVehicle(String vehicleId) {                            //remove Vehicle
        return vehicles.remove(vehicleId);                                      //calls Java-Map method vehicles.remove() to remove entry from the hashmap, returns removed vehicle
    }

    public Map<String, Vehicle> getAllVehicles() {
        return Collections.unmodifiableMap(vehicles);                           //unmodifiableMaps -> can only be read, not changed
    }                                                                           //Collections allows to store multiple Objects (Java Standard Library)

    /*
    Placeholder for logic for getting vehicles by filter
    Will be implemented later.
     */
    public Map<String, Vehicle> getVehicleByFilter() {                          //not imlplemented yet
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
