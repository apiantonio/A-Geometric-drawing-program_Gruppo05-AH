package com.geometricdrawing.decorator;

import com.geometricdrawing.model.AbstractShape;
import javafx.scene.canvas.GraphicsContext;
import org.junit.jupiter.api.Test;

/**
 * Test per ShapeDecorator senza utilizzare Mockito
 */
class ShapeDecoratorTest {

    // Implementazione di supporto per test che registra le chiamate ai metodi
    private static class TestableShape extends AbstractShape {
        private boolean drawCalled = false;
        private double lastX = 0;
        private boolean getWidthCalled = false;
        private double lastWidth = 0;
        private double lastMoveX = 0;
        private double lastMoveY = 0;

        @Override
        public void drawShape(GraphicsContext gc) {
            drawCalled = true;
        }
        @Override
        public void setX(double x) {
            this.lastX = x;
            super.setX(x);
        }

        @Override
        public double getWidth() {
            getWidthCalled = true;
            lastWidth = super.getWidth();
            return lastWidth;
        }

        @Override
        public void moveTo(double newX, double newY) {
            this.lastMoveX = newX;
            this.lastMoveY = newY;
            super.moveTo(newX, newY);
        }

    }

    private static class TestableDecorator extends ShapeDecorator {
        private boolean decorateCalled = false;
        
        public TestableDecorator(AbstractShape shape) { 
            super(shape); 
        }
        
        @Override
        protected void decorateShape(GraphicsContext gc) { 
            decorateCalled = true; 
        }

        public boolean wasDecorateCalled() {
            return decorateCalled;
        }
    }

    @Test
    void drawShouldCallDecorateAndInnerDraw() {
        TestableShape innerShape = new TestableShape();
        TestableDecorator decorator = new TestableDecorator(innerShape);

        decorator.drawShape(null);  // Passiamo null perché nel test non usiamo realmente il GraphicsContext

        assert(decorator.wasDecorateCalled());  // verifica che decorate sia stato chiamato
        assert(innerShape.drawCalled);  // verifica che draw dell'inner shape sia stato chiamato
    }

    @Test
    void methodsShouldDelegateToInnerShape() {
        TestableShape innerShape = new TestableShape();
        TestableDecorator decorator = new TestableDecorator(innerShape);

        decorator.setX(5);
        assert(innerShape.lastX == 5);

        decorator.getWidth();
        assert(innerShape.lastWidth == innerShape.getWidth());

        decorator.moveTo(1, 2);
        assert(innerShape.lastMoveX == 1);
        assert(innerShape.lastMoveY == 2);
    }
}
