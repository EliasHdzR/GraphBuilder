package edu.upvictoria.graphbuilder;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class WarningController {

    private final Stage stage;
    private final BuilderController controller;
    private final String process;

    // elementos de la gui
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    public WarningController(Stage stage, BuilderController controller, String process) {
        this.stage = stage;
        this.controller = controller;
        this.process = process;
    }

    @FXML
    private void initialize() {
        btnCancelar.setCancelButton(true);
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
