package edu.upvictoria.graphbuilder;

import edu.upvictoria.graphbuilder.Figuras.Edge;
import edu.upvictoria.graphbuilder.Figuras.Node;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EdgeController {
    // variables del controlador
    private final Edge arista;
    private final Stage stage;
    private final BuilderController builderController;

    // elementos de la gui
    @FXML
    private TextField nombreArista;
    @FXML
    private RadioButton tieneDireccion;
    @FXML
    private ChoiceBox<String> firstBox;
    @FXML
    private ChoiceBox<String> secondBox;

    private Node nodoUno;
    private Node nodoDos;

    // Aqui se guardan los resultados
    private Node origen;
    private Node destino;

    public EdgeController(Edge arista, Stage stage, BuilderController builderController) {
        this.arista = arista;
        this.stage = stage;
        this.builderController = builderController;
    }

    @FXML
    private void initialize() {
        // aqui poner el nombre de la arista en el textfield
        nombreArista.setText(arista.getName());
        // aqui tmb se puede configurar pa que se carguen de una los nodos en el
        // choiceBox
        String[] nombresNodos = new String[2];
        nodoUno = arista.getNodo1();
        nodoDos = arista.getNodo2();
        nombresNodos[0] = nodoUno.getName();
        nombresNodos[1] = nodoDos.getName();
        firstBox.getItems().addAll(nombresNodos);
        secondBox.getItems().addAll(nombresNodos);

    }

    public void requestFocus() {
        stage.toFront();
        stage.requestFocus();
    }

    @FXML
    private void guardarCambios() {
        guardarNombre();
        checkRadioButton();
        builderController.drawShapes();
        stage.setTitle(nombreArista.getText());
    }

    private void guardarNombre() {
        arista.setName(nombreArista.getText());
    }

    private void checkRadioButton() {
        boolean bandera = tieneDireccion.isSelected();
        if (bandera) {
            try {
                origen = recoverValue(firstBox);
                destino = recoverValue(secondBox);
            } catch (Exception e) {
                e.printStackTrace();
            }
            arista.setName("Funciono" + origen + destino);
        }
    }

    // Aqui le falta una exception real
    private Node recoverValue(ChoiceBox<String> cajita) throws Exception {
        String valor = cajita.getValue().trim();
        if (valor != null) {
            if (nodoUno.getName().trim().equals(valor)) {
                return nodoUno;
            } else {
                return nodoDos;
            }
        } else {
            throw new Exception();
        }
    }

    /*****************************************
     ********** GETTERS Y SETTERS ************
     *****************************************
     */

    public Edge getArista() {
        return arista;
    }
}
