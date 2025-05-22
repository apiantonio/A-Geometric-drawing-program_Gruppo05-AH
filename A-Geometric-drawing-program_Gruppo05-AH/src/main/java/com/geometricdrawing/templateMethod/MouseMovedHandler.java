package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public class MouseMovedHandler extends AbstractMouseHandler {
    public MouseMovedHandler(Canvas canvas, DrawingController controller) {
        super(canvas, controller);
    }

    @Override
    protected void processEvent(MouseEvent event) {
        // TODO
    }
}
