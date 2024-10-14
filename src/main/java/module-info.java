module edu.upvictoria.graphbuilder {
    requires javafx.controls;
    requires javafx.fxml;


    opens edu.upvictoria.graphbuilder to javafx.fxml;
    exports edu.upvictoria.graphbuilder;
}