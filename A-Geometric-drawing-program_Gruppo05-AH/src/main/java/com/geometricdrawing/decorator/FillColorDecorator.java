package com.geometricdrawing.decorator;

import com.geometricdrawing.model.Shape;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// 3) Decorator per il riempimento
public class FillColorDecorator extends ShapeDecorator {
    private final Color fillColor;

    public FillColorDecorator(Shape shape, Color fillColor) {
        super(shape);
        this.fillColor = fillColor;
    }

    @Override
    protected void decorateShape(GraphicsContext gc) {
        gc.setFill(fillColor);
    }
}
