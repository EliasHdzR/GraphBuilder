package edu.upvictoria.graphbuilder;

import edu.upvictoria.graphbuilder.Figuras.Node;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class NodeController {
    //guarda las ventanas abiertas
    public static final Map<Node, Stage> openMenus = new HashMap<>();
    Node nodo;

    //elementos de la gui
    @FXML private TextField nombreNodo;

    public NodeController(Node nodo){
        this.nodo = nodo;
    }

    @FXML
    protected void initialize() {
        if (openMenus.containsKey(nodo)) {
            // Si ya existe, enfocar la ventana
            Stage existingStage = openMenus.get(nodo);
            existingStage.toFront();
            existingStage.requestFocus();
            return;
        }
        nombreNodo.setText(nodo.getName());
    }
}
