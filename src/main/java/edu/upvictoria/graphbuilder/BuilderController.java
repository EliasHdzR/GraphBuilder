package edu.upvictoria.graphbuilder;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
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
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.robot.Robot;

import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.animation.PauseTransition;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BuilderController {
    // variables del controlador
    public Scene scene;
    private final ObservableList<Figure> figures = FXCollections.observableArrayList();
    private Double initialX = null;
    private Double initialY = null;
    private Node selectedNode = null;
    private File archivoGrafo = null;
    private Node clipboard = null;
    private Figure selectFigure = null;
    public final List<Event> undoList = new ArrayList<>();
    private final List<Event> redoList = new ArrayList<>();
    private final List<Node> nodeList = new ArrayList<>();
    private int[][] adjacencyMatrix;

    private boolean isTalkBackOn = false;
    private String currentStatus = "";
    private boolean isMovingaNode = false;
    private VoiceManager voiceManager = VoiceManager.getInstance();
    private Voice voice = null;

    @FXML
    private MenuBar menuBar;


    // info guardada de un archivo abierto (para poder comparar si se le han realizado cambios)
    private List<Node> nodeListBackup;
    private int[][] adjacencyMatrixBackup;

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
    @FXML private MenuItem btnTalkback;
    private final ArrayList<Button> buttons = new ArrayList<>();

    @FXML
    public void initialize() {
        fileTitleLabel.setText("New file");

        buttons.add(moveShapesButton);
        buttons.add(openMenusButton);
        buttons.add(deleteShapesButton);
        buttons.add(drawNodeButton);
        buttons.add(drawEdgeButton);
        menuBar.getMenus().forEach(this::attachTTSHandlersToMenu);
        addTTStoToolbarButtons();

        //desabilitar botones de la barra
        canvasToPngBtn.disableProperty().bind(Bindings.size(figures).isEqualTo(0));

        toolBar.setCursor(Cursor.DEFAULT);
        setMovingShapesStatus();
    }

    /************************************
     **** FUNCIONES DEL BUILDER MAIN ****
     ************************************/

    @FXML
    protected void setMovingShapesStatus(){
        removeHandlers();
        setDefaultStyle();
        setActiveStyle(moveShapesButton);

        scene = canvas.getScene();
        canvas.setOnMouseEntered(me -> canvas.setCursor(Cursor.OPEN_HAND));
        canvas.setOnMouseExited(me -> canvas.setCursor(Cursor.DEFAULT));

        canvas.setOnMouseClicked(this::clickFigure);
        canvas.setOnDragDetected(this::eventDragFigure);
        canvas.setOnMouseDragged(this::moveShape);
        canvas.setOnMouseReleased(this::endMoveShape);

        updateStatus("Moving Shapes");
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

        if (!isMovingaNode){
            textToSpeech("Moving Node" + nodo.getName());
        }
        isMovingaNode = true;
        nodo.move(x, y);
        drawShapes();
    }

    private void endMoveShape(MouseEvent mouseEvent) {
        canvas.setCursor(Cursor.OPEN_HAND);
        isMovingaNode = false;
        setMovingShapesStatus();
    }

    @FXML
    private void setDeleteFigureStatus (){
        removeHandlers();
        setDefaultStyle();
        setActiveStyle(deleteShapesButton);

        scene = canvas.getScene();
        canvas.setOnMouseEntered(me -> canvas.setCursor(Cursor.HAND));
        canvas.setOnMouseExited(me -> canvas.setCursor(Cursor.DEFAULT));
        canvas.setOnMouseClicked(this::eraseFigure);
        updateStatus("Deleting Figures");
    }

    private void eraseFigure(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        Figure figure = getFigureAt(x, y);

        int count = 0;

        // si es un nodo tmb hay que borrar todas las aristas que van hacia este nodo
        if(figure instanceof Node nodo){
            int nodeIndex = nodeList.indexOf(nodo);
            nodeList.remove(nodo);
            textToSpeech("Deleted node " + nodo.getName());

            List<Edge> nodoEdgeList = nodo.getEdgeList();
            for (Edge edge : nodoEdgeList) {
                figures.remove(edge);
                createEvent(2, edge, undoList);
                count++;
            }
            textToSpeech("Deleted " + nodoEdgeList.size() + "edges");

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

        // si es una arista, hay que recalcular el contenido de la matriz de adyacencia
        if(figure instanceof Edge arista){
            Node nodo1 = arista.getNodo1();
            Node nodo2 = arista.getNodo2();
            int fromIndex = nodeList.indexOf(nodo1);
            int toIndex = nodeList.indexOf(nodo2);
            adjacencyMatrix[fromIndex][toIndex] = 0;
            adjacencyMatrix[toIndex][fromIndex] = 0;
            textToSpeech("Deleted edge between " + nodo1.getName() + "and" + nodo2.getName());
        }

        createEvent(x, y, 2, figure, count);
        figures.remove(figure);
        nodeList.remove(figure);
        drawShapes();
        setDeleteFigureStatus();
    }

    @FXML
    private void setOpenFigureMenuStatus() {
        removeHandlers();
        setDefaultStyle();
        setActiveStyle(openMenusButton);

        canvas.setOnMouseEntered(me -> canvas.setCursor(Cursor.HAND));
        canvas.setOnMouseExited(me -> canvas.setCursor(Cursor.DEFAULT));
        canvas.setOnMouseClicked(this::openFigureMenu);
        updateStatus("Edit Figures");
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
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("menuNodo.fxml"));
            Stage subStage = new Stage();
            NodeController nodoControlador = new NodeController(nodo, subStage, this);
            fxmlLoader.setController(nodoControlador);
            Scene scene = new Scene(fxmlLoader.load());

            // Establecer la escena al Stage
            subStage.setTitle(nodo.getName());
            subStage.setScene(scene);

            // Configurar Stage y ventana modal
            subStage.initOwner(canvas.getScene().getWindow());
            subStage.initModality(Modality.WINDOW_MODAL);

            subStage.setMinWidth(319);
            subStage.setMinHeight(180);
            subStage.setMaxWidth(319);
            subStage.setMaxHeight(180);

            subStage.initStyle(StageStyle.UNDECORATED);
            nodoControlador.configureShortcuts();

            if (isTalkBackOn){
                nodoControlador.addTTStoButtons();
            }

            // Mostrar la ventana
            subStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        textToSpeech("Node " + nodo.getName() + " Menu Opened");
    }

    @FXML
    protected void setDrawNodeStatus() {
        removeHandlers();
        setDefaultStyle();
        setActiveStyle(drawNodeButton);

        scene = canvas.getScene();
        canvas.setOnMouseEntered(me -> canvas.setCursor(Cursor.CROSSHAIR));
        canvas.setOnMouseExited(me -> canvas.setCursor(Cursor.DEFAULT));

        canvas.setOnMouseClicked(this::drawNode);
        updateStatus("Drawing Nodes");
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
        Node node = new Node(circleCenter, nodeList.size() + 1);
        figures.add(node);
        selectFigure = node;
        nodeList.add(node);
        createEvent(1, node, undoList);
        FilesManager.initializeMatrix(this);
        textToSpeech("Drawing Node");
        drawShapes();
    }

    private void drawNode(String name, Node recover) {
        figures.add(recover);
        selectFigure = recover;
        FilesManager.initializeMatrix(this);
        drawShapes();
    }

    @FXML
    protected void setDrawEdgeStatus() {
        removeHandlers();
        setDefaultStyle();
        setActiveStyle(drawEdgeButton);

        scene = canvas.getScene();
        canvas.setOnMouseEntered(me -> canvas.setCursor(Cursor.CROSSHAIR));
        canvas.setOnMouseExited(me -> canvas.setCursor(Cursor.DEFAULT));
        canvas.setOnMouseDragged(this::drawEdge);
        canvas.setOnMouseReleased(this::endDrawEdge);
        updateStatus("Drawing Edges");
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
        textToSpeech("Edge between: " + selectedNode.getName() + " and " + nodo2.getName() + "created");
        selectFigure = arista;
        createEvent(1, arista, undoList);
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

    private void recoverEdge(Figure edge) {
        figures.add(edge);
        selectFigure = edge;
        drawShapes();
    }

    private void removeHandlers() {
        canvas.setOnMouseClicked(null);
        canvas.setOnMouseDragged(null);
        canvas.setOnMouseReleased(null);
        canvas.setOnMousePressed(null);
    }

    public boolean hasUnsavedChanges(){

        return !compareMatrices() || !compareNodeList();
    }

    /**
     * Compara la matriz de adyacencia actual con la de backup
     * @return true si son iguales, false si no
     */
    private boolean compareMatrices(){
        if (adjacencyMatrix == null || adjacencyMatrixBackup == null) {
            return false;
        }

        if (adjacencyMatrix.length != adjacencyMatrixBackup.length) {
            return false;
        }

        for (int i = 0; i < adjacencyMatrix.length; i++) {
            if (adjacencyMatrix[i].length != adjacencyMatrixBackup[i].length) {
                return false;
            }

            for (int j = 0; j < adjacencyMatrix[i].length; j++) {
                if (adjacencyMatrix[i][j] != adjacencyMatrixBackup[i][j]) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Compara la lista de nodos actual con la de backup
     * @return true si son iguales, false si no
     */
    private boolean compareNodeList(){
        if (nodeListBackup == null) {
            return false;
        }

        if (nodeList.size() != nodeListBackup.size()) {
            return false;
        }

        for (int i = 0; i < nodeList.size(); i++) {
            Node node = nodeList.get(i);
            Node nodeBackup = nodeListBackup.get(i);

            if(!nodeBackup.equals(node)){
                return false;
            }
        }

        return true;
    }

    /***********************************
     * **** FUNCIONES ARCHIVO *********
     * *********************************
     */

    /**
     * Vacía el canvas y lo deja como nuevo
     */
    @FXML
    public void newFile(){
        FilesManager.checkUnsavedChanges(this, "new file");
    }

    @FXML
    private void openFile(){
        FilesManager.checkUnsavedChanges(this, "open file");
    }

    public void createBackups(){
        nodeListBackup = new ArrayList<>(nodeList.size());

        for (Node node : nodeList) {
            nodeListBackup.add(new Node(node.getName(), new CircleCenter(node.getmCenter().getX(), node.getmCenter().getY())));
        }

        adjacencyMatrixBackup = new int[adjacencyMatrix.length][];
        for (int i = 0; i < adjacencyMatrix.length; i++) {
            adjacencyMatrixBackup[i] = adjacencyMatrix[i].clone();
        }
    }

    public void deleteBackups(){
        nodeListBackup = null;
        adjacencyMatrixBackup = null;
    }

    @FXML
    private void saveFile(){
        FilesManager.saveFile(this);
    }

    // Funcion que guarda en el CSV y muestra el Chooser
    @FXML
    public void saveAs() {
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
        FilesManager.checkUnsavedChanges(this, "exit");
    }

    public void showUnsavedChangesPopup(String process){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("warningPopup.fxml"));
            Stage subStage = new Stage();
            WarningController warningController = new WarningController(subStage, this, process);
            fxmlLoader.setController(warningController);
            Scene scene = new Scene(fxmlLoader.load());

            // Establecer la escena al Stage
            subStage.setScene(scene);

            // Configurar Stage y ventana modal
            subStage.initOwner(canvas.getScene().getWindow());
            subStage.initModality(Modality.WINDOW_MODAL);

            subStage.setMinWidth(342);
            subStage.setMinHeight(139);
            subStage.setMaxWidth(342);
            subStage.setMaxHeight(139);

            subStage.initStyle(StageStyle.UNDECORATED);
            warningController.focusBtnGuardar();

            if (isTalkBackOn){
                warningController.addTTStoButtons();
            }

            // Mostrar la ventana
            subStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /************************************
     ************ SHORTCUTS *************
     ***********************************/

    public void shortcuts() {
        scene.setOnKeyPressed( keyEvent -> {
            if (keyEvent.getCode() == KeyCode.N && keyEvent.isControlDown()) newFile();
            if (keyEvent.getCode() == KeyCode.O && keyEvent.isControlDown()) openFile();
            if (keyEvent.getCode() == KeyCode.S && keyEvent.isControlDown()) saveFile();
            if (keyEvent.getCode() == KeyCode.S && keyEvent.isControlDown() && keyEvent.isShiftDown()) saveAs();
            if (keyEvent.getCode() == KeyCode.P && keyEvent.isControlDown()) CanvasToPng();
            if (keyEvent.getCode() == KeyCode.Q && keyEvent.isControlDown()) exitApp();
            if (keyEvent.getCode() == KeyCode.F2) switchTalkBack();
            if (keyEvent.getCode() == KeyCode.C && keyEvent.isControlDown()) copy();
            if (keyEvent.getCode() == KeyCode.V && keyEvent.isControlDown()) paste();
            if (keyEvent.getCode() == KeyCode.DELETE) suprFigure();
            if (keyEvent.getCode() == KeyCode.X && keyEvent.isControlDown()) {
                copy();
                suprFigure();
            }
            if (keyEvent.getCode() == KeyCode.Z && keyEvent.isControlDown()) undo();
            if (keyEvent.getCode() == KeyCode.Y && keyEvent.isControlDown()) redo();
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

        if(adjacencyMatrixBackup != null){
            if (hasUnsavedChanges()) fileTitleLabel.setText(archivoGrafo.getName() + "*");
            else fileTitleLabel.setText(archivoGrafo.getName());
        }
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
     ************ TTS ***************
     ************************************/
    private void addTTStoToolbarButtons() {
        // Iterate through all toolbar children
        toolBar.getItems().forEach(node -> {
            if (node instanceof Button button) {
                Tooltip tooltip = button.getTooltip(); // Get the tooltip of the button
                if (tooltip != null) {
                    PauseTransition pause = new PauseTransition(Duration.seconds(1)); // x-second delay
                    button.setOnMouseEntered(event -> {
                        pause.setOnFinished(e -> textToSpeech(tooltip.getText()));
                        pause.playFromStart();
                    });
                    button.setOnMouseExited(event -> pause.stop()); // Stop the delay if the mouse exits
                }
            }
        });
    }

    private void attachTTSHandlersToMenu(Menu menu) {
        //Menu itself//
        PauseTransition menuPause = new PauseTransition(Duration.seconds(1)); // x-second delay
        //PauseTransition pause = new PauseTransition(Duration.seconds(1.5)); // works too
        menu.setOnShowing(event -> {
            menuPause.setOnFinished(e -> textToSpeech(menu.getText()));
            menuPause.playFromStart();
        });
        menu.setOnHidden(event -> menuPause.stop()); //Stop if stage hidden

        //MenuItems//
        for (int i = 0; i < menu.getItems().size(); i++) {
            MenuItem originalItem = menu.getItems().get(i);

            if (!(originalItem instanceof CustomMenuItem)) {
                Label label = new Label(originalItem.getText());

                PauseTransition itemPause = new PauseTransition(Duration.seconds(1)); // x-second delay
                label.setOnMouseEntered(e -> {
                    itemPause.setOnFinished(evt -> textToSpeech(originalItem.getText()));
                    itemPause.playFromStart();
                });
                label.setOnMouseExited(e -> itemPause.stop()); // Stop the delay if the mouse exits

                CustomMenuItem customItem = new CustomMenuItem(label);
                customItem.setOnAction(originalItem.getOnAction()); // Preserve original action
                menu.getItems().set(i, customItem);
            }
        }
    }

    private void updateStatus(String newStatus) {
        if (!newStatus.equals(currentStatus)) {
            currentStatus = newStatus;
            textToSpeech("Status " + newStatus + "activated");
        }
    }

    @FXML
    private void switchTalkBack() {
        if (!isTalkBackOn){
            isTalkBackOn = true;
            System.setProperty("freetts.voices",
                    "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            voice = voiceManager.getVoice("kevin16");
            voice.allocate();

            btnTalkback.setText("Disable TalkBack");
            textToSpeech("TalkBack Activated");
        } else {
            textToSpeechWaits();
            voice.deallocate();
            isTalkBackOn = false;
            btnTalkback.setText("Enable TalkBack");
        }
    }

    private void textToSpeechWaits() {
        voice.speak("TalkBack Deactivated");
    }

    public void textToSpeech(String text) {
        if (isTalkBackOn){
            new Thread(() -> {
                try {
                    if (voice != null) {
                        voice.speak(text);
                    } else {
                        System.err.println("Voice 'kevin16' not found.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
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

    private void clickFigure(MouseEvent mouseEvent) {
        /*
         * Esto existe para cuando se clikea una figura pero no se mueve
         */
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();

        selectFigure = getFigureAt(x, y);
    }

    private void eventDragFigure(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        Figure figure = getFigureAt(x, y);
        createEvent(x, y, 3, figure, undoList);
        selectFigure = figure;
    }

    private void suprFigure() {
        if (selectFigure != null) {
            if (selectFigure instanceof Node nodo) {
                List<Edge> nodoEdgeList = nodo.getEdgeList();
                for (Edge edge : nodoEdgeList) {
                    figures.remove(edge);
                }
            }
            createEvent(2, selectFigure, undoList);
            figures.remove(selectFigure);
            selectFigure = null;
            drawShapes();
        }
    }

    private void suprFigure(boolean flag) {
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
    private void copy() {
        if (selectFigure instanceof Node node) {
            /*
             * Cuando acabas de correr el programa aveces no copia nada
             * y no se porque pero x somos chavos mejor vamos a chelear
             * con toda la bandita caguamera.
             */
            clipboard = node;
        }
    }

    @FXML
    private void paste() {
        if (clipboard != null) {
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
            createEvent(1, node, undoList);
            selectFigure = node;
            FilesManager.initializeMatrix(this);
            drawShapes();
        }
    }

    @FXML
    private void cut() {
        copy();
        suprFigure();
    }

    protected void createEvent(int type, Figure figure, List<Event> list) {
        Event event = new Event();
        event.setType(type);
        event.setFigure(figure);
        event.setName(figure);
        list.add(event);
    }

    private void createEvent(double x, double y, int type, Figure figure, List<Event> list) {
        Event event = new Event();
        event.setType(type);
        event.setFigure(figure);
        event.setX(x);
        event.setY(y);
        list.add(event);
    }

    private void createEvent(double x, double y, int type, Figure figure, int count) {
        Event event = new Event();
        event.setType(type);
        event.setFigure(figure);
        event.setX(x);
        event.setY(y);
        event.setCount(count);
        undoList.add(event);
    }

    private void removeFigure(Event event, List<Event> list) {
        selectFigure = event.getFigure();
        suprFigure(true);
        if (event.getFigure() instanceof Node) {
            nodeList.remove(event.getFigure());
        }
        list.add(event);
    }

    private void recoverFigure(Event event, List<Event> list) {
        if (event.getFigure() instanceof Node node) {
            drawNode(node.getName(), node);
            nodeList.add(node);
            if (event.getCount() != 0) {
                for (int i = 0; i < event.getCount(); i++) {
                    Event temp = list.getLast();
                    list.removeLast();
                    recoverEdge(temp.getFigure());
                }
            }
        } else {
            recoverEdge(event.getFigure());
        }
    }

    private void recoverPosition(Event event, List<Event> list) {
        if (event.getFigure() instanceof Node node) {
            createEvent(node.getmCenter().getX(), node.getmCenter().getY(), 3, node, list);
            node.move(event.getX(), event.getY());
        }
        drawShapes();
    }

    private void recoverName(Event event, List<Event> list) {
        if (event.getFigure() instanceof Node node) {
            createEvent(4, node, list);
            node.setName(event.getName());
        }
        drawShapes();
    }

    @FXML
    private void undo() {
        if (!undoList.isEmpty()) {
            Event event = undoList.getLast();
            undoList.removeLast();
            switch (event.getType()) {
                case 1:
                    removeFigure(event, redoList);
                    break;
                case 2:
                    recoverFigure(event, undoList);
                    redoList.add(event);
                    break;
                case 3:
                    recoverPosition(event, redoList);
                    break;
                case 4:
                    recoverName(event, redoList);
                    break;
            }
        }
    }

    @FXML
    private void redo() {
        if (!redoList.isEmpty()) {
            Event event = redoList.getLast();
            redoList.removeLast();
            switch (event.getType()) {
                case 1:
                    recoverFigure(event, redoList);
                    undoList.add(event);
                    break;
                case 2:
                    removeFigure(event, undoList);
                    break;
                case 3:
                    recoverPosition(event, undoList);
                    break;
                case 4:
                    recoverName(event, undoList);
                    break;
            }
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

    public int[][] getAdjacencyMatrix(){
        return adjacencyMatrix;
    }

    public void setAdjacencyMatrix(int[][] adjacencyMatrix) {
        this.adjacencyMatrix = adjacencyMatrix;
    }

    public int[][] getAdjacencyMatrixBackup() {
        return adjacencyMatrixBackup;
    }
}