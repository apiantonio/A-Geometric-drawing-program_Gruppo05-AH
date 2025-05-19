package com.geometricdrawing.decorator;

import com.geometricdrawing.model.AbstractShape;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/*
    La classe decoratore per il colore del riempimento implementa Serializable
    per consentire l'esportazione e importazione
 */
public class FillColorDecorator extends ShapeDecorator {
    private transient Color fillColor;
    private double red, green, blue, alpha;   // i campi serializzati saranno RGBA

    public FillColorDecorator(AbstractShape shape, Color fillColor) {
        super(shape);
        this.fillColor = fillColor;
        this.red = fillColor.getRed();
        this.green = fillColor.getGreen();
        this.blue = fillColor.getBlue();
        this.alpha = fillColor.getOpacity();
    }

    @Override
    protected void decorateShape(GraphicsContext gc) {
        gc.setFill(fillColor);
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.fillColor = new Color(red, green, blue, alpha);
    }

}
