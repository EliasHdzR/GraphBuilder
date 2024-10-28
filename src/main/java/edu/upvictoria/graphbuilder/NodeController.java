package edu.upvictoria.graphbuilder;

import edu.upvictoria.graphbuilder.Figuras.Node;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NodeController {
    //variables del controlador
    private final Node nodo;
    private final Stage stage;
    private final BuilderController builderController;

    //elementos de la gui
    @FXML private TextField nombreNodo;

    public NodeController(Node nodo, Stage stage, BuilderController builderController) {
        this.nodo = nodo;
        this.builderController = builderController;
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        nombreNodo.setText(nodo.getName());
    }

    public void requestFocus() {
        stage.toFront();
        stage.requestFocus();
    }

    public void cerrarVentana() {
        stage.close();
    }

    @FXML
    private void guardarNombre() {
        nodo.setName(nombreNodo.getText());
        builderController.drawShapes();
        stage.setTitle(nombreNodo.getText());
    }

    /*****************************************
     ********** GETTERS Y SETTERS ************
     *****************************************
     */

    public Node getNodo() {
        return nodo;
    }
}
