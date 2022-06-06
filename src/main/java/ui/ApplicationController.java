package ui;

import ai.GeneticNeuralNetwork;
import ai.agent.GeneticAgent;
import ai.agent.PathAgent;
import ch.kaiki.nn.data.Graph;
import ch.kaiki.nn.genetic.CrossoverStrategy;
import ch.kaiki.nn.ui.color.GraphColor;
import ch.kaiki.nn.ui.color.NNChartColor;
import ch.kaiki.nn.ui.color.NNGraphColor;
import ch.kaiki.nn.util.Initializer;
import ch.kaiki.nn.util.Optimizer;
import ch.kaiki.nn.util.Rectifier;
import data.Dataset;
import data.DatasetType;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import main.Mode;

import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;
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
    private Label modeSelectorLabel;
    @FXML
    private ComboBox<String> modeSelector;
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
    private TextField crossoverControl;
    @FXML
    private ComboBox<String> crossoverStrategyControl;
    @FXML
    private Label crossoverLabel;
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
    private HBox neuralVisualization;
    @FXML
    private HBox initializerBox;


    @FXML
    private Button startBut;
   // @FXML
   // private Button skipBut;
    @FXML
    private Button stopBut;
    private Stage stage;
    private final ObservableList<String> datasetList = FXCollections.observableArrayList(Arrays.stream(DatasetType.values()).map(Enum::name).collect(Collectors.toList()));
    private final ObservableList<String> modeList = FXCollections.observableArrayList(Arrays.stream(Mode.values()).map(Enum::name).collect(Collectors.toList()));

    private final ObservableList<String> layerCount = FXCollections.observableArrayList("0", "1", "2", "3", "4", "5");
    private final ObservableList<String> optimizerList = FXCollections.observableArrayList("NONE", "SGD");
    private final ObservableList<String> rectifierList = FXCollections.observableList(Arrays.stream(Rectifier.values()).map(Enum::name).collect(Collectors.toList()));
    private final ObservableList<String> initializerList = FXCollections.observableList(Arrays.stream(Initializer.values()).map(Enum::name).collect(Collectors.toList()));
    private final ObservableList<String> crossoverList = FXCollections.observableList(Arrays.stream(CrossoverStrategy.values()).map(Enum::name).collect(Collectors.toList()));
    private final ObservableList<String> simpleCrossoverList = FXCollections.observableArrayList("SLICE");
    private final State state = State.getInstance();

    private final Color accentColor = Color.web("#c71585");
    private final GraphColor graphColor = new GraphColor(PINK, TRANSPARENT, accentColor, accentColor, LIGHTSALMON, accentColor);
    private final NNChartColor chartColor = new NNChartColor(TRANSPARENT, blend(LIGHTGRAY, TRANSPARENT, 0), DARKGRAY, LIGHTGRAY, LIGHTGRAY, DARKGRAY, DARKGRAY, DARKGRAY);
    private final NNGraphColor nnGraphColor = new NNGraphColor(TRANSPARENT, accentColor, accentColor, LIGHTSALMON, TRANSPARENT, accentColor.brighter(), accentColor.darker(), accentColor.brighter(), accentColor.darker());
    private Timeline timeline;
    final GeneticNeuralNetwork[] geneticNeuralNetwork = {null};

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        state.inititializeNNPlot(nnVisualizationCanvas.getGraphicsContext2D(), nnGraphColor);
        state.initializeCharts(graphCanvas.getGraphicsContext2D(), statsCanvas.getGraphicsContext2D(), chartColor);
        state.setGraphs();

        datasetSelector.setItems(datasetList);
        datasetSelector.getSelectionModel().select(DatasetType.FIVE.ordinal());
        datasetSelector.setOnAction(e -> {
            loadDataset();
        });

        modeSelector.setItems(modeList);
        modeSelector.getSelectionModel().select(Mode.PERMUTATION.ordinal());
        modeSelector.setOnAction(e -> {
            toggleMode();
        });

        startBut.setOnAction(e -> runAlgorithm());
        //skipBut.setOnAction(e -> skip());
        stopBut.setOnAction(e -> state.stopTimeline());
        initializeControls();
        loadDataset();
        updateHiddenLayerCount();
        state.addAttributeRefreshlistener(e -> {
            Platform.runLater(() -> {
                stepCount.setText(state.getSteps() + "");
                maxStepCount.setText(state.getMaxSteps() + "");
                distanceCount.setText(String.format("%,.0f", state.getDistance()) + "");
                targetDistance.setText(String.format("%,.0f", state.gettDistance()) + "");
                generationCount.setText(state.getGen() + "");
                pathTxt.setText(state.getPath());
            });
        });
        state.addStopTimeLineListener(e -> {
            stopTimeline();
        });

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
        state.setCurrentGraph(graph);
        state.plotGraph();

        // TOOD: set hidden layers to default
        updateHiddenLayerCount();
        updateHiddenLayerNodeCount();
        state.loadNeuralNetwork();
    }

    private void toggleMode() {
        Mode mode = Mode.valueOf(modeSelector.getValue());
        boolean showNeural = mode == Mode.NEURAL;
/*
        if (showNeural) {
            crossoverStrategyControl.setItems(crossoverList);
        } else {
            crossoverStrategyControl.setItems(simpleCrossoverList);
        }*/
        updateCrossover();
        toggleManaged(hiddenLayerConfiguration, showNeural);
        toggleManaged(neuralVisualization, showNeural);
        toggleManaged(initializerBox, showNeural);



        //TODO
    }



    private void runAlgorithm() {
        state.loadNeuralNetwork();
        setDisable(true);
        state.setGraphs();
        Mode mode = Mode.valueOf(modeSelector.getValue());
        if (mode == Mode.NEURAL) {
            new GeneticAgent().run();
        } else {
            new PathAgent().run();
        }
        //new BruteForceAgent().run();
    }


    private void stopTimeline() {
        setDisable(false);
    }

    private void initializeControls() {
        generationControl.setText(state.getGenerationCount() + "");
        populationControl.setText(state.getPopulationSize() + "");
        mutationRateControl.setText(state.getMutationRate() + "");
        learningRateControl.setText(state.getLearningRate() + "");
        crossoverControl.setText(state.getCrossoverSliceCount() + "");
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

        AtomicReference<String> tempCrossover = new AtomicReference<>(state.getCrossoverSliceCount() + "");
        crossoverControl.focusedProperty().addListener((o, oldValue, newValue) -> {
            String previousValue = tempCrossover.toString();
            tempCrossover.set(crossoverControl.getText());
            if (validateIntegerField(crossoverControl, 1, 5000, tempCrossover.toString(), previousValue)) {
                state.setCrossoverSliceCount(Integer.parseInt(tempCrossover.toString()));
            } else {
                showPopupMessage("min: 1, max: 5000", crossoverControl);
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

        crossoverStrategyControl.setItems(crossoverList);
        crossoverStrategyControl.getSelectionModel().select(state.getCrossoverStrategy().ordinal());
        crossoverStrategyControl.setOnAction(e -> updateCrossover());

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

    private void updateCrossover() {
        CrossoverStrategy strategy = CrossoverStrategy.valueOf(crossoverStrategyControl.getValue().toString());
        Mode mode = Mode.valueOf(modeSelector.getValue());
        if (strategy == CrossoverStrategy.MEAN && mode == Mode.PERMUTATION) {
            crossoverStrategyControl.getSelectionModel().select(CrossoverStrategy.SLICE.ordinal());
        }
        state.setCrossoverStrategy(strategy);
        crossoverControl.setVisible(strategy != CrossoverStrategy.MEAN);
        crossoverLabel.setVisible(strategy != CrossoverStrategy.MEAN);

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
        state.loadNeuralNetwork();
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
            state.loadNeuralNetwork();
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
        modeSelectorLabel.setDisable(disable);
        modeSelector.setDisable(disable);
        hiddenLayerConfiguration.setDisable(disable);
        geneticControls.setDisable(disable);
        startBut.setDisable(disable);
        //skipBut.setDisable(!disable);
        stopBut.setDisable(!disable);

        Platform.runLater(() -> grid.getParent().requestFocus());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        toggleMode();
    }

    private void toggleManaged(Node node, boolean managed) {
        node.setManaged(managed);
        node.setVisible(managed);
    }

}
