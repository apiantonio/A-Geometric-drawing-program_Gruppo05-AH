package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
import com.geometricdrawing.HandleType;
import com.geometricdrawing.command.ChangeHeightCommand;
import com.geometricdrawing.command.ChangeWidthCommand;
import com.geometricdrawing.command.Command;
import com.geometricdrawing.command.MoveShapeCommand;
// Potentially a CompositeCommand if multiple commands need to be atomic for undo
// import com.geometricdrawing.command.CompositeCommand;
import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Line;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import java.util.ArrayList;
import java.util.List;

// Handler per il rilascio del mouse: finalizza drag o resize e crea i comandi per l'undo
public class MouseReleasedHandler extends AbstractMouseHandler {

    public MouseReleasedHandler(Canvas canvas, DrawingController controller) {
        super(canvas, controller);
    }

    @Override
    protected void preProcess(MouseEvent event) {
        // Nessuna pre-elaborazione necessaria qui: lo stato è già nel controller
    }

    @Override
    protected void processEvent(MouseEvent event) {
        // Recupero l'handle di resize attivo e la shape che è stata ridimensionata (se presenti)
        HandleType activeHandle = controller.getActiveResizeHandle();
        AbstractShape shapeThatWasResized = controller.getShapeBeingResized();

        if (activeHandle != null && shapeThatWasResized != null) {
            // Se sto rilasciando dopo un resize, creo i comandi per undo/redo
            double finalX = shapeThatWasResized.getX();
            double finalY = shapeThatWasResized.getY();
            double finalW = shapeThatWasResized.getWidth();
            double finalH = shapeThatWasResized.getHeight();

            // Recupero lo stato iniziale della shape (prima del resize)
            double initialX = controller.getInitialShapeX_world_resize();
            double initialY = controller.getInitialShapeY_world_resize();
            double initialW = controller.getInitialShapeWidth_world_resize();
            double initialH = controller.getInitialShapeHeight_world_resize();

            List<Command> commands = new ArrayList<>();

            // Se la posizione è cambiata in modo significativo, aggiungo il comando di spostamento
            if (Math.abs(finalX - initialX) > 1e-3 || Math.abs(finalY - initialY) > 1e-3) {
                MoveShapeCommand moveCmd = new MoveShapeCommand(controller.getModel(), shapeThatWasResized, finalX, finalY);
                moveCmd.setOldX(initialX); // Serve per l'undo: imposto la posizione iniziale
                moveCmd.setOldY(initialY);
                commands.add(moveCmd);
            }

            // Se la larghezza è cambiata, aggiungo il comando di cambio larghezza
            if (Math.abs(finalW - initialW) > 1e-3) {
                ChangeWidthCommand widthCmd = new ChangeWidthCommand(controller.getModel(), shapeThatWasResized, finalW, initialW);
                commands.add(widthCmd);
            }

            // Se l'altezza è cambiata (e non sto ridimensionando una Linea tramite i suoi capi), aggiungo il comando di cambio altezza
            if (Math.abs(finalH - initialH) > 1e-3 && !(shapeThatWasResized instanceof Line &&
                    (activeHandle == HandleType.LINE_START || activeHandle == HandleType.LINE_END))) {
                // Le linee non cambiano "altezza" tramite questi handle
                ChangeHeightCommand heightCmd = new ChangeHeightCommand(controller.getModel(), shapeThatWasResized, finalH, initialH);
                commands.add(heightCmd);
            }

            // Eseguo tutti i comandi generati (uno per tipo di modifica)
            if (!commands.isEmpty()) {
                // Se avessi un CompositeCommand, potrei eseguire tutto in un colpo solo per l'undo atomico
                for (Command cmd : commands) {
                    controller.getCommandManager().executeCommand(cmd);
                }
            }
        } else if (controller.isDragging() && controller.getCurrentShape() != null) {
            // Se sto rilasciando dopo un drag della shape intera, creo il comando di spostamento
            AbstractShape currentDraggedShape = controller.getCurrentShape();
            double finalDragX = currentDraggedShape.getX();
            double finalDragY = currentDraggedShape.getY();
            double initialDragX = controller.getInitialDragShapeX_world(); // Salvato al mouse press
            double initialDragY = controller.getInitialDragShapeY_world();

            // Se la posizione è cambiata, aggiungo il comando di spostamento
            if (Math.abs(finalDragX - initialDragX) > 1e-3 || Math.abs(finalDragY - initialDragY) > 1e-3) {
                MoveShapeCommand moveCmd = new MoveShapeCommand(controller.getModel(), currentDraggedShape, finalDragX, finalDragY);
                moveCmd.setOldX(initialDragX);
                moveCmd.setOldY(initialDragY);
                controller.getCommandManager().executeCommand(moveCmd);
            }
        }
    }

    @Override
    protected void postProcess(MouseEvent event) {
        // Resetto lo stato di drag e resize nel controller
        controller.resetDrag();
        controller.setActiveResizeHandle(null);
        controller.setShapeBeingResized(null);

        // Reimposto il cursore di default (MouseMovedHandler lo aggiornerà se serve)
        if (canvas!=null) canvas.setCursor(Cursor.DEFAULT);

        // Aggiorno i controlli UI in base allo stato attuale della shape selezionata
        controller.updateControlState(controller.getCurrentShape());
        controller.updateSpinners(controller.getCurrentShape());
        super.postProcess(event); // Chiama redrawCanvas
    }
}