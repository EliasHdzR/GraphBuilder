package edu.upvictoria.graphbuilder;

import edu.upvictoria.graphbuilder.Figuras.CircleCenter;
import edu.upvictoria.graphbuilder.Figuras.Edge;
import edu.upvictoria.graphbuilder.Figuras.Figure;
import edu.upvictoria.graphbuilder.Figuras.Node;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class BuilderController {
    // variables del controlador
    private Scene scene;
    private final List<Figure> figures = new ArrayList<>();
    private final List<NodeController> nodeMenusOpen = new ArrayList<>();
    private final List<EdgeController> edgeMenusOpen = new ArrayList<>();
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

    /************************************
     **** FUNCIONES DEL BUILDER MAIN ****
     ************************************/

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

    @FXML
    private void setOpenFigureMenuStatus(){
        removeHandlers();
        canvas.setOnMouseEntered(me -> scene.setCursor(Cursor.HAND));
        canvas.setOnMouseExited(me -> scene.setCursor(Cursor.DEFAULT));
        canvas.setOnMouseClicked(this::openFigureMenu);
    }

    private void openFigureMenu(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        Figure figure = getFigureAt(x, y);

        if(figure == null){
            return;
        }

        if(figure instanceof Node nodo){
            openNodeMenu(nodo);
        } else if(figure instanceof Edge arista){
            openEdgeMenu(arista);
        }
    }

    private void openNodeMenu(Node nodo){
        // checamos si ya esta abierto, si lo está entonces traemos la ventana al plano principal
        for(NodeController controlador : nodeMenusOpen){
            Node nodoTemp = controlador.getNodo();
            if(nodoTemp == nodo){
                controlador.requestFocus();
                return;
            }
        }

        // si no pues lo abrimos en una ventana nueva y lo agregamos a los menus abiertos
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("menuNodo.fxml"));
            Stage stage = new Stage();
            NodeController nodoControlador = new NodeController(nodo, stage);
            fxmlLoader.setController(nodoControlador);
            Scene scene = new Scene(fxmlLoader.load());
            nodeMenusOpen.add(nodoControlador);

            stage.setTitle(nodo.getName());
            stage.setScene(scene);
            stage.setMinWidth(346);
            stage.setMinHeight(126);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEdgeMenu(Edge arista){
        // checamos si ya esta abierto, si lo está entonces traemos la ventana al plano principal
        for(EdgeController controlador : edgeMenusOpen){
            Edge aristaTemp = controlador.getArista();
            if(aristaTemp == arista){
                controlador.requestFocus();
                return;
            }
        }

        // si no pues lo abrimos en una ventana nueva y lo agregamos a los menus abiertos
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("menuArista.fxml"));
            Stage stage = new Stage();
            EdgeController aristaControlador = new EdgeController(arista, stage);
            fxmlLoader.setController(aristaControlador);
            Scene scene = new Scene(fxmlLoader.load());
            edgeMenusOpen.add(aristaControlador);

            stage.setTitle(arista.getName());
            stage.setScene(scene);
            stage.setMinWidth(346);
            stage.setMinHeight(282);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        // si esa figura no es un nodo o es el que ya elegimos entonces deja de dibujar el borrador de arista
        if(!(fig2 instanceof Node nodo2) || nodo2 == selectedNode || selectedNode == null){
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
        initialX = null;
        initialY = null;
        selectedNode = null;
        setDrawEdgeStatus();
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
}