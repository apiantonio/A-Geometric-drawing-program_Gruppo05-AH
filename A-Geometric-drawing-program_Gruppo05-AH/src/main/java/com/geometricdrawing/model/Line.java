package com.geometricdrawing.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Line extends AbstractShape {
    // Per una linea, width e height di AbstractShape potrebbero rappresentare (endX-startX) e (endY-startY).
    // Manteniamo endX ed endY per chiarezza.
    private double actualEndX;
    private double actualEndY;

    public Line(double startX, double startY, double endX, double endY) {
        // x,y di AbstractShape saranno startX, startY
        super(startX, startY, endX - startX, endY - startY);
        this.actualEndX = endX;
        this.actualEndY = endY;
        super.fillColor = Color.TRANSPARENT; // Le linee non hanno riempimento
        // borderColor è già BLACK di default da AbstractShape
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(getBorderColorInternal());
        // this.x e this.y sono startX e startY da AbstractShape
        gc.strokeLine(this.x, this.y, this.actualEndX, this.actualEndY);
    }
    // contains(), getBounds(), move(), setPosition() (oltre costruttore) verranno aggiunti in US successive.
}