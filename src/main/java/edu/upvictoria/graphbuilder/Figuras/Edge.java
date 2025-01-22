package edu.upvictoria.graphbuilder.Figuras;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Edge implements Figure {
    private String name;
    private final Node nodo1;
    private final Node nodo2;

    private double startX;
    private double startY;
    private double endX;
    private double endY;

    public Edge(Node nodo1, Node nodo2){
        this.nodo1 = nodo1;
        this.nodo2 = nodo2;
        this.startX = nodo1.getmCenter().getX();
        this.startY = nodo1.getmCenter().getY();
        this.endX = nodo2.getmCenter().getX();
        this.endY = nodo2.getmCenter().getY();
        name = "Arista";
    }

    /**
     * Compara dos nodos recibidos con los dos nodos existentes para checar si estos nodos ya
     * están unidos por una arista, así se evita dibujar más de una arista entre dos mismos nodos
     * @param nodo1 un nodo
     * @param nodo2 otro nodo
     * @return true si el nodo1 es igual a this.nodo1, igual con el nodo2
     */
    public boolean doesExist(Node nodo1, Node nodo2){
        return (nodo1 == this.nodo1 && nodo2 == this.nodo2) || (nodo2 == this.nodo1 && nodo1 == this.nodo2);
    }

    public void changeCoords(Node nodo, double x, double y){
        if(nodo == nodo1){
            startX = x;
            startY = y;
        } else if(nodo == nodo2){
            endX = x;
            endY = y;
        }
    }

    public String getName() {
        return name;
    }

    public Node getNodo1() {
        return nodo1;
    }

    public Node getNodo2() {
        return nodo2;
    }

    /************************************************
     ********** FUNCIONES DE LA INTERFAZ ************
     ***********************************************/
    public static void drawDraft(GraphicsContext gc, double startX, double startY, double endX, double endY) {
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(2);
        gc.strokeLine(startX, startY, endX, endY);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(startX, startY, endX, endY);
    }

    @Override
    public void move(double deltaX, double deltaY) {
        //jiji no se mueve desde aquí, checa la clase Node
    }

    @Override
    public boolean contains(double x, double y) {
        double distance = pointLineDistance(startX, startY, endX, endY, x, y);
        return distance < 5;
    }

    // Magia
    private double pointLineDistance(double x1, double y1, double x2, double y2, double px, double py) {
        double A = px - x1;
        double B = py - y1;
        double C = x2 - x1;
        double D = y2 - y1;

        double dot = A * C + B * D;
        double len_sq = C * C + D * D;
        double param = -1;
        if (len_sq != 0) {
            param = dot / len_sq;
        }

        double xx, yy;

        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }

        double dx = px - xx;
        double dy = py - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
