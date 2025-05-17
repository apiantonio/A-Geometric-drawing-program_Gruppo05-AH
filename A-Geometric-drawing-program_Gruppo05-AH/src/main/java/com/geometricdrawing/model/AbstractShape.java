package com.geometricdrawing.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.*;

public abstract class AbstractShape implements Serializable {
    protected double x; // Posizione x (es. angolo sup-sx, o startX per linea)
    protected double y; // Posizione y (es. angolo sup-sx, o startY per linea)
    protected int z;    // Livello di profondità della figura

    // Dimensioni definite dalle factory per US-3
    protected double width;
    protected double height;

    // Colori di default - marcati come transient per evitare errori di serializzazione
    protected transient Color borderColor = Color.BLACK;
    protected transient Color fillColor = Color.TRANSPARENT; // Default per linea o forme senza riempimento specificato

    // Array per memorizzare i componenti del colore durante la serializzazione
    protected double[] serializableBorderColor;
    protected double[] serializableFillColor;

    public AbstractShape(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        // Inizializza gli array per i colori serializzabili
        updateSerializableColors();
    }

    // Costruttore di default
    protected AbstractShape() {}

    // Metodo per aggiornare gli array serializzabili
    private void updateSerializableColors() {
        if (borderColor != null) {
            this.serializableBorderColor = new double[]{borderColor.getRed(), borderColor.getGreen(),
                    borderColor.getBlue(), borderColor.getOpacity()};
        }
        if (fillColor != null) {
            this.serializableFillColor = new double[]{fillColor.getRed(), fillColor.getGreen(),
                    fillColor.getBlue(), fillColor.getOpacity()};
        }
    }

    // Metodo per disegnare la figura
    public abstract void draw(GraphicsContext gc);

    // Metodi getter per i colori
    public Color getBorderColor() { return this.borderColor; }

    public Color getFillColor() { return this.fillColor; }

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

    // Aggiungi setter per i colori che aggiornano anche gli array serializzabili
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        if (borderColor != null) {
            this.serializableBorderColor = new double[]{borderColor.getRed(), borderColor.getGreen(),
                    borderColor.getBlue(), borderColor.getOpacity()};
        }
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        if (fillColor != null) {
            this.serializableFillColor = new double[]{fillColor.getRed(), fillColor.getGreen(),
                    fillColor.getBlue(), fillColor.getOpacity()};
        }
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        // Assicurati che gli array serializzabili siano aggiornati prima di serializzare
        updateSerializableColors();

        // Serializza i campi non-transient (inclusi gli array dei colori)
        out.defaultWriteObject();
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // Deserializza i campi non-transient (inclusi gli array dei colori)
        in.defaultReadObject();

        // Ricrea gli oggetti Color dagli array
        if (serializableBorderColor != null && serializableBorderColor.length == 4) {
            this.borderColor = new Color(serializableBorderColor[0], serializableBorderColor[1],
                    serializableBorderColor[2], serializableBorderColor[3]);
        } else {
            this.borderColor = Color.BLACK; // Default se qualcosa è andato storto
        }

        if (serializableFillColor != null && serializableFillColor.length == 4) {
            this.fillColor = new Color(serializableFillColor[0], serializableFillColor[1],
                    serializableFillColor[2], serializableFillColor[3]);
        } else {
            this.fillColor = Color.TRANSPARENT; // Default
        }
    }

}
