package com.geometricdrawing.command;

import com.geometricdrawing.decorator.FillColorDecorator;
import com.geometricdrawing.model.DrawingModel;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeFillColorCommandTest {

    @Mock
    private DrawingModel mockModel;
    @Mock
    private FillColorDecorator mockDecorator;

    @Test
    void executeShouldCallSetFillColorOnModel() {
        Color newColor = Color.GREEN;
        when(mockDecorator.getFillColor()).thenReturn(Color.YELLOW);
        ChangeFillColorCommand command = new ChangeFillColorCommand(mockModel, mockDecorator, newColor);
        command.execute();
        verify(mockModel, times(1)).setFillColor(mockDecorator, newColor);
    }

    @Test
    void undoShouldRestoreOldColor() {
        Color oldColor = Color.YELLOW;
        Color newColor = Color.GREEN;
        when(mockDecorator.getFillColor()).thenReturn(oldColor);
        ChangeFillColorCommand command = new ChangeFillColorCommand(mockModel, mockDecorator, newColor);
        command.execute();
        command.undo();
        verify(mockModel, times(1)).setFillColor(mockDecorator, oldColor);
    }
}

