package main;

import ai.GraphWalker;
import ch.kaiki.nn.data.Graph;
import ch.kaiki.nn.genetic.GeneticAlgorithmBatch;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.NN2DPlot;
import ch.kaiki.nn.ui.NNGraph;
import ch.kaiki.nn.ui.color.GraphColor;
import ch.kaiki.nn.ui.color.NNGraphColor;
import data.Dataset;
import data.DatasetType;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static javafx.scene.paint.Color.*;

public class ApplicationController implements Initializable {
    @FXML
    private GridPane grid;
    @FXML
    private Canvas graphCanvas;
    @FXML
    private HBox hiddenLayerConfiguration;
    @FXML
    private Canvas nnVisualizationCanvas;
    @FXML
    private Label datasetSelectorLabel;
    @FXML
    private ComboBox<String> datasetSelector;
    @FXML
    private HBox geneticControls;
    @FXML
    private ComboBox<String> hiddenLayerCount;
    @FXML
    private HBox hiddenLayerControls;

    @FXML
    private Label generationCount;
    @FXML
    private Label stepCount;
    @FXML
    private Label distanceCount;
    @FXML
    private Label targetDistance;
    @FXML
    private TextArea pathTxt;
    @FXML
    private TextField generationControl;
    @FXML
    private TextField populationControl;
    @FXML
    private TextField randomizationControl;

    @FXML
    private Button startBut;

    private Stage stage;
    private final ObservableList<String> datasetList = FXCollections.observableArrayList(Arrays.stream(DatasetType.values()).map(Enum::name).collect(Collectors.toList()));
    private ObservableList<String> layerCount = FXCollections.observableArrayList("0", "1", "2", "3", "4", "5");
    private State state = State.getInstance();
    private NN2DPlot graphPlot;
    private NNGraph nnPlot;
    private GraphColor graphColor = new GraphColor(PINK, TRANSPARENT, DARKRED, DARKRED, LIGHTSALMON, DARKRED);
    private NNGraphColor nnGraphColor = new NNGraphColor(TRANSPARENT, DARKRED, DARKRED, LIGHTSALMON, TRANSPARENT, DARKRED.brighter(), DARKRED.darker(),DARKRED.brighter(), DARKRED.darker());
    private Timeline timeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        graphPlot = new NN2DPlot(graphCanvas.getGraphicsContext2D());
        graphPlot.showTickMarkLabels(false);
        graphPlot.showTickMarks(false);

        nnPlot = new NNGraph(nnVisualizationCanvas.getGraphicsContext2D());
        nnPlot.setColorPalette(nnGraphColor);
        nnPlot.setNodeRadius(8);
        nnPlot.setLineWidth(0.5);
        nnPlot.setDynamicGrowth(false, true);

