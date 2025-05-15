package com.geometricdrawing.factory;

import com.geometricdrawing.model.Shape;
import com.geometricdrawing.model.Ellipse;

public class EllipseFactory extends ShapeFactory {
    @Override
    public Shape createShape(double x, double y) {
        return new Ellipse(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}