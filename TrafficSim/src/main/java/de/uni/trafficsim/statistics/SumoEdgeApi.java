package de.uni.trafficsim.statistics;
import org.eclipse.sumo.libtraci.Edge;
import org.eclipse.sumo.libtraci.Lane;
import java.util.List;



public final class SumoEdgeApi {

    public List<String> getIdList() {
        return Edge.getIDList();                                //returns List od edgeIDs from Sumo
    }

    public int getLastStepVehicleNumber(String edgeId) {
        return Edge.getLastStepVehicleNumber(edgeId);           //How many vehicles were on this edge in the last step?
    }

    public double getLastStepMeanSpeed(String edgeId) {
        return Edge.getLastStepMeanSpeed(edgeId);               //Average speed on the edge in the last step (m/s).
    }

    public double getLastStepOccupancy(String edgeId) {
        return Edge.getLastStepOccupancy(edgeId);               //Occupancy in percent (0–100). -1 if not available
    }

    public double getLengthMeters(String edgeId) {
        String laneId = edgeId + "_0";                           //Converting edgeId from StatsCollector to laneId format
        return Lane.getLength(laneId);
    }

}