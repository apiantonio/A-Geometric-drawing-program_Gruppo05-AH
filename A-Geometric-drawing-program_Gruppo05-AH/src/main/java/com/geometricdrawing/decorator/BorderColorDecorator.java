package com.geometricdrawing.decorator;

import com.geometricdrawing.model.AbstractShape;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.*;

/**
 * La classe decoratore per il colore del bordo implementa Serializable
 * per consentire l'esportazione e importazione.
 */
public class BorderColorDecorator extends ShapeDecorator {
    private transient Color borderColor;
    private double red, green, blue, alpha;   // i campi serializzati saranno RGBA

    public BorderColorDecorator(AbstractShape shape, Color borderColor) {
        super(shape);
        setBorderColorAndUpdateRGBA(borderColor);
    }

    private void setBorderColorAndUpdateRGBA(Color color) {
        this.borderColor = color;
        if (this.borderColor != null) {
            this.red = this.borderColor.getRed();
            this.green = this.borderColor.getGreen();
            this.blue = this.borderColor.getBlue();
            this.alpha = this.borderColor.getOpacity();
        } else {
            // Gestione del caso di un colore nullo, se applicabile
            this.red = 0;
            this.green = 0;
            this.blue = 0;
            this.alpha = 1;
        }
    }

    /**
     * Metodo per modificare il colore del bordo dopo la creazione.
     * Assicura che anche i campi RGBA per la serializzazione siano aggiornati.
     * @param newBorderColor Il nuovo colore del bordo.
     */
    public void setBorderColor(Color newBorderColor) {
        setBorderColorAndUpdateRGBA(newBorderColor);
    }

    public Color getBorderColor() {
        return this.borderColor;
    }

    @Override
    protected void decorateShape(GraphicsContext gc) {
        if (borderColor != null) {
            gc.setStroke(borderColor);
        }
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Ricostruisce il colore transient tenendo conto del fatto di un possibile colore nullo
        if (red == 0 && green == 0 && blue == 0 && alpha == 0 && this.borderColor == null) {
            this.borderColor = Color.BLACK;
        } else {
            this.borderColor = new Color(red, green, blue, alpha);
        }
    }
}
