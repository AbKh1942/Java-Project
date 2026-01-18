package de.uni.trafficsim.statistics;
import de.uni.trafficsim.model.VehicleWrapper;
import java.util.*;


/**
 * Collects aggregated simulation statistics for a single time step.
 * <p>
 * Computes global vehicle metrics and per-edge snapshots using a SUMO edge API.
 */
public final class StatsCollector {

    private final SumoEdgeApi edgeApi;                      //for reading edge-data from SUMO
    private final List<String> edgeIds;

    private double sumCo2 = 0.0;
    private double sumFuel = 0.0;

    /**
     * Constructor.
     * Creates a collector using the given edge API.
     *
     * @param edgeApi API used to query per-edge statistics
     */
    public StatsCollector(SumoEdgeApi edgeApi) {
        this.edgeApi = Objects.requireNonNull(edgeApi);
        this.edgeIds = List.copyOf(edgeApi.getIdList());     //initializing edge collection, Copies edge ID list once and makes it immutable
    }

    
    /**
     * Main method: creates a snapshot row (StatsSnapshot) for a frame
     * Collects statistics for the current simulation step.
     *
     * @param simTimeSec current simulation time in seconds
     * @param vehicles vehicles present in the simulation
     * @param arrivedVehiclesTotal cumulative count of arrived vehicles
     * @return snapshot of aggregated statistics
     */
    public StatsSnapshot collect(double simTimeSec,
                                 Collection<VehicleWrapper> vehicles,
                                 int arrivedVehiclesTotal) {
        Objects.requireNonNull(vehicles);                   //No Null Collection

        int totalVehicles = vehicles.size();                //get vehicle count from vehicle manager

        double sumSpeed = 0.0;
        int stoppedVehicles = 0;

        for (VehicleWrapper v : vehicles) { //for each vehicleWrapper Object in the collection vehicles
            //cumulative speed
            double speed = v.getSpeed();
            sumSpeed = sumSpeed + speed;

            //count stopped vehicles
            if (speed < 0.1) {
                stoppedVehicles++;
            }

            //cummulative emmisions and fuel
            sumCo2 += v.getCo2() / 1_000_000.0;       //get Co2 from SUMO
            sumFuel += v.getFuel() / 1_000_000.0;    //get Fuel from SUMO
        }

        //global average speed
        double globalAvgSpeedMs;
        if (totalVehicles == 0) {               //if vehicles is empty-> set to 0.
            globalAvgSpeedMs = 0.0;
        } else {
            globalAvgSpeedMs = sumSpeed / totalVehicles;
        }

        //Per-edge Snapshots
        Map<String, EdgeSnapshot> edges = getStringEdgeSnapshotMap();

        //build Snapshot
        return new StatsSnapshot(
                simTimeSec,
                globalAvgSpeedMs,
                totalVehicles,
                stoppedVehicles,
                sumCo2,
                sumFuel,
                arrivedVehiclesTotal,
                Map.copyOf(edges)
        );
    }


    /**
     * Builds a map of per-edge statistics for the current step.
     * <p>
     * For each edge ID, queries SUMO for vehicle count, mean speed, occupancy,
     * and length, computes density (vehicles per km), and stores the results
     * in an {@link EdgeSnapshot}.
     *
     * @return map of edge ID to computed edge statistics
     */
    private Map<String, EdgeSnapshot> getStringEdgeSnapshotMap() {
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
            //builds EdgeSnapshot Object with edge data and saves it in the Map
            edges.put(edgeId, new EdgeSnapshot(n, meanSpeedMs, occupancy, densityPerKm));
        }
        return edges;
    }
}