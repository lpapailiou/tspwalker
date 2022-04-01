package ui;

import ai.GeneticAlgorithm;
import ch.kaiki.nn.data.Graph;
import ch.kaiki.nn.genetic.GeneticAlgorithmBatch;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.NN2DPlot;
import ch.kaiki.nn.ui.NNGraph;
import ch.kaiki.nn.ui.color.GraphColor;
import ch.kaiki.nn.ui.color.NNChartColor;
import ch.kaiki.nn.ui.color.NNGraphColor;
import ch.kaiki.nn.util.Initializer;
import ch.kaiki.nn.util.Optimizer;
import ch.kaiki.nn.util.Rectifier;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static ch.kaiki.nn.ui.color.NNColor.blend;
import static javafx.scene.paint.Color.*;

public class ApplicationController implements Initializable {
    @FXML
    private GridPane grid;
    @FXML
    private Canvas graphCanvas;
    @FXML
    private Canvas statsCanvas;
    @FXML
    private HBox hiddenLayerConfiguration;
    @FXML
    private Canvas nnVisualizationCanvas;
    @FXML
    private Label datasetSelectorLabel;
    @FXML
    private ComboBox<String> datasetSelector;
    @FXML
    private VBox geneticControls;



    @FXML
    private ComboBox<String> hiddenLayerCount;
    @FXML
    private HBox hiddenLayerControls;

    @FXML
    private Label generationCount;
    @FXML
    private TextField parentCountControl;
    @FXML
    private TextField poolSizeControl;

    @FXML
    private TextField mutationRateControl;
    @FXML
    private ComboBox<String> mutationRateOptimizerControl;
    @FXML
    private Label mutationRateDecayLabel;
    @FXML
    private TextField mutationRateDecayControl;
    @FXML
    private TextField learningRateControl;
    @FXML
    private ComboBox<String> learningRateOptimizerControl;
    @FXML
    private Label learningRateDecayLabel;
    @FXML
    private TextField learningRateDecayControl;
@FXML
private ComboBox rectifierControl;
@FXML
private ComboBox initializerControl;
    @FXML
    private Label stepCount;
    @FXML
    private Label maxStepCount;
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
    private Button startBut;
   // @FXML
   // private Button skipBut;
    @FXML
    private Button stopBut;

