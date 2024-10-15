package edu.upvictoria.graphbuilder;

import edu.upvictoria.graphbuilder.Figuras.CentroCirculo;
import edu.upvictoria.graphbuilder.Figuras.Nodo;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

public class BuilderController {
    private Scene scene;
    private String status = "";

    @FXML private Canvas canvas;
    @FXML private Button moveShapesButton;
    @FXML private Button editInfoButton;
    @FXML private Button delShapeButton;
    @FXML private Button drawNodeButton;
    @FXML private Button drawLineButton;

    @FXML
    public void initialize() {
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleMouseClicked);
        //canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        //canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        //canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
    }

    @FXML
    protected void setDrawNodeStatus(){
        status = "drawingNode";
        scene = moveShapesButton.getScene();
        canvas.setOnMouseEntered(me -> scene.setCursor(Cursor.CROSSHAIR));
        canvas.setOnMouseExited(me -> scene.setCursor(Cursor.DEFAULT));
    }

    private void handleMouseClicked(MouseEvent mouseEvent) {
        switch(status){
            case "drawingNode":
                break;
            case "drawingLine":
                break;
            case "editing":
                break;
            case "deleting":
                break;
            case "moving":
                break;
        }
    }

    private void drawNode(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();


    }
}