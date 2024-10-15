module edu.upvictoria.graphbuilder {
    requires javafx.controls;
    requires javafx.fxml;


    opens edu.upvictoria.graphbuilder to javafx.fxml;
    exports edu.upvictoria.graphbuilder;
    exports edu.upvictoria.graphbuilder.Figuras;
    opens edu.upvictoria.graphbuilder.Figuras to javafx.fxml;
}