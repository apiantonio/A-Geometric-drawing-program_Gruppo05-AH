package com.geometricdrawing.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Rectangle extends AbstractShape {

    public Rectangle(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    // il metodo procede alla creazione del rettangolo con i colori di riempimento e bordo settati
    @Override
    public void drawShape(GraphicsContext gc) {
        // Disegna il rettangolo il cui vertice in alto a sinistra coincide con l'origine (0, 0) nuova del gc sottraendo metà dimensioni
        gc.fillRect(-width / 2, -height / 2, width, height);
        gc.strokeRect(-width / 2, -height / 2, width, height);
    }

}