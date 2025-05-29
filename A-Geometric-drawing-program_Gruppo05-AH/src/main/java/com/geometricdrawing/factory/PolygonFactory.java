package com.geometricdrawing.factory;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Polygon;

public class PolygonFactory extends ShapeFactory {
    private int maxPoints;

    public PolygonFactory(int maxPoints) {
        this.maxPoints = Math.min(12, Math.max(3, maxPoints)); // Limita tra 3 e 12
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    @Override
    public AbstractShape createShape(double x, double y) {
        return new Polygon(x, y);
    }
}