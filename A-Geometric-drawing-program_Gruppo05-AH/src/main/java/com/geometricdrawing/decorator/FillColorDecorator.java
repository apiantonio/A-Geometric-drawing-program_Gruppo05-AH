package com.geometricdrawing.decorator;

import com.geometricdrawing.model.AbstractShape;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.*;

/**
 * La classe decoratore per il colore del riempimento implementa Serializable
 * per consentire l'esportazione e importazione.
 */
public class FillColorDecorator extends ShapeDecorator {
    private transient Color fillColor;
    private double red, green, blue, alpha;   // i campi serializzati saranno RGBA

    public FillColorDecorator(AbstractShape shape, Color fillColor) {
        super(shape);
        // Imposta il colore e i componenti RGBA
        setFillColorAndUpdateRGBA(fillColor);
    }

    private void setFillColorAndUpdateRGBA(Color color) {
        this.fillColor = color;
        if (this.fillColor != null) {
            this.red = this.fillColor.getRed();
            this.green = this.fillColor.getGreen();
            this.blue = this.fillColor.getBlue();
            this.alpha = this.fillColor.getOpacity();
        } else {
            // Gestione del caso in cui il colore sia nullo
            this.red = 0;
            this.green = 0;
            this.blue = 0;
            this.alpha = 0;
        }
    }

    /**
     * Metodo per modificare il colore di riempimento dopo la creazione.
     * Assicura che anche i campi RGBA per la serializzazione siano aggiornati.
     * @param newFillColor Il nuovo colore di riempimento.
     */
    public void setFillColor(Color newFillColor) {
        setFillColorAndUpdateRGBA(newFillColor);
    }

    public Color getFillColor() {
        return this.fillColor;
    }

    @Override
    protected void decorateShape(GraphicsContext gc) {
        if (fillColor != null) { // Controlla che fillColor non sia null prima di usarlo
            gc.setFill(fillColor);
        }
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject(); // Serializza red, green, blue, alpha
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); // Deserializza red, green, blue, alpha
        // Ricostruisce il colore transient tenendo conto del fatto di un possibile colore nullo
        if (red == 0 && green == 0 && blue == 0 && alpha == 0 && this.fillColor == null) {
            this.fillColor = new Color(0,0,0,0); // Colore di default
        } else {
            this.fillColor = new Color(red, green, blue, alpha);
        }
    }
}