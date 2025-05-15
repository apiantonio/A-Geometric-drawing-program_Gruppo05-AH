package com.geometricdrawing.factory;

import com.geometricdrawing.model.Shape;

public abstract class ShapeFactory {
    // Dimensioni prefissate per US-3
    protected static final double DEFAULT_WIDTH = 60.0;
    protected static final double DEFAULT_HEIGHT = 40.0;
    protected static final double DEFAULT_LINE_LENGTH = 50.0;

    /**
     * Crea una forma alle coordinate specificate con dimensioni/lunghezza di default.
     * I colori di default sono hardcoded nelle classi Shape o AbstractShape.
     */
    public abstract Shape createShape(double x, double y);
}
