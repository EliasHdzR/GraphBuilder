package edu.upvictoria.graphbuilder;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.util.Duration;

public class WarningController {

    private final Stage stage;
    private final BuilderController controller;
    private final String process;

    // elementos de la gui
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Button btnIgnore;

    public WarningController(Stage stage, BuilderController controller, String process) {
        this.stage = stage;
        this.controller = controller;
        this.process = process;
    }

    @FXML
    private void initialize() {
        btnCancelar.setCancelButton(true);
    }

    public void addTTStoButtons() {
        controller.textToSpeech("Save changes before closing?");

        PauseTransition pauseGuardar = new PauseTransition(Duration.seconds(1));
        btnGuardar.setOnMouseEntered(event -> {
            pauseGuardar.setOnFinished(e -> controller.textToSpeech("save"));
            pauseGuardar.playFromStart();
        });
        btnGuardar.setOnMouseExited(event -> pauseGuardar.stop());

        PauseTransition pauseIgnore = new PauseTransition(Duration.seconds(1));
        btnIgnore.setOnMouseEntered(event -> {
            pauseIgnore.setOnFinished(e -> controller.textToSpeech("close without saving"));
            pauseIgnore.playFromStart();
        });
        btnIgnore.setOnMouseExited(event -> pauseIgnore.stop());

        PauseTransition pauseCancelar = new PauseTransition(Duration.seconds(1));
        btnCancelar.setOnMouseEntered(event -> {
            pauseCancelar.setOnFinished(e -> controller.textToSpeech("cancel"));
            pauseCancelar.playFromStart();
        });
        btnCancelar.setOnMouseExited(event -> pauseCancelar.stop());
    }

    public void focusBtnGuardar() {
        btnGuardar.requestFocus();
    }

    @FXML
    public void closePopup() {
        stage.close();
    }

    @FXML
    public void saveAndExit() {
        if(process.equals("new file")){
            FilesManager.saveFile(controller);
            FilesManager.newFile(controller);
            closePopup();
        }

        if(process.equals("open file")){
            FilesManager.saveFile(controller);
            FilesManager.openFile(controller);
            closePopup();
        }

        if (process.equals("exit")) {
            FilesManager.saveFile(controller);
            System.exit(0);
        }
    }

    @FXML
    public void exit(){
        if (process.equals("new file")) {
            FilesManager.newFile(controller);
            closePopup();
        }

        if (process.equals("open file")) {
            FilesManager.openFile(controller);
            closePopup();
        }

        if (process.equals("exit")) {
            System.exit(0);
        }
    }
}
