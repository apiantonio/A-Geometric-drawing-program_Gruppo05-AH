package com.geometricdrawing.mousehandler;

import com.geometricdrawing.controller.DrawingController;
import com.geometricdrawing.controller.HandleType;
import com.geometricdrawing.controller.ZoomHandler;
import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Line;
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

        double finalX;
        double finalY;
        double finalW;
        double finalH;

        final double MIN_SIZE = 1.0;
        final double MAX_DIMENSION_ALLOWED = 1000.0;

        if (baseShapeToAnalyze instanceof Line) {
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
            // serve per capire quanto stiamo trascinando lungo la linea
            double dragMagnitudeAlongLineAxis = localMouseDeltaX * lineUnitVecX + localMouseDeltaY * lineUnitVecY;

            HandleType effectiveHandle = handleType;
            // se la linea è stata specchiata orizzontalmente, devi invertire gli handle da applicare
            if (iScaleX == -1) {
                if (handleType == HandleType.LINE_START) effectiveHandle = HandleType.LINE_END;
                else if (handleType == HandleType.LINE_END) effectiveHandle = HandleType.LINE_START;
            }
            if (effectiveHandle == HandleType.LINE_START) {
                // Mantiene fisso il punto finale e muove solo il punto iniziale
                double newLength = initialLength - dragMagnitudeAlongLineAxis;

                // Applica i limiti di dimensione
                newLength = Math.max(MIN_SIZE, Math.min(newLength, MAX_LINE_LENGTH_ALLOWED));

                // Calcola le nuove coordinate del punto iniziale
                finalX = initialEndPoint_world.getX() - newLength * lineUnitVecX;
                finalY = initialEndPoint_world.getY() - newLength * lineUnitVecY;
                finalW = initialEndPoint_world.getX() - finalX;
                finalH = initialEndPoint_world.getY() - finalY;

            } else if (effectiveHandle == HandleType.LINE_END) {
                // Mantiene fisso il punto iniziale e muove solo il punto finale
                double newLength = initialLength + dragMagnitudeAlongLineAxis;

                // Applica i limiti di dimensione
                newLength = Math.max(MIN_SIZE, Math.min(newLength, MAX_LINE_LENGTH_ALLOWED));

                // Il punto iniziale rimane fisso
                finalX = initialStartPoint_world.getX();
                finalY = initialStartPoint_world.getY();
                finalW = newLength * lineUnitVecX;
                finalH = newLength * lineUnitVecY;
            } else {
                return;
            }
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

            finalW = Math.max(MIN_SIZE, Math.min(tentativeW, MAX_DIMENSION_ALLOWED));
            finalH = Math.max(MIN_SIZE, Math.min(tentativeH, MAX_DIMENSION_ALLOWED));

            double dw = finalW - iW;
            double dh = finalH - iH;

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

            finalX = final_world_center_x - finalW / 2.0;
            finalY = final_world_center_y - finalH / 2.0;
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