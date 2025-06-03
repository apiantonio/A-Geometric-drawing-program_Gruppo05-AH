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

        double iX = controller.getInitialShapeX_world_resize();
        double iY = controller.getInitialShapeY_world_resize();
        double iW = controller.getInitialShapeWidth_world_resize();
        double iH = controller.getInitialShapeHeight_world_resize();
        double iAngle = controller.getInitialShapeAngle_resize();
        int iScaleX = controller.getInitialShapeScaleX_resize();
        int iScaleY = controller.getInitialShapeScaleY_resize();

        Point2D startMouseWorld = zoomHandler.screenToWorld(controller.getResizeStartMousePos_screen().getX(), controller.getResizeStartMousePos_screen().getY());
        Point2D currentMouseWorld = zoomHandler.screenToWorld(currentScreenMousePos.getX(), currentScreenMousePos.getY());

        Point2D initialMouseLocal = shapeToUpdate.inverseTransformPoint(startMouseWorld.getX(), startMouseWorld.getY());
        Point2D currentMouseLocal = shapeToUpdate.inverseTransformPoint(currentMouseWorld.getX(), currentMouseWorld.getY());

        double localMouseDeltaX = currentMouseLocal.getX() - initialMouseLocal.getX();
        double localMouseDeltaY = currentMouseLocal.getY() - initialMouseLocal.getY();

        double finalX_calculated;
        double finalY_calculated;
        double finalW_calculated;
        double finalH_calculated;

        final double MIN_SIZE = 5.0;
        final double MAX_DIMENSION_ALLOWED = 1000.0;

        if (baseShapeToAnalyze instanceof Line) {
            // Logic for Line shapes (preserved from your original implementation)
            final double MAX_LINE_LENGTH_ALLOWED = 1000.0;
            Point2D initialStartPoint_world = new Point2D(iX, iY);
            Point2D initialEndPoint_world = new Point2D(iX + iW, iY + iH);
            double lineUnitVecX = 0, lineUnitVecY = 0;
            double initialLength = Math.sqrt(iW * iW + iH * iH);
            if (initialLength > 1e-6) {
                lineUnitVecX = iW / initialLength;
                lineUnitVecY = iH / initialLength;
            } else {
                lineUnitVecX = Math.cos(Math.toRadians(iAngle));
                lineUnitVecY = Math.sin(Math.toRadians(iAngle));
                if (Math.sqrt(lineUnitVecX * lineUnitVecX + lineUnitVecY * lineUnitVecY) < 1e-6) lineUnitVecX = 1.0;
            }
            double dragMagnitudeAlongLineAxis = localMouseDeltaX * lineUnitVecX + localMouseDeltaY * lineUnitVecY;
            double deltaLength = dragMagnitudeAlongLineAxis;
            double tempFinalX = iX, tempFinalY = iY, tempFinalW = iW, tempFinalH = iH;
            if (handleType == HandleType.LINE_START) {
                tempFinalX = initialEndPoint_world.getX() - (iW - deltaLength * lineUnitVecX);
                tempFinalY = initialEndPoint_world.getY() - (iH - deltaLength * lineUnitVecY);
                tempFinalW = initialEndPoint_world.getX() - tempFinalX;
                tempFinalH = initialEndPoint_world.getY() - tempFinalY;
                double currentActualLength = Math.sqrt(tempFinalW * tempFinalW + tempFinalH * tempFinalH);
                if (currentActualLength < MIN_SIZE) {
                    if (currentActualLength > 1e-6) {
                        double ratio = MIN_SIZE / currentActualLength;
                        tempFinalW *= ratio;
                        tempFinalH *= ratio;
                    } else {
                        tempFinalW = MIN_SIZE * lineUnitVecX;
                        tempFinalH = MIN_SIZE * lineUnitVecY;
                    }
                    tempFinalX = initialEndPoint_world.getX() - tempFinalW;
                    tempFinalY = initialEndPoint_world.getY() - tempFinalH;
                }
                if (currentActualLength > MAX_LINE_LENGTH_ALLOWED) {
                    double ratio = MAX_LINE_LENGTH_ALLOWED / currentActualLength;
                    tempFinalW *= ratio;
                    tempFinalH *= ratio;
                    tempFinalX = initialEndPoint_world.getX() - tempFinalW;
                    tempFinalY = initialEndPoint_world.getY() - tempFinalH;
                }
            } else if (handleType == HandleType.LINE_END) {
                tempFinalX = iX;
                tempFinalY = iY;
                tempFinalW = iW + deltaLength * lineUnitVecX;
                tempFinalH = iH + deltaLength * lineUnitVecY;
                double currentActualLength = Math.sqrt(tempFinalW * tempFinalW + tempFinalH * tempFinalH);
                if (currentActualLength < MIN_SIZE) {
                    if (currentActualLength > 1e-6) {
                        double ratio = MIN_SIZE / currentActualLength;
                        tempFinalW *= ratio;
                        tempFinalH *= ratio;
                    } else {
                        tempFinalW = MIN_SIZE * lineUnitVecX;
                        tempFinalH = MIN_SIZE * lineUnitVecY;
                    }
                }
                if (currentActualLength > MAX_LINE_LENGTH_ALLOWED) {
                    double ratio = MAX_LINE_LENGTH_ALLOWED / currentActualLength;
                    tempFinalW *= ratio;
                    tempFinalH *= ratio;
                }
            } else {
                return;
            }
            finalX_calculated = tempFinalX;
            finalY_calculated = tempFinalY;
            finalW_calculated = tempFinalW;
            finalH_calculated = tempFinalH;

        } else if (baseShapeToAnalyze instanceof TextShape) {
            // --- BEGINNING OF ORIGINAL LOGIC FOR TextShape (MODIFIED TO ALLOW COMPRESSION) ---
            double effDeltaX = localMouseDeltaX * iScaleX; //
            double effDeltaY = localMouseDeltaY * iScaleY; //

            double tentativeW = iW;
            double tentativeH = iH;
            double tentative_dx_local_origin = 0.0; //
            double tentative_dy_local_origin = 0.0; //

            switch (handleType) { //
                case TOP_LEFT:
                    tentativeW = iW - effDeltaX;
                    tentativeH = iH - effDeltaY;
                    tentative_dx_local_origin = effDeltaX;
                    tentative_dy_local_origin = effDeltaY;
                    break;
                case TOP_RIGHT:
                    tentativeW = iW + effDeltaX;
                    tentativeH = iH - effDeltaY;
                    tentative_dx_local_origin = 0;
                    tentative_dy_local_origin = effDeltaY;
                    break;
                case BOTTOM_LEFT:
                    tentativeW = iW - effDeltaX;
                    tentativeH = iH + effDeltaY;
                    tentative_dx_local_origin = effDeltaX;
                    tentative_dy_local_origin = 0;
                    break;
                case BOTTOM_RIGHT:
                    tentativeW = iW + effDeltaX;
                    tentativeH = iH + effDeltaY;
                    tentative_dx_local_origin = 0;
                    tentative_dy_local_origin = 0;
                    break;
                case TOP_CENTER:
                    tentativeW = iW;
                    tentativeH = iH - effDeltaY;
                    tentative_dx_local_origin = 0;
                    tentative_dy_local_origin = effDeltaY;
                    break;
                case BOTTOM_CENTER:
                    tentativeW = iW;
                    tentativeH = iH + effDeltaY;
                    tentative_dx_local_origin = 0;
                    tentative_dy_local_origin = 0;
                    break;
                case LEFT_CENTER:
                    tentativeW = iW - effDeltaX;
                    tentativeH = iH;
                    tentative_dx_local_origin = effDeltaX;
                    tentative_dy_local_origin = 0;
                    break;
                case RIGHT_CENTER:
                    tentativeW = iW + effDeltaX;
                    tentativeH = iH;
                    tentative_dx_local_origin = 0;
                    tentative_dy_local_origin = 0;
                    break;
                default:
                    return;
            }

            double clampedTentativeW = Math.max(MIN_SIZE, Math.min(tentativeW, MAX_DIMENSION_ALLOWED)); //
            double clampedTentativeH = Math.max(MIN_SIZE, Math.min(tentativeH, MAX_DIMENSION_ALLOWED)); //

            double dx_adjustment_from_clamping = 0; //
            if (tentativeW > clampedTentativeW) {
                if (handleType == HandleType.TOP_LEFT || handleType == HandleType.BOTTOM_LEFT || handleType == HandleType.LEFT_CENTER ||
                        (iScaleX == -1 && (handleType == HandleType.TOP_RIGHT || handleType == HandleType.BOTTOM_RIGHT || handleType == HandleType.RIGHT_CENTER))) { //
                    dx_adjustment_from_clamping = (tentativeW - clampedTentativeW); //
                }
            } else if (tentativeW < clampedTentativeW) {
                if (handleType == HandleType.TOP_LEFT || handleType == HandleType.BOTTOM_LEFT || handleType == HandleType.LEFT_CENTER ||
                        (iScaleX == -1 && (handleType == HandleType.TOP_RIGHT || handleType == HandleType.BOTTOM_RIGHT || handleType == HandleType.RIGHT_CENTER))) { //
                    dx_adjustment_from_clamping = -(clampedTentativeW - tentativeW); //
                }
            }

            double dy_adjustment_from_clamping = 0; //
            if (tentativeH > clampedTentativeH) {
                if (handleType == HandleType.TOP_LEFT || handleType == HandleType.TOP_RIGHT || handleType == HandleType.TOP_CENTER ||
                        (iScaleY == -1 && (handleType == HandleType.BOTTOM_LEFT || handleType == HandleType.BOTTOM_RIGHT || handleType == HandleType.BOTTOM_CENTER))) { //
                    dy_adjustment_from_clamping = (tentativeH - clampedTentativeH); //
                }
            } else if (tentativeH < clampedTentativeH) {
                if (handleType == HandleType.TOP_LEFT || handleType == HandleType.TOP_RIGHT || handleType == HandleType.TOP_CENTER ||
                        (iScaleY == -1 && (handleType == HandleType.BOTTOM_LEFT || handleType == HandleType.BOTTOM_RIGHT || handleType == HandleType.BOTTOM_CENTER))) { //
                    dy_adjustment_from_clamping = -(clampedTentativeH - tentativeH); //
                }
            }

            double dx_local_origin = tentative_dx_local_origin + dx_adjustment_from_clamping; //
            double dy_local_origin = tentative_dy_local_origin + dy_adjustment_from_clamping; //

            // Dimensions are now clamped, but not yet forced by text natural size
            finalW_calculated = clampedTentativeW;
            finalH_calculated = clampedTentativeH;

            TextShape textShape = (TextShape) baseShapeToAnalyze;
            Point2D naturalTextDims = textShape.getNaturalTextBlockDimensions(finalW_calculated); //
            double minTextW = naturalTextDims.getX();
            double minTextH = naturalTextDims.getY();

            double textWidthCorrection = 0;
            if (finalW_calculated < minTextW) {
                textWidthCorrection = minTextW - finalW_calculated;
                // NON forzare finalW_calculated = minTextW; se si vuole permettere compressione
            }
            double textHeightCorrection = 0;
            if (finalH_calculated < minTextH) {
                textHeightCorrection = minTextH - finalH_calculated;
                // NON forzare finalH_calculated = minTextH; se si vuole permettere compressione
            }

            // Gli aggiustamenti a dx_local_origin e dy_local_origin basati su textWidthCorrection
            // e textHeightCorrection vengono mantenuti come nella logica originale.
            // Questi aggiustamenti ora rifletteranno la compressione o l'espansione
            // rispetto alle dimensioni naturali del testo.
            if (textWidthCorrection != 0) { // Modificato per != 0 dato che ora può essere < 0 se si espande oltre il nativo dopo compressione
                if (handleType == HandleType.TOP_LEFT || handleType == HandleType.BOTTOM_LEFT || handleType == HandleType.LEFT_CENTER ||
                        (iScaleX == -1 && (handleType == HandleType.TOP_RIGHT || handleType == HandleType.BOTTOM_RIGHT || handleType == HandleType.RIGHT_CENTER))) { //
                    dx_local_origin -= textWidthCorrection; //
                }
            }
            if (textHeightCorrection != 0) { // Modificato per != 0
                if (handleType == HandleType.TOP_LEFT || handleType == HandleType.TOP_RIGHT || handleType == HandleType.TOP_CENTER ||
                        (iScaleY == -1 && (handleType == HandleType.BOTTOM_LEFT || handleType == HandleType.BOTTOM_RIGHT || handleType == HandleType.BOTTOM_CENTER))) { //
                    dy_local_origin -= textHeightCorrection; //
                }
            }

            double angleRad = Math.toRadians(iAngle); //
            double cosA = Math.cos(angleRad); //
            double sinA = Math.sin(angleRad); //

            double world_offsetX_shift = (dx_local_origin * cosA - dy_local_origin * sinA); //
            double world_offsetY_shift = (dx_local_origin * sinA + dy_local_origin * cosA); //

            finalX_calculated = iX + world_offsetX_shift; //
            finalY_calculated = iY + world_offsetY_shift; //
            // --- END OF ORIGINAL LOGIC FOR TextShape (MODIFIED) ---

        } else { // For other non-Line, non-Text shapes (Rectangles, Ellipses, Polygons)
            // --- Use the latest revised logic (center-based anchoring with mirroring fix) ---
            double effDeltaX = localMouseDeltaX * iScaleX;
            double effDeltaY = localMouseDeltaY * iScaleY;

            double tentativeW = iW;
            double tentativeH = iH;

            switch (handleType) {
                case TOP_LEFT:      tentativeW = iW - effDeltaX; tentativeH = iH - effDeltaY; break;
                case TOP_RIGHT:     tentativeW = iW + effDeltaX; tentativeH = iH - effDeltaY; break;
                case BOTTOM_LEFT:   tentativeW = iW - effDeltaX; tentativeH = iH + effDeltaY; break;
                case BOTTOM_RIGHT:  tentativeW = iW + effDeltaX; tentativeH = iH + effDeltaY; break;
                case TOP_CENTER:    tentativeW = iW; tentativeH = iH - effDeltaY; break;
                case BOTTOM_CENTER: tentativeW = iW; tentativeH = iH + effDeltaY; break;
                case LEFT_CENTER:   tentativeW = iW - effDeltaX; tentativeH = iH; break;
                case RIGHT_CENTER:  tentativeW = iW + effDeltaX; tentativeH = iH; break;
                default: return;
            }

            finalW_calculated = Math.max(MIN_SIZE, Math.min(tentativeW, MAX_DIMENSION_ALLOWED));
            finalH_calculated = Math.max(MIN_SIZE, Math.min(tentativeH, MAX_DIMENSION_ALLOWED));

            double dw = finalW_calculated - iW;
            double dh = finalH_calculated - iH;

            double base_dv_center_x_local = 0;
            double base_dv_center_y_local = 0;

            switch (handleType) {
                case TOP_LEFT:      base_dv_center_x_local = -dw / 2.0; base_dv_center_y_local = -dh / 2.0; break;
                case TOP_RIGHT:     base_dv_center_x_local =  dw / 2.0; base_dv_center_y_local = -dh / 2.0; break;
                case BOTTOM_LEFT:   base_dv_center_x_local = -dw / 2.0; base_dv_center_y_local =  dh / 2.0; break;
                case BOTTOM_RIGHT:  base_dv_center_x_local =  dw / 2.0; base_dv_center_y_local =  dh / 2.0; break;
                case TOP_CENTER:    base_dv_center_x_local =  0;        base_dv_center_y_local = -dh / 2.0; break;
                case BOTTOM_CENTER: base_dv_center_x_local =  0;        base_dv_center_y_local =  dh / 2.0; break;
                case LEFT_CENTER:   base_dv_center_x_local = -dw / 2.0; base_dv_center_y_local =  0;        break;
                case RIGHT_CENTER:  base_dv_center_x_local =  dw / 2.0; base_dv_center_y_local =  0;        break;
            }

            double dv_center_x_local = base_dv_center_x_local * iScaleX;
            double dv_center_y_local = base_dv_center_y_local * iScaleY;

            double angleRad = Math.toRadians(iAngle);
            double cosA = Math.cos(angleRad);
            double sinA = Math.sin(angleRad);

            double delta_world_center_x = dv_center_x_local * cosA - dv_center_y_local * sinA;
            double delta_world_center_y = dv_center_x_local * sinA + dv_center_y_local * cosA;

            double initial_world_center_x = iX + iW / 2.0;
            double initial_world_center_y = iY + iH / 2.0;

            double final_world_center_x = initial_world_center_x + delta_world_center_x;
            double final_world_center_y = initial_world_center_y + delta_world_center_y;

            finalX_calculated = final_world_center_x - finalW_calculated / 2.0;
            finalY_calculated = final_world_center_y - finalH_calculated / 2.0;
        }

        controller.getModel().moveShapeTo(shapeToUpdate, finalX_calculated, finalY_calculated);
        controller.getModel().setShapeWidth(shapeToUpdate, finalW_calculated);
        controller.getModel().setShapeHeight(shapeToUpdate, finalH_calculated);

        controller.updateSpinners(shapeToUpdate);
    }


    @Override
    protected void postProcess(MouseEvent event) {
        controller.redrawCanvas();
    }
}