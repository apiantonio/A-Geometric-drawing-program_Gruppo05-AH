package com.geometricdrawing.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Rectangle extends AbstractShape {

    public Rectangle(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    // il metodo procede alla creazione del rettangolo con i colori di riempimento e bordo settati
    @Override
    protected void drawShape(GraphicsContext gc) {
        gc.fillRect(x, y, width, height);
        gc.strokeRect(x, y, width, height);
    }

}