package de.uni.trafficsim.model;

import org.eclipse.sumo.libtraci.TraCIColor;
import org.eclipse.sumo.libtraci.TraCIPosition;

import java.util.Objects;

/*
Wrapper around a single SUMO vehicle that gives us a simplified API for our Simulation Control.
Every get or set call pulls the latest values directly
from the SUMO Simulation via the TraCI Interface, so all the data is up to date
 */


public class Vehicle {                              //defining new class Vehicle, describes a single Vehicle in SUMO-Simulation
    private final String id;                        //only Vehicle class can access id, cannot be changed (final). Every vehicle instance has unique id
    private double[] position = new double[2];      //Position is a double Array for 2 numbers (x,y coordinate). position[0] = x, position[1] = y
    private double speed;
    private String route;                           //route id in SUMO is a string. the route id identifies a list of edges (roads)
    private String color;                           //Color as Hex-number string in RGB format

    public Vehicle(String id) {                     //Konstruktor, is called when new vehicle is created
        if (id == null || id.isEmpty()) {           //checks that id is not empty. id == null, checks if nothing has been handed over, id.isBlank() makes sure empty strings like "   " cant be put in.
            throw new IllegalArgumentException("Vehicle id must not be empty"); //Id cannot be empty!
        }
        this.id = id;
    }

    //Getter-Methods
    public String getId() {
        return id;
    }

    public double getSpeed() {
        speed = org.eclipse.sumo.libtraci.Vehicle.getSpeed(id); //calls internal getSpeed method in SUMO, live-query from current simulation
        return speed;
    }

    public double[] getPosition() {
        TraCIPosition sumoPos = org.eclipse.sumo.libtraci.Vehicle.getPosition(id); //SUMO getPosition returns two numbers: x and y and stores it in a TraCI-Object (TraCIPosition)
        position = new double[]{sumoPos.getX(), sumoPos.getY()}; //write x and y coordinate into new position Array
        return position.clone(); //returning a copy of the position Array, so whoever calls the getter can't change the internal car position, only the copy.
    }

    public String getRoute() {
        route = org.eclipse.sumo.libtraci.Vehicle.getRouteID(id); //calls internal getRoute method in SUMO/libtraci. returns route id string.
        return route;
    }

    public String getColor() {
        TraCIColor sumoColor = org.eclipse.sumo.libtraci.Vehicle.getColor(id); //calls internal getColor method in SUMO. Returns a TraCI-RGB-Object (TraCIColor)
        color = formatColor(sumoColor); //the command formatColor() changes the RGB-Object into an RGB-formatted string, i.e. #00FF00 (#RRGGBB)
        return color;
    }

    //Setter-Methods
    public void setSpeed(double speed) {
        org.eclipse.sumo.libtraci.Vehicle.setSpeed(id, speed); //calls internal setSpeed method in SUMO/libtraci -> changes speed directly in SUMO.
        this.speed = speed; //saves speed value to current vehicle instance
    }

    public void setRoute(String routeId) {
        Objects.requireNonNull(routeId, "routeId must not be null"); //internal Java standard Class/method, checks if the value is null -> if yes, throws error (NullPointerException)
        org.eclipse.sumo.libtraci.Vehicle.setRouteID(id, routeId); //calls internal setRouteID method in SUMO/libtraci -> changes route directly in SUMO
        this.route = routeId;
    }

    public void setColor(String colorHex) {
        TraCIColor sumoColor = parseColor(colorHex); //parseColor does the opposite of formatColor, turns Hex-RGBstring into TraCI-RGB-Object, so we can set the color internally in the TraCI-Object
        org.eclipse.sumo.libtraci.Vehicle.setColor(id, sumoColor); //calls internal setColor method in SUMO/libtraci
        this.color = formatColor(sumoColor); //saves the color as Hex-string in our java wrapper vehicle instance
    }

    //method for adding a new vehicle into the simulation
    public void spawnVehicle(String routeId) {
        org.eclipse.sumo.libtraci.Vehicle.add(id, routeId); //calls internal add() method in libtraci
        this.route = routeId;
    }

    //formatColor method to interpret TraCIColor-Object as Hex-RGB-format-String
    private static String formatColor(TraCIColor color) {
        return String.format("#%02X%02X%02X", color.getR(), color.getG(), color.getB()); //gets the internal red, green and blue values from the TraCI Color object and formats it to  6 digit Hex-string (fill with leading 0's if neccessary)
    }                                                                                    //getR() etc come from libtraci

    //parseColor method to translate Hex-RGB-format-String into TraCIColor-Object
    //substring and parseInt come from java-Standardclasses
    private static TraCIColor parseColor(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Color must not be null"); //makes sure input cannot be empty
        }
        String hex = value.startsWith("#") ? value.substring(1) : value; //makes sure the input is in Hex-RGB-format (starts with #, number has a length of 6) with format-string
        if (hex.length() != 6) {
            throw new IllegalArgumentException("Expected a RGB hex string such as #FF0000");
        }
        int r = Integer.parseInt(hex.substring(0, 2), 16); //takes the red-substring(0 - inclusive, 2 - exclusive) (first two Numbers #RRGGBB) and turns it to an integer using the base 16 (Hexadecimal)
        int g = Integer.parseInt(hex.substring(2, 4), 16); //same as with red
        int b = Integer.parseInt(hex.substring(4, 6), 16);//same as with red
        return new TraCIColor(r, g, b, 255); //255 is the alpha, which controls the opacity of the color. 255 = full opacity
    }
}
