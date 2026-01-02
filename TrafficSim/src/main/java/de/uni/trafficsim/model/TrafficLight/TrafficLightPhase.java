package de.uni.trafficsim.model.TrafficLight;

public class TrafficLightPhase {
    private final String state;   // e.g. "yrGGGyy"
    private final double duration; // seconds

    public TrafficLightPhase(String state, double duration) {
        this.state = state;
        this.duration = duration;
    }

    public String getState() { return state; }
    public double getDuration() { return duration; }
}
