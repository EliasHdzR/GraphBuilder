package edu.upvictoria.graphbuilder;

import edu.upvictoria.graphbuilder.Figuras.Node;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
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

        int maxCharacters = 10;
        nombreNodo.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            // Validar longitud máxima
            if (change.getControlNewText().length() > maxCharacters) {
                return null;
            }

            // Validar que solo contenga letras y números
            if (!change.getControlNewText().matches("[a-zA-Z0-9]*")) {
                return null;
            }

            return change;
        }));
    }

    public void configureShortcuts() {
        stage.getScene().setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                guardarNombre();
            }
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                cerrarVentana();
            }
        });
    }

    public void cerrarVentana() {
        stage.close();
    }

    @FXML
    private void guardarNombre() {
        nodo.setName(nombreNodo.getText());
        builderController.drawShapes();
        stage.close();
    }

    /*****************************************
     ********** GETTERS Y SETTERS ************
     *****************************************
     */

    public Node getNodo() {
        return nodo;
    }
}
