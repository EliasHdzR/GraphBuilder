package edu.upvictoria.graphbuilder;

import edu.upvictoria.graphbuilder.Figuras.Node;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

public class NodeController {
    //variables del controlador
    private final Node nodo;
    private final Stage stage;
    private final BuilderController builderController;

    //elementos de la gui
    @FXML private TextField nombreNodo;
    @FXML private Button btnAccept;
    @FXML private Button btnCancel;

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
            if (change.getControlNewText().length() > maxCharacters) return null;
            btnAccept.setDisable(change.getControlNewText().isEmpty());
            // Validar que solo contenga letras y números
            if (!change.getControlNewText().matches("[a-zA-Z0-9]*")) return null;
            return change;
        }));

        nombreNodo.setOnKeyTyped(event -> builderController.textToSpeech(event.getCharacter()));
    }

    public void configureShortcuts() {
        stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                guardarNombre();
                keyEvent.consume();
            }
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                cerrarVentana();
                keyEvent.consume();
            }
        });
    }

    public void addTTStoButtons() {
        PauseTransition pauseNombre = new PauseTransition(Duration.seconds(1));
        nombreNodo.setOnMouseEntered(event -> {
            pauseNombre.setOnFinished(e -> builderController.textToSpeech("node name input"));
            pauseNombre.playFromStart();
        });
        nombreNodo.setOnMouseExited(event -> pauseNombre.stop());

        PauseTransition pauseGuardar = new PauseTransition(Duration.seconds(1));
        btnAccept.setOnMouseEntered(event -> {
            pauseGuardar.setOnFinished(e -> builderController.textToSpeech("accept"));
            pauseGuardar.playFromStart();
        });
        btnAccept.setOnMouseExited(event -> pauseGuardar.stop());

        PauseTransition pauseCancelar = new PauseTransition(Duration.seconds(1));
        btnCancel.setOnMouseEntered(event -> {
            pauseCancelar.setOnFinished(e -> builderController.textToSpeech("cancel"));
            pauseCancelar.playFromStart();
        });
        btnCancel.setOnMouseExited(event -> pauseCancelar.stop());
    }

    public void cerrarVentana() {
        stage.close();
        builderController.textToSpeech("Node " + nodo.getName() + " Menu closed");
    }

    @FXML
    private void guardarNombre() {
        builderController.createEvent(4, nodo, builderController.undoList);
        nodo.setName(nombreNodo.getText());
        builderController.drawShapes();
        cerrarVentana();
    }

    /*****************************************
     ********** GETTERS Y SETTERS ************
     *****************************************
     */

    public Node getNodo() {
        return nodo;
    }
}