    private Stage stage;
    private final ObservableList<String> datasetList = FXCollections.observableArrayList(Arrays.stream(DatasetType.values()).map(Enum::name).collect(Collectors.toList()));
    private ObservableList<String> layerCount = FXCollections.observableArrayList("0", "1", "2", "3", "4", "5");
    private ObservableList<String> optimizerList = FXCollections.observableArrayList("NONE", "SGD");
    private ObservableList<String> rectifierList = FXCollections.observableList(Arrays.stream(Rectifier.values()).map(Enum::name).collect(Collectors.toList()));
    private ObservableList<String> initializerList = FXCollections.observableList(Arrays.stream(Initializer.values()).map(Enum::name).collect(Collectors.toList()));
    private State state = State.getInstance();
    private NN2DPlot graphPlot;
    private NN2DPlot statsPlot;
    private NNGraph nnPlot;
    private final Color accentColor = Color.web("#c71585");
    private final GraphColor graphColor = new GraphColor(PINK, TRANSPARENT, accentColor, accentColor, LIGHTSALMON, accentColor);
    private final NNChartColor chartColor = new NNChartColor(TRANSPARENT, blend(LIGHTGRAY, TRANSPARENT, 0), DARKGRAY, LIGHTGRAY, LIGHTGRAY, DARKGRAY, DARKGRAY, DARKGRAY);
    private final NNGraphColor nnGraphColor = new NNGraphColor(TRANSPARENT, accentColor, accentColor, LIGHTSALMON, TRANSPARENT, accentColor.brighter(), accentColor.darker(), accentColor.brighter(), accentColor.darker());
    private Timeline timeline;
    final GeneticAlgorithm[] geneticAlgorithm = {null};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setGraphs();

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
        //skipBut.setOnAction(e -> skip());
        stopBut.setOnAction(e -> stopTimeline());
        initializeControls();
        loadDataset();
        updateHiddenLayerCount();
        Platform.runLater(() -> grid.getParent().requestFocus());
    }

    private void setGraphs() {
        graphPlot = new NN2DPlot(graphCanvas.getGraphicsContext2D());
        graphPlot.setChartColors(chartColor);
        graphPlot.showTickMarkLabels(false);
        graphPlot.showTickMarks(false);
        graphPlot.showGridContent(false);
        graphPlot.setTitle("graph for dataset " + state.getDatasetName());
        graphPlot.triggerInvalidate();


        statsPlot = new NN2DPlot(statsCanvas.getGraphicsContext2D());
        statsPlot.setChartColors(chartColor);
        statsPlot.setTitle("statistics");
        statsPlot.showLegend(true);
        statsPlot.triggerInvalidate();

    }

    private void loadDataset() {
        // get dataset
        DatasetType type = DatasetType.valueOf(datasetSelector.getValue());
        Dataset dataset = state.getDataset(type);

        // load dataset to graph
        Graph graph = dataset.getGraph();
        int verticeCount = graph.getVertices().size();
        state.setConfiguration(new int[]{verticeCount*3, verticeCount*2, verticeCount});
        graphPlot.plotGraph(graph, graphColor);

        // TOOD: set hidden layers to default
        updateHiddenLayerCount();
        updateHiddenLayerNodeCount();
        loadNeuralNetwork();
    }

    // TODO: make agent for algorithm
    // TODO: implement go / continue mode

    private void loadNeuralNetwork() {
        // initialize new neural network
        // TODO: move functionality to state
        NeuralNetwork neuralNetwork = new NeuralNetwork.Builder(state.getConfiguration())
                .setMutationRate(state.getMutationRate())
                .setMutationRateOptimizer(state.getMutationRateOptimizer())
                .setMutationRateMomentum(state.getMutationRateDecay())
                .setLearningRate(state.getLearningRate())
                .setLearningRateOptimizer(state.getLearningRateOptimizer())
                .setLearningRateMomentum(state.getLearningRateDecay())
                .setInitializer(state.getInitializer())
                .setDefaultRectifier(state.getRectifier())
                .build();
        state.setNeuralNetwork(neuralNetwork, nnPlot);

    }


    private void runAlgorithm() {
        loadNeuralNetwork();
        setDisable(true);
        setGraphs();
        GeneticAlgorithmBatch<GeneticAlgorithm> batch = new GeneticAlgorithmBatch<>(GeneticAlgorithm.class, state.getNeuralNetwork(), state.getPopulationSize())
                .setReproductionPoolSize(state.getPoolSize())
                .setReproductionSpecimenCount(state.getParentCount());

        int maxGenerations = state.getGenerationCount();
        AtomicInteger currentGeneration = new AtomicInteger(-1);
        timeline = new Timeline((new KeyFrame(Duration.millis(200), e -> {
            if (geneticAlgorithm[0] == null) {
                currentGeneration.getAndIncrement();
                batch.processGeneration();
                NeuralNetwork best = batch.getBestNeuralNetwork();
                geneticAlgorithm[0] = new GeneticAlgorithm(best);

                if (maxGenerations != currentGeneration.get()) {
                    state.setNeuralNetwork(best, nnPlot);
                    graphPlot.plotGraph(geneticAlgorithm[0].getGraph(), graphColor);
                }
            }

            if (geneticAlgorithm[0] == null || maxGenerations == currentGeneration.get()) {
                stopTimeline();
            } else {
                boolean running = geneticAlgorithm[0].perform();
                graphPlot.plotGraph(geneticAlgorithm[0].getGraph(), graphColor);
                int steps = geneticAlgorithm[0].getSteps();
                int maxSteps = geneticAlgorithm[0].getMaxSteps();
                double distance = geneticAlgorithm[0].getDistance();
                double tDistance = geneticAlgorithm[0].getTargetDistance();
                String path = geneticAlgorithm[0].getPath().toString();
                int gen = currentGeneration.get();
                double fitness = geneticAlgorithm[0].getFitness();
                double cost = geneticAlgorithm[0].getCost();

                Platform.runLater(() -> {
                    stepCount.setText(steps + "");
                    maxStepCount.setText(maxSteps + "");
                    distanceCount.setText(String.format("%,.0f", distance) + "");
                    targetDistance.setText(String.format("%,.0f", tDistance) + "");
                    generationCount.setText(gen + "");
                    pathTxt.setText(path);
                });

                if (!running) {
                    statsPlot.plotLine(currentGeneration.get(), cost, "cost", ORANGE.darker());
                    statsPlot.plotLine(currentGeneration.get(), fitness, "fitness", LIMEGREEN.darker());
                    statsPlot.plotLine(currentGeneration.get(), distance, "distance", accentColor.darker());


                    geneticAlgorithm[0] = null;
                }

            }
        })));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void skip() {
        geneticAlgorithm[0] = null;
    }

    private void stopTimeline() {
        geneticAlgorithm[0] = null;
        if (timeline != null) {
            timeline.stop();
        }
        setDisable(false);
    }

    private void initializeControls() {
        generationControl.setText(state.getGenerationCount() + "");
        populationControl.setText(state.getPopulationSize() + "");
        mutationRateControl.setText(state.getMutationRate() + "");
        learningRateControl.setText(state.getLearningRate() + "");
        parentCountControl.setText(state.getParentCount() + "");
        poolSizeControl.setText(state.getPoolSize() + "");
        mutationRateDecayControl.setText(state.getMutationRateDecay() + "");
        learningRateDecayControl.setText(state.getLearningRateDecay() + "");
        //skipBut.setDisable(true);
        stopBut.setDisable(true);
        pathTxt.setEditable(false);
        pathTxt.setFocusTraversable(false);

        AtomicReference<String> tempGenerations = new AtomicReference<>(state.getGenerationCount() + "");
        generationControl.focusedProperty().addListener((o, oldValue, newValue) -> {
            String previousValue = tempGenerations.toString();
            tempGenerations.set(generationControl.getText());
            if (validateIntegerField(generationControl, 1, 5000, tempGenerations.toString(), previousValue)) {
                state.setGenerationCount(Integer.parseInt(tempGenerations.toString()));
            } else {
                showPopupMessage("min: 1, max: 5000", generationControl);
            }
        });

        AtomicReference<String> tempPopulations = new AtomicReference<>(state.getPopulationSize() + "");
        populationControl.focusedProperty().addListener((o, oldValue, newValue) -> {
            String previousValue = tempPopulations.toString();
            tempPopulations.set(populationControl.getText());
            if (validateIntegerField(populationControl, 1, 5000, tempPopulations.toString(), previousValue)) {
                state.setPopulationSize(Integer.parseInt(tempPopulations.toString()));
            } else {
                showPopupMessage("min: 1, max: 5000", populationControl);
            }
        });

        AtomicReference<String> tempParent = new AtomicReference<>(state.getParentCount() + "");
        parentCountControl.focusedProperty().addListener((o, oldValue, newValue) -> {
            String previousValue = tempParent.toString();
            tempParent.set(parentCountControl.getText());
            if (validateIntegerField(parentCountControl, 1, 16, tempParent.toString(), previousValue)) {
                state.setParentCount(Integer.parseInt(tempParent.toString()));
            } else {
                showPopupMessage("min: 1, max: 16", parentCountControl);
            }
        });

        AtomicReference<String> tempRate = new AtomicReference<>(state.getMutationRate() + "");
        mutationRateControl.focusedProperty().addListener((o, oldValue, newValue) -> {
            String previousValue = tempRate.toString();
            tempRate.set(mutationRateControl.getText());
            if (validateDoubleField(mutationRateControl, 0, 1, tempRate.toString(),
                    previousValue)) {
                state.setMutationRate(Double.parseDouble(tempRate.toString()));
            } else {
                showPopupMessage("min: 0, max: 1", mutationRateControl);
            }
        });

        AtomicReference<String> tempLRate = new AtomicReference<>(state.getLearningRate() + "");
        learningRateControl.focusedProperty().addListener((o, oldValue, newValue) -> {
            String previousValue = tempLRate.toString();
            tempLRate.set(learningRateControl.getText());
            if (validateDoubleField(learningRateControl, 0, 1, tempLRate.toString(),
                    previousValue)) {
                state.setLearningRate(Double.parseDouble(tempLRate.toString()));
            } else {
                showPopupMessage("min: 0, max: 1", learningRateControl);
            }
        });

        AtomicReference<String> tempDecay = new AtomicReference<>(state.getMutationRateDecay() + "");
        mutationRateDecayControl.focusedProperty().addListener((o, oldValue, newValue) -> {
            String previousValue = tempDecay.toString();
            tempDecay.set(mutationRateDecayControl.getText());
            if (validateDoubleField(mutationRateDecayControl, 0, 1, tempDecay.toString(), previousValue)) {
                state.setMutationRateDecay(Double.parseDouble(tempDecay.toString()));
            } else {
                showPopupMessage("min: 0, max: 1", mutationRateDecayControl);
            }
        });

        AtomicReference<String> tempLDecay = new AtomicReference<>(state.getLearningRateDecay() + "");
        learningRateDecayControl.focusedProperty().addListener((o, oldValue, newValue) -> {
            String previousValue = tempLDecay.toString();
            tempLDecay.set(learningRateDecayControl.getText());
            if (validateDoubleField(learningRateDecayControl, 0, 1, tempLDecay.toString(), previousValue)) {
                state.setLearningRateDecay(Double.parseDouble(tempLDecay.toString()));
            } else {
                showPopupMessage("min: 0, max: 1", learningRateDecayControl);
            }
        });

        AtomicReference<String> tempPool = new AtomicReference<>(state.getPoolSize() + "");
        poolSizeControl.focusedProperty().addListener((o, oldValue, newValue) -> {
            String previousValue = tempPool.toString();
            tempPool.set(poolSizeControl.getText());
            if (validateDoubleField(poolSizeControl, 0, 1, tempPool.toString(), previousValue)) {
                state.setPoolSize(Double.parseDouble(tempPool.toString()));
            } else {
                showPopupMessage("min: 0, max: 1", poolSizeControl);
            }
        });

        mutationRateOptimizerControl.setItems(optimizerList);
        mutationRateOptimizerControl.getSelectionModel().select(state.getMutationRateOptimizer().ordinal());
        mutationRateOptimizerControl.setOnAction(e -> updateMROptimizerSelection());
        mutationRateDecayLabel.setVisible(state.getMutationRateOptimizer() != Optimizer.NONE);
        mutationRateDecayControl.setVisible(state.getMutationRateOptimizer() != Optimizer.NONE);

        learningRateOptimizerControl.setItems(optimizerList);
        learningRateOptimizerControl.getSelectionModel().select(state.getLearningRateOptimizer().ordinal());
        learningRateOptimizerControl.setOnAction(e -> updateLROptimizerSelection());
        learningRateDecayLabel.setVisible(state.getLearningRateOptimizer() != Optimizer.NONE);
        learningRateDecayControl.setVisible(state.getLearningRateOptimizer() != Optimizer.NONE);

        initializerControl.setItems(initializerList);
        initializerControl.getSelectionModel().select(state.getInitializer().ordinal());
        initializerControl.setOnAction(e -> updateInitializer());

        rectifierControl.setItems(rectifierList);
        rectifierControl.getSelectionModel().select(state.getRectifier().ordinal());
        rectifierControl.setOnAction(e -> updateRectifier());

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

    private void updateMROptimizerSelection() {
        Optimizer selectedOptimizer = Optimizer.valueOf(mutationRateOptimizerControl.getValue());
        mutationRateDecayLabel.setVisible(selectedOptimizer != Optimizer.NONE);
        mutationRateDecayControl.setVisible(selectedOptimizer != Optimizer.NONE);
        state.setMutationRateOptimizer(selectedOptimizer);
    }

    private void updateInitializer() {
        Initializer initializer = Initializer.valueOf(initializerControl.getValue().toString());
        state.setInitializer(initializer);
    }

    private void updateRectifier() {
        Rectifier rectifier = Rectifier.valueOf(rectifierControl.getValue().toString());
        state.setRectifier(rectifier);
    }

    private void updateLROptimizerSelection() {
        Optimizer selectedOptimizer = Optimizer.valueOf(learningRateOptimizerControl.getValue());
        learningRateDecayLabel.setVisible(selectedOptimizer != Optimizer.NONE);
        learningRateDecayControl.setVisible(selectedOptimizer != Optimizer.NONE);
        state.setLearningRateOptimizer(selectedOptimizer);
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
        //skipBut.setDisable(!disable);
        stopBut.setDisable(!disable);

        Platform.runLater(() -> grid.getParent().requestFocus());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
