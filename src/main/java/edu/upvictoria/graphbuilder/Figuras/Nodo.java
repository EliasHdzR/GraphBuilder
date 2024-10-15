package edu.upvictoria.graphbuilder.Figuras;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Nodo implements Figura {
    private int counter = 1;
    private String name = "Nodo " + counter;
    private CentroCirculo mCenter;
    public final static double mRadius = 2.5;

    public Nodo(CentroCirculo center) {
        this.mCenter = center;
    }

    /************************************************
     ********** FUNCIONES DE LA INTERFAZ ************
     ***********************************************/
    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
        gc.fillOval(mCenter.getX() - mRadius, mCenter.getY() - mRadius, mRadius, mRadius);
        gc.strokeOval(mCenter.getX(), mCenter.getY(), mRadius * 2, mRadius * 2);
    }

    @Override
    public void move(double deltaX, double deltaY) {

    }

    @Override
    public boolean isInside(double x, double y) {
        double mX = x - mCenter.getX();
        double mY = y - mCenter.getY();

        return (!(x > mX + mRadius) || !(y > mY + mRadius)) && (!(x < mX - mRadius) || !(y < mY - mRadius));
    }
}
