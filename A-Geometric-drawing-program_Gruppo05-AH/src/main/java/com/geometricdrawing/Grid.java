package com.geometricdrawing;
import com.geometricdrawing.DrawingController;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Autore: Gruppo05
 * Scopo: Classe che gestisce la visualizzazione della griglia in diverse grandezze.
 */

public class Grid {
    public static final double SMALL_GRID_SIZE = 10.0;
    public static final double MEDIUM_GRID_SIZE = 20.0;
    public static final double BIG_GRID_SIZE = 50.0;

    private final DrawingController drawingController;
    private boolean gridVisible = false;
    private double currentGridSize = MEDIUM_GRID_SIZE;  // Dimensione di default delle celle della griglia

    public Grid(DrawingController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("DrawingController non può essere null.");
        }
        this.drawingController = controller;
    }

    private void setGridSize(double size) {
        this.currentGridSize = size;
        if (gridVisible) {
            drawGrid();
        }
    }

    public void setGridSizeSmall() {
        setGridSize(SMALL_GRID_SIZE);
    }

    public void setGridSizeMedium() {
        setGridSize(MEDIUM_GRID_SIZE);
    }

    public void setGridSizeBig() {
        setGridSize(BIG_GRID_SIZE);
    }

    public void toggleGrid(boolean show) {
        this.gridVisible = show;
        if (show) {
            drawGrid();
        }
    }

    public void drawGrid() {
        if (!isGridVisible()) return;

        Canvas canvas = drawingController.getDrawingCanvas();
        if (canvas == null) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.save(); // Salva lo stato corrente

        // Disegna la griglia
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);   // lo spessore della linea è sempre uguale, qualunque sia la dimensione delle celle

        // Applica lo zoom corrente
        double zoomFactor = drawingController.getZoomHandler().getZoomFactor();
        double scaledGridSize = currentGridSize * zoomFactor;

        for (double x = 0; x < canvas.getWidth(); x += scaledGridSize) {
            gc.strokeLine(x, 0, x, canvas.getHeight());
        }
        for (double y = 0; y < canvas.getHeight(); y += scaledGridSize) {
            gc.strokeLine(0, y, canvas.getWidth(), y);
        }

        gc.restore(); // Ripristina lo stato precedente
    }

    public boolean isGridVisible() {
        return gridVisible;
    }
}