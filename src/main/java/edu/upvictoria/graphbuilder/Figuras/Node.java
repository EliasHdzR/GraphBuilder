package edu.upvictoria.graphbuilder.Figuras;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Node implements Figure {
    private String name;
    private final CircleCenter mCenter;
    private final static double mRadius = 8 ;
    private final List<Edge> edgeList = new ArrayList<>();

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

    public CircleCenter getmCenter() {
        return mCenter;
    }

    public void addToEdgeList(Edge edge) {
        edgeList.add(edge);
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

        javafx.scene.text.Text textNode = new javafx.scene.text.Text(name);  // Crea un objeto Text para medir el tamaño del texto
        double textWidth = textNode.getLayoutBounds().getWidth(); // Obtiene el ancho del texto
        double textX = mCenter.getX() - (textWidth / 2);
        double textY = mCenter.getY() + mRadius + 15;
        gc.setFill(Color.BLACK);
        gc.fillText(name, textX, textY);
    }

    @Override
    public void move(double deltaX, double deltaY) {
        // aquí se settea el nuevo centro del circulo
        this.mCenter.setmX(deltaX);
        this.mCenter.setmY(deltaY);

        for (Edge edge : edgeList) {
            edge.changeCoords(this, deltaX, deltaY);
        }
    }

    @Override
    public boolean contains(double x, double y) {
        double minX = mCenter.getX() - mRadius;
        double minY = mCenter.getY() - mRadius;
        double maxX = mCenter.getX() + mRadius;
        double maxY = mCenter.getY() + mRadius;

        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }
}
