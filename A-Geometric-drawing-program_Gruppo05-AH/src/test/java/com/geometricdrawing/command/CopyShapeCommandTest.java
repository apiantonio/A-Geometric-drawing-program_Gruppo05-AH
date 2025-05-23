package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CopyShapeCommandTest {

    @Mock
    private ClipboardManager mockClipboardManager;

    private AbstractShape testShape;

    @BeforeEach
    void setUp() {
        testShape = new Rectangle(0, 0, 10, 10);
    }

    @Test
    void executeShouldCallCopyToClipboardOnManager() {
        CopyShapeCommand copyCommand = new CopyShapeCommand(testShape, mockClipboardManager);
        copyCommand.execute();
        verify(mockClipboardManager, times(1)).copyToClipboard(testShape);
    }

    @Test
    void executeWithNullShapeShouldNotInteractWithClipboardManager() {
        CopyShapeCommand copyCommand = new CopyShapeCommand(null, mockClipboardManager);
        copyCommand.execute();
        verifyNoInteractions(mockClipboardManager);
    }

    @Test
    void executeWithNullClipboardManagerShouldNotThrowExceptionAndDoNothing() { // Nome leggermente modificato
        CopyShapeCommand copyCommand = new CopyShapeCommand(testShape, null);
        assertDoesNotThrow(copyCommand::execute);
        // Non possiamo verificare interazioni su un mock nullo,
        // ma ci aspettiamo che operationPerformed rimanga false
    }

    // --- Test per Undo ---

    @Test
    void undo_afterSuccessfulExecute_shouldClearClipboard() {
        // Arrange
        CopyShapeCommand command = new CopyShapeCommand(testShape, mockClipboardManager);
        // Simula l'esecuzione che popola la clipboard
        command.execute();
        // Verifica che execute abbia chiamato copyToClipboard (implicando operationPerformed = true)
        verify(mockClipboardManager).copyToClipboard(testShape);

        // Act
        command.undo();

        // Assert
        // Verifica che clearClipboard sia stato chiamato perché execute ha avuto successo
        verify(mockClipboardManager, times(1)).clearClipboard();
    }

    @Test
    void undo_whenExecuteDidNotPerformOperationDueToNullShape_shouldNotClearClipboard() {
        // Arrange: shapeToCopy è null, quindi execute() non fa nulla e operationPerformed è false
        CopyShapeCommand command = new CopyShapeCommand(null, mockClipboardManager);
        command.execute();

        // Verifica che execute non abbia interagito (operationPerformed è false)
        verifyNoInteractions(mockClipboardManager);

        // Act
        command.undo();

        // Assert: undo non dovrebbe fare nulla perché execute non ha fatto nulla
        // Quindi, clearClipboard non dovrebbe essere stato chiamato.
        // Poiché non ci sono state interazioni precedenti, verifyNoMoreInteractions è appropriato.
        verifyNoMoreInteractions(mockClipboardManager);
    }

    @Test
    void undo_whenExecuteDidNotPerformOperationDueToNullManager_shouldNotThrowAndNotInteract() {
        // Arrange: clipboardManager è null, quindi execute() non fa nulla e operationPerformed è false
        CopyShapeCommand command = new CopyShapeCommand(testShape, null);
        command.execute(); // operationPerformed sarà false

        // Act & Assert
        assertDoesNotThrow(command::undo);
        // Non possiamo verificare interazioni su mockClipboardManager perché è null nel comando,
        // ma ci aspettiamo che non ci siano eccezioni.
    }


    @Test
    void undo_whenClipboardManagerIsNullForUndoButOperationWasPerformed_shouldNotThrowException() {

        ClipboardManager initialManager = mock(ClipboardManager.class); // Un manager valido per execute
        CopyShapeCommand command = new CopyShapeCommand(testShape, initialManager);
        command.execute(); // operationPerformed diventa true

        // Questo test verifica che se il clipboardManager è null quando si costruisce il comando,
        // execute non fa nulla, e undo non fa nulla.
        CopyShapeCommand commandWithInitiallyNullManager = new CopyShapeCommand(testShape, null);
        commandWithInitiallyNullManager.execute(); // operationPerformed sarà false
        assertDoesNotThrow(commandWithInitiallyNullManager::undo); // undo non farà nulla e non lancerà eccezioni
    }

}