package edu.upvictoria.graphbuilder.Figuras;

import javafx.scene.canvas.GraphicsContext;

public interface Figure {
    void draw(GraphicsContext gc); // para dibujar las pfiguras en el canva
    void move(double deltaX, double deltaY); // para arrastar las figuras
    boolean isInside(double x, double y); // para saber si el cursor está dentro de la figura
}
