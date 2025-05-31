package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
import com.geometricdrawing.HandleType;
import com.geometricdrawing.ZoomHandler;
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
        } else if (shapeToDragEntirely != null && event.getButton() == MouseButton.PRIMARY) {
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

        // Gestione specifica per le LINEE (rimane invariata)
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

        // Applica vincoli specifici per TextShape
        if (baseShapeToAnalyze instanceof TextShape textShape) {
            double widthForTextWrapCalc = iW; // Larghezza di riferimento per il wrapping
            boolean affectsWidth = false; // L'handle corrente modifica la larghezza?
            boolean affectsHeight = false; // L'handle corrente modifica l'altezza?

            // Determina se l'handle modifica larghezza e/o altezza
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
                // Usa la larghezza (ma non meno di MIN_SIZE) per il calcolo del testo
                widthForTextWrapCalc = Math.max(MIN_SIZE, tentativeW);
            }

            // Ottieni le dimensioni naturali del testo
            Point2D naturalTextDims = textShape.getNaturalTextBlockDimensions(widthForTextWrapCalc);
            double minTextW = naturalTextDims.getX();
            double minTextH = naturalTextDims.getY();

            if (affectsWidth) {
                tentativeW = Math.max(tentativeW, minTextW); // Applica larghezza minima del testo
            }
            if (affectsHeight) {
                // Se la larghezza è stata modificata (e potenzialmente aumentata dal vincolo del testo),
                // ricalcola l'altezza minima del testo con la nuova larghezza, poiché il wrapping potrebbe cambiare.
                if (affectsWidth && Math.abs(tentativeW - widthForTextWrapCalc) > 1e-3) { // Se tentativeW è stata clampata da minTextW
                    Point2D reevalTextDims = textShape.getNaturalTextBlockDimensions(tentativeW);
                    minTextH = reevalTextDims.getY();
                }
                tentativeH = Math.max(tentativeH, minTextH); // Applica altezza minima del testo
            }
        }

        // Applica il MIN_SIZE assoluto e assegna a newW, newH finali
        // (newW e newH sono inizializzate a iW, iH all'inizio del metodo)
        boolean dimensionsChangedByHandle = false;
        switch (handleType) {
            case TOP_LEFT: case TOP_RIGHT: case BOTTOM_LEFT: case BOTTOM_RIGHT:
            case LEFT_CENTER: case RIGHT_CENTER: // Handles che modificano la larghezza
                newW = Math.max(MIN_SIZE, tentativeW);
                dimensionsChangedByHandle = true;
                // Non break, continua a controllare per l'altezza se è un angolo
            case TOP_CENTER: case BOTTOM_CENTER: // Handles che modificano l'altezza (o continuano da sopra)
                if (handleType == HandleType.TOP_CENTER || handleType == HandleType.BOTTOM_CENTER ||
                        handleType == HandleType.TOP_LEFT || handleType == HandleType.TOP_RIGHT ||
                        handleType == HandleType.BOTTOM_LEFT || handleType == HandleType.BOTTOM_RIGHT) {
                    newH = Math.max(MIN_SIZE, tentativeH);
                    dimensionsChangedByHandle = true;
                }
                break;
        }
        // Se l'handle non ha modificato le dimensioni (es. handle non di resize, anche se questo metodo non dovrebbe essere chiamato),
        // newW e newH rimangono iW e iH. Se le ha modificate, ora sono clampate.


        // Calcola la nuova posizione (newX, newY) della forma.
        // Questa logica è quella originale e si basa sui localDeltaX/Y (movimento del mouse non clampato)
        // per determinare lo spostamento dell'origine (x,y) della forma.
        double angleRad = Math.toRadians(iAngle);
        double cosA = Math.cos(angleRad);
        double sinA = Math.sin(angleRad);
        // newX e newY sono già inizializzate a iX, iY

        switch (handleType) {
            case TOP_LEFT:
                double shiftX_tl = (localDeltaX * iScaleX * cosA - localDeltaY * iScaleY * sinA);
                double shiftY_tl = (localDeltaX * iScaleX * sinA + localDeltaY * iScaleY * cosA);
                newX = iX + shiftX_tl;
                newY = iY + shiftY_tl;
                break;
            case TOP_RIGHT:
                double shiftX_tr = (0 - localDeltaY * iScaleY * sinA);
                double shiftY_tr = (0 + localDeltaY * iScaleY * cosA);
                newX = iX + shiftX_tr;
                newY = iY + shiftY_tr;
                break;
            case BOTTOM_LEFT:
                double shiftX_bl = (localDeltaX * iScaleX * cosA - 0);
                double shiftY_bl = (localDeltaX * iScaleX * sinA + 0);
                newX = iX + shiftX_bl;
                newY = iY + shiftY_bl;
                break;
            case BOTTOM_RIGHT:
                // newX, newY rimangono iX, iY (origine non si sposta)
                break;
            case TOP_CENTER:
                double shiftX_tc = (0 - localDeltaY * iScaleY * sinA);
                double shiftY_tc = (0 + localDeltaY * iScaleY * cosA);
                newX = iX + shiftX_tc;
                newY = iY + shiftY_tc;
                break;
            case BOTTOM_CENTER:
                // newX, newY rimangono iX, iY
                break;
            case LEFT_CENTER:
                double shiftX_lc = (localDeltaX * iScaleX * cosA - 0);
                double shiftY_lc = (localDeltaX * iScaleX * sinA + 0);
                newX = iX + shiftX_lc;
                newY = iY + shiftY_lc;
                break;
            case RIGHT_CENTER:
                // newX, newY rimangono iX, iY
                break;
        }

        // Applicazione finale dei valori calcolati (newX, newY) e dimensioni clampate (newW, newH)
        controller.getModel().moveShapeTo(shapeToUpdate, newX, newY);
        shapeToUpdate.setWidth(newW);
        shapeToUpdate.setHeight(newH);

        controller.updateSpinners(shapeToUpdate);
    }

    @Override
    protected void postProcess(MouseEvent event) {
        controller.redrawCanvas();
    }
}