package com.geometricdrawing.mousehandler;

import com.geometricdrawing.controller.DrawingController;
import com.geometricdrawing.controller.HandleType;
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
        HandleType activeHandle = controller.getActiveResizeHandle();
        AbstractShape shapeThatWasResized = controller.getShapeBeingResized(); // Figura dopo la modifica

        if (activeHandle != null && shapeThatWasResized != null) {
            // L'operazione era un ridimensionamento/stretch
            double finalX = shapeThatWasResized.getX();
            double finalY = shapeThatWasResized.getY();
            double finalW = shapeThatWasResized.getWidth();
            double finalH = shapeThatWasResized.getHeight();

            // Recupera lo stato iniziale della figura (prima del resize) dal controller
            double initialX = controller.getInitialShapeX_world_resize();
            double initialY = controller.getInitialShapeY_world_resize();
            double initialW = controller.getInitialShapeWidth_world_resize();
            double initialH = controller.getInitialShapeHeight_world_resize();

            // Determina se c'è stato un cambiamento effettivo in posizione o dimensioni
            // Usa una soglia molto piccola per i confronti in virgola mobile
            boolean hasChanged = (Math.abs(finalX - initialX) > 1e-7 ||
                    Math.abs(finalY - initialY) > 1e-7 ||
                    Math.abs(finalW - initialW) > 1e-7 ||
                    Math.abs(finalH - initialH) > 1e-7);

            // Gestione specifica per Line, poiché width/height sono dx/dy
            if (shapeThatWasResized instanceof com.geometricdrawing.model.Line &&
                    (activeHandle == HandleType.LINE_START || activeHandle == HandleType.LINE_END)) {
                // Per le linee, un cambiamento nella posizione del punto iniziale (finalX, finalY vs initialX, initialY)
                // o nelle componenti del vettore (finalW, finalH vs initialW, initialH) costituisce uno stretch.
                hasChanged = (Math.abs(finalX - initialX) > 1e-7 ||
                        Math.abs(finalY - initialY) > 1e-7 ||
                        Math.abs(finalW - initialW) > 1e-7 ||
                        Math.abs(finalH - initialH) > 1e-7);
            }


            if (hasChanged) {
                // Crea un singolo comando che incapsula l'intera operazione di stretch
                // Assicurati che StretchShapeCommand sia importato correttamente
                com.geometricdrawing.command.StretchShapeCommand stretchCmd = new com.geometricdrawing.command.StretchShapeCommand(
                        controller.getModel(),
                        shapeThatWasResized,
                        initialX, initialY, initialW, initialH,
                        finalX, finalY, finalW, finalH
                );
                controller.getCommandManager().executeCommand(stretchCmd);
                // Rimuovi o commenta questa stampa in produzione
                System.out.println("[DEBUG] MouseReleasedHandler: Added StretchShapeCommand. initialX=" + initialX + ", finalX=" + finalX +
                        "; initialW=" + initialW + ", finalW=" + finalW +
                        "; initialH=" + initialH + ", finalH=" + finalH +
                        "; initialY=" + initialY + ", finalY=" + finalY);
            } else {
                // Rimuovi o commenta questa stampa in produzione
                System.out.println("[DEBUG] MouseReleasedHandler: No significant change detected for StretchShapeCommand. " +
                        "finalX=" + finalX + ", initialX=" + initialX +
                        ", finalY=" + finalY + ", initialY=" + initialY +
                        ", finalW=" + finalW + ", initialW=" + initialW +
                        ", finalH=" + finalH + ", initialH=" + initialH);
            }

        } else if (controller.isDragging() && controller.getCurrentShape() != null) {
            // Questa parte gestisce il trascinamento normale (non stretch) e usa MoveShapeCommand.
            AbstractShape currentDraggedShape = controller.getCurrentShape();
            double finalDragX = currentDraggedShape.getX();
            double finalDragY = currentDraggedShape.getY();
            // Recupera lo stato iniziale del trascinamento dal controller
            double initialDragX = controller.getInitialDragShapeX_world();
            double initialDragY = controller.getInitialDragShapeY_world();

            // Se la posizione è cambiata significativamente durante il trascinamento
            if (Math.abs(finalDragX - initialDragX) > 1e-7 || Math.abs(finalDragY - initialDragY) > 1e-7) {
                // Assicurati che MoveShapeCommand sia importato correttamente
                com.geometricdrawing.command.MoveShapeCommand moveCmd = new com.geometricdrawing.command.MoveShapeCommand(
                        controller.getModel(),
                        currentDraggedShape,
                        finalDragX,
                        finalDragY
                );
                moveCmd.setOldX(initialDragX); // Imposta esplicitamente le coordinate "vecchie" per l'undo
                moveCmd.setOldY(initialDragY);
                controller.getCommandManager().executeCommand(moveCmd);
                // Rimuovi o commenta questa stampa in produzione
                System.out.println("[DEBUG] MouseReleasedHandler: Added MoveShapeCommand for DRAG. initialX=" + initialDragX + ", finalX=" + finalDragX +
                        "; initialY=" + initialDragY + ", finalY=" + finalDragY + ". Command's oldX set to " + initialDragX);
            } else {
                // Rimuovi o commenta questa stampa in produzione
                System.out.println("[DEBUG] MouseReleasedHandler: MoveShapeCommand for DRAG NOT added. No significant position change. " +
                        "finalDragX=" + finalDragX + ", initialDragX=" + initialDragX +
                        ", finalDragY=" + finalDragY + ", initialDragY=" + initialDragY);
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