package de.uni.trafficsim.statistics;


//Stores all key metrics for a single road (edge) at a given point in time.
//record is a pure data class, automatically creates constructor and getters, attributes are private final
public record EdgeSnapshot (
        int vehicleCount,                                   //number of vehicles on Edge
        double meanSpeedMs,                                    //Avg Speed (m/s)
        double occupancyPercent,                            //Occupancy in %, -1 if not available
        double densityPerKm                                 //Vehicle density (vehicles / km)
) {}

