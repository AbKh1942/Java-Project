package de.uni.trafficsim.manager;

import de.uni.trafficsim.model.VehicleWrapper;         //import Vehicle class
import org.eclipse.sumo.libtraci.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

//class for handling all vehicle entities in our simulation, used by SimulationController
public class VehicleManager {
    //Map: Key = id (String), value = Vehicle-Object. Final: Map-reference cannot be changed, but the map-contents can be changed (add/remove)
    private final List<VehicleWrapper> vehicles = new ArrayList<>();

    //injecting new vehicle
    public void addVehicleToSimulation(VehicleWrapper vehicle, String typeId) {
        //makes sure input cant be NULL
        if (vehicle == null) {
            throw new IllegalArgumentException("vehicle must not be null");
        }
        //calls vehicles.put() method from JAva-Map to add a new entry(id, vehicle) to the Hashmap
        Vehicle.add(vehicle.getId(), vehicle.getRoute(), typeId, "now", "first", "0", "0");
        Vehicle.setColor(vehicle.getId(), vehicle.getTraCIColor());
        System.out.println("Injected vehicle " + vehicle.getId() + " on route " + vehicle.getRoute());
    }

    public void addVehicle(VehicleWrapper vehicle) {
        vehicles.add(vehicle);
    }

    public List<VehicleWrapper> getVehicles() {
        return vehicles;                           //unmodifiableMaps -> can only be read, not changed
    }                                                                           //Collections allows to store multiple Objects (Java Standard Library)

    public boolean removeVehicle(VehicleWrapper vehicle) {                            //remove Vehicle
        return vehicles.remove(vehicle);                                      //calls Java-Map method vehicles.remove() to remove entry from the hashmap, returns removed vehicle
    }

    /*
    Placeholder for logic for getting vehicles by filter
    Will be implemented later.
     */
    public Map<String, VehicleWrapper> getVehicleByFilter() {                          //not imlplemented yet
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