        datasetSelector.setItems(datasetList);
        datasetSelector.getSelectionModel().select(DatasetType.FIVE.ordinal());
        datasetSelector.setOnAction(e -> {
            loadDataset();
        });
        startBut.setOnAction(e -> runAlgorithm());
        initializeControls();
        loadDataset();
        updateHiddenLayerCount();
        Platform.runLater(() -> grid.getParent().requestFocus());
    }

    private void loadDataset() {
        // get dataset
        DatasetType type = DatasetType.valueOf(datasetSelector.getValue());
        Dataset dataset = state.getDataset(type);

        // load dataset to graph
        Graph graph = dataset.getGraph();
        int verticeCount = graph.getVertices().size();
        state.setConfiguration(new int[]{verticeCount*3, verticeCount*2, verticeCount});
        graphPlot.setTitle("Graph for dataset " + type.name());
        graphPlot.plotGraph(graph, graphColor);
        updateHiddenLayerCount();
        updateHiddenLayerNodeCount();
        loadNeuralNetwork();
    }

    private void loadNeuralNetwork() {
        // initialize new neural network

        NeuralNetwork neuralNetwork = new NeuralNetwork.Builder(state.getConfiguration())
                .setMutationRate(state.getRandomizationRate()).build();
        state.setNeuralNetwork(neuralNetwork);

        // visualize neural network
        nnPlot.setNeuralNetwork(neuralNetwork);
    }


    private void runAlgorithm() {
        setDisable(true);
        GeneticAlgorithmBatch<GraphWalker> batch = new GeneticAlgorithmBatch<>(GraphWalker.class, state.getNeuralNetwork(), state.getPopulationSize());
        final GraphWalker[] graphWalker = {null};
        int maxGenerations = state.getGenerationCount();
        AtomicInteger currentGeneration = new AtomicInteger(-1);
        timeline = new Timeline((new KeyFrame(Duration.millis(200), e -> {
            if (graphWalker[0] == null) {
                currentGeneration.getAndIncrement();
                batch.processGeneration();
                NeuralNetwork best = batch.getBestNeuralNetwork();
                graphWalker[0] = new GraphWalker(best);
                nnPlot.setNeuralNetwork(graphWalker[0].getNeuralNetwork());
                graphPlot.plotGraph(graphWalker[0].getGraph(), graphColor);
            }

            if (graphWalker[0] == null || maxGenerations == currentGeneration.get()) {
                stopTimeline();
            } else {
                boolean running = graphWalker[0].perform();
                graphPlot.plotGraph(graphWalker[0].getGraph(), graphColor);
                Platform.runLater(() -> {
                    if (graphWalker[0] != null) {
                        stepCount.setText(graphWalker[0].getSteps() + "");
                        distanceCount.setText(graphWalker[0].getDistance() + "");
                        targetDistance.setText(graphWalker[0].getTargetDistance() + "");
                        generationCount.setText(currentGeneration + "");
                        pathTxt.setText(graphWalker[0].getPath().toString());
                    }
                });

                if (!running) {
                    graphWalker[0] = null;
                }
            }
        })));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();



    }

    private void stopTimeline() {
        if (timeline != null) {
            timeline.stop();
        }
        setDisable(false);
    }

    private void initializeControls() {
        generationControl.setText(state.getGenerationCount() + "");
        populationControl.setText(state.getPopulationSize() + "");
        randomizationControl.setText(state.getRandomizationRate() + "");
        pathTxt.setEditable(false);
        pathTxt.setFocusTraversable(false);

        AtomicReference<String> tempGenerations = new AtomicReference<>(state.getGenerationCount() + "");
        generationControl.focusedProperty().addListener((o, oldValue, newValue) -> {
            String previousValue = tempGenerations.toString();
            tempGenerations.set(generationControl.getText());
            if (validateIntegerField(generationControl, 1, 5000, tempGenerations.toString(),
                    previousValue)) {
                state.setGenerationCount(Integer.parseInt(tempGenerations.toString()));
            } else {
                showPopupMessage("min: 1, max: 5000", generationControl);
            }
        });

        AtomicReference<String> tempPopulations = new AtomicReference<>(
                state.getPopulationSize() + "");
        populationControl.focusedProperty().addListener((o, oldValue, newValue) -> {
            String previousValue = tempPopulations.toString();
            tempPopulations.set(populationControl.getText());
            if (validateIntegerField(populationControl, 1, 5000, tempPopulations.toString(),
                    previousValue)) {
                state.setPopulationSize(Integer.parseInt(tempPopulations.toString()));
            } else {
                showPopupMessage("min: 1, max: 5000", populationControl);
            }
        });

        AtomicReference<String> tempRate = new AtomicReference<>(
                state.getRandomizationRate() + "");
        randomizationControl.focusedProperty().addListener((o, oldValue, newValue) -> {
            String previousValue = tempRate.toString();
            tempRate.set(randomizationControl.getText());
            if (validateDoubleField(randomizationControl, 0, 1, tempRate.toString(),
                    previousValue)) {
                state.setRandomizationRate(Double.parseDouble(tempRate.toString()));
            } else {
                showPopupMessage("min: 0, max: 1", randomizationControl);
            }
        });

        hiddenLayerCount.setItems(layerCount);
        hiddenLayerCount.getSelectionModel().select((state.getConfiguration().length)-2);
        hiddenLayerCount.setOnAction(e -> updateHiddenLayerCount());

        for (int i = 0; i < hiddenLayerControls.getChildren().size(); i++) {
            TextField field = (TextField) hiddenLayerControls.getChildren().get(i);
            int[] configuration = state.getConfiguration();
            if (i < configuration.length - 2) {
                field.setText(configuration[i + 1] + "");
            } else {
                field.setVisible(false);
            }
            String nodeCount = (configuration.length > i + 1) ? configuration[i + 1] + "" : Integer.toString(configuration[configuration.length-1]);
            AtomicReference<String> tempValue = new AtomicReference<>(nodeCount);
            field.focusedProperty().addListener((o, oldValue, newValue) -> {
                String previousValue = tempValue.toString();
                tempValue.set(field.getText());
                if (validateIntegerField(field, 1, 1024, tempValue.toString(), previousValue)) {
                    if (!tempValue.toString().equals(previousValue)) {
                        updateHiddenLayerNodeCount();
                    }
                } else {
                    showPopupMessage("min: 1, max: 1024", field);
                }
            });
        }
    }

    private void updateHiddenLayerCount() {
        int selection = Integer.parseInt(hiddenLayerCount.getValue());
        int[] configuration = state.getConfiguration();
        int[] newConfiguration = new int[selection+2];
        for (int i = 0; i < newConfiguration.length; i++) {
            if (configuration.length > i) {
                newConfiguration[i] = configuration[i];
            } else {
                newConfiguration[i] = configuration[configuration.length-1];
            }
        }
        newConfiguration[newConfiguration.length-1] = configuration[configuration.length-1];
        for (int i = 0; i < hiddenLayerControls.getChildren().size(); i++) {
            TextField field = (TextField) hiddenLayerControls.getChildren().get(i);
            if (i < selection) {
                field.setText(newConfiguration[i+1] + "");
                if (!field.isVisible()) {
                    field.setVisible(true);
                }
            } else {
                field.setVisible(false);
            }
        }
        state.setConfiguration(newConfiguration);
        loadNeuralNetwork();
    }

    private void updateHiddenLayerNodeCount() {
        int[] configuration = state.getConfiguration();
        int nodes = (int) hiddenLayerControls.getChildren().stream().filter(Node::isVisible).count();
        int[] newConfiguration = new int[nodes + 2];
        newConfiguration[0] = configuration[0];
        newConfiguration[newConfiguration.length - 1] = configuration[configuration.length - 1];

        int index = 1;
        for (Node node : hiddenLayerControls.getChildren()) {
            TextField field = (TextField) node;
            if (field.isVisible()) {
                newConfiguration[index] = Integer.parseInt(field.getText());
                index++;
            }
        }
        state.setConfiguration(newConfiguration);
        if (!Arrays.equals(configuration, newConfiguration)) {
            loadNeuralNetwork();
        }
    }

    private boolean validateIntegerField(TextField field, int min, int max, String newValue, String oldValue) {
        try {
            int result = Integer.parseInt(newValue);
            if (result >= min && result <= max) {
                field.setText(result + "");
                return true;
            } else {
                field.setText(oldValue);
            }
        } catch (Exception e) {
            field.setText(oldValue);
        }
        return false;
    }

    private boolean validateDoubleField(TextField field, int min, int max, String newValue, String oldValue) {
        try {
            double result = Double.parseDouble(newValue);
            if (result >= min && result <= max) {
                field.setText(result + "");
                return true;
            } else {
                field.setText(oldValue);
            }
        } catch (Exception e) {
            field.setText(oldValue);
        }
        return false;
    }

    private Popup createPopup(final String message) {
        final Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        Label label = new Label("  " + message + "  ");    // space for graphic reasons
        label.setOnMouseReleased(e -> popup.hide());
        popup.getContent().add(label);
        return popup;
    }
    private void showPopupMessage(final String message, Node node) {
        final Popup popup = createPopup(message);
        popup.setOnShown(e -> {
            popup.setX(node.localToScreen(node.getBoundsInLocal()).getMinX());
            popup.setY(node.localToScreen(node.getBoundsInLocal()).getMinY() - 30);
        });
        if (stage != null) {
            popup.show(stage);
        }
    }

    private void setDisable(boolean disable) {
        datasetSelectorLabel.setDisable(disable);
        datasetSelector.setDisable(disable);
        hiddenLayerConfiguration.setDisable(disable);
        geneticControls.setDisable(disable);
        startBut.setDisable(disable);


        if (!disable) {
            Platform.runLater(() -> grid.getParent().requestFocus());
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
