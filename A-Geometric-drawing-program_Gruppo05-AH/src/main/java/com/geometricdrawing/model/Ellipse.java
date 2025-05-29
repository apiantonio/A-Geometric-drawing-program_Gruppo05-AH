package com.geometricdrawing.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


public class Ellipse extends AbstractShape {

    public Ellipse(double x, double y, double width, double height) {
        super(x, y, width, height); // x,y sono angolo sup-sx del bbox, width/height sono i diametri
    }

    // il metodo procede alla creazione dell'ellisse con i colori di riempimento e bordo settati
    @Override
    public void drawShape(GraphicsContext gc) {
        // Disegna l'ellisse il cui vertice in alto a sinistra coincide con l'origine (0, 0) nuova del gc sottraendo metà dimensioni
        gc.fillOval(-width / 2, -height / 2, width, height);
        gc.strokeOval(-width / 2, -height / 2, width, height);
    }
}