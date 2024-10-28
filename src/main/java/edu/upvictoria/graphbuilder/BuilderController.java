package edu.upvictoria.graphbuilder;

import edu.upvictoria.graphbuilder.Figuras.CircleCenter;
import edu.upvictoria.graphbuilder.Figuras.Edge;
import edu.upvictoria.graphbuilder.Figuras.Figure;
import edu.upvictoria.graphbuilder.Figuras.Node;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BuilderController {
    // variables del controlador
    private Scene scene;
    private final List<Figure> figures = new ArrayList<>();
    private Double initialX = null;
    private Double initialY = null;
    private Node selectedNode = null;

    // elementos de la gui
    @FXML private Canvas canvas;
    @FXML private ToolBar toolBar;
    @FXML private Label nodeCounterLaber;
    @FXML private Label edgeCounterLaber;

    @FXML
    public void initialize() {
        toolBar.setCursor(Cursor.DEFAULT);
    }

    @FXML
    protected void setMovingShapesStatus(){
        removeHandlers();
        scene = canvas.getScene();
        canvas.setOnMouseEntered(me -> scene.setCursor(Cursor.OPEN_HAND));
        canvas.setOnMouseExited(me -> scene.setCursor(Cursor.DEFAULT));

        canvas.setOnMouseDragged(this::moveShape);
        canvas.setOnMouseReleased(this::endMoveShape);
    }

    private void moveShape(MouseEvent mouseEvent) {
        canvas.setCursor(Cursor.CLOSED_HAND);
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        Figure figura = getFigureAt(x, y);

        if(!(figura instanceof Node nodo)){
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
    protected void setDrawNodeStatus(){
        removeHandlers();
        scene = canvas.getScene();
        canvas.setOnMouseEntered(me -> scene.setCursor(Cursor.CROSSHAIR));
        canvas.setOnMouseExited(me -> scene.setCursor(Cursor.DEFAULT));

        canvas.setOnMouseClicked(this::drawNode);
    }

    /**
     * Crea un nodo en el lugar en donde se hizo click
     * @param mouseEvent La accion del mouse
     */
    private void drawNode(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();

        CircleCenter circleCenter = new CircleCenter(x, y);
        Node node = new Node(circleCenter);
        figures.add(node);
        nodeList.add(node);
        initializeMatrix();
        drawShapes();
    }

    @FXML
    protected void setDrawEdgeStatus(){
        removeHandlers();
        scene = canvas.getScene();
        canvas.setOnMouseEntered(me -> scene.setCursor(Cursor.CROSSHAIR));
        canvas.setOnMouseExited(me -> scene.setCursor(Cursor.DEFAULT));
        canvas.setOnMouseDragged(this::drawEdge);
        canvas.setOnMouseReleased(this::endDrawEdge);   
    }

    private void drawEdge(MouseEvent mouseEvent) {
        if(initialX == null && initialY == null && selectedNode == null){
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
        //recuperamos la figura en la que se dejó de mantener presionado el clic izquirdo
        Figure fig2 = getFigureAt(mouseEvent.getX(), mouseEvent.getY());

        // si esa figura no es un nodo entonces deja de dibujar el borrador de arista
        if(!(fig2 instanceof Node nodo2)){
            initialX = null;
            initialY = null;
            selectedNode = null;
            setDrawEdgeStatus();
            drawShapes();
            return;
        }

        //creamos la arista y por cada figura en la lista:
        // 1. si la fig recuperada de la lista es una arista, tenemos que checar si entre los dos nodos elegidos
        //     ya existe una arista que los una, si sí, cancelamos el dibujado completamente
        // 2. si la fig recuperada es un nodo, checamos si es el nodo de inicio o final de la arista, si lo son
        //     entonces añadimos la arista a su lista de aristas propia
        Edge arista = new Edge(selectedNode, nodo2);
        for(Figure figure : figures){
            if(figure instanceof Edge aristaTemp && aristaTemp.doesExist(selectedNode, nodo2)){
                initialX = null;
                initialY = null;
                selectedNode = null;
                setDrawEdgeStatus();
                drawShapes();
                return;
            }

            if(figure == selectedNode || figure == nodo2){
                ((Node) figure).addToEdgeList(arista);
            }
        }

        // añadimos la arista a la lista y la dibujamos, posteriormente reiniciamos el estado de dibujo
        figures.add(arista);
        drawShapes();

        // Actualiza la matriz de adyacencia
        int fromIndex = nodeList.indexOf(selectedNode);
        int toIndex = nodeList.indexOf(nodo2);
        adjacencyMatrix[fromIndex][toIndex] = 1;
        adjacencyMatrix[toIndex][fromIndex] = 1; // Para grafo no dirigido

        initialX = null;
        initialY = null;
        selectedNode = null;
        setDrawEdgeStatus();
    }

    @FXML
    private void setDeleteFigureStatus(){
        removeHandlers();
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
            List<Edge> nodoEdgeList = nodo.getEdgeList();
            for(Edge edge : nodoEdgeList){
                figures.remove(edge);
            }
        }

        figures.remove(figure);
        drawShapes();
        setDeleteFigureStatus();
    }

    private void removeHandlers(){
        canvas.setOnMouseClicked(null);
        canvas.setOnMouseDragged(null);
        canvas.setOnMouseReleased(null);
        canvas.setOnMousePressed(null);
    }

    /************************************
     **** FUNCIONES PARA LAS FIGURAS ****
     ************************************/
    /**
     * Redibuja todas las figuras en el canvas para actualizar sus posiciones
     * y actualiza el label inferior
     */
    private void drawShapes(){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        int nodeCount = 0;
        int edgeCount = 0;

        for (Figure figure : figures) {
            figure.draw(gc);
            if(figure instanceof Node){
                nodeCount++;
            } else if(figure instanceof Edge){
                edgeCount++;
            }
        }

        nodeCounterLaber.setText(String.valueOf(nodeCount));
        edgeCounterLaber.setText(String.valueOf(edgeCount));
    }

    /**
     * Obtiene la figura que contenga las coordenadas del evento
     * @param x Coordenada x del click
     * @param y Coordenada y del click
     * @return Una figura
     */
    private Figure getFigureAt(double x, double y){
        for (Figure figure : figures) {
            if(figure.contains(x, y)) {
                return figure;
            }
        }

        return null;
    }

    //reinicia la matriz cada que se valla agregando un nodo
    private List<Node> nodeList = new ArrayList<>();
    private int[][] adjacencyMatrix;

    private void initializeMatrix() {
        int size = nodeList.size();
        adjacencyMatrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                adjacencyMatrix[i][j] = 0; // Inicializa todas las conexiones a 0
            }
        }
    }

    //guarda la matriz en csv
    private void saveMatrixToCSV(String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // Escribe la primera fila con encabezados
            writer.write(";");
            for (Node node : nodeList) {
                writer.write(node.getName() + ";");
            }
            writer.newLine();

            // Escribe cada fila de la matriz de adyacencia
            for (int i = 0; i < adjacencyMatrix.length; i++) {
                writer.write(nodeList.get(i).getName() + ";"); // Nombre del nodo al inicio de la fila
                for (int j = 0; j < adjacencyMatrix[i].length; j++) {
                    writer.write(adjacencyMatrix[i][j] + ";");
                }
                writer.newLine();
            }
        }
    }

    //Funcion que guarda en el CSV y muestra el Chooser
    @FXML
    private void saveToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Matriz como CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());

        if (file != null) {
            try {
                saveMatrixToCSV(file.getAbsolutePath());
                System.out.println("La matriz de adyacencia se ha guardado en '" + file.getName() + "'.");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error al guardar el archivo CSV.");
            }
        }
    }
}