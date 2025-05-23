package com.geometricdrawing.command;

import com.geometricdrawing.decorator.BorderColorDecorator;
import com.geometricdrawing.model.DrawingModel;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeBorderColorCommandTest {

    @Mock
    private DrawingModel mockModel;
    @Mock
    private BorderColorDecorator mockDecorator;

    @Test
    void executeShouldCallSetBorderColorOnModel() {
        Color newColor = Color.RED;
        when(mockDecorator.getBorderColor()).thenReturn(Color.BLUE);
        ChangeBorderColorCommand command = new ChangeBorderColorCommand(mockModel, mockDecorator, newColor);
        command.execute();
        verify(mockModel, times(1)).setBorderColor(mockDecorator, newColor);
    }

    @Test
    void undoShouldRestoreOldColor() {
        Color oldColor = Color.BLUE;
        Color newColor = Color.RED;
        when(mockDecorator.getBorderColor()).thenReturn(oldColor);
        ChangeBorderColorCommand command = new ChangeBorderColorCommand(mockModel, mockDecorator, newColor);
        command.execute();
        command.undo();
        verify(mockModel, times(1)).setBorderColor(mockDecorator, oldColor);
    }
}
