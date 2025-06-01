package com.geometricdrawing.model;

import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class TextShape extends AbstractShape {
    private String text;
    private int fontSize;
    private final String fontFamily;

    public TextShape(double x, double y, double width, double height, String text, int fontSize) {
        super(x, y, width, height);
        this.text = (text == null) ? "" : text;
        this.fontSize = fontSize;
        this.fontFamily = "System";
    }

    @Override
    public void drawShape(GraphicsContext gc) {
        // Salva lo stato iniziale del GraphicsContext.
        gc.save();

        Font font = new Font(fontFamily, fontSize);
        gc.setFont(font);
        gc.setTextBaseline(VPos.TOP); // Allinea il testo al margine superiore per i calcoli di y

        // Definisce l'area di clipping. Qualsiasi cosa disegnata fuori da questo rettangolo
        // (definito nello spazio di coordinate locale centrato della forma) non sarà visibile.
        gc.beginPath();
        gc.rect(-this.width / 2, -this.height / 2, this.width, this.height);
        gc.clip();

        if (this.text == null || this.text.trim().isEmpty()) {
            gc.restore(); // Ripristina lo stato del gc se non c'è testo
            return;
        }

        // Misura l'altezza di una riga di testo standard (non scalata)
        Text tempMeasure = new Text("Mg"); // Stringa di esempio per misurare
        tempMeasure.setFont(font);
        double lineHeightOriginal = tempMeasure.getLayoutBounds().getHeight();

        // Esegue il word wrapping del testo per adattarlo alla larghezza del bounding box (this.width).
        // Questo determina quali parole vanno su quali righe.
        List<String> lines = wrapText(this.text, font, this.width);

        // Calcola la larghezza naturale massima tra tutte le righe wrappate.
        double maxNaturalLineWidth = 0;
        if (!lines.isEmpty()) {
            for (String lineContent : lines) {
                if (lineContent != null && !lineContent.isEmpty()) {
                    tempMeasure.setText(lineContent);
                    maxNaturalLineWidth = Math.max(maxNaturalLineWidth, tempMeasure.getLayoutBounds().getWidth());
                }
            }
        }

        // Calcola il fattore di scala X uniforme per l'intero blocco di testo.
        // Serve perché il testo renderizzato (considerando la sua riga più lunga) riempia orizzontalmente this.width.
        double textContentScaleX = 1.0;
        if (this.width > 1e-6 && maxNaturalLineWidth > 1e-6) {
            textContentScaleX = this.width / maxNaturalLineWidth;
        }

        // Calcola l'altezza naturale totale del blocco di testo (numero di righe * altezza riga originale).
        double naturalTextBlockHeight = 0;
        if (!lines.isEmpty()) {
            naturalTextBlockHeight = lines.size() * lineHeightOriginal;
        }

        // Calcola il fattore di scala Y uniforme per l'intero blocco di testo.
        // Serve perché il testo renderizzato riempia verticalmente this.height.
        double textContentScaleY = 1.0;
        if (this.height > 1e-6 && naturalTextBlockHeight > 1e-6) {
            textContentScaleY = this.height / naturalTextBlockHeight;
        }

        // Salva lo stato del GC prima di applicare lo scaling specifico del contenuto testuale.
        gc.save();
        // Applica lo scaling orizzontale e verticale. Tutto ciò che viene disegnato dopo
        // sarà influenzato da questi fattori di scala.
        gc.scale(textContentScaleX, textContentScaleY);

        double xDrawPosition;
        if (Math.abs(textContentScaleX) < 1e-6) { // Evita divisione per zero
            xDrawPosition = -this.width / 2; // Fallback se lo scaleX è (quasi) zero
        } else {
            xDrawPosition = (-this.width / 2) / textContentScaleX;
        }

        // Calcola la posizione Y di disegno per la prima riga, nel sistema *pre-scalato*.
        double yDrawPositionCurrent;
        if (Math.abs(textContentScaleY) < 1e-6) { // Evita divisione per zero
            yDrawPositionCurrent = -this.height / 2; // Fallback se lo scaleY è (quasi) zero
        } else {
            yDrawPositionCurrent = (-this.height / 2) / textContentScaleY;
        }

        // Calcola l'altezza di una riga nel sistema *pre-scalato*.
        double scaledLineHeight;
        if (Math.abs(textContentScaleY) < 1e-6) {
            scaledLineHeight = lineHeightOriginal; // Fallback
        } else {
            scaledLineHeight = lineHeightOriginal / textContentScaleY;
        }

        for (String lineContent : lines) {
            // Controllo opzionale per non disegnare righe che eccedono verticalmente il bounding box.
            // La Y visualizzata della riga corrente è yDrawPositionCurrent * textContentScaleY.
            // L'altezza visualizzata della riga è scaledLineHeight * textContentScaleY = lineHeightOriginal.
            // Se (Y visualizzata + altezza visualizzata riga) > bordo inferiore del BB...
            if ((yDrawPositionCurrent * textContentScaleY) + lineHeightOriginal > this.height / 2 + 1e-2) { // Aggiunta tolleranza
                break; // Interrompi se la prossima riga uscirebbe dal box
            }

            gc.fillText(lineContent, xDrawPosition, yDrawPositionCurrent);
            yDrawPositionCurrent += scaledLineHeight; // Incrementa Y nel sistema di coordinate pre-scalato
        }

        gc.restore(); // Ripristina GC (rimuove lo scaling del contenuto textContentScaleX/Y)
        gc.restore(); // Ripristina GC (salvato all'inizio del metodo, prima del clip)
    }

    // Il metodo wrapText (word wrapping) rimane come precedentemente fornito e corretto.
    private List<String> wrapText(String inputText, Font font, double maxWidth) {
        List<String> lines = new ArrayList<>();
        if (inputText == null || inputText.trim().isEmpty() || maxWidth <= 1e-6) {
            return lines;
        }
        String trimmedText = inputText.trim();
        String[] words = trimmedText.split("\\s+");
        if (words.length == 0 || (words.length == 1 && words[0].isEmpty())) {
            return lines;
        }
        StringBuilder currentLine = new StringBuilder();
        Text tempTextMeasure = new Text();
        tempTextMeasure.setFont(font);
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            tempTextMeasure.setText(word);
            double wordWidth = tempTextMeasure.getLayoutBounds().getWidth();
            if (currentLine.length() == 0) {
                if (wordWidth <= maxWidth) {
                    currentLine.append(word);
                } else {
                    lines.add(word);
                }
            } else {
                tempTextMeasure.setText(currentLine.toString() + " " + word);
                double testLineWidth = tempTextMeasure.getLayoutBounds().getWidth();
                if (testLineWidth <= maxWidth) {
                    currentLine.append(" ").append(word);
                } else {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                    if (wordWidth <= maxWidth) {
                        currentLine.append(word);
                    } else {
                        lines.add(word);
                    }
                }
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        return lines;
    }

    /**
     * Calcola le dimensioni naturali del blocco di testo, considerando il wrapping e le dimensioni minime.
     * @param proposedWidthForWrapping La larghezza proposta per il wrapping del testo.
     * @return Un oggetto Point2D che rappresenta la larghezza e l'altezza naturali del blocco di testo.
     */
    public Point2D getNaturalTextBlockDimensions(double proposedWidthForWrapping) {
        final double absoluteMinDimension = 5.0; // Una dimensione minima assoluta

        if (this.text == null || this.text.trim().isEmpty()) {
            return new Point2D(absoluteMinDimension, absoluteMinDimension);
        }

        Font font = new Font(this.fontFamily, this.fontSize);

        Text tempTextMeasure = new Text("Mg"); // Usato per misurare l'altezza di una riga
        tempTextMeasure.setFont(font);
        double lineHeightOriginal = tempTextMeasure.getLayoutBounds().getHeight();

        // Fallback per l'altezza della riga se il calcolo dà zero o valori troppo piccoli
        if (lineHeightOriginal < 1e-6) {
            lineHeightOriginal = Math.max(1.0, (double)this.fontSize); // Usa la dimensione del font come approssimazione
        }
        if (lineHeightOriginal < 1.0) { // Ulteriore fallback
            lineHeightOriginal = absoluteMinDimension;
        }

        // Usa una larghezza di wrapping sicura (almeno 1.0) per evitare problemi con wrapText
        double safeWrapWidth = Math.max(1.0, proposedWidthForWrapping);
        List<String> lines = this.wrapText(this.text, font, safeWrapWidth); // Chiama il metodo wrapText esistente

        double maxNaturalLineWidth = 0;
        if (!lines.isEmpty()) {
            for (String lineContent : lines) {
                if (lineContent != null && !lineContent.isEmpty()) {
                    tempTextMeasure.setText(lineContent);
                    maxNaturalLineWidth = Math.max(maxNaturalLineWidth, tempTextMeasure.getLayoutBounds().getWidth());
                }
            }
        } else if (this.text != null && !this.text.trim().isEmpty()) {
            // Se wrapText restituisce una lista vuota (es. safeWrapWidth era troppo piccolo o testo solo spazi)
            // Misura il testo originale come una singola riga.
            tempTextMeasure.setText(this.text);
            maxNaturalLineWidth = tempTextMeasure.getLayoutBounds().getWidth();
            if (maxNaturalLineWidth > 0 && lineHeightOriginal > 0) {
                lines.add(this.text); // Consideralo come una linea per il calcolo dell'altezza
            }
        }

        // Assicura una larghezza minima se quella calcolata è troppo piccola ma c'è testo
        if (maxNaturalLineWidth < 1.0 && this.text != null && !this.text.isEmpty()){
            tempTextMeasure.setText("W"); // Carattere di riferimento
            maxNaturalLineWidth = Math.max(maxNaturalLineWidth, tempTextMeasure.getLayoutBounds().getWidth());
            if (maxNaturalLineWidth < 1.0) maxNaturalLineWidth = absoluteMinDimension; // Fallback assoluto
        }

        double naturalTextBlockHeight = lines.isEmpty() ? lineHeightOriginal : lines.size() * lineHeightOriginal;
        if (naturalTextBlockHeight < 1.0 && this.text != null && !this.text.isEmpty()) {
            naturalTextBlockHeight = lineHeightOriginal; // Almeno l'altezza di una riga
        }
        if (naturalTextBlockHeight < 1.0) naturalTextBlockHeight = absoluteMinDimension; // Fallback assoluto


        return new Point2D(Math.max(absoluteMinDimension, maxNaturalLineWidth), Math.max(absoluteMinDimension, naturalTextBlockHeight));
    }

    // Getter e Setter standard
    public void setText(String text) {
        this.text = (text == null) ? "" : text;
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