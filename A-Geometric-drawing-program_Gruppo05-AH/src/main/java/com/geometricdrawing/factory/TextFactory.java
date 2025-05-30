package com.geometricdrawing.factory;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.TextShape;

public class TextFactory extends ShapeFactory {

    @Override
    public AbstractShape createShape(double x, double y) {
        return new TextShape(x, y,DEFAULT_WIDTH, DEFAULT_HEIGHT, "",12);
    }
}