package com.geometricdrawing.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color; // È utile per definire i colori di default

public abstract class AbstractShape implements Shape {
    protected double x; // Posizione x (es. angolo sup-sx, o startX per linea)
    protected double y; // Posizione y (es. angolo sup-sx, o startY per linea)

    // Dimensioni definite dalle factory per US-3
    protected double width;
    protected double height;

    // Colori di default hardcoded per US-3
    protected Color borderColor = Color.BLACK;
    protected Color fillColor = Color.TRANSPARENT; // Default per linea o forme senza riempimento specificato

    public AbstractShape(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Il metodo draw è l'unico richiesto dall'interfaccia Shape per US-3
    @Override
    public abstract void draw(GraphicsContext gc);

    // Metodi getter per i colori potrebbero essere utili per il disegno,
    // anche se non esposti nell'interfaccia Shape per US-2/3.
    protected Color getBorderColorInternal() { return this.borderColor; }
    protected Color getFillColorInternal() { return this.fillColor; }

    // Metodi come setPosition, move, setDimensions, contains, getBounds
    // saranno aggiunti quando le User Story lo richiederanno.
}