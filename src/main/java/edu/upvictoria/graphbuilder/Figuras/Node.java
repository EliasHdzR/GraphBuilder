package edu.upvictoria.graphbuilder.Figuras;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

public class Node implements Figure {
    private String name;
    private CircleCenter mCenter;
    private final static double mRadius = 8;
    private List<Edge> edgeList;

    public Node(CircleCenter center) {
        this.mCenter = center;
        this.name = "Nodo";
    }

    /*****************************************
     ********** GETTERS Y SETTERS ************
     *****************************************
     */

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setmCenter(CircleCenter mCenter) {
        this.mCenter = mCenter;
    }

    public CircleCenter getmCenter() {
        return mCenter;
    }

    public void setEdgeList(List<Edge> edgeList) {
        this.edgeList = edgeList;
    }

    public List<Edge> getEdgeList() {
        return edgeList;
    }

    /************************************************
     ********** FUNCIONES DE LA INTERFAZ ************
     ***********************************************/
    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.WHITE);
        gc.fillOval(mCenter.getX() - mRadius, mCenter.getY() - mRadius, mRadius * 2, mRadius * 2);
        gc.strokeOval(mCenter.getX() - mRadius, mCenter.getY() - mRadius, mRadius * 2, mRadius * 2);
    }

    @Override
    public void move(double deltaX, double deltaY) {
        // aquí se settea el nuevo centro del circulo
        this.mCenter.setmX(this.mCenter.getX() + deltaX);
        this.mCenter.setmY(this.mCenter.getY() + deltaY);
    }

    @Override
    public boolean isInside(double x, double y) {
        double mX = x - mCenter.getX();
        double mY = y - mCenter.getY();

        return (!(x > mX + mRadius) || !(y > mY + mRadius)) && (!(x < mX - mRadius) || !(y < mY - mRadius));
    }
}
