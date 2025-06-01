package com.geometricdrawing.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class UserGuide {
    private final Alert helpAlert;
    private static final String TITLE = "Guida Utente";
    private static final String HEADER = "Guida all'utilizzo dell'applicazione";

    public UserGuide() {
        this.helpAlert = new Alert(Alert.AlertType.INFORMATION);
    }

    public void show() {
        helpAlert.setTitle(TITLE);
        helpAlert.setHeaderText(HEADER);

        TextArea textArea = createTextArea();

        // ScrollPane per permettere lo scrolling
        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefViewportWidth(580);
        scrollPane.setPrefViewportHeight(400);

        helpAlert.getDialogPane().setContent(scrollPane);
        helpAlert.getDialogPane().setPrefWidth(600);
        helpAlert.getDialogPane().setPrefHeight(500);

        helpAlert.showAndWait();
    }

    private TextArea createTextArea() {
        // TextArea per contenere il testo della guida
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);

        String guideContent = loadGuideContent();

        textArea.setText(guideContent);
        return textArea;
    }

    private String loadGuideContent() {
        try {
            // Carica il file dal classpath (resources)
            InputStream inputStream = getClass().getResourceAsStream("/user_guide.txt");
            if (inputStream == null) {
                return "Errore: impossibile caricare la guida utente.";
            }

            // Legge tutto il contenuto del file
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            inputStream.close();
            return content;
        } catch (IOException e) {
            e.printStackTrace();
            return "Errore nel caricamento della guida utente: " + e.getMessage();
        }
    }

}
