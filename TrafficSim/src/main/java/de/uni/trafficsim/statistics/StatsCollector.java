package de.uni.trafficsim.statistics;
import de.uni.trafficsim.model.VehicleWrapper;
import java.util.*;

public final class StatsCollector {

    private final SumoEdgeApi edgeApi;                      //for reading edge-data from SUMO
    private final List<String> edgeIds;

    //Constructor
    public StatsCollector(SumoEdgeApi edgeApi) {
        this.edgeApi = Objects.requireNonNull(edgeApi);
        this.edgeIds = List.copyOf(edgeApi.getIdList());     //initializing edge collection, Copies edge ID list once and makes it immutable
    }

    //main method: creates a snapshot row (StatsSnapshot) for a frame
    public StatsSnapshot collect(double simTimeSec,
                                 Collection<VehicleWrapper> vehicles,
                                 int arrivedVehiclesTotal) {
        Objects.requireNonNull(vehicles);                   //No Null Collection

        int totalVehicles = vehicles.size();                //get vehicle count from vehicle manager


        double sumSpeed = 0.0;
        int stoppedVehicles = 0;
        double sumCo2 = 0.0;
        double sumFuel = 0.0;

        for (VehicleWrapper v : vehicles) { //for each vehicleWrapper Object in the collection vehicles
            //cumulative speed
            double speed = v.getSpeed();
            sumSpeed = sumSpeed + speed;

            //count stopped vehicles
            if (speed < 0.1) {
                stoppedVehicles++;
            }

            //cummulative emmisions and fuel
            sumCo2 = sumCo2 + v.getCo2();       //get Co2 from SUMO
            sumFuel = sumFuel + v.getFuel();    //get Fuel from SUMO
        }

        //global average speed
        double globalAvgSpeedMs;
        if (totalVehicles == 0) {               //if vehicles is empty-> set to 0.
            globalAvgSpeedMs = 0.0;
        } else {
            globalAvgSpeedMs = sumSpeed / totalVehicles;
        }

        //co2 and Fuel in kg?
        double totalCo2Kg = sumCo2 / 1000.0;
        double totalFuelL = sumFuel / 1000.0;



        //Per-edge Snapshots
        Map<String, EdgeSnapshot> edges = new HashMap<>(edgeIds.size());                    //Mapping edgeIds to EdgeSnapshot
        for (String edgeId : edgeIds) {                                                     //for every edgeId in List
            int n = edgeApi.getLastStepVehicleNumber(edgeId);                               //n = number of vehicles in edge
            double meanSpeedMs = edgeApi.getLastStepMeanSpeed(edgeId);                      //avg speed on edge

            double occupancy;
            try {
                occupancy = edgeApi.getLastStepOccupancy(edgeId);                            //get occupancy
            } catch (Exception ex) {
                occupancy = -1.0;                                                            //-1 if not available
            }

            //calculating density per km
            double lengthMeters = edgeApi.getLengthMeters(edgeId);

            double densityPerKm;
            if (lengthMeters > 0) {
                densityPerKm = n / (lengthMeters / 1000.0);   // vehicles per km
            } else {
                densityPerKm = 0.0;
            }


            edges.put(edgeId, new EdgeSnapshot(n, meanSpeedMs, occupancy, densityPerKm)); //builds EdgeSnapshot Object with
                                                                                          //edge data and saves it in the Map
        }

        //build Snapshot
        return new StatsSnapshot(
                simTimeSec,
                globalAvgSpeedMs,
                totalVehicles,
                stoppedVehicles,
                totalCo2Kg,
                totalFuelL,
                arrivedVehiclesTotal,
                Map.copyOf(edges)
        );

    }
}