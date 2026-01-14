package de.uni.trafficsim.model;

import java.awt.*;

public class VehicleFilter {
    public boolean enabled = false;
    public double minSpeed = 0.0;
    public double maxSpeed = 200.0; // High default
    public Color filterColor = null; // Replaced typeIdContains
    public boolean showStoppedOnly = false;

    public boolean matches(VehicleWrapper v) {
        if (!enabled) return true;

        // Speed Filter
        if (v.getSpeed() < minSpeed || v.getSpeed() > maxSpeed) return false;

        // Color Filter
        if (filterColor != null) {
            if (v.getColor().getRed() != filterColor.getRed() ||
                v.getColor().getGreen() != filterColor.getGreen() ||
                v.getColor().getBlue() != filterColor.getBlue()) {
                return false;
            }
        }

        // Stopped Filter
        return !showStoppedOnly || !(v.getSpeed() > 0.1);
    }
}
