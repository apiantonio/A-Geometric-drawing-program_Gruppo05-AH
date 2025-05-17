package com.geometricdrawing.factory;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Line;

public class LineFactory extends ShapeFactory {
    @Override
    public AbstractShape createShape(double x, double y) {
        return new Line(x, y, x + DEFAULT_LINE_LENGTH, y); // Linea orizzontale di default
    }
}