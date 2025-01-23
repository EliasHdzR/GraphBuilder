package edu.upvictoria.graphbuilder;

import edu.upvictoria.graphbuilder.Figuras.Figure;
import edu.upvictoria.graphbuilder.Figuras.Node;

public class Event {
    private int type = 0;
    private String name = null;
    private Figure figure = null;
    private double x = 0;
    private double y = 0;
    private int count = 0;


    public String getName() {
        return this.name;
    }

    public void setName(Figure figure) {
        if(figure instanceof Node node){
            this.name = node.getName();
        }
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Figure getFigure() {
        return this.figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }
    
}
