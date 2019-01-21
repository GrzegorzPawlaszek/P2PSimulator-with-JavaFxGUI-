package pl.nanaki.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import pl.nanaki.controllers.FirstWindowController;
import pl.nanaki.controllers.SecondWindowController;

import java.io.IOException;

public class Main extends Application {
    public Stage window;
    private FirstWindowController firstWindowController;

    public static void main(String args[]) {
        launch(args);
    }


    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FirstWindow.fxml"));
        Pane root = loader.load();
        window.setScene(new Scene(root, root.getPrefWidth(), root.getPrefHeight()));

        primaryStage.setTitle("P2P Simulator");
        primaryStage.show();

        firstWindowController = loader.getController();
        firstWindowController.setMain(this);
    }

    public void loadScene(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Pane root = loader.load();
        window.setScene(new Scene(root, root.getPrefWidth(), root.getPrefHeight()));
        SecondWindowController secondWindowController = loader.getController();
        secondWindowController.setFirstWindowController(firstWindowController);
    }


}