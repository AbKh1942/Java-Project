package de.uni.trafficsim.statistics;
import java.util.Map;


/**
 * Immutable snapshot of statistics for a single simulation step.
 * <p>
 * Holds global metrics and per-edge statistics.
 */
public record StatsSnapshot (                   //record class (pure data class), automatically creates getters
        double simulationTimeSec,              //Simulation Time in Seconds
        double globalAvgSpeedMs,               //global average speed
        int totalVehicles,                     // vehicles currently in simulation
        int stoppedVehicles,                   // vehicles with speed < threshold
        double totalCo2Kg,                     // total CO2 (kg) for this step
        double totalFuelL,                     // total fuel (L) for this step
        int arrivedVehiclesTotal,              // cumulative arrived vehicles count up to this step
        Map<String, EdgeSnapshot> edges        //Mapping edgeId to Edgesnapshot
) {}