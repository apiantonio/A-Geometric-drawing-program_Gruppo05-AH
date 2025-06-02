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

        // Convertiamo le coordinate dello schermo in coordinate del mondo
        double worldDeltaXUnrotated = screenDeltaX / zoomHandler.getZoomFactor();
        double worldDeltaYUnrotated = screenDeltaY / zoomHandler.getZoomFactor();

        double angleRad_inv = Math.toRadians(-iAngle);
        double cosA_inv = Math.cos(angleRad_inv);
        double sinA_inv = Math.sin(angleRad_inv);

        double localDeltaX = (worldDeltaXUnrotated * cosA_inv - worldDeltaYUnrotated * sinA_inv) / iScaleX;
        double localDeltaY = (worldDeltaXUnrotated * sinA_inv + worldDeltaYUnrotated * cosA_inv) / iScaleY;

        double newX = iX;
        double newY = iY;
        double newW = iW;
        double newH = iH;
        final double MIN_SIZE = 5.0; // Esistente: dimensione minima assoluta

        // Gestione specifica per le LINEE
        if (baseShapeToAnalyze instanceof Line) {
            Point2D initialStartPoint_world = new Point2D(iX, iY);
            Point2D initialEndPoint_world = new Point2D(iX + iW, iY + iH);

            double lineVecInitialX = iW;
            double lineVecInitialY = iH;
            double lineInitialLength = Math.sqrt(lineVecInitialX * lineVecInitialX + lineVecInitialY * lineVecInitialY);
            System.out.printf("  [DEBUG] Vettore Linea Iniziale: vecX=%.2f, vecY=%.2f, lunghezza=%.2f%n", lineVecInitialX, lineVecInitialY, lineInitialLength);

            double lineUnitVecX = 0, lineUnitVecY = 0;
            if (lineInitialLength > 1e-6) {
                lineUnitVecX = lineVecInitialX / lineInitialLength;
                lineUnitVecY = lineVecInitialY / lineInitialLength;
            } else {
                lineUnitVecX = Math.cos(Math.toRadians(iAngle));
                lineUnitVecY = Math.sin(Math.toRadians(iAngle));
                if (Math.sqrt(lineUnitVecX * lineUnitVecX + lineUnitVecY * lineUnitVecY) < 1e-6) {
                    lineUnitVecX = 1.0;
                    lineUnitVecY = 0.0;
                }
                System.out.println("  [DEBUG] Linea con lunghezza zero, usato fallback per versore direzione.");
            }
            System.out.printf("  [DEBUG] Versore Direzione Linea: uX=%.2f, uY=%.2f%n", lineUnitVecX, lineUnitVecY);

            double dragMagnitudeAlongLineAxis = worldDeltaXUnrotated * lineUnitVecX + worldDeltaYUnrotated * lineUnitVecY;
            System.out.printf("  [DEBUG] Magnitudo Drag Proiettata su Asse Linea: %.2f%n", dragMagnitudeAlongLineAxis);

            if (iAngle == 0) {
                if (Math.abs(lineUnitVecX) > Math.abs(lineUnitVecY)) {
                    if (iScaleX != 0) dragMagnitudeAlongLineAxis /= iScaleX;
                } else if (Math.abs(lineUnitVecY) > 0) {
                    if (iScaleY != 0) dragMagnitudeAlongLineAxis /= iScaleY;
                }
            }
            System.out.printf("  [DEBUG] Magnitudo Drag (dopo tentativo correzione mirroring): %.2f%n", dragMagnitudeAlongLineAxis);


            double stretchVecWorldX = dragMagnitudeAlongLineAxis * lineUnitVecX;
            double stretchVecWorldY = dragMagnitudeAlongLineAxis * lineUnitVecY;
            System.out.printf("  [DEBUG] Vettore Stiramento Mondo: sX=%.2f, sY=%.2f%n", stretchVecWorldX, stretchVecWorldY);

            double newX_line = iX;
            double newY_line = iY;
            double newW_line = iW;
            double newH_line = iH;

            if (handleType == HandleType.LINE_START) {
                newX = initialStartPoint_world.getX() + stretchVecWorldX;
                newY = initialStartPoint_world.getY() + stretchVecWorldY;
                newW = initialEndPoint_world.getX() - newX;
                newH = initialEndPoint_world.getY() - newY;
            } else if (handleType == HandleType.LINE_END) {
                newX = initialStartPoint_world.getX();
                newY = initialStartPoint_world.getY();
                double newEndX = initialEndPoint_world.getX() + stretchVecWorldX;
                double newEndY = initialEndPoint_world.getY() + stretchVecWorldY;
                newW = newEndX - newX;
                newH = newEndY - newY;
            } else {
                System.out.println("  [DEBUG] HandleType non valido per Linea: " + handleType);
                return;
            }
            System.out.printf("  [DEBUG] Calcolato Pre-MIN_SIZE: nX=%.2f, nY=%.2f, nW=%.2f, nH=%.2f%n", newX, newY, newW, newH);

            double currentLength = Math.sqrt(newW * newW + newH * newH);
            System.out.printf("  [DEBUG] Lunghezza Calcolata: %.2f, MIN_SIZE: %.2f%n", currentLength, MIN_SIZE);

            if (currentLength < MIN_SIZE) {
                System.out.println("  [DEBUG] Lunghezza calcolata < MIN_SIZE.");
                if (currentLength > 1e-6) {
                    double scaleFactorToMin = MIN_SIZE / currentLength;
                    System.out.printf("    [DEBUG] scaleFactorToMin: %.2f%n", scaleFactorToMin);
                    if (handleType == HandleType.LINE_START) {
                        double vecX_E_to_nS = newX - initialEndPoint_world.getX();
                        double vecY_E_to_nS = newY - initialEndPoint_world.getY();
                        vecX_E_to_nS *= scaleFactorToMin;
                        vecY_E_to_nS *= scaleFactorToMin;
                        newX = initialEndPoint_world.getX() + vecX_E_to_nS;
                        newY = initialEndPoint_world.getY() + vecY_E_to_nS;
                        newW = initialEndPoint_world.getX() - newX;
                        newH = initialEndPoint_world.getY() - newY;
                    } else { // HandleType.LINE_END
                        newW *= scaleFactorToMin;
                        newH *= scaleFactorToMin;
                    }
                    System.out.printf("    [DEBUG] Corretto per MIN_SIZE: nX=%.2f, nY=%.2f, nW=%.2f, nH=%.2f%n", newX, newY, newW, newH);
                } else if (MIN_SIZE > 0) {
                    System.out.println("    [DEBUG] Linea collassata, nessun aggiornamento.");
                    return;
                }
            }

            System.out.printf("  [DEBUG] Applicando a Linea: finalX=%.2f, finalY=%.2f, finalW=%.2f, finalH=%.2f%n", newX, newY, newW, newH);
            controller.getModel().moveShapeTo(shapeToUpdate, newX, newY);
            shapeToUpdate.setWidth(newW);
            shapeToUpdate.setHeight(newH);
            System.out.printf("  [DEBUG] Stato Linea Dopo Aggiornamento: x=%.2f, y=%.2f, w=%.2f, h=%.2f%n", shapeToUpdate.getX(), shapeToUpdate.getY(), shapeToUpdate.getWidth(), shapeToUpdate.getHeight());

            controller.updateSpinners(shapeToUpdate);
            System.out.println("--- STRETCH LINEA FINE ---");
            return;
        }
        // FINE gestione specifica per LINEE

        // Inizializza le dimensioni proposte con quelle iniziali
        double tentativeW = iW;
        double tentativeH = iH;

        // Calcola le dimensioni tentative in base al drag del mouse e all'handle
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
                break;
            case BOTTOM_CENTER:
                tentativeH = iH + localDeltaY;
                break;
            case LEFT_CENTER:
                tentativeW = iW - localDeltaX;
                break;
            case RIGHT_CENTER:
                tentativeW = iW + localDeltaX;
                break;
        }

        // Gestione del testo
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

        double finalW = Math.max(MIN_SIZE, tentativeW);
        double finalH = Math.max(MIN_SIZE, tentativeH);

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
                break;
            case TOP_CENTER:
                dy_origin_local = iH - finalH;
                break;
            case BOTTOM_CENTER:
                break;
            case LEFT_CENTER:
                dx_origin_local = iW - finalW;
                break;
            case RIGHT_CENTER:
                break;
            default:
                System.err.println("Tipo di maniglia di ridimensionamento non gestito: " + handleType);
                return;
        }

        double angleRad_for_transform = Math.toRadians(iAngle);
        double cosA_for_transform = Math.cos(angleRad_for_transform);
        double sinA_for_transform = Math.sin(angleRad_for_transform);

        double world_offsetX = (dx_origin_local * iScaleX * cosA_for_transform - dy_origin_local * iScaleY * sinA_for_transform);
        double world_offsetY = (dx_origin_local * iScaleX * sinA_for_transform + dy_origin_local * iScaleY * cosA_for_transform);

        double finalX = iX + world_offsetX;
        double finalY = iY + world_offsetY;

        // Applica posizione e dimensioni finali
        controller.getModel().moveShapeTo(shapeToUpdate, finalX, finalY);
        shapeToUpdate.setWidth(finalW);
        shapeToUpdate.setHeight(finalH);

        controller.updateSpinners(shapeToUpdate);
    }

    @Override
    protected void postProcess(MouseEvent event) {
        controller.redrawCanvas();
    }
}