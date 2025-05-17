package com.geometricdrawing.decorator;

import com.geometricdrawing.model.Shape;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class ShapeDecorator implements Shape {
    protected final Shape shape;

    public ShapeDecorator(Shape shape) {
        this.shape = shape;
    }

    public Shape getInnerShape() {
        return shape;
    }

    public Shape unwrap() {
        Shape current = this;
        while (current instanceof ShapeDecorator) {
            current = ((ShapeDecorator) current).getInnerShape();
        }
        return current;
    }

    @Override
    public void draw(GraphicsContext graficctx) {
        shape.draw(graficctx);
    }

    @Override public double getX() { return shape.getX(); }
    @Override public double getY() { return shape.getY();}
    @Override public void setX(double x) { shape.setX(x);}
    @Override public void setY(double y) { shape.setY(y);}
    @Override public int getZ() { return shape.getZ(); }
    @Override public void setZ(int z) { shape.setZ(z); }

    @Override public double getWidth()      { return shape.getWidth(); }
    @Override public double getHeight()     { return shape.getHeight(); }
    @Override public Color getFillColor()     { return shape.getFillColor(); }
    @Override public Color getBorderColor()     { return shape.getBorderColor(); }
    @Override public void setFillColor(Color c)     { shape.setFillColor(c); }
    @Override public void setBorderColor(Color c)     { shape.setBorderColor(c); }

    @Override public boolean containsPoint(double x, double y, double threshold) { return false; }
}
