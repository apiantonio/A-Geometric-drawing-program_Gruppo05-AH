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

    @Override
    public final void drawShape(GraphicsContext gc) {
        //gc.save();
        // il metodo successivo è quello usato per settare colore di riempimento e di bordo
        decorateShape(gc);
        decoratedShape.draw(gc);            //redraw della forma addobbata
        //gc.restore();
    }

    protected abstract void decorateShape(GraphicsContext gc);

    @Override
    public boolean containsPoint(double x, double y, double threshold) {
        return decoratedShape.containsPoint(x, y, threshold);
    }

    @Override
    public void rotateBy(double deltaAngle) {
        decoratedShape.rotateBy(deltaAngle);
    }


    @Override
    public void moveTo(double newX, double newY) {
        decoratedShape.moveTo(newX, newY);
    }

    @Override
    public void moveBy(double deltaX, double deltaY) {
        decoratedShape.moveBy(deltaX, deltaY);
    }

    @Override public double getX()          { return decoratedShape.getX(); }
    @Override public double getY()          { return decoratedShape.getY(); }
    @Override public int getZ()             { return decoratedShape.getZ(); }
    @Override public double getEndX()       { return decoratedShape.getEndX(); }
    @Override public double getEndY()       { return decoratedShape.getEndY(); }
    @Override public double getWidth()      { return decoratedShape.getWidth(); }
    @Override public double getHeight()     { return decoratedShape.getHeight(); }
    @Override public double getRotationAngle() { return decoratedShape.getRotationAngle();}

    @Override public void setX(double x)           { decoratedShape.setX(x); }
    @Override public void setY(double y)           { decoratedShape.setY(y); }
    @Override public void setZ(int z)              { decoratedShape.setZ(z); }
    @Override public void setEndX(double x)        { decoratedShape.setEndX(x); }
    @Override public void setEndY(double y)        { decoratedShape.setEndY(y); }
    @Override public void setWidth(double width)   { decoratedShape.setWidth(width); }
    @Override public void setHeight(double height) { decoratedShape.setHeight(height); }
    @Override public void setRotationAngle(double angle) { decoratedShape.setRotationAngle(angle);}
}
