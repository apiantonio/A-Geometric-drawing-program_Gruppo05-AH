package com.geometricdrawing.factory;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Ellipse;

public class EllipseFactory extends ShapeFactory {
    @Override
    public AbstractShape createShape(double x, double y) {
        return new Ellipse(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}