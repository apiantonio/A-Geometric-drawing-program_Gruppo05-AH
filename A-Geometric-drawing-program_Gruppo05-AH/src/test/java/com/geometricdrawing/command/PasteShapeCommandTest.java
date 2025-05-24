package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle; // Esempio di forma concreta
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

    @Captor
    private ArgumentCaptor<AbstractShape> shapeCaptorForAdd;
    @Captor
    private ArgumentCaptor<AbstractShape> shapeCaptorForMove;
    @Captor
    private ArgumentCaptor<Double> xCaptorForMove;
    @Captor
    private ArgumentCaptor<Double> yCaptorForMove;


    private AbstractShape originalShapeInClipboardSetup; // Rappresenta la forma originale che sarebbe nella clipboard
    private AbstractShape clonedShapeFromClipboard;      // Rappresenta il clone che getFromClipboard() restituirebbe

    @BeforeEach
    void setUp() {
        // Questa è la forma "originale" che immaginiamo sia stata copiata precedentemente.
        originalShapeInClipboardSetup = new Rectangle(50, 60, 100, 80);
        // Quando mockClipboardManager.getFromClipboard() viene chiamato, restituirà questo clone.
        clonedShapeFromClipboard = originalShapeInClipboardSetup.deepClone();
    }

    @Test
    void execute_whenClipboardHasContent_withDefaultOffset_shouldMoveAndAddShape() {
        // Arrange
        when(mockClipboardManager.hasContent()).thenReturn(true);
        // getFromClipboard() restituisce un clone
        when(mockClipboardManager.getFromClipboard()).thenReturn(clonedShapeFromClipboard);

        PasteShapeCommand command = new PasteShapeCommand(mockModel, mockClipboardManager);

        // Act
        command.execute();

        // Assert
        verify(mockClipboardManager, times(1)).hasContent();
        verify(mockClipboardManager, times(1)).getFromClipboard();

        // Verifica che moveShapeTo sia chiamato sul modello con il clone e le coordinate corrette
        // Le coordinate originali del clone sono (50, 60)
        double expectedX = clonedShapeFromClipboard.getX() + 10.0; // 50.0 + 10.0 = 60.0
        double expectedY = clonedShapeFromClipboard.getY() + 10.0; // 60.0 + 10.0 = 70.0

        verify(mockModel, times(1)).moveShapeTo(shapeCaptorForMove.capture(), xCaptorForMove.capture(), yCaptorForMove.capture());
        assertSame(clonedShapeFromClipboard, shapeCaptorForMove.getValue(), "moveShapeTo should be called with the cloned shape.");
        assertEquals(expectedX, xCaptorForMove.getValue(), "X coordinate for moveShapeTo (default offset) is incorrect.");
        assertEquals(expectedY, yCaptorForMove.getValue(), "Y coordinate for moveShapeTo (default offset) is incorrect.");

        // Verifica che addShape sia chiamato sul modello con il clone
        verify(mockModel, times(1)).addShape(shapeCaptorForAdd.capture());
        assertSame(clonedShapeFromClipboard, shapeCaptorForAdd.getValue(), "The shape added to the model should be the cloned one from clipboard.");

        // getPastedShape dovrebbe restituire il clone che è stato elaborato
        assertSame(clonedShapeFromClipboard, command.getPastedShape(), "getPastedShape should return the processed (cloned) shape.");
    }

    @Test
    void execute_whenClipboardHasContent_withAbsoluteCoordinates_shouldMoveAndAddShapeAtTarget() {
        // Arrange
        when(mockClipboardManager.hasContent()).thenReturn(true);
        when(mockClipboardManager.getFromClipboard()).thenReturn(clonedShapeFromClipboard);

        double targetX = 200.0;
        double targetY = 250.0;
        PasteShapeCommand command = new PasteShapeCommand(mockModel, mockClipboardManager, targetX, targetY, true);

        // Act
        command.execute();

        // Assert
        verify(mockClipboardManager, times(1)).hasContent();
        verify(mockClipboardManager, times(1)).getFromClipboard();

        // Verifica che moveShapeTo sia chiamato con il clone e le coordinate target
        verify(mockModel, times(1)).moveShapeTo(shapeCaptorForMove.capture(), xCaptorForMove.capture(), yCaptorForMove.capture());
        assertSame(clonedShapeFromClipboard, shapeCaptorForMove.getValue(), "moveShapeTo should be called with the cloned shape for absolute coords.");
        assertEquals(targetX, xCaptorForMove.getValue(), "X coordinate for moveShapeTo (absolute) is incorrect.");
        assertEquals(targetY, yCaptorForMove.getValue(), "Y coordinate for moveShapeTo (absolute) is incorrect.");

        // Verifica che addShape sia chiamato con il clone
        verify(mockModel, times(1)).addShape(shapeCaptorForAdd.capture());
        assertSame(clonedShapeFromClipboard, shapeCaptorForAdd.getValue());
        assertSame(clonedShapeFromClipboard, command.getPastedShape());
    }

    @Test
    void execute_whenClipboardIsEmpty_shouldNotGetFromClipboardAndNotInteractWithModel() {
        // Arrange
        when(mockClipboardManager.hasContent()).thenReturn(false);
        PasteShapeCommand command = new PasteShapeCommand(mockModel, mockClipboardManager);

        // Act
        command.execute();

        // Assert
        verify(mockClipboardManager, times(1)).hasContent();
        verify(mockClipboardManager, never()).getFromClipboard();
        verify(mockModel, never()).moveShapeTo(any(AbstractShape.class), anyDouble(), anyDouble());
        verify(mockModel, never()).addShape(any(AbstractShape.class));
        assertNull(command.getPastedShape(), "Pasted shape should be null if clipboard was empty.");
    }

    @Test
    void execute_whenClipboardHasContentButGetFromClipboardReturnsNull_shouldNotInteractWithModel() {
        // Arrange
        when(mockClipboardManager.hasContent()).thenReturn(true);
        when(mockClipboardManager.getFromClipboard()).thenReturn(null); // Simula getFromClipboard che restituisce null

        PasteShapeCommand command = new PasteShapeCommand(mockModel, mockClipboardManager);

        // Act
        command.execute();

        // Assert
        verify(mockClipboardManager, times(1)).hasContent();
        verify(mockClipboardManager, times(1)).getFromClipboard();
        verify(mockModel, never()).moveShapeTo(any(AbstractShape.class), anyDouble(), anyDouble());
        verify(mockModel, never()).addShape(any(AbstractShape.class));
        assertNull(command.getPastedShape(), "Pasted shape should be null if getFromClipboard returned null.");
    }

    @Test
    void undo_afterSuccessfulExecute_shouldRemovePastedShapeFromModel() {
        // Arrange
        when(mockClipboardManager.hasContent()).thenReturn(true);
        when(mockClipboardManager.getFromClipboard()).thenReturn(clonedShapeFromClipboard);

        PasteShapeCommand command = new PasteShapeCommand(mockModel, mockClipboardManager);
        command.execute(); // pastedShape nel comando sarà clonedShapeFromClipboard

        // Questo è il clone che è stato aggiunto al modello
        AbstractShape shapeThatWasAddedToModel = command.getPastedShape();
        assertNotNull(shapeThatWasAddedToModel, "Pasted shape in command should not be null after execute.");
        assertSame(clonedShapeFromClipboard, shapeThatWasAddedToModel, "The command's pastedShape should be the clone.");

        // Verifica che il clone sia stato aggiunto
        // (già verificato nei test di execute, ma utile per il contesto di undo)
        verify(mockModel).addShape(shapeThatWasAddedToModel);


        // Act
        command.undo();

        // Assert
        // L'undo dovrebbe rimuovere la stessa istanza di forma (il clone) che è stata aggiunta
        verify(mockModel, times(1)).removeShape(shapeThatWasAddedToModel);
    }

    @Test
    void undo_whenExecuteDidNotPasteShape_shouldNotCallRemoveShape() {
        // Arrange
        when(mockClipboardManager.hasContent()).thenReturn(false); // Clipboard vuota, execute non farà nulla
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
        // Questo caso si verifica se execute() non è mai stato chiamato,
        // o se clipboardManager.getFromClipboard() ha restituito null.
        PasteShapeCommand command = new PasteShapeCommand(mockModel, mockClipboardManager);
        // Non chiamiamo execute(), quindi command.pastedShape è null.

        // Act
        command.undo();

        // Assert
        verify(mockModel, never()).removeShape(any(AbstractShape.class));
    }
}
