package com.geometricdrawing;

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
            drawingController.redrawCanvas(); // Richiedi un ridisegno se la dimensione della griglia cambia
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
        // Il ridisegno è gestito dal controller quando lo stato del menu cambia
        // o quando viene chiamato esplicitamente redrawCanvas().
    }

    /**
     * Disegna la griglia sul GraphicsContext fornito, tenendo conto della vista del mondo corrente.
     * Il GraphicsContext dovrebbe essere già trasformato (scalato e traslato) per mappare
     * le coordinate del mondo alla porzione visibile del canvas fisico.
     *
     * @param gc                 Il GraphicsContext su cui disegnare.
     * @param worldViewX         La coordinata X del mondo all'estrema sinistra della vista corrente.
     * @param worldViewY         La coordinata Y del mondo all'estrema parte superiore della vista corrente.
     * @param worldViewWidth     La larghezza della vista corrente in coordinate del mondo.
     * @param worldViewHeight    La altezza della vista corrente in coordinate del mondo.
     */
    public void drawWorldGrid(GraphicsContext gc,
                              double worldViewX,
                              double worldViewY,
                              double worldViewWidth,
                              double worldViewHeight) {
        if (!isGridVisible() || gc == null || drawingController.getZoomHandler() == null) {
            return;
        }

        gc.save();

        gc.setStroke(Color.LIGHTGRAY);
        double screenLineWidth = 0.5; // Spessore desiderato della linea sullo schermo
        // Dato che gc è già scalato per zoomFactor, la larghezza della linea in unità del mondo
        // deve essere (screenLineWidth / zoomFactor) per apparire corretta.
        double worldLineWidth = screenLineWidth / drawingController.getZoomHandler().getZoomFactor();
        gc.setLineWidth(worldLineWidth);
        gc.setLineDashes(0); // Linee continue

        // Linee Verticali
        // Trova la prima coordinata X della linea verticale nel mondo che è >= worldViewX
        double firstWorldLineX = Math.floor(worldViewX / currentGridSize) * currentGridSize;
        if (firstWorldLineX < worldViewX - 1e-9) { // Aggiungi una piccola tolleranza per errori di floating point
            firstWorldLineX += currentGridSize;
        }
        // Disegna le linee verticali che sono visibili all'interno della vista del mondo
        for (double x = firstWorldLineX; x < worldViewX + worldViewWidth + 1e-9; x += currentGridSize) {
            // Le coordinate sono in unità del mondo; il gc trasformato le mapperà correttamente.
            gc.strokeLine(x, worldViewY, x, worldViewY + worldViewHeight);
        }

        // Linee Orizzontali
        // Trova la prima coordinata Y della linea orizzontale nel mondo che è >= worldViewY
        double firstWorldLineY = Math.floor(worldViewY / currentGridSize) * currentGridSize;
        if (firstWorldLineY < worldViewY - 1e-9) { // Tolleranza
            firstWorldLineY += currentGridSize;
        }
        // Disegna le linee orizzontali che sono visibili all'interno della vista del mondo
        for (double y = firstWorldLineY; y < worldViewY + worldViewHeight + 1e-9; y += currentGridSize) {
            gc.strokeLine(worldViewX, y, worldViewX + worldViewWidth, y);
        }

        gc.restore();
    }


    /**
     * Metodo deprecato o da non usare con la logica del viewport virtuale,
     * poiché disegna la griglia fissata al canvas fisico senza considerare lo scorrimento del mondo.
     * Mantenuto per riferimento o se si volesse una griglia fissa sul canvas per qualche motivo.
     */
    @Deprecated
    public void drawGrid_fixedToCanvas() {
        if (!isGridVisible()) return;

        Canvas canvas = drawingController.getDrawingCanvas();
        if (canvas == null || canvas.getWidth() <= 0 || canvas.getHeight() <= 0) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.save();

        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5); // Spessore fisso in pixel dello schermo

        double zoomFactor = drawingController.getZoomHandler().getZoomFactor();
        // scaledGridSize è la dimensione di una cella della griglia in pixel dello schermo
        double scaledGridSize = currentGridSize * zoomFactor;

        if (scaledGridSize < 1) scaledGridSize = 1; // Evita loop infiniti o divisioni per zero

        for (double x = 0; x < canvas.getWidth(); x += scaledGridSize) {
            gc.strokeLine(x, 0, x, canvas.getHeight());
        }
        for (double y = 0; y < canvas.getHeight(); y += scaledGridSize) {
            gc.strokeLine(0, y, canvas.getWidth(), y);
        }

        gc.restore();
    }


    public boolean isGridVisible() {
        return gridVisible;
    }
}