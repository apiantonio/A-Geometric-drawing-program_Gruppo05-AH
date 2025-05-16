package com.geometricdrawing.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class AbstractShape implements Shape {
    protected double x; // Posizione x (es. angolo sup-sx, o startX per linea)
    protected double y; // Posizione y (es. angolo sup-sx, o startY per linea)

    // Dimensioni definite dalle factory per US-3
    protected double width;
    protected double height;

    // Colori di default
    protected Color borderColor = Color.BLACK;
    protected Color fillColor = Color.TRANSPARENT; // Default per linea o forme senza riempimento specificato

    public AbstractShape(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    //Metodo per disegnare la figura
    @Override
    public abstract void draw(GraphicsContext gc);

    // Metodi getter per i colori
    protected Color getBorderColorInternal() { return this.borderColor; }
    protected Color getFillColorInternal() { return this.fillColor; }

    // Metodi getter e setter per x, y, width, height e colori
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }
}