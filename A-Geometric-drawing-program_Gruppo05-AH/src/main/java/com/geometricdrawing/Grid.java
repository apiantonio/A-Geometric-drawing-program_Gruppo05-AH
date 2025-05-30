package com.geometricdrawing;

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
    private double currentGridSize = MEDIUM_GRID_SIZE;

    public Grid(DrawingController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("DrawingController non può essere nullo.");
        }
        this.drawingController = controller;
    }

    private void setGridSize(double size) {
        this.currentGridSize = size;
        // Il ridisegno è gestito dal DrawingController quando questi metodi vengono chiamati
    }

    public void setGridSizeSmall() { setGridSize(SMALL_GRID_SIZE); }
    public void setGridSizeMedium() { setGridSize(MEDIUM_GRID_SIZE); }
    public void setGridSizeBig() { setGridSize(BIG_GRID_SIZE); }

    public void toggleGrid(boolean show) {
        this.gridVisible = show;
        // Il ridisegno è gestito dal DrawingController
    }

    /**
     * @param gc Il GraphicsContext su cui disegnare.
     * @param visibleWorldX La coordinata X dell'angolo in alto a sinistra dell'area del mondo visibile.
     * @param visibleWorldY La coordinata Y dell'angolo in alto a sinistra dell'area del mondo visibile.
     * @param visibleWorldWidth La larghezza dell'area del mondo visibile.
     * @param visibleWorldHeight L'altezza dell'area del mondo visibile.
     */
    public void drawGrid(GraphicsContext gc, double visibleWorldX, double visibleWorldY, double visibleWorldWidth, double visibleWorldHeight) {
        if (!isGridVisible() || gc == null || drawingController.getZoomHandler() == null) return;

        gc.save();
        gc.setStroke(Color.LIGHTGRAY);

        // Per ottenere una linea di spessore 0.5px costante sullo schermo,
        // adatta lo spessore della linea nel mondo in base allo zoom.
        double desiredScreenLineWidth = 0.5;
        double worldLineWidth = desiredScreenLineWidth / drawingController.getZoomHandler().getZoomFactor();
        gc.setLineWidth(worldLineWidth);

        // Disegna linee verticali:
        // Itera dalla prima linea della griglia che potrebbe essere visibile all'ultima.
        // Le coordinate sono coordinate assolute del mondo.
        double startGridLineX = Math.floor(visibleWorldX / currentGridSize) * currentGridSize;
        double endGridLineXLimit = visibleWorldX + visibleWorldWidth;

        for (double x = startGridLineX; x <= endGridLineXLimit; x += currentGridSize) {
            // Disegna solo se la linea è effettivamente (o molto vicina a) essere nella viewport
            // Questo è un piccolo aggiustamento per evitare di disegnare linee troppo lontane se i calcoli sono leggermente imprecisi
            if (x >= visibleWorldX - currentGridSize && x <= visibleWorldX + visibleWorldWidth + currentGridSize) {
                gc.strokeLine(x, visibleWorldY, x, visibleWorldY + visibleWorldHeight);
            }
        }

        // Disegna linee orizzontali:
        double startGridLineY = Math.floor(visibleWorldY / currentGridSize) * currentGridSize;
        double endGridLineYLimit = visibleWorldY + visibleWorldHeight;

        for (double y = startGridLineY; y <= endGridLineYLimit; y += currentGridSize) {
            if (y >= visibleWorldY - currentGridSize && y <= visibleWorldY + visibleWorldHeight + currentGridSize) {
                gc.strokeLine(visibleWorldX, y, visibleWorldX + visibleWorldWidth, y);
            }
        }
        gc.restore();
    }

    public boolean isGridVisible() {
        return gridVisible;
    }
}