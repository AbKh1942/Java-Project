package de.uni.trafficsim.statistics;
import org.eclipse.sumo.libtraci.Edge;
import org.eclipse.sumo.libtraci.Lane;
import java.util.List;


/**
 * Wrapper around SUMO edge/lane TraCI calls.
 * <p>
 * Provides access to per-edge metrics used by the statistics collector.
 */
public final class SumoEdgeApi {

    /**
    * Returns all edge IDs from SUMO.
    *
     * @return list of edge IDs
     */
    public List<String> getIdList() {
        return Edge.getIDList();                                
    }

    /**
     * Returns the vehicle count on an edge from the last step.
     *
     * @param edgeId edge ID
     * @return number of vehicles
     */
    public int getLastStepVehicleNumber(String edgeId) {
        return Edge.getLastStepVehicleNumber(edgeId);           //How many vehicles were on this edge in the last step?
    }

    /**
     * Returns the mean speed on an edge from the last step.
     *
     * @param edgeId edge ID
     * @return mean speed in m/s
     */
    public double getLastStepMeanSpeed(String edgeId) {
        return Edge.getLastStepMeanSpeed(edgeId);               
    }

    /**
     * Returns the occupancy percentage on an edge from the last step.
     *
     * @param edgeId edge ID
     * @return occupancy percentage (0–100), or -1 if unavailable
     */
    public double getLastStepOccupancy(String edgeId) {
        return Edge.getLastStepOccupancy(edgeId);               
    }

    /**
     * Returns the length of the edge in meters (using lane 0).
     * onverting edgeId from StatsCollector to laneId format.
     * 
     * @param edgeId edge ID
     * @return length in meters
     */
    public double getLengthMeters(String edgeId) {
        String laneId = edgeId + "_0";                           
        return Lane.getLength(laneId);
    }

}