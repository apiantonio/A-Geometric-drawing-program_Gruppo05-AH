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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class MouseDraggedHandler extends AbstractMouseHandler {

    public MouseDraggedHandler(Canvas canvas, DrawingController controller) {
        super(canvas, controller);
    }

    @Override
    protected void preProcess(MouseEvent event) {
        currentShape = controller.getCurrentShape();

        if (controller.getActiveResizeHandle() != null && controller.getShapeBeingResized() != null) {
            canvas.setCursor(controller.getCursorForHandle(controller.getActiveResizeHandle(), controller.getShapeBeingResized()));
        } else if (currentShape != null && event.getButton() == MouseButton.PRIMARY) {
            canvas.setCursor(Cursor.CLOSED_HAND);
        }
    }

    @Override
    protected void processEvent(MouseEvent event) {
        HandleType activeHandle = controller.getActiveResizeHandle();
        AbstractShape shapeToUpdate = controller.getShapeBeingResized();
        AbstractShape shapeToDragEntirely = controller.getCurrentShape();

        if (activeHandle != null && shapeToUpdate != null) {
            handleShapeResize(event, activeHandle, shapeToUpdate);
            return;
        }
        if (shapeToDragEntirely != null && event.getButton() == MouseButton.PRIMARY) {
            handleShapeDrag(event, shapeToDragEntirely);
        }
    }

    private void handleShapeDrag(MouseEvent event, AbstractShape shapeToDragEntirely) {
        if (!controller.isDragging()) {
            controller.setStartDragX(event.getX());
            controller.setStartDragY(event.getY());
        }

        ZoomHandler zoomHandler = controller.getZoomHandler();
        if (zoomHandler == null || controller.getHorizontalScrollBar() == null || controller.getVerticalScrollBar() == null) {
            return;
        }

        Point2D worldMouseCoords = zoomHandler.screenToWorld(event.getX(), event.getY());
        double newWorldX = worldMouseCoords.getX() - controller.getDragOffsetX();
        double newWorldY = worldMouseCoords.getY() - controller.getDragOffsetY();

        double currentShapeWidth = shapeToDragEntirely.getWidth();
        double currentShapeHeight = shapeToDragEntirely.getHeight();
        double worldCanvasWidth = canvas.getWidth() / zoomHandler.getZoomFactor();
        double worldCanvasHeight = canvas.getHeight() / zoomHandler.getZoomFactor();
        double currentScrollX = controller.getHorizontalScrollBar().getValue();
        double currentScrollY = controller.getVerticalScrollBar().getValue();
        double effectiveBorderMargin = AbstractMouseHandler.BORDER_MARGIN / zoomHandler.getZoomFactor();

        double minClampedX = currentScrollX + effectiveBorderMargin - currentShapeWidth * AbstractMouseHandler.VISIBLE_SHAPE_PORTION;
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

        Point2D startMouseWorld = zoomHandler.screenToWorld(startScreenMousePos.getX(), startScreenMousePos.getY());
        Point2D currentMouseWorld = zoomHandler.screenToWorld(currentScreenMousePos.getX(), currentScreenMousePos.getY());

        Point2D initialMouseLocal = shapeToUpdate.inverseTransformPoint(startMouseWorld.getX(), startMouseWorld.getY());
        Point2D currentMouseLocal = shapeToUpdate.inverseTransformPoint(currentMouseWorld.getX(), currentMouseWorld.getY());

        // Delta del mouse nello spazio locale originale (non specchiato, non ruotato) della forma
        double localMouseDeltaX = currentMouseLocal.getX() - initialMouseLocal.getX();
        double localMouseDeltaY = currentMouseLocal.getY() - initialMouseLocal.getY();

        double finalX = iX;
        double finalY = iY;
        double finalW = iW;
        double finalH = iH;

        final double MIN_SIZE = 5.0;
        final double MAX_DIMENSION_ALLOWED = 1000.0;

        if (baseShapeToAnalyze instanceof Line) {
            // Logica per le linee (mantenuta invariata, si presume corretta per il problema specifico)
            final double MAX_LINE_LENGTH_ALLOWED = 1000.0;
            Point2D initialStartPoint_world = new Point2D(iX, iY);
            Point2D initialEndPoint_world = new Point2D(iX + iW, iY + iH);
            double lineUnitVecX = 0, lineUnitVecY = 0;
            double initialLength = Math.sqrt(iW * iW + iH * iH);
            if (initialLength > 1e-6) {
                lineUnitVecX = iW / initialLength; lineUnitVecY = iH / initialLength;
            } else {
                lineUnitVecX = Math.cos(Math.toRadians(iAngle)); lineUnitVecY = Math.sin(Math.toRadians(iAngle));
                if (Math.sqrt(lineUnitVecX * lineUnitVecX + lineUnitVecY * lineUnitVecY) < 1e-6) lineUnitVecX = 1.0;
            }
            double dragMagnitudeAlongLineAxis = localMouseDeltaX * lineUnitVecX + localMouseDeltaY * lineUnitVecY;
            double deltaLength = dragMagnitudeAlongLineAxis;
            double tempFinalX = iX, tempFinalY = iY, tempFinalW = iW, tempFinalH = iH;
            if (handleType == HandleType.LINE_START) {
                tempFinalX = initialEndPoint_world.getX() - (iW - deltaLength * lineUnitVecX);
                tempFinalY = initialEndPoint_world.getY() - (iH - deltaLength * lineUnitVecY);
                tempFinalW = initialEndPoint_world.getX() - tempFinalX; tempFinalH = initialEndPoint_world.getY() - tempFinalY;
                double currentActualLength = Math.sqrt(tempFinalW * tempFinalW + tempFinalH * tempFinalH);
                if (currentActualLength < MIN_SIZE) {
                    double ratio = MIN_SIZE / currentActualLength; tempFinalW *= ratio; tempFinalH *= ratio;
                    tempFinalX = initialEndPoint_world.getX() - tempFinalW; tempFinalY = initialEndPoint_world.getY() - tempFinalH;
                }
                if (currentActualLength > MAX_LINE_LENGTH_ALLOWED) {
                    double ratio = MAX_LINE_LENGTH_ALLOWED / currentActualLength; tempFinalW *= ratio; tempFinalH *= ratio;
                    tempFinalX = initialEndPoint_world.getX() - tempFinalW; tempFinalY = initialEndPoint_world.getY() - tempFinalH;
                }
            } else if (handleType == HandleType.LINE_END) {
                tempFinalX = iX; tempFinalY = iY;
                tempFinalW = iW + deltaLength * lineUnitVecX; tempFinalH = iH + deltaLength * lineUnitVecY;
                double currentActualLength = Math.sqrt(tempFinalW * tempFinalW + tempFinalH * tempFinalH);
                if (currentActualLength < MIN_SIZE) {
                    double ratio = MIN_SIZE / currentActualLength; tempFinalW *= ratio; tempFinalH *= ratio;
                }
                if (currentActualLength > MAX_LINE_LENGTH_ALLOWED) {
                    double ratio = MAX_LINE_LENGTH_ALLOWED / currentActualLength; tempFinalW *= ratio; tempFinalH *= ratio;
                }
            } else { return; }
            finalX = tempFinalX; finalY = tempFinalY; finalW = tempFinalW; finalH = tempFinalH;
        } else { // Forme non-Linea
            // Delta effettivi "visivi" del mouse lungo gli assi della forma
            // Se iScaleX = -1, un localMouseDeltaX positivo (movimento a destra nello spazio originale)
            // diventa un effDeltaX negativo (movimento a sinistra nello spazio visivo).
            double effDeltaX = localMouseDeltaX * iScaleX;
            double effDeltaY = localMouseDeltaY * iScaleY;

            double dW = 0; // Variazione della larghezza matematica
            double dH = 0; // Variazione dell'altezza matematica
            double dx_local_origin = 0.0; // Spostamento dell'origine X matematica
            double dy_local_origin = 0.0; // Spostamento dell'origine Y matematica


            // Calcola dW, dH (cambiamenti nelle dimensioni matematiche)
            // e dx_local_origin, dy_local_origin (spostamenti dell'origine matematica x,y)
            // per mantenere ancorato il lato/angolo VISIVAMENTE opposto.
            switch (handleType) {
                case TOP_LEFT: // Trascina handle visivo Alto-Sinistra, ancora handle visivo Basso-Destra
                    dW = -effDeltaX; // Se trascino visivamente a destra (effDeltaX>0), la larghezza diminuisce
                    dH = -effDeltaY; // Se trascino visivamente in basso (effDeltaY>0), l'altezza diminuisce
                    // Spostamento dell'origine matematica (x,y) per ancorare il BR visivo:
                    // Se non specchiato (iScaleX=1), il BR visivo è il BR matematico (x+W, y+H).
                    //   Per ancorare x+W, x deve spostarsi di -dW. Ma dW è -effDeltaX, quindi x si sposta di effDeltaX.
                    // Se specchiato (iScaleX=-1), il BR visivo è il BL matematico (x, y+H).
                    //   Per ancorare x, x non si sposta. dx_local_origin = 0.
                    dx_local_origin = (iScaleX == 1) ? effDeltaX : 0;
                    dy_local_origin = (iScaleY == 1) ? effDeltaY : 0;
                    break;
                case TOP_RIGHT: // Trascina handle visivo Alto-Destra, ancora handle visivo Basso-Sinistra
                    dW = effDeltaX;  // Se trascino visivamente a destra (effDeltaX>0), la larghezza aumenta
                    dH = -effDeltaY;
                    // Spostamento dell'origine matematica (x,y) per ancorare il BL visivo:
                    // Se non specchiato (iScaleX=1), il BL visivo è il BL matematico (x, y+H). x non si sposta.
                    // Se specchiato (iScaleX=-1), il BL visivo è il BR matematico (x+W, y+H). x si sposta di -dW.
                    dx_local_origin = (iScaleX == 1) ? 0 : -dW;
                    dy_local_origin = (iScaleY == 1) ? effDeltaY : 0;
                    break;
                case BOTTOM_LEFT: // Trascina handle visivo Basso-Sinistra, ancora handle visivo Alto-Destra
                    dW = -effDeltaX;
                    dH = effDeltaY; // Se trascino visivamente in basso (effDeltaY>0), l'altezza aumenta
                    dx_local_origin = (iScaleX == 1) ? effDeltaX : 0;
                    // Spostamento dell'origine matematica (x,y) per ancorare il TR visivo:
                    // Se non specchiato (iScaleY=1), il TR visivo è il TR matematico (x+W, y). y non si sposta.
                    // Se specchiato (iScaleY=-1), il TR visivo è il BR matematico (x+W, y+H). y si sposta di -dH.
                    dy_local_origin = (iScaleY == 1) ? 0 : -dH;
                    break;
                case BOTTOM_RIGHT: // Trascina handle visivo Basso-Destra, ancora handle visivo Alto-Sinistra
                    dW = effDeltaX;
                    dH = effDeltaY;
                    dx_local_origin = (iScaleX == 1) ? 0 : -dW;
                    dy_local_origin = (iScaleY == 1) ? 0 : -dH;
                    break;
                case TOP_CENTER: // Trascina handle visivo Alto-Centro, ancora bordo visivo Basso
                    dW = 0; // La larghezza non cambia
                    dH = -effDeltaY;
                    dx_local_origin = 0; // L'origine X non si sposta
                    dy_local_origin = (iScaleY == 1) ? effDeltaY : 0;
                    break;
                case BOTTOM_CENTER: // Trascina handle visivo Basso-Centro, ancora bordo visivo Alto
                    dW = 0;
                    dH = effDeltaY;
                    dx_local_origin = 0;
                    dy_local_origin = (iScaleY == 1) ? 0 : -dH;
                    break;
                case LEFT_CENTER: // Trascina handle visivo Sinistra-Centro, ancora bordo visivo Destro
                    dW = -effDeltaX;
                    dH = 0; // L'altezza non cambia
                    dx_local_origin = (iScaleX == 1) ? effDeltaX : 0;
                    dy_local_origin = 0; // L'origine Y non si sposta
                    break;
                case RIGHT_CENTER: // Trascina handle visivo Destra-Centro, ancora bordo visivo Sinistro
                    dW = effDeltaX;
                    dH = 0;
                    dx_local_origin = (iScaleX == 1) ? 0 : -dW;
                    dy_local_origin = 0;
                    break;
                default: return;
            }

            double tentativeW = iW + dW;
            double tentativeH = iH + dH;

            // Clamping delle dimensioni
            double clampedTentativeW = Math.max(MIN_SIZE, Math.min(tentativeW, MAX_DIMENSION_ALLOWED));
            double clampedTentativeH = Math.max(MIN_SIZE, Math.min(tentativeH, MAX_DIMENSION_ALLOWED));

            // Aggiustamento dell'origine per compensare il clamping, se necessario, per mantenere l'ancoraggio.
            // widthAdjustment > 0 significa che la larghezza calcolata (tentativeW) è stata ridotta a clampedTentativeW.
            double widthAdjustment = tentativeW - clampedTentativeW;
            double heightAdjustment = tentativeH - clampedTentativeH;


            if (dx_local_origin != 0.0) { // Si applica solo se l'origine X era la parte mobile per l'ancoraggio della larghezza
                if (handleType == HandleType.TOP_LEFT || handleType == HandleType.BOTTOM_LEFT || handleType == HandleType.LEFT_CENTER) {
                    // Per questi handle, dx_local_origin è calcolato per spostare il lato sinistro matematico
                    // Se dW era negativo (es. -effDeltaX per TOP_LEFT), dx_local_origin = effDeltaX (o 0)
                    // Se la larghezza (tentativeW) viene ridotta dal clamp (widthAdjustment > 0),
                    // significa che -effDeltaX (il contributo alla larghezza) era troppo grande.
                    // dx_local_origin deve "recuperare" questo adjustment.
                    if ((iScaleX == 1 && (handleType == HandleType.TOP_LEFT || handleType == HandleType.BOTTOM_LEFT || handleType == HandleType.LEFT_CENTER)) ||
                            (iScaleX == -1 && (handleType == HandleType.TOP_RIGHT || handleType == HandleType.BOTTOM_RIGHT || handleType == HandleType.RIGHT_CENTER)) ) {
                        // Questi sono i casi in cui l'origine X matematica si sposta perché si trascina un handle sinistro visivo
                        dx_local_origin += widthAdjustment;
                    }
                } else if (handleType == HandleType.TOP_RIGHT || handleType == HandleType.BOTTOM_RIGHT || handleType == HandleType.RIGHT_CENTER) {
                    // Per questi handle, l'origine X matematica si sposta se specchiato (dx_local_origin = -dW)
                    // per ancorare il lato destro matematico.
                    if (iScaleX == -1) { // Solo se l'origine X si stava spostando a causa del mirroring e ancoraggio
                        dx_local_origin += widthAdjustment; // Se W è ridotta, -dW diventa meno negativo (si sposta a dx)
                    }
                }
            }

            if (dy_local_origin != 0.0) { // Logica simile per l'altezza
                if ((iScaleY == 1 && (handleType == HandleType.TOP_LEFT || handleType == HandleType.TOP_RIGHT || handleType == HandleType.TOP_CENTER)) ||
                        (iScaleY == -1 && (handleType == HandleType.BOTTOM_LEFT || handleType == HandleType.BOTTOM_RIGHT || handleType == HandleType.BOTTOM_CENTER)) ) {
                    dy_local_origin += heightAdjustment;
                } else if (handleType == HandleType.BOTTOM_LEFT || handleType == HandleType.BOTTOM_RIGHT || handleType == HandleType.BOTTOM_CENTER) {
                    if (iScaleY == -1) {
                        dy_local_origin += heightAdjustment;
                    }
                }
            }

            tentativeW = clampedTentativeW;
            tentativeH = clampedTentativeH;

            // Aggiustamenti per TextShape (mantenuti simili, ma valutare se dx/dy_local_origin sono corretti)
            if (baseShapeToAnalyze instanceof TextShape textShape) {
                Point2D naturalTextDims = textShape.getNaturalTextBlockDimensions(tentativeW);
                double minTextW = naturalTextDims.getX();
                double minTextH = naturalTextDims.getY();

                double textWidthCorrection = 0; // Quanto la larghezza DEVE aumentare per il testo
                if (tentativeW < minTextW) {
                    textWidthCorrection = minTextW - tentativeW;
                    tentativeW = minTextW;
                }
                double textHeightCorrection = 0; // Quanto l'altezza DEVE aumentare per il testo
                if (tentativeH < minTextH) {
                    textHeightCorrection = minTextH - tentativeH;
                    tentativeH = minTextH;
                }

                // Se la dimensione è aumentata a causa del testo, l'origine deve compensare per mantenere l'ancoraggio
                if (dx_local_origin != 0.0 && textWidthCorrection > 0) {
                    // Se l'origine X si sposta e W aumenta, X deve "arretrare" (diminuire se si spostava a dx, aumentare se a sx)
                    // Questo dipende da quale handle ha causato lo spostamento di dx_local_origin
                    if ((iScaleX == 1 && (handleType == HandleType.TOP_LEFT || handleType == HandleType.BOTTOM_LEFT || handleType == HandleType.LEFT_CENTER)) ||
                            (iScaleX == -1 && (handleType == HandleType.TOP_RIGHT || handleType == HandleType.BOTTOM_RIGHT || handleType == HandleType.RIGHT_CENTER)) ) {
                        dx_local_origin -= textWidthCorrection; // Sposta a sx se prima si spostava a dx
                    }
                }
                if (dy_local_origin != 0.0 && textHeightCorrection > 0) {
                    if ((iScaleY == 1 && (handleType == HandleType.TOP_LEFT || handleType == HandleType.TOP_RIGHT || handleType == HandleType.TOP_CENTER)) ||
                            (iScaleY == -1 && (handleType == HandleType.BOTTOM_LEFT || handleType == HandleType.BOTTOM_RIGHT || handleType == HandleType.BOTTOM_CENTER)) ) {
                        dy_local_origin -= textHeightCorrection;
                    }
                }
            }

            finalW = tentativeW;
            finalH = tentativeH;

            // Trasforma gli spostamenti locali dell'origine (dx_local_origin, dy_local_origin) in coordinate del mondo
            double angleRad = Math.toRadians(iAngle);
            double cosA = Math.cos(angleRad);
            double sinA = Math.sin(angleRad);

            // dx_local_origin e dy_local_origin sono già nello spazio locale corretto (non specchiato ma orientato per il mirror)
            // quindi li ruotiamo semplicemente per ottenere lo shift nel mondo.
            double world_offsetX_shift = (dx_local_origin * cosA - dy_local_origin * sinA);
            double world_offsetY_shift = (dx_local_origin * sinA + dy_local_origin * cosA);

            finalX = iX + world_offsetX_shift;
            finalY = iY + world_offsetY_shift;
        }

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