package edu.upvictoria.graphbuilder;

import edu.upvictoria.graphbuilder.Figuras.CircleCenter;
import edu.upvictoria.graphbuilder.Figuras.Figure;
import edu.upvictoria.graphbuilder.Figuras.Node;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

public class BuilderController {
    private Scene scene;
    private List<Figure> figures = new ArrayList<>();

    @FXML private Canvas canvas;

    @FXML
    public void initialize() {
        //canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        //canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        //canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
    }

    @FXML
    protected void setMovingShapesStatus(){
        scene = canvas.getScene();
        canvas.setCursor(Cursor.OPEN_HAND);
        canvas.setOnMouseExited(me -> scene.setCursor(Cursor.DEFAULT));
    }

    private void moveShape(MouseEvent mouseEvent) {

    }

    @FXML
    protected void setDrawNodeStatus(){
        scene = canvas.getScene();
        canvas.setOnMouseEntered(me -> scene.setCursor(Cursor.CROSSHAIR));
        canvas.setOnMouseExited(me -> scene.setCursor(Cursor.DEFAULT));
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::drawNode);
    }

    /**
     * Crea un nodo en el lugar en donde se hizo click
     * @param mouseEvent La accion del mouse
     */
    private void drawNode(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();

        CircleCenter circleCenter = new CircleCenter(x, y);
        Node node = new Node(circleCenter);
        figures.add(node);
        drawShapes();
    }

    /**
     * Redibuja todas las figuras en el canvas para actualizar sus posiciones
     */
    private void drawShapes(){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (Figure figure : figures) {
            figure.draw(gc);
        }
    }


}