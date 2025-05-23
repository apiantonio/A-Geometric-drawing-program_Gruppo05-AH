package com.geometricdrawing.decorator;

import com.geometricdrawing.model.AbstractShape;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.*;

/**
 * La classe decoratore per il colore del bordo implementa Serializable
 * per consentire l'esportazione e importazione
 */
public class BorderColorDecorator extends ShapeDecorator {
    private transient Color borderColor;
    private double red, green, blue, alpha;   // i campi serializzati saranno RGBA


    public BorderColorDecorator(AbstractShape shape, Color borderColor) {
        super(shape);
        this.borderColor = borderColor;
        this.red = borderColor.getRed();
        this.green = borderColor.getGreen();
        this.blue = borderColor.getBlue();
        this.alpha = borderColor.getOpacity();
    }

    @Override
    protected void decorateShape(GraphicsContext gc) {
        gc.setStroke(borderColor);
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.borderColor = new Color(red, green, blue, alpha);
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }
}
