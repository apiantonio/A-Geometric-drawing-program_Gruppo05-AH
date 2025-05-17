package com.geometricdrawing.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


public class Ellipse extends AbstractShape {

    public Ellipse(double x, double y, double width, double height) {
        super(x, y, width, height); // x,y sono angolo sup-sx del bbox, width/height sono i diametri
    }

    // il metodo procede alla creazione dell'ellisse con i colori di riempimento e bordo settati
    @Override
    public void draw(GraphicsContext gc) {
        gc.fillOval(x, y, width, height);
        gc.strokeOval(x, y, width, height);
    }
}