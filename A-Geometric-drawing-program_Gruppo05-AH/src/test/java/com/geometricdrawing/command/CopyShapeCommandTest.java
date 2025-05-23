package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Rectangle;
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

    @Test
    void executeShouldCallCopyToClipboardOnManager() {
        AbstractShape shapeToCopy = new Rectangle(0, 0, 10, 10);
        CopyShapeCommand copyCommand = new CopyShapeCommand(shapeToCopy, mockClipboardManager);

        copyCommand.execute();

        verify(mockClipboardManager, times(1)).copyToClipboard(shapeToCopy);
    }

    @Test
    void executeWithNullShapeShouldNotInteractWithClipboardManager() {
        // Crea il comando con una forma nulla
        CopyShapeCommand copyCommand = new CopyShapeCommand(null, mockClipboardManager);

        copyCommand.execute();

        // Verifica che non ci siano state interazioni con il mockClipboardManager
        // perché la condizione nell'execute() del comando impedirà la chiamata.
        verifyNoInteractions(mockClipboardManager);
    }

    @Test
    void executeWithNullClipboardManagerShouldNotThrowException() {
        AbstractShape shapeToCopy = new Rectangle(0, 0, 10, 10);
        CopyShapeCommand copyCommand = new CopyShapeCommand(shapeToCopy, null);

        // L'esecuzione non dovrebbe causare errori, anche se non farà nulla di utile.
        // L'alternativa è lanciare IllegalArgumentException nel costruttore se il manager è null.
        assertDoesNotThrow(copyCommand::execute);
    }
}