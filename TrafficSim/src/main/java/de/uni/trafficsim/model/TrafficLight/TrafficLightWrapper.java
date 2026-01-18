package de.uni.trafficsim.model.TrafficLight;

import de.uni.trafficsim.App;
import org.eclipse.sumo.libtraci.TraCIPosition;
import org.eclipse.sumo.libtraci.TrafficLight;

import java.awt.*;


/**
 * Wrapper for a SUMO traffic light signal.
 * <p>
 * Stores position, orientation, and current state color for rendering.
 */
public class TrafficLightWrapper {
    // Properties
    private final String id;
    private final TraCIPosition position;
    private final double angle;
    private final TLState state;

    /**
     * Constructor.
     * Creates a traffic light wrapper from SUMO state data.
     *
     * @param id traffic light system ID
     * @param position position of the signal
     * @param angle orientation angle for drawing
     * @param state SUMO state character (e.g., G, g, y, r)
     */
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

    /**
     * changes the traffic light to the next phase immediately.
     * <p>
     * Uses SUMO to set the current phase duration to zero.
     */
    public void changeState() {
        try {
            // Forces immediate transition to the next phase
            TrafficLight.setPhaseDuration(id, 0.0);
            App.logger.info("Switched TLS {}", id);
        } catch (Exception e) {
            App.logger.error("Error switching TLS {}: {}", id, e.getMessage());
        }
    }

    /**
     * Compares this traffic light to another for equality.
     *
     * @param obj other object
     * @return true if ID, state, and position match
     */
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