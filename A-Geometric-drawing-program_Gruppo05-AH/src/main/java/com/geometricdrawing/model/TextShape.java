package com.geometricdrawing.model;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class TextShape extends AbstractShape{
    private String text;
    private int fontSize;
    private String fontFamily;
    public TextShape(double x, double y, double width, double height, String text, int fontSize) {
        super(x, y, width, height);
        this.text = text;
        this.fontSize = fontSize;
        this.fontFamily = "System";
    }
    @Override
    public void drawShape(GraphicsContext gc) {
        gc.save();

        Font font = new Font(fontFamily, fontSize);
        gc.setFont(font);
        gc.setTextBaseline(VPos.TOP);

        gc.beginPath();
        gc.rect(-this.width / 2, -this.height / 2, this.width, this.height);
        gc.clip();

        if (this.text == null || this.text.trim().isEmpty()) {
            gc.restore();
            return;
        }

        Text tempMeasure = new Text("Mg");
        tempMeasure.setFont(font);
        double lineHeight = tempMeasure.getLayoutBounds().getHeight();

        List<String> lines = wrapText(this.text, font, this.width);

        double currentY = -this.height / 2; // Y iniziale (relativo al centro della forma, angolo superiore)

        for (String line : lines) {
            // Controlla se disegnare questa riga supererebbe l'altezza del bounding box
            // Aggiungiamo un piccolo buffer o controlliamo contro il bordo inferiore del testo
            if (currentY + lineHeight > this.height / 2) {
                break; // Smetti di disegnare se il testo esce dal box
            }
            gc.fillText(line, -this.width / 2, currentY);
            currentY += lineHeight;
        }

        gc.restore();
    }

    /**
     * Metodo helper per dividere il testo in righe che si adattano alla larghezza della forma.
     * @param inputText Il testo da wrappare.
     * @param font Il font utilizzato per la misurazione.
     * @param maxWidth La larghezza massima consentita per una riga.
     * @return Una lista di stringhe, dove ogni stringa è una riga di testo.
     */
    private List<String> wrapText(String inputText, Font font, double maxWidth) {
        List<String> lines = new ArrayList<>();
        if (inputText == null || inputText.trim().isEmpty()) {
            return lines; // Niente da wrappare
        }

        String trimmedText = inputText.trim(); // Rimuove spazi iniziali-finali
        String[] words = trimmedText.split("\\s+"); // Divide per uno o più spazi

        if (words.length == 0) {
            return lines;
        }

        StringBuilder currentLine = new StringBuilder();
        Text tempTextMeasure = new Text(); // Nodi Text usati per misurare la larghezza del testo
        tempTextMeasure.setFont(font);

        for (String word : words) {
            if (word.isEmpty()) { // Salta parole vuote
                continue;
            }

            tempTextMeasure.setText(word);
            double wordWidth = tempTextMeasure.getLayoutBounds().getWidth();

            if (currentLine.isEmpty()) { // Se la riga corrente è vuota
                if (wordWidth <= maxWidth) {
                    currentLine.append(word);
                } else {
                    // La parola singola è più lunga della larghezza massima
                    lines.add(word);
                    // currentLine rimane vuoto, la parola è stata "consumata"
                }
            } else { // La riga corrente ha già del contenuto
                tempTextMeasure.setText(currentLine + " " + word);
                double testLineWidth = tempTextMeasure.getLayoutBounds().getWidth();

                if (testLineWidth <= maxWidth) {
                    currentLine.append(" ").append(word);
                } else {
                    // La parola non sta nella riga corrente.
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(); // Inizia una nuova riga

                    // Ora gestisce la parola per la nuova riga
                    if (wordWidth <= maxWidth) {
                        currentLine.append(word);
                    } else {
                        // La parola è troppo lunga anche per la nuova riga.
                        lines.add(word); // Aggiungila come riga a sé
                        // currentLine rimane vuoto, perché la parola lunga è stata "consumata".
                    }
                }
            }
        }

        // Aggiunge l'ultima riga se c'è qualcosa rimasto nel buffer
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }
        return lines;
    }

    public void setText(String text) {
        this.text = text;
    }
    public String getText() {
        return text;
    }
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }
    public int getFontSize() {
        return fontSize;
    }
    public String getFontFamily() {
        return fontFamily;
    }
}