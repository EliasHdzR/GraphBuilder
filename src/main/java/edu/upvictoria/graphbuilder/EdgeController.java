package edu.upvictoria.graphbuilder;

import edu.upvictoria.graphbuilder.Figuras.Edge;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EdgeController {
    // variables del controlador
    private final Edge arista;
    private final Stage stage;

    //elementos de la gui
    @FXML private TextField nombreArista;

    public EdgeController(Edge arista, Stage stage) {
        this.arista = arista;
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        // aqui poner el nombre de la arista en el textfield
        nombreArista.setText(arista.getName());
        // aqui tmb se puede configurar pa que se carguen de una los nodos en el choiceBox
    }

    public void requestFocus() {
        stage.toFront();
        stage.requestFocus();
    }

    public void cerrarVentana() {
        stage.close();
    }

    /*****************************************
     ********** GETTERS Y SETTERS ************
     *****************************************
     */

    public Edge getArista() {
        return arista;
    }
}
