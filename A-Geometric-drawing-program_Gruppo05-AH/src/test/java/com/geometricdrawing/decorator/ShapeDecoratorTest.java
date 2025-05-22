package com.geometricdrawing.decorator;

import com.geometricdrawing.command.ChangeHeightCommand;
import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle;
import javafx.scene.canvas.GraphicsContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ShapeDecoratorTest {

    @Mock
    private DrawingModel mockDrawingModel;

    // Sottoclasse fittizia per testare ShapeDecorator
    static class DummyDecorator extends ShapeDecorator {
        boolean decorateCalled = false;
        public DummyDecorator(AbstractShape shape) { super(shape); }
        @Override
        protected void decorateShape(GraphicsContext gc) { decorateCalled = true; }
    }

    @Test
    void drawShouldCallDecorateAndInnerDraw() {
        AbstractShape mockShape = mock(AbstractShape.class);
        GraphicsContext mockGc = mock(GraphicsContext.class);
        DummyDecorator decorator = new DummyDecorator(mockShape);

        decorator.draw(mockGc);

        assert(decorator.decorateCalled);
        verify(mockShape, times(1)).draw(mockGc);
        verify(mockGc, times(1)).save();
        verify(mockGc, times(1)).restore();
    }

    @Test
    void methodsShouldDelegateToInnerShape() {
        AbstractShape mockShape = mock(AbstractShape.class);
        DummyDecorator decorator = new DummyDecorator(mockShape);

        decorator.setX(5);
        verify(mockShape).setX(5);

        decorator.getWidth();
        verify(mockShape).getWidth();

        decorator.moveTo(1, 2);
        verify(mockShape).moveTo(1, 2);
    }
}
