package com.geometricdrawing.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Polygon extends AbstractShape {
    private transient ArrayList<Point2D> vertices;

    public Polygon(double x, double y) {
        super(x, y, 0.0, 0.0); // inizializza con larghezza e altezza 0
        this.vertices = new ArrayList<>();
        this.vertices.add(new Point2D(x, y)); // Primo punto
    }

    public void addVertex(double x, double y) {
        vertices.add(new Point2D(x, y));
        updateBounds();
    }

    public void removeVertex(int index) {
        if (index >= 0 && index < vertices.size()) {
            vertices.remove(index);
            updateBounds();
        }
    }

    private void updateBounds() {
        if (vertices.isEmpty()) return;

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (Point2D p : vertices) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
        }

        setX(minX);
        setY(minY);
        setWidth(maxX - minX);
        setHeight(maxY - minY);
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (vertices.size() < 3) {
            return; // il poligono deve avere almeno 3 punti
        }

        double[] xVertices = new double[vertices.size()];
        double[] yVertices = new double[vertices.size()];

        for (int i = 0; i < vertices.size(); i++) {
            xVertices[i] = vertices.get(i).getX();
            yVertices[i] = vertices.get(i).getY();
        }

        gc.fillPolygon(xVertices, yVertices, vertices.size());
        gc.strokePolygon(xVertices, yVertices, vertices.size());
    }

    @Override
    public boolean containsPoint(double x, double y, double tolerance) {
        // Aumenta il bounding box per la selezione
        double expandedX = this.x - tolerance;
        double expandedY = this.y - tolerance;
        double expandedWidth = this.width + (2 * tolerance);
        double expandedHeight = this.height + (2 * tolerance);

        // Verifica prima il bounding box espanso
        if (x >= expandedX && x <= expandedX + expandedWidth &&
                y >= expandedY && y <= expandedY + expandedHeight) {
            return isPointInPolygon(x, y);
        }
        return false;
    }

    /**
     * Verifica se un punto (x, y) è all'interno del poligono definito dai punti utilizzando l'algoritmo di ray-casting.
     */
    private boolean isPointInPolygon(double x, double y) {
        boolean inside = false;
        int j = vertices.size() - 1; // indice del punto precedente

        // esamina ogni coppia di vertici consecutivi del poligono e verifica se una linea
        // orizzontale che passa per il punto (x,y) interseca il lato del poligono.
        // Ad ogni intersezione valida, lo stato "dentro/fuori" viene invertito.
        for (int i = 0; i < vertices.size(); i++) {
            double xi = vertices.get(i).getX();
            double yi = vertices.get(i).getY();
            double xj = vertices.get(j).getX();
            double yj = vertices.get(j).getY();

            boolean intersects = (yi > y) != (yj > y); // il segmento attraversa la riga orizzontale a y

            if (intersects) {
                // calcola la coordinata x del punto di intersezione tra il lato e la riga orizzontale
                double xIntersection = (xj - xi) * (y - yi) / (yj - yi) + xi;

                if (x < xIntersection) {
                    inside = !inside;
                }
            }

            j = i; // aggiorna il punto precedente
        }

        // se il numero di intersezioni è dispari il punto è all'interno
        // se il numero di intersezioni è pari il punto è all'esterno
        return inside;
    }

    public void clearVertices() {
        vertices.clear();
        setX(0);
        setY(0);
        setWidth(0);
        setHeight(0);
    }

    @Override
    public void moveBy(double deltaX, double deltaY) {
        for (int i = 0; i < vertices.size(); i++) {
            Point2D vertex = vertices.get(i);
            vertices.set(i, new Point2D(vertex.getX() + deltaX, vertex.getY() + deltaY));
        }
        updateBounds();
    }

    public List<Point2D> getVertices() {
        return vertices;
    }

    public void setVertices(ArrayList<Point2D> vertices) {
        this.vertices = vertices;
        updateBounds();
    }

    /* Metodi per la serializzazione necessari perché Point2D non è Serializable */

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        // vertices come coordinate double che sono serializzabili
        out.writeInt(vertices.size());
        for (Point2D point : vertices) {
            out.writeDouble(point.getX());
            out.writeDouble(point.getY());
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        int size = in.readInt();
        vertices = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            double x = in.readDouble();
            double y = in.readDouble();
            vertices.add(new Point2D(x, y));
        }
    }

}