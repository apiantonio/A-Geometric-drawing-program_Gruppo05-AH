package com.geometricdrawing.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Line extends AbstractShape {
    // Per una linea, width e height di AbstractShape potrebbero rappresentare (endX-startX) e (endY-startY).
    // Manteniamo endX ed endY per chiarezza.
    private double endX;
    private double endY;

    public Line(double startX, double startY, double endX, double endY) {
        // x,y di AbstractShape saranno startX, startY
        super(startX, startY, endX - startX, endY - startY);
        this.endX = endX;
        this.endY = endY;
        super.fillColor = Color.TRANSPARENT; // Le linee non hanno riempimento
        // borderColor è già BLACK di default da AbstractShape
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.strokeLine(this.x, this.y, this.endX, this.endY);
    }

    public double getLength() {
        double deltaX = this.endX - this.x; // this.x è startX
        double deltaY = this.endY - this.y; // this.y è startY
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    @Override
    public void moveBy(double deltaX, double deltaY) {
        // Sposta entrambi i punti mantenendo la forma della linea
        super.moveBy(deltaX, deltaY);
        this.width += deltaX;  // width rappresenta endX - x
        this.height += deltaY; // height rappresenta endY - y
        this.endX += deltaX;
        this.endY += deltaY;
    }

    public double getEndX() {
        return endX;
    }

    public void setEndX(double endX) {
        this.endX = endX;
    }

    public double getEndY() {
        return endY;
    }

    public void setEndY(double endY) {
        this.endY = endY;
    }
}