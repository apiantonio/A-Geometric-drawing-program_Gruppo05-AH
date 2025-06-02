package com.geometricdrawing.mousehandler;

import com.geometricdrawing.controller.DrawingController;
import com.geometricdrawing.controller.HandleType;
import com.geometricdrawing.controller.ZoomHandler;
import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Line;
import com.geometricdrawing.model.TextShape;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton; // Import MouseButton
import javafx.scene.input.MouseEvent;

public class MouseDraggedHandler extends AbstractMouseHandler {

    public MouseDraggedHandler(Canvas canvas, DrawingController controller) {
        super(canvas, controller);
    }

    @Override
    protected void preProcess(MouseEvent event) {
        // Imposta il cursore un base all'operaziine da effettuare
        currentShape = controller.getCurrentShape(); // currentShape presa dal controller

        if (controller.getActiveResizeHandle() != null && controller.getShapeBeingResized() != null) {
            canvas.setCursor(controller.getCursorForHandle(controller.getActiveResizeHandle(), controller.getShapeBeingResized()));
        } else if (currentShape != null && event.getButton() == MouseButton.PRIMARY) {
            //Se l'utente sta trascinando
            canvas.setCursor(Cursor.CLOSED_HAND);
        }
    }

    @Override
    protected void processEvent(MouseEvent event) {
        HandleType activeHandle = controller.getActiveResizeHandle();
        AbstractShape shapeToUpdate = controller.getShapeBeingResized(); // la shape che si sta modificando
        AbstractShape shapeToDragEntirely = controller.getCurrentShape(); // la shape che si sta trascinando

        if (activeHandle != null && shapeToUpdate != null) {
            // logica di resize
            handleShapeResize(event, activeHandle, shapeToUpdate);
            return;
        }
        if (shapeToDragEntirely != null && event.getButton() == MouseButton.PRIMARY) {
            handleShapeDrag(event, shapeToDragEntirely);
        }
    }

    private void handleShapeDrag(MouseEvent event, AbstractShape shapeToDragEntirely) {
        // logica di trascinamento
        if (!controller.isDragging()) {
            controller.setStartDragX(event.getX());
            controller.setStartDragY(event.getY());
        }

        ZoomHandler zoomHandler = controller.getZoomHandler();
        if (zoomHandler == null || controller.getHorizontalScrollBar() == null || controller.getVerticalScrollBar() == null) { // Added null check for scrollbars
            return;
        }

        Point2D worldMouseCoords = zoomHandler.screenToWorld(event.getX(), event.getY());
        // usiamo le coordinate del mouse in world per calcolare la nuova posizione della shape
        double newWorldX = worldMouseCoords.getX() - controller.getDragOffsetX();
        double newWorldY = worldMouseCoords.getY() - controller.getDragOffsetY();

        // logica di clamping per evitare che la shape esca dai bordi del canvas
        double currentShapeWidth = shapeToDragEntirely.getWidth();
        double currentShapeHeight = shapeToDragEntirely.getHeight();
        double worldCanvasWidth = canvas.getWidth() / zoomHandler.getZoomFactor();
        double worldCanvasHeight = canvas.getHeight() / zoomHandler.getZoomFactor();
        double currentScrollX = controller.getHorizontalScrollBar().getValue();
        double currentScrollY = controller.getVerticalScrollBar().getValue();
        double effectiveBorderMargin = AbstractMouseHandler.BORDER_MARGIN / zoomHandler.getZoomFactor();

        double minClampedX = currentScrollX + effectiveBorderMargin - currentShapeWidth * AbstractMouseHandler.VISIBLE_SHAPE_PORTION;
        // Sticking to user's original calculation for maxClampedX which did not subtract border margin at the far end
        double maxClampedX = currentScrollX + worldCanvasWidth - currentShapeWidth * AbstractMouseHandler.HIDDEN_SHAPE_PORTION;
        newWorldX = Math.max(minClampedX, Math.min(newWorldX, maxClampedX));

        double minClampedY = currentScrollY + effectiveBorderMargin - currentShapeHeight * AbstractMouseHandler.VISIBLE_SHAPE_PORTION;
        double maxClampedY = currentScrollY + worldCanvasHeight - currentShapeHeight * AbstractMouseHandler.HIDDEN_SHAPE_PORTION;
        newWorldY = Math.max(minClampedY, Math.min(newWorldY, maxClampedY));

        controller.getModel().moveShapeTo(shapeToDragEntirely, newWorldX, newWorldY);
    }

