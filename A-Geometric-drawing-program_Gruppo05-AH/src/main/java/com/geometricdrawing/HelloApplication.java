package com.geometricdrawing;

import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.command.CommandManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));

        AnchorPane root = fxmlLoader.load();

        DrawingController controller = fxmlLoader.getController();
        DrawingModel model = new DrawingModel();
        CommandManager commandManager = new CommandManager();

        controller.setModel(model);
        controller.setCommandManager(commandManager);

        Scene scene = new Scene(root);
        stage.setTitle("Hello!");

        // setting di un minimo rimpicciolimento dello stage in altezza e larghezza
        stage.setMinWidth(500);
        stage.setMinHeight(600);


        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}