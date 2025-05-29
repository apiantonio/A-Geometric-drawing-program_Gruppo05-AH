package com.geometricdrawing.model;

import javafx.scene.canvas.GraphicsContext;
import java.io.*;

public abstract class AbstractShape implements Serializable{
    protected double x; // Posizione x (es. angolo sup-sx, o startX per linea)
    protected double y; // Posizione y (es. angolo sup-sx, o startY per linea)
    protected int z;    // Livello di profondità della figura

    protected double rotationAngle = 0.0; // Angolo di rotazione della figura espresso in gradi

    // Dimensioni definite dalle factory per US-3
    protected double width;
    protected double height;

    public AbstractShape(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Costruttore di default
    protected AbstractShape() {}

    public void draw(GraphicsContext gc) {
        gc.save();

        double centerX = x + width / 2;
        double centerY = y + height / 2;

        gc.translate(centerX, centerY); // l'origine del context adesso è impostato come il centro della figura
        gc.rotate(rotationAngle);

        drawShape(gc);

        gc.restore();
    }

    public abstract void drawShape(GraphicsContext gc); // ogni forma concreta implementa questo

    public boolean containsPoint(double x, double y, double threshold) {
        return x >= this.x - threshold && x <= this.x + this.width + threshold &&
               y >= this.y - threshold && y <= this.y + this.height + threshold;
    }

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

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    // sposta la figura a una nuova posizione (newX, newY)
    public void moveTo(double newX, double newY) {
        double deltaX = newX - this.x;
        double deltaY = newY - this.y;
        moveBy(deltaX, deltaY);
    }

    // sposta la figura di un delta
    public void moveBy(double deltaX, double deltaY) {
        this.x += deltaX;
        this.y += deltaY;
    }

    public double getEndX() {
        return x + width;
    }

    public void setEndX(double endX) {
        this.width = this.x + endX;
        this.width -= this.x;
    }

    public double getEndY() {
        return y - height;
    }

    public void setEndY(double endY) {
        this.height = this.y - endY;
        this.height -= this.y;
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(double angle) {
        this.rotationAngle = angle;
    }

    public void rotateBy(double deltaAngle) {
        this.rotationAngle += deltaAngle;
    }

    //Utilizzato per copiare la figura nella clipboard
    public AbstractShape deepClone() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            AbstractShape clonedShape = (AbstractShape) ois.readObject();
            ois.close();
            return clonedShape;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore durante il deep cloning della figura: " + e.getMessage());
            e.printStackTrace();
            return null; // o lanciare una RuntimeException
        }
    }
}
