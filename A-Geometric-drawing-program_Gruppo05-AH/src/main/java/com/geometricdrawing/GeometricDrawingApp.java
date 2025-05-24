package com.geometricdrawing;

import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.command.CommandManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @Autore: Gruppo05
 * @Scopo: Classe che estende application e carica il file FXML
 */

public class GeometricDrawingApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GeometricDrawingApp.class.getResource("DrawingView.fxml"));

        AnchorPane root = fxmlLoader.load();

        DrawingController controller = fxmlLoader.getController();


        Scene scene = new Scene(root);
        stage.setTitle("Geometric Drawing App");

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