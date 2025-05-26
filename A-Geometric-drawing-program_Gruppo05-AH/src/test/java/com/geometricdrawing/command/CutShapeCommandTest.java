package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CutShapeCommandTest {

    private DrawingModel model;
    private ClipboardManager clipboardManager;
    private AbstractShape shapeToCut;
    private AbstractShape preExistingClipboardShape;
    private CutShapeCommand cutShapeCommand;

    @BeforeEach
    void setUp() {
        model = new DrawingModel();
        clipboardManager = new ClipboardManager();
        shapeToCut = new Rectangle(10, 10, 100, 50);
        preExistingClipboardShape = new Rectangle(200, 200, 30, 30);

        // forma al model per i test
        model.addShape(shapeToCut);
    }

    @Test
    void constructorShouldStoreArguments() {
        cutShapeCommand = new CutShapeCommand(model, shapeToCut, clipboardManager);
        assertSame(shapeToCut, cutShapeCommand.getCutShape(), "Il costruttore dovrebbe memorizzare la figura da tagliare.");
    }

    @Test
    void execute_shouldCopyShapeToClipboardAndRemoveFromModel_whenClipboardIsEmpty() {
        cutShapeCommand = new CutShapeCommand(model, shapeToCut, clipboardManager);

        cutShapeCommand.execute();

        assertTrue(clipboardManager.hasContent(), "La clipboard dovrebbe contenere la forma tagliata");
        AbstractShape clipboardContent = clipboardManager.getFromClipboard();
        assertNotNull(clipboardContent, "Il contenuto della clipboard non dovrebbe essere null");
        assertEquals(shapeToCut.getX(), clipboardContent.getX(), "La posizione X della forma nella clipboard dovrebbe corrispondere");
        assertEquals(shapeToCut.getY(), clipboardContent.getY(), "La posizione Y della forma nella clipboard dovrebbe corrispondere");
        assertTrue(model.getShapes().isEmpty(), "Il model dovrebbe essere vuoto dopo il taglio");
    }

    @Test
    void execute_shouldStorePreviousClipboardContentAndThenCopyShape_whenClipboardHasContent() {
        clipboardManager.copyToClipboard(preExistingClipboardShape);
        AbstractShape originalClipboardContent = clipboardManager.getFromClipboard();
        cutShapeCommand = new CutShapeCommand(model, shapeToCut, clipboardManager);

        cutShapeCommand.execute();

        assertTrue(clipboardManager.hasContent(), "La clipboard dovrebbe contenere la forma tagliata");
        AbstractShape newClipboardContent = clipboardManager.getFromClipboard();
        assertNotNull(newClipboardContent, "Il contenuto della clipboard non dovrebbe essere null");
        assertEquals(shapeToCut.getX(), newClipboardContent.getX(), "La posizione X della forma nella clipboard dovrebbe corrispondere");
        assertEquals(shapeToCut.getY(), newClipboardContent.getY(), "La posizione Y della forma nella clipboard dovrebbe corrispondere");
        assertTrue(model.getShapes().isEmpty(), "Il model dovrebbe essere vuoto dopo il taglio");
    }

    @Test
    void execute_withNullModel_shouldDoNothing() {
        cutShapeCommand = new CutShapeCommand(null, shapeToCut, clipboardManager);
        cutShapeCommand.execute();
        assertFalse(clipboardManager.hasContent(), "La clipboard dovrebbe essere vuota se il modello è null");
    }

    @Test
    void execute_withNullShape_shouldDoNothing() {
        cutShapeCommand = new CutShapeCommand(model, null, clipboardManager);
        cutShapeCommand.execute();
        assertEquals(1, model.getShapes().size(), "Il model non dovrebbe essere modificato con una forma null");
        assertFalse(clipboardManager.hasContent(), "La clipboard non dovrebbe contenere nulla con una forma null");
    }

    @Test
    void execute_withNullClipboardManager_shouldDoNothing() {
        cutShapeCommand = new CutShapeCommand(model, shapeToCut, null);
        cutShapeCommand.execute();
        assertEquals(1, model.getShapes().size(), "Il model non dovrebbe essere modificato con clipboard null");
    }

    @Test
    void undo_afterExecuteWhenClipboardWasEmpty_shouldAddShapeBackAndClearClipboard() {
        cutShapeCommand = new CutShapeCommand(model, shapeToCut, clipboardManager);
        cutShapeCommand.execute();

        cutShapeCommand.undo();

        assertEquals(1, model.getShapes().size(), "Il model dovrebbe contenere la forma ripristinata");
        assertTrue(model.getShapes().contains(shapeToCut), "Il model dovrebbe contenere la forma originale");
        assertFalse(clipboardManager.hasContent(), "La clipboard dovrebbe essere vuota dopo l'undo");
    }

    @Test
    void undo_afterExecuteWhenClipboardHadContent_shouldAddShapeBackAndRestorePreviousClipboardContent() {
        clipboardManager.copyToClipboard(preExistingClipboardShape);
        AbstractShape originalClipboardContent = clipboardManager.getFromClipboard();
        cutShapeCommand = new CutShapeCommand(model, shapeToCut, clipboardManager);
        cutShapeCommand.execute();

        cutShapeCommand.undo();

        assertEquals(1, model.getShapes().size(), "Il model dovrebbe contenere la forma ripristinata");
        assertTrue(model.getShapes().contains(shapeToCut), "Il model dovrebbe contenere la forma originale");

        assertTrue(clipboardManager.hasContent(), "La clipboard dovrebbe contenere il contenuto precedente");
        AbstractShape restoredClipboardContent = clipboardManager.getFromClipboard();
        assertEquals(preExistingClipboardShape.getX(), restoredClipboardContent.getX(), "La posizione X della forma ripristinata nella clipboard dovrebbe corrispondere");
        assertEquals(preExistingClipboardShape.getY(), restoredClipboardContent.getY(), "La posizione Y della forma ripristinata nella clipboard dovrebbe corrispondere");
    }

    @Test
    void undo_whenExecuteDidNotRunOrFailed_shouldNotInteractWithModelOrClipboard() {
        cutShapeCommand = new CutShapeCommand(null, shapeToCut, clipboardManager);
        cutShapeCommand.undo();
        assertEquals(1, model.getShapes().size(), "Il modello non dovrebbe essere modificato se execute non è stato eseguito");
        assertFalse(clipboardManager.hasContent(), "La clipboard non dovrebbe essere modificata se execute non è stato eseguito");
    }

    @Test
    void getCutShape_shouldReturnTheShapePassedToConstructor() {
        cutShapeCommand = new CutShapeCommand(model, shapeToCut, clipboardManager);
        assertSame(shapeToCut, cutShapeCommand.getCutShape());
    }
}

