package com.geometricdrawing;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Autore: Gruppo05
 * Scopo: Classe che gestisce la visualizzazione della griglia in diverse grandezze.
 */
public class Grid {
    // questi valori rappresentano la distanza tra le linee della griglia in pixels quindi un lato del quadretto della griglia ha questa lunghezza
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
    }

    public boolean isGridVisible() {
        return gridVisible;
    }

    /**
     * @param gc Il GraphicsContext su cui disegnare.
     * @param visibleWorldX La coordinata X dell'angolo in alto a sinistra dell'area attualmente visibile sullo schermo.
     * @param visibleWorldY La coordinata Y dell'angolo in alto a sinistra dell'area attualmente visibile sullo schermo.
     * @param visibleWorldWidth La larghezza dell'area del mondo visibile.
     * @param visibleWorldHeight L'altezza dell'area del mondo visibile.
     */
    public void drawGrid(GraphicsContext gc, double visibleWorldX, double visibleWorldY, double visibleWorldWidth, double visibleWorldHeight) {
        if (!isGridVisible() || gc == null || drawingController.getZoomHandler() == null) return;

        gc.save();
        gc.setStroke(Color.LIGHTGRAY);  // si imposta il colore delle linee della griglia ad un grigio chiaro

        // Lo spessore della linea della griglia deve apparire sottile indipendentemente dallo zoom
        double desiredLineWidth = 0.5;
        // all'aumentare dello zoom l'area visibile sullo schermo deve essere ridotta
        double worldLineWidth = desiredLineWidth / drawingController.getZoomHandler().getZoomFactor();
        gc.setLineWidth(worldLineWidth);

        /* La griglia non può essere disegnata dal visibleWorldX perchè potrebbe essere sfasata. In questo modo si calcola
           la prima linea dela griglia sulla sinistra che è visibile nella viewport.
        */
        double startGridLineX = Math.floor(visibleWorldX / currentGridSize) * currentGridSize;
        // Si calcola l'ultima linea della griglia a destra che è visibile nella viewport
        double endGridLineX = visibleWorldX + visibleWorldWidth;

        for (double x = startGridLineX; x <= endGridLineX; x += currentGridSize) {
            gc.strokeLine(x, visibleWorldY, x, visibleWorldY + visibleWorldHeight);
        }

        // Disegna linee orizzontali:
        double startGridLineY = Math.floor(visibleWorldY / currentGridSize) * currentGridSize;
        double endGridLineYLimit = visibleWorldY + visibleWorldHeight;

        for (double y = startGridLineY; y <= endGridLineYLimit; y += currentGridSize) {
            gc.strokeLine(visibleWorldX, y, visibleWorldX + visibleWorldWidth, y);
        }
        gc.restore();
    }
}