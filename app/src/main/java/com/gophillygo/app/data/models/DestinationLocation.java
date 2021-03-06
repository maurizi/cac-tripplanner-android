package com.gophillygo.app.data.models;

/**
 * Holds the coordinates of a destination
 */

public class DestinationLocation {

    private final double x;
    private final double y;

    public DestinationLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return getY() + "," + getX();
    }
}
