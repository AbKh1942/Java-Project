package de.uni.trafficsim.model.TrafficLight;

/**
 * Represents a single traffic light phase.
 * <p>
 * Stores the state string and its duration in seconds.
 */
public class TrafficLightPhase {
    private final String state;   // e.g. "yrGGGyy"
    private final double duration; // seconds

    /**
     * Constructor.
     * Creates a traffic light phase.
     *
     * @param state phase state string (e.g., "yrGGGyy")
     * @param duration phase duration in seconds
     */
    public TrafficLightPhase(String state, double duration) {
        this.state = state;
        this.duration = duration;
    }

    public String getState() { return state; }
    public double getDuration() { return duration; }
}
