package main;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            createApplicationWindow(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        Platform.exit();
        System.exit(0);
    }

    private void createApplicationWindow(Stage stage) throws IOException {
        ClassLoader classLoader = Main.class.getClassLoader();
        FXMLLoader loader = new FXMLLoader(classLoader.getResource("Application.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1600, 800);
        scene.getStylesheets().add(classLoader.getResource("application.css").toExternalForm());
        scene.getStylesheets().add(classLoader.getResource("darkTheme.css").toExternalForm());
        stage.setScene(scene);
        stage.setMinHeight(839);
        stage.setMinWidth(1616);
        stage.setMaxHeight(839);
        stage.setMaxWidth(1616);
        stage.setTitle("TSP Walker | FFHS Bern 2022");
        //stage.getIcons().add(new Image("tsp.png"));
        ((ApplicationController) loader.getController()).setStage(stage);
        stage.show();
    }
}


