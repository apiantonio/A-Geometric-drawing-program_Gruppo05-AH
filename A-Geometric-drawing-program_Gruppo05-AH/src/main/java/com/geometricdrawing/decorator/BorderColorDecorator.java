package com.geometricdrawing.decorator;

import com.geometricdrawing.model.AbstractShape;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// 4) Decorator per il bordo
public class BorderColorDecorator extends ShapeDecorator {
    private final Color borderColor;

    public BorderColorDecorator(AbstractShape shape, Color borderColor) {
        super(shape);
        this.borderColor = borderColor;
    }

    @Override
    protected void decorateShape(GraphicsContext gc) {
        gc.setStroke(borderColor);
    }

}
