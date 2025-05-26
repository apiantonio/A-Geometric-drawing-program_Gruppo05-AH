package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CutShapeCommandTest {

    @Mock
    private DrawingModel mockModel;
    @Mock
    private ClipboardManager mockClipboardManager;

    private AbstractShape shapeToCut;
    private AbstractShape preExistingClipboardShape;
    private CutShapeCommand cutShapeCommand;

    @BeforeEach
    void setUp() {
        shapeToCut = new Rectangle(10, 10, 100, 50);
        preExistingClipboardShape = new Rectangle(200, 200, 30, 30); // Un'altra forma per simulare la clipboard preesistente
        // Il command viene ricreato in ogni test per chiarezza,
        // ma potremmo inizializzarlo qui se i parametri del costruttore fossero sempre gli stessi.
    }

    @Test
    void constructorShouldStoreArguments() {
        cutShapeCommand = new CutShapeCommand(mockModel, shapeToCut, mockClipboardManager);
        assertSame(shapeToCut, cutShapeCommand.getCutShape(), "Il costruttore dovrebbe memorizzare la figura da tagliare.");
    }

    @Test
    void execute_shouldCopyShapeToClipboardAndRemoveFromModel_whenClipboardIsEmpty() {
        // Arrange
        when(mockClipboardManager.hasContent()).thenReturn(false);
        cutShapeCommand = new CutShapeCommand(mockModel, shapeToCut, mockClipboardManager);

        // Act
        cutShapeCommand.execute();

        // Assert
        verify(mockClipboardManager, times(1)).copyToClipboard(shapeToCut);
        verify(mockModel, times(1)).removeShape(shapeToCut);
        assertEquals(shapeToCut, cutShapeCommand.getCutShape());
    }

    @Test
    void execute_shouldStorePreviousClipboardContentAndThenCopyShape_whenClipboardHasContent() {
        // Arrange
        when(mockClipboardManager.hasContent()).thenReturn(true);
        // getFromClipboard restituisce un clone, quindi mockiamo un clone
        AbstractShape clonedPreExistingShape = preExistingClipboardShape.deepClone();
        when(mockClipboardManager.getFromClipboard()).thenReturn(clonedPreExistingShape);

        cutShapeCommand = new CutShapeCommand(mockModel, shapeToCut, mockClipboardManager);

        // Act
        cutShapeCommand.execute();

        // Assert
        // Prima controlla getFromClipboard (per shapePreviouslyInClipboard), poi copyToClipboard per la nuova forma
        InOrder inOrder = inOrder(mockClipboardManager, mockModel);
        inOrder.verify(mockClipboardManager).hasContent();
        inOrder.verify(mockClipboardManager).getFromClipboard();
        inOrder.verify(mockClipboardManager).copyToClipboard(shapeToCut);
        inOrder.verify(mockModel).removeShape(shapeToCut);

        assertEquals(shapeToCut, cutShapeCommand.getCutShape());
    }


    @Test
    void execute_withNullModel_shouldDoNothing() {
        cutShapeCommand = new CutShapeCommand(null, shapeToCut, mockClipboardManager);
        cutShapeCommand.execute();
        verifyNoInteractions(mockClipboardManager); // Model è nullo, quindi non ci saranno interazioni con esso
        verify(mockClipboardManager, never()).copyToClipboard(any());
    }

    @Test
    void execute_withNullShape_shouldDoNothing() {
        cutShapeCommand = new CutShapeCommand(mockModel, null, mockClipboardManager);
        cutShapeCommand.execute();
        verifyNoInteractions(mockModel);
        verify(mockClipboardManager, never()).copyToClipboard(any());
    }

    @Test
    void execute_withNullClipboardManager_shouldDoNothing() {
        cutShapeCommand = new CutShapeCommand(mockModel, shapeToCut, null);
        cutShapeCommand.execute();
        verifyNoInteractions(mockModel);
        // Non possiamo verificare mockClipboardManager perché è nullo, ma non dovrebbero esserci eccezioni
    }


    @Test
    void undo_afterExecuteWhenClipboardWasEmpty_shouldAddShapeBackAndClearClipboard() {
        // Arrange
        when(mockClipboardManager.hasContent()).thenReturn(false); // Clipboard inizialmente vuota
        cutShapeCommand = new CutShapeCommand(mockModel, shapeToCut, mockClipboardManager);
        cutShapeCommand.execute(); // Questo imposterà shapePreviouslyInClipboard a null

        // Act
        cutShapeCommand.undo();

        // Assert
        verify(mockModel, times(1)).addShape(shapeToCut);
        // copyToClipboard(null) viene chiamato per ripristinare la clipboard vuota
        verify(mockClipboardManager, times(1)).copyToClipboard(null);
    }

    @Test
    void undo_afterExecuteWhenClipboardHadContent_shouldAddShapeBackAndRestorePreviousClipboardContent() {
        // Arrange
        when(mockClipboardManager.hasContent()).thenReturn(true);
        AbstractShape clonedPreExistingShape = preExistingClipboardShape.deepClone();
        when(mockClipboardManager.getFromClipboard()).thenReturn(clonedPreExistingShape);

        cutShapeCommand = new CutShapeCommand(mockModel, shapeToCut, mockClipboardManager);
        cutShapeCommand.execute(); // shapePreviouslyInClipboard ora contiene clonedPreExistingShape

        // Act
        cutShapeCommand.undo();

        // Assert
        verify(mockModel, times(1)).addShape(shapeToCut);

        ArgumentCaptor<AbstractShape> clipboardCaptor = ArgumentCaptor.forClass(AbstractShape.class);
        // L'ultima chiamata a copyToClipboard (quella in undo) dovrebbe essere con la forma preesistente.
        // Ci sono state due chiamate: una in execute() e una in undo().
        verify(mockClipboardManager, times(2)).copyToClipboard(clipboardCaptor.capture());
        List<AbstractShape> capturedShapes = clipboardCaptor.getAllValues();

        // L'ultima forma copiata (durante l'undo) dovrebbe essere un clone della forma preesistente
        AbstractShape restoredShapeInClipboard = capturedShapes.get(1);
        assertNotNull(restoredShapeInClipboard);
        assertNotSame(preExistingClipboardShape, restoredShapeInClipboard, "Dovrebbe essere un clone ripristinato.");
        assertEquals(preExistingClipboardShape.getX(), restoredShapeInClipboard.getX());
        assertEquals(preExistingClipboardShape.getClass(), restoredShapeInClipboard.getClass());
    }

    @Test
    void undo_whenExecuteDidNotRunOrFailed_shouldNotInteractWithModelOrClipboard() {
        // Simula uno scenario in cui execute non ha fatto nulla (es. parametri nulli)
        cutShapeCommand = new CutShapeCommand(null, shapeToCut, mockClipboardManager);
        // execute() non è chiamato o non ha modificato lo stato 'shapeWasRemovedFromModel'

        // Act
        cutShapeCommand.undo();

        // Assert
        verify(mockModel, never()).addShape(any());
        // copyToClipboard non dovrebbe essere chiamato da undo se execute non ha avuto successo
        verify(mockClipboardManager, never()).copyToClipboard(any());
    }

    @Test
    void getCutShape_shouldReturnTheShapePassedToConstructor() {
        cutShapeCommand = new CutShapeCommand(mockModel, shapeToCut, mockClipboardManager);
        assertSame(shapeToCut, cutShapeCommand.getCutShape());
    }
}