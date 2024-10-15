package edu.upvictoria.graphbuilder.Figuras;

public class CentroCirculo {
    private double mX;
    private double mY;
    private final double mRadius = 2.5;

    public CentroCirculo () {
        this (0,0);
    }

    public CentroCirculo(double x, double y) {
        mX = x;
        mY = y;
    }

    public double getX() {
        return mX;
    }

    public double getY() {
        return mY;
    }

    public double getmRadius() {
        return mRadius;
    }
}
