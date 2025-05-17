package com.geometricdrawing.factory;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Rectangle;

public class RectangleFactory extends ShapeFactory {
    @Override
    public AbstractShape createShape(double x, double y) {
        return new Rectangle(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}