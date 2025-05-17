package com.geometricdrawing.decorator;

import com.geometricdrawing.model.AbstractShape;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class ShapeDecorator extends AbstractShape {
    protected final AbstractShape decoratedShape;

    public ShapeDecorator(AbstractShape shape) {
        this.decoratedShape = shape;
    }

    public AbstractShape getInnerShape() {
        return decoratedShape;
    }

//    public Shape unwrap() {
//        Shape current = this;
//        while (current instanceof ShapeDecorator) {
//            current = ((ShapeDecorator) current).getInnerShape();
//        }
//        return current;
//    }

    @Override
    public final void draw(GraphicsContext gc) {
        gc.save();
        // il metodo successivo è quello usato per settare colore di riempimento e di bordo
        decorateShape(gc);
        decoratedShape.draw(gc);            //redraw della forma addobbata
        gc.restore();
    }

    protected abstract void decorateShape(GraphicsContext gc);


    @Override public double getX()          { return decoratedShape.getX(); }
    @Override public double getY()          { return decoratedShape.getY(); }
    @Override public void setX(double x)    {   decoratedShape.setX(x); }
    @Override public void setY(double y)    { decoratedShape.setY(y); }
    @Override public int getZ()             { return decoratedShape.getZ(); }
    @Override public void setZ(int z)       { decoratedShape.setZ(z); }

    @Override public double getWidth()              { return decoratedShape.getWidth(); }
    @Override public double getHeight()             { return decoratedShape.getHeight(); }
    @Override public Color getFillColor()           { return decoratedShape.getFillColor(); }
    @Override public Color getBorderColor()         { return decoratedShape.getBorderColor(); }
    @Override public void setFillColor(Color c)     { decoratedShape.setFillColor(c); }
    @Override public void setBorderColor(Color c)   { decoratedShape.setBorderColor(c); }
    @Override public void setWidth(double width)    { decoratedShape.setWidth(width); }
    @Override public void setHeight(double height)  { decoratedShape.setHeight(height); }

    @Override public boolean containsPoint(double x, double y, double threshold) {
        return decoratedShape.containsPoint(x, y, threshold);
    }

}
