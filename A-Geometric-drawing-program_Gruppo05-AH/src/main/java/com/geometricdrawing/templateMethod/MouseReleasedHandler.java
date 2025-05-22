package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public class MouseReleasedHandler extends AbstractMouseHandler{
    public MouseReleasedHandler(Canvas canvas, DrawingController controller) {
        super(canvas, controller);
    }

    @Override
    protected void preProcess(MouseEvent event) {
        // Nessun preprocessing necessario
        return;
    }

    @Override
    protected void processEvent(MouseEvent event) {
        canvas.setCursor(Cursor.DEFAULT);
    }
}
