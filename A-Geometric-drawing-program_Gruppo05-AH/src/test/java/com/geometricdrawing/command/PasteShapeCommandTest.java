package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle; // Esempio di forma concreta
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasteShapeCommandTest {

    @Mock
    private DrawingModel mockModel;
    @Mock
    private ClipboardManager mockClipboardManager;

    private AbstractShape shapeFromClipboard; // Unica variabile per la forma di test

    @BeforeEach
    void setUp() {
        // Creiamo una forma con coordinate iniziali note per i test di posizionamento
        shapeFromClipboard = new Rectangle(50, 60, 100, 80);
    }

    @Test
    void execute_whenClipboardHasContent_withDefaultOffset_shouldAddShapeWithOffset() {
        // Arrange
        when(mockClipboardManager.hasContent()).thenReturn(true);
        when(mockClipboardManager.getFromClipboard()).thenReturn(shapeFromClipboard); // Usa la variabile inizializzata

        PasteShapeCommand command = new PasteShapeCommand(mockModel, mockClipboardManager);

        // Act
        command.execute();

        // Assert
        verify(mockClipboardManager, times(1)).hasContent();
        verify(mockClipboardManager, times(1)).getFromClipboard();

        assertEquals(50 + 10.0, shapeFromClipboard.getX(), "X coordinate should be original X + default offset.");
        assertEquals(60 + 10.0, shapeFromClipboard.getY(), "Y coordinate should be original Y + default offset.");

        ArgumentCaptor<AbstractShape> shapeCaptor = ArgumentCaptor.forClass(AbstractShape.class);
        verify(mockModel, times(1)).addShape(shapeCaptor.capture());
        assertSame(shapeFromClipboard, shapeCaptor.getValue(), "The shape added to the model should be the one from clipboard.");
        assertSame(shapeFromClipboard, command.getPastedShape(), "getPastedShape should return the shape that was pasted.");
    }

    @Test
    void execute_whenClipboardHasContent_withAbsoluteCoordinates_shouldAddShapeAtTarget() {
        // Arrange
        when(mockClipboardManager.hasContent()).thenReturn(true);
        when(mockClipboardManager.getFromClipboard()).thenReturn(shapeFromClipboard); // Usa la variabile inizializzata

        double targetX = 200.0;
        double targetY = 250.0;
        PasteShapeCommand command = new PasteShapeCommand(mockModel, mockClipboardManager, targetX, targetY, true);

        // Act
        command.execute();

        // Assert
        verify(mockClipboardManager, times(1)).hasContent();
        verify(mockClipboardManager, times(1)).getFromClipboard();

        assertEquals(targetX, shapeFromClipboard.getX(), "X coordinate should be targetX.");
        assertEquals(targetY, shapeFromClipboard.getY(), "Y coordinate should be targetY.");

        ArgumentCaptor<AbstractShape> shapeCaptor = ArgumentCaptor.forClass(AbstractShape.class);
        verify(mockModel, times(1)).addShape(shapeCaptor.capture());
        assertSame(shapeFromClipboard, shapeCaptor.getValue());
        assertSame(shapeFromClipboard, command.getPastedShape());
    }

    @Test
    void execute_whenClipboardIsEmpty_shouldNotGetFromClipboardAndNotAddShape() {
        // Arrange
        when(mockClipboardManager.hasContent()).thenReturn(false);

        PasteShapeCommand command = new PasteShapeCommand(mockModel, mockClipboardManager);

        // Act
        command.execute();

        // Assert
        verify(mockClipboardManager, times(1)).hasContent();
        verify(mockClipboardManager, never()).getFromClipboard();
        verify(mockModel, never()).addShape(any(AbstractShape.class));
        assertNull(command.getPastedShape(), "Pasted shape should be null if clipboard was empty.");
    }

    @Test
    void execute_whenClipboardHasContentButGetFromClipboardReturnsNull_shouldNotAddShape() {
        // Arrange
        when(mockClipboardManager.hasContent()).thenReturn(true);
        when(mockClipboardManager.getFromClipboard()).thenReturn(null); // Simula getFromClipboard che restituisce null

        PasteShapeCommand command = new PasteShapeCommand(mockModel, mockClipboardManager);

        // Act
        command.execute();

        // Assert
        verify(mockClipboardManager, times(1)).hasContent();
        verify(mockClipboardManager, times(1)).getFromClipboard();
        verify(mockModel, never()).addShape(any(AbstractShape.class));
        assertNull(command.getPastedShape(), "Pasted shape should be null if getFromClipboard returned null.");
    }

    @Test
    void undo_afterSuccessfulExecute_shouldRemovePastedShapeFromModel() {
        // Arrange
        when(mockClipboardManager.hasContent()).thenReturn(true);
        when(mockClipboardManager.getFromClipboard()).thenReturn(shapeFromClipboard); // CORRETTO: usa la variabile inizializzata

        PasteShapeCommand command = new PasteShapeCommand(mockModel, mockClipboardManager);
        command.execute();

        AbstractShape executedPastedShape = command.getPastedShape();
        assertNotNull(executedPastedShape, "Pasted shape should not be null after execute for undo test."); // Ora dovrebbe passare
        assertSame(shapeFromClipboard, executedPastedShape, "PastedShape in command should be the instance from clipboard for this test setup.");
        verify(mockModel).addShape(executedPastedShape);

        // Act
        command.undo();

        // Assert
        verify(mockModel, times(1)).removeShape(executedPastedShape);
    }

    @Test
    void undo_whenExecuteDidNotPasteShape_shouldNotCallRemoveShape() {
        // Arrange
        when(mockClipboardManager.hasContent()).thenReturn(false);

        PasteShapeCommand command = new PasteShapeCommand(mockModel, mockClipboardManager);
        command.execute();

        assertNull(command.getPastedShape(), "Pasted shape should be null if execute did not paste anything.");

        // Act
        command.undo();

        // Assert
        verify(mockModel, never()).removeShape(any(AbstractShape.class));
    }

    @Test
    void undo_whenPastedShapeIsNullInternally_shouldNotCallRemoveShape() {
        PasteShapeCommand command = new PasteShapeCommand(mockModel, mockClipboardManager);
        assertNull(command.getPastedShape()); // Nessuna esecuzione, pastedShape è null

        // Act
        command.undo();

        // Assert
        verify(mockModel, never()).removeShape(any(AbstractShape.class));
    }
}