    private void handleShapeResize(MouseEvent event, HandleType handleType, AbstractShape shapeToUpdate) {
        AbstractShape baseShapeToAnalyze = controller.getBaseShape(shapeToUpdate);

        ZoomHandler zoomHandler = controller.getZoomHandler();
        Point2D currentScreenMousePos = new Point2D(event.getX(), event.getY());
        Point2D startScreenMousePos = controller.getResizeStartMousePos_screen();

        double iX = controller.getInitialShapeX_world_resize();
        double iY = controller.getInitialShapeY_world_resize();
        double iW = controller.getInitialShapeWidth_world_resize();
        double iH = controller.getInitialShapeHeight_world_resize();
        double iAngle = controller.getInitialShapeAngle_resize();
        int iScaleX = controller.getInitialShapeScaleX_resize();
        int iScaleY = controller.getInitialShapeScaleY_resize();

        double screenDeltaX = currentScreenMousePos.getX() - startScreenMousePos.getX();
        double screenDeltaY = currentScreenMousePos.getY() - startScreenMousePos.getY();

        double worldDeltaXUnrotated = screenDeltaX / zoomHandler.getZoomFactor();
        double worldDeltaYUnrotated = screenDeltaY / zoomHandler.getZoomFactor();

        double angleRad_inv = Math.toRadians(-iAngle);
        double cosA_inv = Math.cos(angleRad_inv);
        double sinA_inv = Math.sin(angleRad_inv);

        double localDeltaX = (worldDeltaXUnrotated * cosA_inv - worldDeltaYUnrotated * sinA_inv) / iScaleX;
        double localDeltaY = (worldDeltaXUnrotated * sinA_inv + worldDeltaYUnrotated * cosA_inv) / iScaleY;

        // Valori finali da calcolare e applicare
        double finalX = iX;
        double finalY = iY;
        double finalW = iW;
        double finalH = iH;

        final double MIN_SIZE = 5.0;
        final double MAX_DIMENSION_ALLOWED = 1000.0; // Limite massimo per larghezza/altezza

        if (baseShapeToAnalyze instanceof Line) {
            final double MAX_LINE_LENGTH_ALLOWED = 1000.0;

            Point2D initialStartPoint_world = new Point2D(iX, iY);
            Point2D initialEndPoint_world = new Point2D(iX + iW, iY + iH);

            // Calcola il vettore unitario della linea iniziale per mantenere la direzione
            double lineUnitVecX = 0, lineUnitVecY = 0;
            double initialLength = Math.sqrt(iW * iW + iH * iH);
            if (initialLength > 1e-6) {
                lineUnitVecX = iW / initialLength;
                lineUnitVecY = iH / initialLength;
            } else { // Se la linea iniziale ha lunghezza zero, usa l'angolo della forma o una direzione di default
                lineUnitVecX = Math.cos(Math.toRadians(iAngle));
                lineUnitVecY = Math.sin(Math.toRadians(iAngle));
                if (Math.sqrt(lineUnitVecX * lineUnitVecX + lineUnitVecY * lineUnitVecY) < 1e-6) {
                    lineUnitVecX = 1.0; // Default orizzontale
                }
            }

            // Proiezione del delta del mouse sull'asse della linea
            double dragMagnitudeAlongLineAxis = worldDeltaXUnrotated * lineUnitVecX + worldDeltaYUnrotated * lineUnitVecY;
            // Correzione per mirroring (semplificata, come nel codice originale)
            if (iAngle == 0) {
                if (Math.abs(lineUnitVecX) > Math.abs(lineUnitVecY)) { // Prevalentemente orizzontale
                    if (iScaleX != 0) dragMagnitudeAlongLineAxis /= iScaleX;
                } else if (Math.abs(lineUnitVecY) > 0) { // Prevalentemente verticale
                    if (iScaleY != 0) dragMagnitudeAlongLineAxis /= iScaleY;
                }
            }

            double stretchVecWorldX = dragMagnitudeAlongLineAxis * lineUnitVecX;
            double stretchVecWorldY = dragMagnitudeAlongLineAxis * lineUnitVecY;

            double tempFinalX = iX, tempFinalY = iY, tempFinalW = iW, tempFinalH = iH;

            if (handleType == HandleType.LINE_START) {
                tempFinalX = initialStartPoint_world.getX() + stretchVecWorldX;
                tempFinalY = initialStartPoint_world.getY() + stretchVecWorldY;
                tempFinalW = initialEndPoint_world.getX() - tempFinalX;
                tempFinalH = initialEndPoint_world.getY() - tempFinalY;
            } else if (handleType == HandleType.LINE_END) {
                // tempFinalX, tempFinalY rimangono iX, iY (origine della linea)
                double newEndX = initialEndPoint_world.getX() + stretchVecWorldX;
                double newEndY = initialEndPoint_world.getY() + stretchVecWorldY;
                tempFinalW = newEndX - tempFinalX;
                tempFinalH = newEndY - tempFinalY;
            } else {
                return; // Handle non valido per la linea
            }

            double currentLength = Math.sqrt(tempFinalW * tempFinalW + tempFinalH * tempFinalH);
            double targetLength = currentLength;

            if (targetLength > MAX_LINE_LENGTH_ALLOWED) {
                targetLength = MAX_LINE_LENGTH_ALLOWED;
            }
            if (targetLength < MIN_SIZE) {
                targetLength = MIN_SIZE;
            }

            if (Math.abs(currentLength - targetLength) > 1e-6) { // Se è necessario un aggiustamento della lunghezza
                if (currentLength > 1e-6) {
                    double scale = targetLength / currentLength;
                    tempFinalW *= scale;
                    tempFinalH *= scale;
                } else if (targetLength > 0) {

                    double dirX = worldDeltaXUnrotated, dirY = worldDeltaYUnrotated;
                    double dragLen = Math.sqrt(dirX*dirX + dirY*dirY);
                    if (dragLen > 1e-6) {
                        dirX /= dragLen; dirY /= dragLen;
                    } else { // Fallback alla direzione unitaria originale o default
                        dirX = lineUnitVecX; dirY = lineUnitVecY;
                    }
                    tempFinalW = dirX * targetLength;
                    tempFinalH = dirY * targetLength;
                }
                // Se LINE_START è stato trascinato, l'origine (tempFinalX, tempFinalY) deve essere ricalcolata
                // in modo che il punto finale (initialEndPoint_world) rimanga fisso.
                if (handleType == HandleType.LINE_START) {
                    tempFinalX = initialEndPoint_world.getX() - tempFinalW;
                    tempFinalY = initialEndPoint_world.getY() - tempFinalH;
                }
            }
            finalX = tempFinalX; finalY = tempFinalY; finalW = tempFinalW; finalH = tempFinalH;

        } else { // Gestione per forme non-Linea
            double tentativeW = iW;
            double tentativeH = iH;

            switch (handleType) {
                case TOP_LEFT:
                    tentativeW = iW - localDeltaX;
                    tentativeH = iH - localDeltaY;
                    break;
                case TOP_RIGHT:
                    tentativeW = iW + localDeltaX;
                    tentativeH = iH - localDeltaY;
                    break;
                case BOTTOM_LEFT:
                    tentativeW = iW - localDeltaX;
                    tentativeH = iH + localDeltaY;
                    break;
                case BOTTOM_RIGHT:
                    tentativeW = iW + localDeltaX;
                    tentativeH = iH + localDeltaY;
                    break;
                case TOP_CENTER:
                    tentativeH = iH - localDeltaY;
                    // tentativeW rimane iW
                    break;
                case BOTTOM_CENTER:
                    tentativeH = iH + localDeltaY;
                    // tentativeW rimane iW
                    break;
                case LEFT_CENTER:
                    tentativeW = iW - localDeltaX;
                    // tentativeH rimane iH
                    break;
                case RIGHT_CENTER:
                    tentativeW = iW + localDeltaX;
                    // tentativeH rimane iH
                    break;
            }

            // Applica MAX_DIMENSION_ALLOWED
            tentativeW = Math.min(tentativeW, MAX_DIMENSION_ALLOWED);
            tentativeH = Math.min(tentativeH, MAX_DIMENSION_ALLOWED);

            // Gestione del testo per assicurare dimensioni minime basate sul contenuto
            if (baseShapeToAnalyze instanceof TextShape textShape) {
                double widthForTextWrapCalc = iW;
                boolean affectsWidth = false;
                boolean affectsHeight = false;

                switch (handleType) {
                    case TOP_LEFT: case TOP_RIGHT: case BOTTOM_LEFT: case BOTTOM_RIGHT:
                        affectsWidth = true;
                        affectsHeight = true;
                        break;
                    case LEFT_CENTER: case RIGHT_CENTER:
                        affectsWidth = true;
                        break;
                    case TOP_CENTER: case BOTTOM_CENTER:
                        affectsHeight = true;
                        break;
                }

                if (affectsWidth) {
                    widthForTextWrapCalc = Math.max(MIN_SIZE, tentativeW);
                }

                Point2D naturalTextDims = textShape.getNaturalTextBlockDimensions(widthForTextWrapCalc);
                double minTextW = naturalTextDims.getX();
                double minTextH = naturalTextDims.getY();

                if (affectsWidth) {
                    tentativeW = Math.max(tentativeW, minTextW);
                }
                if (affectsHeight) {
                    if (affectsWidth && Math.abs(tentativeW - widthForTextWrapCalc) > 1e-3) {
                        Point2D reevalTextDims = textShape.getNaturalTextBlockDimensions(tentativeW);
                        minTextH = reevalTextDims.getY();
                    }
                    tentativeH = Math.max(tentativeH, minTextH);
                }
            }


            finalW = Math.max(MIN_SIZE, tentativeW);
            finalH = Math.max(MIN_SIZE, tentativeH);

            // Calcola lo spostamento dell'origine (dx_origin_local, dy_origin_local)
            // Questo spostamento è relativo al sistema di coordinate locale della forma
            // ed è necessario perché se si ridimensiona da sinistra o dall'alto, l'origine si sposta.
            double dx_origin_local = 0.0;
            double dy_origin_local = 0.0;

            switch (handleType) {
                case TOP_LEFT:
                    dx_origin_local = iW - finalW;
                    dy_origin_local = iH - finalH;
                    break;
                case TOP_RIGHT:
                    dy_origin_local = iH - finalH;
                    break;
                case BOTTOM_LEFT:
                    dx_origin_local = iW - finalW;
                    break;
                case BOTTOM_RIGHT:
                    // Nessuno spostamento dell'origine
                    break;
                case TOP_CENTER:
                    dy_origin_local = iH - finalH;
                    break;
                case BOTTOM_CENTER:
                    // Nessuno spostamento dell'origine
                    break;
                case LEFT_CENTER:
                    dx_origin_local = iW - finalW;
                    break;
                case RIGHT_CENTER:
                    // Nessuno spostamento dell'origine
                    break;
                default:
                    System.err.println("Tipo di maniglia di ridimensionamento non gestito: " + handleType);
                    return;
            }

            // Trasforma lo spostamento dell'origine locale in coordinate del mondo
            double angleRad_for_transform = Math.toRadians(iAngle);
            double cosA_for_transform = Math.cos(angleRad_for_transform);
            double sinA_for_transform = Math.sin(angleRad_for_transform);

            double world_offsetX = (dx_origin_local * iScaleX * cosA_for_transform - dy_origin_local * iScaleY * sinA_for_transform);
            double world_offsetY = (dx_origin_local * iScaleX * sinA_for_transform + dy_origin_local * iScaleY * cosA_for_transform);

            finalX = iX + world_offsetX;
            finalY = iY + world_offsetY;
        }

        // Non usiamo controller.getCommandManager().executeCommand() qui perché deve essere un semplice feedback visivo, il command viene richiamato in mouseReleasedHandler
        controller.getModel().moveShapeTo(shapeToUpdate, finalX, finalY);
        controller.getModel().setShapeWidth(shapeToUpdate, finalW);
        controller.getModel().setShapeHeight(shapeToUpdate, finalH);

        controller.updateSpinners(shapeToUpdate);
    }

    @Override
    protected void postProcess(MouseEvent event) {
        controller.redrawCanvas();
    }
}