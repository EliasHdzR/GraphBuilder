package edu.upvictoria.graphbuilder;

import edu.upvictoria.graphbuilder.Figuras.CircleCenter;
import edu.upvictoria.graphbuilder.Figuras.Edge;
import edu.upvictoria.graphbuilder.Figuras.Figure;
import edu.upvictoria.graphbuilder.Figuras.Node;

import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.robot.Robot;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BuilderController {
    // variables del controlador
    public Scene scene;
    private final ObservableList<Figure> figures = FXCollections.observableArrayList();
    private final List<NodeController> nodeMenusOpen = new ArrayList<>();
    private Double initialX = null;
    private Double initialY = null;
    private Node selectedNode = null;
    private File archivoGrafo = null;
    private Node clipboard = null;
    private Figure selectFigure = null;
    private final List<Node> nodeList = new ArrayList<>();
    private int[][] adjacencyMatrix;

    // elementos de la gui
    @FXML private Canvas canvas;
    @FXML private ToolBar toolBar;
    @FXML private Label nodeCounterLabel;
    @FXML private Label edgeCounterLabel;
    @FXML private Label fileTitleLabel;
    @FXML private Pane messagePane;
    @FXML private Label messageLabel;

    // botones de la barra de herramientas
    @FXML private Button moveShapesButton;
    @FXML private Button openMenusButton;
    @FXML private Button deleteShapesButton;
    @FXML private Button drawNodeButton;
    @FXML private Button drawEdgeButton;
    @FXML private MenuItem canvasToPngBtn;
    private final ArrayList<Button> buttons = new ArrayList<>();

    @FXML
    public void initialize() {
        fileTitleLabel.setText("Nuevo Archivo");

        buttons.add(moveShapesButton);
        buttons.add(openMenusButton);
        buttons.add(deleteShapesButton);
        buttons.add(drawNodeButton);
        buttons.add(drawEdgeButton);

        //desabilitar botones de la barra
        canvasToPngBtn.disableProperty().bind(Bindings.size(figures).isEqualTo(0));

        toolBar.setCursor(Cursor.DEFAULT);
        setMovingShapesStatus();
    }

    /************************************
     **** FUNCIONES DEL BUILDER MAIN ****
     ************************************/

    @FXML
    protected void setMovingShapesStatus() {
        removeHandlers();
        setDefaultStyle();
        setActiveStyle(moveShapesButton);

        scene = canvas.getScene();
        canvas.setOnMouseEntered(me -> scene.setCursor(Cursor.OPEN_HAND));
        canvas.setOnMouseExited(me -> scene.setCursor(Cursor.DEFAULT));

        canvas.setOnMouseClicked(this::clickFigure);
        canvas.setOnMouseDragged(this::moveShape);
        canvas.setOnMouseReleased(this::endMoveShape);
    }

    private void moveShape(MouseEvent mouseEvent) {
        canvas.setCursor(Cursor.CLOSED_HAND);
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        Figure figura = getFigureAt(x, y);

        selectFigure = figura;

        if (!(figura instanceof Node nodo)) {
            return;
        }

        nodo.move(x, y);
        drawShapes();
    }

    private void endMoveShape(MouseEvent mouseEvent) {
        canvas.setCursor(Cursor.OPEN_HAND);
        setMovingShapesStatus();
    }

    @FXML
    private void setDeleteFigureStatus() {
        removeHandlers();
        setDefaultStyle();
        setActiveStyle(deleteShapesButton);

        scene = canvas.getScene();
        canvas.setOnMouseEntered(me -> scene.setCursor(Cursor.HAND));
        canvas.setOnMouseExited(me -> scene.setCursor(Cursor.DEFAULT));
        canvas.setOnMouseClicked(this::eraseFigure);
    }

    private void eraseFigure(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        Figure figure = getFigureAt(x, y);

        // si es un nodo tmb hay que borrar todas las aristas que van hacia este nodo
        if(figure instanceof Node nodo){
            int nodeIndex = nodeList.indexOf(nodo);
            nodeList.remove(nodo);

            List<Edge> nodoEdgeList = nodo.getEdgeList();
            for (Edge edge : nodoEdgeList) {
                figures.remove(edge);
            }

            int[][] newMatrix = new int[nodeList.size()][nodeList.size()];
            int[][] oldMatrix = adjacencyMatrix;

            for (int i = 0, newI = 0; i < oldMatrix.length; i++) {
                if (i == nodeIndex) continue;
                for (int j = 0, newJ = 0; j < oldMatrix[i].length; j++) {
                    if (j == nodeIndex) continue;
                    newMatrix[newI][newJ] = oldMatrix[i][j];
                    newJ++;
                }
                newI++;
            }

            adjacencyMatrix = newMatrix;
        }

        figures.remove(figure);
        drawShapes();
        setDeleteFigureStatus();
    }

    @FXML
    private void setOpenFigureMenuStatus() {
        removeHandlers();
        setDefaultStyle();
        setActiveStyle(openMenusButton);

        canvas.setOnMouseEntered(me -> scene.setCursor(Cursor.HAND));
        canvas.setOnMouseExited(me -> scene.setCursor(Cursor.DEFAULT));
        canvas.setOnMouseClicked(this::openFigureMenu);
    }

    private void openFigureMenu(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        Figure figure = getFigureAt(x, y);

        if (figure == null) {
            return;
        }

        selectFigure = figure;
        if (figure instanceof Node nodo) {
            openNodeMenu(nodo);
        }
    }

    private void openNodeMenu(Node nodo) {
        // checamos si ya esta abierto, si lo está entonces traemos la ventana al plano principal
        for (NodeController controlador : nodeMenusOpen) {
            if (controlador.getNodo() == nodo) {
                controlador.requestFocus();
                return;
            }
        }
        // si no pues lo abrimos en una ventana nueva y lo agregamos a los menus abiertos
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("menuNodo.fxml"));
            Stage stage = new Stage();
            NodeController nodoControlador = new NodeController(nodo, stage, this);
            fxmlLoader.setController(nodoControlador);
            Scene scene = new Scene(fxmlLoader.load());
            nodeMenusOpen.add(nodoControlador);
            stage.setOnCloseRequest(event -> nodeMenusOpen.remove(nodoControlador));

            stage.setTitle(nodo.getName());
            stage.setScene(scene);
            stage.setMinWidth(346);
            stage.setMinHeight(126);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void setDrawNodeStatus() {
        removeHandlers();
        setDefaultStyle();
        setActiveStyle(drawNodeButton);

        scene = canvas.getScene();
        canvas.setOnMouseEntered(me -> scene.setCursor(Cursor.CROSSHAIR));
        canvas.setOnMouseExited(me -> scene.setCursor(Cursor.DEFAULT));

        canvas.setOnMouseClicked(this::drawNode);
    }

    /**
     * Crea un nodo en el lugar en donde se hizo click
     * 
     * @param mouseEvent La accion del mouse
     */
    private void drawNode(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();

        CircleCenter circleCenter = new CircleCenter(x, y);
        Node node = new Node(circleCenter, nodeList.size()+1);
        figures.add(node);
        selectFigure = node;
        nodeList.add(node);
        FilesManager.initializeMatrix(this);
        drawShapes();
    }

    @FXML
    protected void setDrawEdgeStatus() {
        removeHandlers();
        setDefaultStyle();
        setActiveStyle(drawEdgeButton);

        scene = canvas.getScene();
        canvas.setOnMouseEntered(me -> scene.setCursor(Cursor.CROSSHAIR));
        canvas.setOnMouseExited(me -> scene.setCursor(Cursor.DEFAULT));
        canvas.setOnMouseDragged(this::drawEdge);
        canvas.setOnMouseReleased(this::endDrawEdge);
    }

    private void drawEdge(MouseEvent mouseEvent) {
        if (initialX == null && initialY == null && selectedNode == null) {
            initialX = mouseEvent.getX();
            initialY = mouseEvent.getY();

            Figure figInicial = getFigureAt(initialX, initialY);
            if (!(figInicial instanceof Node)) {
                initialX = null;
                initialY = null;
                selectedNode = null;
                return;
            }
            selectedNode = (Node) figInicial;
        }

        drawShapes();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Edge.drawDraft(gc, initialX, initialY, mouseEvent.getX(), mouseEvent.getY());
    }

    private void endDrawEdge(MouseEvent mouseEvent) {
        // recuperamos la figura en la que se dejó de mantener presionado el clic
        // izquirdo
        Figure fig2 = getFigureAt(mouseEvent.getX(), mouseEvent.getY());

        // si esa figura no es un nodo o es el que ya elegimos entonces deja de dibujar
        // el borrador de arista
        if (!(fig2 instanceof Node nodo2) || nodo2 == selectedNode || selectedNode == null) {
            initialX = null;
            initialY = null;
            selectedNode = null;
            setDrawEdgeStatus();
            drawShapes();
            return;
        }

        // creamos la arista y por cada figura en la lista:
        // 1. si la fig recuperada de la lista es una arista, tenemos que checar si
        // entre los dos nodos elegidos
        // ya existe una arista que los una, si sí, cancelamos el dibujado completamente
        // 2. si la fig recuperada es un nodo, checamos si es el nodo de inicio o final
        // de la arista, si lo son
        // entonces añadimos la arista a su lista de aristas propia
        Edge arista = new Edge(selectedNode, nodo2);
        for (Figure figure : figures) {
            if (figure instanceof Edge aristaTemp && aristaTemp.doesExist(selectedNode, nodo2)) {
                initialX = null;
                initialY = null;
                selectedNode = null;
                setDrawEdgeStatus();
                drawShapes();
                return;
            }

            if (figure == selectedNode || figure == nodo2) {
                ((Node) figure).addToEdgeList(arista);
            }
        }

        // añadimos la arista a la lista y la dibujamos, posteriormente reiniciamos el estado de dibujo
        figures.add(arista);
        selectFigure = arista;
        drawShapes();

        int fromIndex = nodeList.indexOf(selectedNode);
        int toIndex = nodeList.indexOf(nodo2);
        if (fromIndex != -1 && toIndex != -1) {
            adjacencyMatrix[fromIndex][toIndex] = 1;
            adjacencyMatrix[toIndex][fromIndex] = 1;
        }

        initialX = null;
        initialY = null;
        selectedNode = null;
        setDrawEdgeStatus();
    }

    private void removeHandlers() {
        canvas.setOnMouseClicked(null);
        canvas.setOnMouseDragged(null);
        canvas.setOnMouseReleased(null);
        canvas.setOnMousePressed(null);
    }

    /***********************************
     * **** FUNCIONES ARCHIVO *********
     * *********************************
     */

    /**
     * Vacía el canvas y lo deja como nuevo
     */
    @FXML
    private void newFile(){
        FilesManager.newFile(this);
    }

    @FXML
    private void openFile(){
        FilesManager.openFile(this);
    }

    @FXML
    private void saveFile(){
        FilesManager.saveFile(this);
    }

    // Funcion que guarda en el CSV y muestra el Chooser
    @FXML
    private void saveAs() {
        FilesManager.saveAs(this);
    }

    /**
     * Me quiero matar bro
     * */
    @FXML
    private void CanvasToPng() {
        FilesManager.CanvasToPng(this);
    }

    public void exitApp() {

    }

    /************************************
     ************ SHORTCUTS *************
     ***********************************/

    public void shortcuts() {
        scene.setOnKeyPressed( keyEvent -> {
            if (keyEvent.getCode() == KeyCode.N && keyEvent.isControlDown()) {
                newFile();
            }
            if (keyEvent.getCode() == KeyCode.O && keyEvent.isControlDown()) {
                openFile();
            }
            if (keyEvent.getCode() == KeyCode.S && keyEvent.isControlDown()) {
                saveFile();
            }
            if (keyEvent.getCode() == KeyCode.S && keyEvent.isControlDown() && keyEvent.isShiftDown()) {
                saveAs();
            }
            if (keyEvent.getCode() == KeyCode.P && keyEvent.isControlDown()) {
                CanvasToPng();
            }
        });
    }

    /************************************
     **** FUNCIONES PARA LAS FIGURAS ****
     ************************************/
    /**
     * Redibuja todas las figuras en el canvas para actualizar sus posiciones
     * y actualiza el label inferior
     */
    public void drawShapes() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        int edgeCount = 0;

        for (Figure figure : figures) {
            figure.draw(gc);
            if(figure instanceof Edge){
                edgeCount++;
            }
        }

        nodeCounterLabel.setText(String.valueOf(nodeList.size()));
        edgeCounterLabel.setText(String.valueOf(edgeCount));
    }

    /**
     * Obtiene la figura que contenga las coordenadas del evento
     * @param x Coordenada x del click
     * @param y Coordenada y del click
     * @return Una figura
     */
    private Figure getFigureAt(double x, double y) {
        for (Figure figure : figures) {
            if (figure.contains(x, y)) {
                return figure;
            }
        }

        return null;
    }

    /************************************
     ************ ESTILOS ***************
     ************************************/

    private void setActiveStyle(Button button) {
        button.setStyle("-fx-background-color: #7298d6;");
    }

    private void setDefaultStyle() {
        for (Button button : buttons) {
            button.setStyle("");
        }
    }

    public void showMessage(String message){
        int fadeDurationSeconds = 3;
        int delaySeconds = 3;
        messageLabel.setText(message);
        messagePane.setOpacity(1.0);

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(fadeDurationSeconds), messagePane);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setDelay(Duration.seconds(delaySeconds));
        fadeTransition.play();
    }

    /************************************
     ************ EDITAR ***************
     ************************************/

    private void clickFigure(MouseEvent mouseEvent){
        /*
         * Esto existe para cuando se clikea una figura pero no se mueve
         */
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();

        selectFigure = getFigureAt(x, y);

    }

    private void suprFigure() {
        if (selectFigure != null) {
            if (selectFigure instanceof Node nodo) {
                List<Edge> nodoEdgeList = nodo.getEdgeList();
                for (Edge edge : nodoEdgeList) {
                    figures.remove(edge);
                }
            }
            figures.remove(selectFigure);
            selectFigure = null;
            drawShapes();
        }
    }

    @FXML
    private void copy(){
        if(selectFigure instanceof Node node){
            /*
             * Cuando acabas de correr el programa aveces no copia nada
             * y no se porque pero x somos chavos mejor vamos a chelear
             * con toda la bandita caguamera.
             */
            clipboard = node;
        }
    }

    @FXML
    private void paste(){
        if(clipboard != null){
            Robot robot = new Robot();
            /*
             * No se por que se le restan esas cantidades, solo se que
             * por alguna razon siempre le suma eso a la posicion del
             * mouse.
             */
            double x = robot.getMousePosition().getX() - 321;
            double y = robot.getMousePosition().getY() - 278;
            CircleCenter circleCenter = new CircleCenter(x, y);
            Node node = new Node(clipboard.getName() + " Copy", circleCenter);
            figures.add(node);
            nodeList.add(node);
            selectFigure = node;
            FilesManager.initializeMatrix(this);
            drawShapes();
        }
    }

    /************************************
     ****      GETTERS Y SETTERS     ****
     ************************************/

    public File getArchivoGrafo(){
        return archivoGrafo;
    }

    public void setArchivoGrafo(File file){
        archivoGrafo = file;
    }

    public Canvas getCanvas(){
        return canvas;
    }

    public List<Figure> getFigures(){
        return figures;
    }

    public List<Node> getNodeList(){
        return nodeList;
    }

    public Label getNodeCounterLabel(){
        return nodeCounterLabel;
    }

    public Label getEdgeCounterLabel(){
        return edgeCounterLabel;
    }

    public Label getFileTitleLabel(){
        return fileTitleLabel;
    }

    public List<NodeController> getNodeMenusOpen(){
        return nodeMenusOpen;
    }

    public int[][] getAdjacencyMatrix(){
        return adjacencyMatrix;
    }

    public void setAdjacencyMatrix(int[][] adjacencyMatrix) {
        this.adjacencyMatrix = adjacencyMatrix;
    }

    public Node getSelectedNode() {
        return selectedNode;
    }


}