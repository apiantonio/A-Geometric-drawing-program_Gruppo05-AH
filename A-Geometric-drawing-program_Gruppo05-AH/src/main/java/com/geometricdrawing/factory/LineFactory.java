package com.geometricdrawing.factory;

import com.geometricdrawing.model.Shape;
import com.geometricdrawing.model.Line;

public class LineFactory extends ShapeFactory {
    @Override
    public Shape createShape(double x, double y) {
        return new Line(x, y, x + DEFAULT_LINE_LENGTH, y); // Linea orizzontale di default
    }
}
