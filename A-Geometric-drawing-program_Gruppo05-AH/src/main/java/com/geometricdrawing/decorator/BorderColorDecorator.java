package com.geometricdrawing.decorator;

import com.geometricdrawing.model.Shape;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// 4) Decorator per il bordo
public class BorderColorDecorator extends ShapeDecorator {
    private final Color borderColor;

    public BorderColorDecorator(Shape shape, Color borderColor) {
        super(shape);
        this.borderColor = borderColor;
    }

    @Override
    public void draw(GraphicsContext graficctx) {
        shape.setBorderColor(borderColor);
        super.draw(graficctx);
    }
}
