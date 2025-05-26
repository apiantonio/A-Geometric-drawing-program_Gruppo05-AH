package com.geometricdrawing.command;

import com.geometricdrawing.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CopyShapeCommandTest {

    private ClipboardManager clipboardManager;
    private AbstractShape shape;

    @BeforeEach
    void setUp() {
        clipboardManager = new ClipboardManager();
        shape = new Rectangle(0, 0, 10, 10);
    }

    @Test
    void executeShouldCopyShapeToClipboard() {
        CopyShapeCommand command = new CopyShapeCommand(shape, clipboardManager);
        command.execute();

        AbstractShape fromClipboard = clipboardManager.getFromClipboard();
        assertNotNull(fromClipboard);   // non deve essere null
        assertShapesAreEqualButNotSame(shape, fromClipboard);
    }

    // metodo di supporto che verifica che siano uguali ma non lo stesso oggetto
    static void assertShapesAreEqualButNotSame(AbstractShape expected, AbstractShape actual) {
        assertNotSame(expected, actual);
        // devono essere della stessa classe e avere stesse coordinate di partenza
        assertEquals(expected.getClass(), actual.getClass());
        assertEquals(expected.getX(), actual.getX());
        assertEquals(expected.getY(), actual.getY());

       if(expected instanceof Line e && actual instanceof Line a) {
            assertEquals(e.getEndX(), a.getEndX());
            assertEquals(e.getEndY(), a.getEndY());
        } else {
            assertEquals(expected.getWidth(), actual.getWidth());
            assertEquals(expected.getHeight(), actual.getHeight());
        }
    }


    @Test
    void executeWithNullShapeShouldDoNothing() {
        CopyShapeCommand command = new CopyShapeCommand(null, clipboardManager);
        command.execute();

        assertFalse(clipboardManager.hasContent());
    }

    @Test
    void executeWithNullClipboardManagerShouldNotThrow() {
        CopyShapeCommand command = new CopyShapeCommand(shape, null);
        assertDoesNotThrow(command::execute);
    }

    @Test
    void undoAfterSuccessfulCopyShouldClearClipboard() {
        CopyShapeCommand command = new CopyShapeCommand(shape, clipboardManager);
        command.execute();
        assertTrue(clipboardManager.hasContent());
        assertShapesAreEqualButNotSame(shape, clipboardManager.getFromClipboard());

        command.undo();
        assertFalse(clipboardManager.hasContent());
    }

    @Test
    void undoWhenNothingWasCopiedShouldNotThrowOrChangeAnything() {
        CopyShapeCommand command = new CopyShapeCommand(null, clipboardManager);
        command.execute();
        assertDoesNotThrow(command::undo);
        assertFalse(clipboardManager.hasContent());
    }
}
