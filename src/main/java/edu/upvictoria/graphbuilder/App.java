package edu.upvictoria.graphbuilder;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("builder.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
            BuilderController controller = fxmlLoader.getController();
            controller.scene = scene;
            /* if(controller.scene == null){
                System.out.println("App Es null");
            }else{
                System.out.println("App Noes");
            } */
            stage.setTitle("Graph Builder");
            stage.setScene(scene);
            controller.shortcuts();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}