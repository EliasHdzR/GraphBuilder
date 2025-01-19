package edu.upvictoria.graphbuilder;

import edu.upvictoria.graphbuilder.Figuras.CircleCenter;
import edu.upvictoria.graphbuilder.Figuras.Edge;
import edu.upvictoria.graphbuilder.Figuras.Figure;
import edu.upvictoria.graphbuilder.Figuras.Node;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FilesManager {
    public static void newFile(BuilderController controller) {
        controller.setArchivoGrafo(null);

        Canvas canvas = controller.getCanvas();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        List<Figure> figures = controller.getFigures();
        figures.clear();

        List<Node> nodes = controller.getNodeList();
        nodes.clear();

        initializeMatrix(controller);

        Label nodeCounterLabel = controller.getNodeCounterLabel();
        nodeCounterLabel.setText("0");

        Label edgeCounterLabel = controller.getEdgeCounterLabel();
        edgeCounterLabel.setText("0");

        Label fileTitleLabel = controller.getFileTitleLabel();
        fileTitleLabel.setText("Nuevo Archivo");
    }

    public static void initializeMatrix(BuilderController controller) {
        List<Node> nodes = controller.getNodeList();
        int currentSize = nodes.size();

        int[][] adjacencyMatrix = controller.getAdjacencyMatrix();
        // Si la matriz es null, inicializa como una matriz vacía
        if (adjacencyMatrix == null) {
            adjacencyMatrix = new int[currentSize][currentSize];
            controller.setAdjacencyMatrix(adjacencyMatrix);
        }
        // Si la matriz ya existe, expande la matriz para acomodar nuevos nodos
        else if (adjacencyMatrix.length < currentSize) {
            int[][] newMatrix = new int[currentSize][currentSize];

            // Copia las conexiones existentes a la nueva matriz
            for (int i = 0; i < adjacencyMatrix.length; i++) {
                System.arraycopy(adjacencyMatrix[i], 0, newMatrix[i], 0, adjacencyMatrix[i].length);
            }

            // Asigna la nueva matriz como la matriz de adyacencia
            controller.setAdjacencyMatrix(newMatrix);
        }
    }

    public static void openFile(BuilderController controller) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Abrir Archivo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));

        File file = fileChooser.showOpenDialog(null);

        if(file == null){
            return;
        }

        newFile(controller);

        if(!file.getAbsolutePath().endsWith(".csv")){
            file = new File(file.getAbsolutePath() + ".csv");
        }

        controller.setArchivoGrafo(file);

        readCSVcontent(controller);
        controller.getFileTitleLabel().setText(controller.getArchivoGrafo().getAbsolutePath());
        controller.showMessage("   Abierto " + controller.getArchivoGrafo().getName());
    }

    private static void readCSVcontent(BuilderController controller) {
        int filaMatriz = 0;
        ArrayList<Edge> edgeList = new ArrayList<>();
        try  {
            File archivoGrafo = controller.getArchivoGrafo();
            BufferedReader br = new BufferedReader(new FileReader(archivoGrafo));
            String linea;
            boolean leyendoCoordenadas = true;

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                if (leyendoCoordenadas) {
                    if (linea.startsWith(";")) {
                        leyendoCoordenadas = false;
                        continue;
                    }

                    // Procesa las coordenadas del nodo
                    String[] partes = linea.split(";");
                    String nombre = partes[0];
                    double x = Double.parseDouble(partes[1]);
                    double y = Double.parseDouble(partes[2]);

                    CircleCenter circleCenter = new CircleCenter(x, y);
                    Node newNode = new Node(nombre, circleCenter);

                    List<Figure> figures = controller.getFigures();
                    figures.add(newNode);

                    List<Node> nodes = controller.getNodeList();
                    nodes.add(newNode);
                } else {
                    // Procesa la matriz de adyacencia
                    String[] partes = linea.split(";");

                    // Itera desde el índice 1 para omitir el nombre del nodo en la matriz
                    List<Node> nodes = controller.getNodeList();
                    Node nodo1 = nodes.get(filaMatriz);
                    for (int i = 1; i < partes.length; i++) {
                        if (partes[i].equals("1")) {
                            Node nodo2 = nodes.get(i - 1);
                            Edge edge = new Edge(nodo1, nodo2);

                            // Verifica si la arista ya existe antes de agregar
                            boolean exists = false;
                            for (Edge edgeTemp : edgeList) {
                                if (edgeTemp.doesExist(nodo1, nodo2)) {
                                    exists = true;
                                    break;
                                }
                            }

                            // Agrega la arista solo si no existe
                            if (!exists) {
                                List<Figure> figures = controller.getFigures();
                                figures.add(edge);
                                edgeList.add(edge);
                                nodo1.addToEdgeList(edge);
                                nodo2.addToEdgeList(edge);

                                Node selectedNode = controller.getSelectedNode();
                                int fromIndex = nodes.indexOf(selectedNode);
                                int toIndex = nodes.indexOf(nodo2);

                                if (fromIndex != -1 && toIndex != -1) {
                                    int[][] adjacencyMatrix = controller.getAdjacencyMatrix();
                                    adjacencyMatrix[fromIndex][toIndex] = 1;
                                    adjacencyMatrix[toIndex][fromIndex] = 1;
                                }
                            }
                        }
                    }
                    filaMatriz++;  // Aumenta después de cada fila de la matriz
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        controller.drawShapes();
    }

    public static void saveFile(BuilderController controller) {
        File archivoGrafo = controller.getArchivoGrafo();
        // si es un archivo nuevo
        if(archivoGrafo == null){
            saveAs(controller);
            controller.showMessage("   Guardado en " + controller.getArchivoGrafo().getName());
            System.out.println(controller.getArchivoGrafo().getAbsolutePath());
            return;
        }

        try {
            if(!archivoGrafo.getAbsolutePath().endsWith(".csv")){
                archivoGrafo = new File(archivoGrafo.getAbsolutePath() + ".csv");
            }

            saveMatrixToCSV(archivoGrafo.getAbsolutePath(), controller);
            System.out.println(archivoGrafo.getAbsolutePath());
            controller.getFileTitleLabel().setText(archivoGrafo.getAbsolutePath());
            controller.showMessage("   Guardado en " + archivoGrafo.getName());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al guardar el archivo CSV.");
        }
    }

    public static void saveAs(BuilderController controller) {
        Stage stage = (Stage) controller.getCanvas().getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Grafo como CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));

        controller.setArchivoGrafo(fileChooser.showSaveDialog(stage));
        File archivoGrafo = controller.getArchivoGrafo();

        if (archivoGrafo != null) {
            if (!archivoGrafo.getName().toLowerCase().endsWith(".csv")) {
                archivoGrafo = new File(archivoGrafo.getAbsolutePath() + ".csv");
            }
            try {
                saveMatrixToCSV(archivoGrafo.getAbsolutePath(), controller);
                controller.getFileTitleLabel().setText(archivoGrafo.getAbsolutePath());
                System.out.println("La matriz de adyacencia se ha guardado en '" + archivoGrafo.getName() + "'.");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error al guardar el archivo CSV.");
            }
        }
    }

    //guarda la matriz en csv
    public static void saveMatrixToCSV(String filePath, BuilderController controller) throws IOException {
        List<Node> nodeList = controller.getNodeList();

        if(!filePath.endsWith(".csv")){
            filePath = filePath + ".csv";
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Node node : nodeList) {
                writer.write(node.getName() + ";" + node.getmCenter().getX() + ";" + node.getmCenter().getY());
                writer.newLine();
            }
            writer.newLine();

            // Escribe la primera fila con encabezados
            writer.write(";X;");
            for (Node node : nodeList) {
                writer.write(node.getName() + ";");
            }
            writer.newLine();

            int[][] adjacencyMatrix = controller.getAdjacencyMatrix();
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

    /**
     * Me quiero matar bro
     * */
    public static void CanvasToPng(BuilderController controller) {
        Canvas canvas = controller.getCanvas();
        Stage stage = (Stage) canvas.getScene().getWindow();

        // Inicializamos los límites
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        List<Figure> figures = controller.getFigures();
        for (Figure figure : figures) {
            if (figure instanceof Node node) {
                double nodeX = node.getmCenter().getX();
                double nodeY = node.getmCenter().getY();
                double radius = node.getmRadius();

                minX = Math.min(minX, nodeX - radius); // Izquierda
                minY = Math.min(minY, nodeY - radius); // Arriba
                maxX = Math.max(maxX, nodeX + radius); // Derecha
                maxY = Math.max(maxY, nodeY + radius); // Abajo
            }
        }

        if (minX == Double.MAX_VALUE || minY == Double.MAX_VALUE ||
                maxX == Double.MIN_VALUE || maxY == Double.MIN_VALUE) {
            return;
        }

        /* Agregar margen a los límites */
        double margin = 20;
        minX = Math.max(minX - margin, 0);
        minY = Math.max(minY - margin, 0);
        maxX = Math.min(maxX + margin, canvas.getWidth());
        maxY = Math.min(maxY + margin, canvas.getHeight());

        WritableImage image = new WritableImage((int) (maxX - minX), (int) (maxY - minY));

        SnapshotParameters params = new SnapshotParameters();
        params.setViewport(new Rectangle2D(minX, minY, maxX - minX, maxY - minY));

        canvas.snapshot(params, image);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar imagen como");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagen PNG", "*.png"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            if (!file.getName().toLowerCase().endsWith(".png")) {
                file = new File(file.getAbsolutePath() + ".png");
            }
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                controller.showMessage("   Guardado en " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
