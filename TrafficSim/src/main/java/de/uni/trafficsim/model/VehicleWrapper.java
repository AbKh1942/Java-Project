package de.uni.trafficsim.model;

import org.eclipse.sumo.libtraci.TraCIColor;
import org.eclipse.sumo.libtraci.TraCIPosition;

import java.awt.*;
import java.util.Objects;

/*
Wrapper around a single SUMO vehicle that gives us a simplified API for our Simulation Control.
Every get or set call pulls the latest values directly
from the SUMO Simulation via the TraCI Interface, so all the data is up to date
 */

//defining new class Vehicle, describes a single Vehicle in SUMO-Simulation
public class VehicleWrapper {
    //only Vehicle class can access id, cannot be changed (final). Every vehicle instance has unique id
    private final String id;
    private TraCIPosition position;
    private double angle;
    private double speed;
    private double length;
    //route id in SUMO is a string. the route id identifies a list of edges (roads)
    private final String route;
    private final Color color;
    private double co2;
    private double fuel;

    //Konstruktor is called when a new vehicle is created
    public VehicleWrapper(
            String id,
            TraCIPosition position,
            double angle,
            double speed,
            double length,
            String route,
            TraCIColor color,
            double co2,
            double fuel
    ) {
        this.id = id;
        this.position = position;
        this.angle = angle;
        this.speed = speed;
        this.length = length;
        this.route = route;
        this.color = formatColor(color);
        this.co2 = co2;
        this.fuel = fuel;
    }

    public VehicleWrapper(String id, String route, Color color) {
        this.id = id;
        this.route = route;
        this.color = color;
    }

    //Getter-Methods
    public String getId() {
        return id;
    }

    public double getSpeed() {
        return speed;
    }

    public double getLength() { return length; }

    public TraCIPosition getPosition() {
        return position;
    }

    public double getAngle() {
        return angle;
    }

    public String getRoute() {
        return route;
    }

    public Color getColor() {
        return color;
    }

    public double getCo2() { return co2; }

    public double getFuel() { return fuel; }

    // Get color of a car
    public TraCIColor getTraCIColor() {
        TraCIColor sumoColor = new TraCIColor();
        sumoColor.setR(color.getRed());
        sumoColor.setG(color.getGreen());
        sumoColor.setB(color.getBlue());
        sumoColor.setA(255);
        return sumoColor;
    }

    // format color from TraCIColor to Color (java.awt)
    private static Color formatColor(TraCIColor color) {
        return new Color(color.getR(), color.getG(), color.getB());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        VehicleWrapper that = (VehicleWrapper) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
