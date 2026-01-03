package de.uni.trafficsim.model.TrafficLight;

import org.eclipse.sumo.libtraci.TraCIPosition;
import org.eclipse.sumo.libtraci.TrafficLight;

import java.awt.*;

public class TrafficLightWrapper {
    // Properties
    private final String id;
    private final TraCIPosition position;
    private final double angle;
    private final TLState state;

    // Constructor
    public TrafficLightWrapper(String id, TraCIPosition position, double angle, char state) {
        this.id = id;
        this.position = position;
        this.angle = angle;
        if (state == 'G') this.state = TLState.FULL_GREEN;
        else if (state == 'g') this.state = TLState.GREEN;
        else if (state == 'y') this.state = TLState.YELLOW;
        else this.state = TLState.RED;
    }

    // Getters
    public String getId() {
        return id;
    }

    public TraCIPosition getPosition() {
        return position;
    }

    public Color getColor() {
        return state.getColor();
    }

    public double getAngle() { return angle; }

    // function for changing the state of traffic light
    public void changeState() {
        try {
            // Forces immediate transition to the next phase
            TrafficLight.setPhaseDuration(id, 0.0);
            System.out.println("Switched TLS " + id);
        } catch (Exception e) {
            System.err.println("Error switching TLS " + id + ": " + e.getMessage());
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TrafficLightWrapper &&
                id.equals(((TrafficLightWrapper)obj).id) &&
                state.equals(((TrafficLightWrapper)obj).state) &&
                position.equals(((TrafficLightWrapper)obj).position);
    }

    // Enum for traffic light`s states
    enum TLState {
        FULL_GREEN, GREEN, RED, YELLOW;

        public Color getColor() {
            switch (this) {
                case FULL_GREEN: return new Color(3, 255, 0);
                case GREEN: return new Color(2, 179, 2);
                case RED: return Color.RED;
                case YELLOW: return Color.YELLOW;
                default: return Color.WHITE;
            }
        }
    }
}