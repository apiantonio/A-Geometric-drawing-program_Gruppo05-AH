package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MirrorShapeCommandTest {

    private DrawingModel drawingModel;
    private AbstractShape shapeToMirror;
    private int initialScaleX;
    private int initialScaleY;

    @BeforeEach
    void setUp() {
        drawingModel = new DrawingModel();
        shapeToMirror = new Rectangle(10, 10, 50, 50);
        initialScaleX = shapeToMirror.getScaleX();
        initialScaleY = shapeToMirror.getScaleY();
        drawingModel.addShape(shapeToMirror);
    }

    @Test
    void executeHorizontalMirrorShouldInvertScaleX() {
        MirrorShapeCommand command = new MirrorShapeCommand(drawingModel, shapeToMirror, true);

        command.execute();

        assertEquals(-initialScaleX, shapeToMirror.getScaleX(),
                "Il mirroring orizzontale dovrebbe invertire la scala X");
        assertEquals(initialScaleY, shapeToMirror.getScaleY(),
                "Il mirroring orizzontale non dovrebbe modificare la scala Y");
    }

    @Test
    void executeVerticalMirrorShouldInvertScaleY() {
        MirrorShapeCommand command = new MirrorShapeCommand(drawingModel, shapeToMirror, false);

        command.execute();

        assertEquals(initialScaleX, shapeToMirror.getScaleX(),
                "Il mirroring verticale non dovrebbe modificare la scala X");
        assertEquals(-initialScaleY, shapeToMirror.getScaleY(),
                "Il mirroring verticale dovrebbe invertire la scala Y");
    }

    @Test
    void undoHorizontalMirrorShouldRestoreOriginalScaleX() {
        MirrorShapeCommand command = new MirrorShapeCommand(drawingModel, shapeToMirror, true);
        command.execute();

        assertEquals(-initialScaleX, shapeToMirror.getScaleX());

        command.undo();

        assertEquals(initialScaleX, shapeToMirror.getScaleX(),
                "L'undo del mirroring orizzontale dovrebbe ripristinare la scala X originale");
        assertEquals(initialScaleY, shapeToMirror.getScaleY(),
                "L'undo del mirroring orizzontale non dovrebbe modificare la scala Y");
    }

    @Test
    void undoVerticalMirrorShouldRestoreOriginalScaleY() {
        MirrorShapeCommand command = new MirrorShapeCommand(drawingModel, shapeToMirror, false);
        command.execute();

        assertEquals(-initialScaleY, shapeToMirror.getScaleY());

        command.undo();

        assertEquals(initialScaleX, shapeToMirror.getScaleX(),
                "L'undo del mirroring verticale non dovrebbe modificare la scala X");
        assertEquals(initialScaleY, shapeToMirror.getScaleY(),
                "L'undo del mirroring verticale dovrebbe ripristinare la scala Y originale");
    }

    @Test
    void executeWithNullShapeShouldNotThrow() {
        MirrorShapeCommand command = new MirrorShapeCommand(drawingModel, null, true);

        assertDoesNotThrow(command::execute,
                "Il comando non dovrebbe lanciare eccezioni se la shape è null");
    }

    @Test
    void executeWithNullModelShouldThrow() {
        MirrorShapeCommand command = new MirrorShapeCommand(null, shapeToMirror, true);

        assertThrows(NullPointerException.class, command::execute,
                "Il comando dovrebbe lanciare NullPointerException se il model è null");
    }

    @Test
    void doubleHorizontalMirrorShouldRestoreOriginalScale() {
        MirrorShapeCommand command = new MirrorShapeCommand(drawingModel, shapeToMirror, true);

        // Prima applicazione del mirroring orizzontale
        command.execute();
        assertEquals(-initialScaleX, shapeToMirror.getScaleX());

        // Seconda applicazione del mirroring orizzontale
        command.execute();
        assertEquals(initialScaleX, shapeToMirror.getScaleX(),
                "Due mirroring orizzontali consecutivi dovrebbero ripristinare la scala originale");
    }

    @Test
    void doubleVerticalMirrorShouldRestoreOriginalScale() {
        MirrorShapeCommand command = new MirrorShapeCommand(drawingModel, shapeToMirror, false);

        // Prima applicazione del mirroring verticale
        command.execute();
        assertEquals(-initialScaleY, shapeToMirror.getScaleY());

        // Seconda applicazione del mirroring verticale
        command.execute();
        assertEquals(initialScaleY, shapeToMirror.getScaleY(),
                "Due mirroring verticali consecutivi dovrebbero ripristinare la scala originale");
    }

    @Test
    void combinedHorizontalAndVerticalMirrorShouldInvertBothScales() {
        MirrorShapeCommand horizontalCommand = new MirrorShapeCommand(drawingModel, shapeToMirror, true);
        MirrorShapeCommand verticalCommand = new MirrorShapeCommand(drawingModel, shapeToMirror, false);

        horizontalCommand.execute();
        verticalCommand.execute();

        assertEquals(-initialScaleX, shapeToMirror.getScaleX(),
                "Dopo entrambi i mirroring, la scala X dovrebbe essere invertita");
        assertEquals(-initialScaleY, shapeToMirror.getScaleY(),
                "Dopo entrambi i mirroring, la scala Y dovrebbe essere invertita");
    }
}