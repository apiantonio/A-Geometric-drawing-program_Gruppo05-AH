package com.geometricdrawing.model;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import java.io.*;

public abstract class AbstractShape implements Serializable{
    protected double x; // Posizione x sul Canvas (angolo in alto a sx, o startX per linea)
    protected double y; // Posizione y sul Canvas (angolo in alto a sx, o startY per linea)
    protected int z;    // Livello di profondità della figura

    /*
      attributi di tipo intero per scalare la figura (utili per effettuare mirroring).
      di default non c'è mirroring nè orizzontale, nè verticale quindi impostati a 1
    */
    protected int scaleX = 1;
    protected int scaleY = 1;

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
        if (gc == null) {
            drawShape(null);
            System.err.println("GraphicsContext non inizializzato. Impossibile disegnare la figura.");
            return;
        }
        gc.save();

        double centerX = x + width / 2;
        double centerY = y + height / 2;

        gc.translate(centerX, centerY); // l'origine del context adesso è impostato come il centro della figura
        gc.scale(scaleX, scaleY);   // applica mirroring (specchiatura) orizzontale o verticale. DEVE NECESSARIAMENTE ESSERE SOPRA LA ROTAZIONE
        gc.rotate(rotationAngle);   // effettua la rotazione con l'angolo specificato

        drawShape(gc);

        gc.restore();
    }

    public abstract void drawShape(GraphicsContext gc); // ogni forma concreta implementa questo

    /**
     * Metodo che controlla se un punto di coordinate (x,y) è presente nella figura.
     * Sono necessari controlli aggiuntivi per verificare che il punto sia nella figura
     * specialmente se quest'ultima è stata in precedenza ruotata e/o specchiata.
     * @param x coordinate lungo le ascisse
     * @param y coordinate lungo le ordinate
     * @param threshold distanza massima dalla figura per considerare il punto all'interno
     * @return true se il punto è all'interno della figura, false altrimenti
     */
    public boolean containsPoint(double x, double y, double threshold) {
        // Prima applica la trasformazione inversa completa al punto
        Point2D transformedPoint = inverseTransformPoint(x, y);
        double transformedX = transformedPoint.getX();
        double transformedY = transformedPoint.getY();

        return isPointWithinBounds(transformedX, transformedY, threshold);
    }


    /**
     * Applica la trasformazione inversa completa a un punto per riportarlo
     * al sistema di riferimento originale della figura (senza rotazione e mirroring)
     * @implSpec
     * Le coordinate del punto (x, y) sono considerate rispetto al centro della figura,
     * @return un Point2D che rappresenta le coordinate del punto trasformato rispetto al centro della figura originale
     */
    public Point2D inverseTransformPoint(double x, double y) {
        double centerX = this.x + this.width / 2;
        double centerY = this.y + this.height / 2;

        // Trasla il punto rispetto al centro della figura
        double localX = x - centerX;
        double localY = y - centerY;

        double unscaledX = localX / scaleX;
        double unscaledY = localY / scaleY;

        // Then apply inverse rotation
        double angleRad = Math.toRadians(-rotationAngle);
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);

        double unrotatedX = unscaledX * cos - unscaledY * sin;
        double unrotatedY = unscaledX * sin + unscaledY * cos;

        return new Point2D(unrotatedX, unrotatedY);
    }


    /**
     * Verifica se un punto si trova all'interno dei bounds della figura considerando la soglia
     */
    protected boolean isPointWithinBounds(double x, double y, double threshold) {
        // Il punto trasformato è in coordinate centrate nell'origine,
        // quindi controllo rispetto a un rettangolo centrato in (0,0)
        double halfWidth = this.width / 2;
        double halfHeight = this.height / 2;

        return x >= -halfWidth - threshold && x <= halfWidth + threshold &&
                y >= -halfHeight - threshold && y <= halfHeight + threshold;
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

    public int getScaleX() { return scaleX; }

    public void setScaleX(int scaleX) {
        // Se scaleX è negativo, inverte la direzione della figura sull'asse X
        if (scaleX < 0 != this.scaleX < 0) { // Se cambia segno
            this.rotationAngle = normalizeAngle(-this.rotationAngle);
        }
        this.scaleX = scaleX;
    }

    public int getScaleY() { return scaleY;}

    public void setScaleY(int scaleY) {
        // Se scaleY è negativo, inverte la direzione della figura sull'asse Y
        if (scaleY < 0 != this.scaleY < 0) { // Se cambia segno
            this.rotationAngle = normalizeAngle(-this.rotationAngle); // Ruota di 180 gradi
        }
        this.scaleY = scaleY;
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
        this.width = endX - x;
    }

    public double getEndY() {
        return y + height;
    }

    public void setEndY(double endY) {
        this.height = endY - y;
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
        // Calcola il nuovo angolo dopo aver corretto deltaAngle
        double newAngle = this.rotationAngle + deltaAngle;
        newAngle = normalizeAngle(newAngle);

        this.rotationAngle = newAngle;
    }

    private double normalizeAngle(double angle) {
        // Normalizza l'angolo tra -180 e 180 gradi
        while (angle > 180) {
            angle -= 360;
        }
        while (angle < -180) {
            angle += 360;
        }
        return angle;
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