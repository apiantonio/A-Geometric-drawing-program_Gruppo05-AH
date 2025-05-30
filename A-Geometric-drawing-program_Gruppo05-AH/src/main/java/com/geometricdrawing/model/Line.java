package com.geometricdrawing.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Line extends AbstractShape {

    public Line(double startX, double startY, double endX, double endY) {
        // x,y di AbstractShape saranno startX, startY
        super(startX, startY, endX - startX, endY - startY);
    }

    @Override
    public void drawShape(GraphicsContext gc) {
        // Calcola il centro della linea
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        
        double startXRel = x - centerX;
        double startYRel = y - centerY;
        double endXRel = getEndX() - centerX;
        double endYRel = getEndY() - centerY;

        gc.strokeLine(startXRel, startYRel, endXRel, endYRel);
    }

    public double getLength() {
        return Math.sqrt(width * width + height * height);
    }

    @Override
    public void moveBy(double deltaX, double deltaY) {
        // Sposta entrambi i punti mantenendo la forma della linea
        super.moveBy(deltaX, deltaY);
    }

    @Override
    public double getEndX() {
        return x + width;
    }

    @Override
    public void setEndX(double endX) {
        this.width = endX - x;
    }

    @Override
    public double getEndY() {
        return y + height;
    }

    @Override
    public void setEndY(double endY) {
        this.height = endY - y;
    }
}