package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle; // Usa una forma concreta
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ChangeHeightCommandTest {

    @Mock
    private DrawingModel mockDrawingModel;

    @Test
    void executeShouldCallSetShapeHeightOnModel() {
        AbstractShape shapeToResize = new Rectangle(10, 10, 50, 50);
        double newHeight = 60.0;
        ChangeHeightCommand command = new ChangeHeightCommand(mockDrawingModel, shapeToResize, newHeight);

        command.execute();

        verify(mockDrawingModel, times(1)).setShapeHeight(shapeToResize, newHeight);
    }
}