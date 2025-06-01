package com.geometricdrawing.controller;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollBar;

/**
 * Gestisce i livelli di zoom per un Canvas in un'applicazione JavaFX.
 * Converte le coordinate tra lo spazio dello schermo e lo spazio del mondo logico del canvas.
 */
public class ZoomHandler {

    public static final double ZOOM_25 = 0.25;
    public static final double ZOOM_50 = 0.50;
    public static final double ZOOM_75 = 0.75;
    public static final double ZOOM_100 = 1.0; // Default
    public static final double ZOOM_150 = 1.50;
    public static final double ZOOM_200 = 2.0;

    private double currentZoomFactor = ZOOM_100;
    private final DrawingController drawingController;

    /**
     * Costruttore per ZoomHandler.
     * @param controller Il DrawingController principale dell'applicazione.
     */
    public ZoomHandler(DrawingController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("DrawingController e Canvas non possono essere nulli.");
        }
        this.drawingController = controller;
    }

    /**
     * Imposta il livello di zoom specificato.
     * @param zoomFactor Il fattore di zoom da applicare (es. 1.0 per 100%).
     */
    public void setZoomLevel(double zoomFactor) {
        if (zoomFactor <= 0) {
            System.err.println("Fattore di zoom non valido: " + zoomFactor);
            return;
        }
        this.currentZoomFactor = zoomFactor;
        if (this.drawingController != null) {
            this.drawingController.updateScrollBars(); // Aggiorna le scrollbar PRIMA di ridisegnare
            this.drawingController.redrawCanvas();     // Poi ridisegna
        }
    }

    // Metodi pubblici per impostare livelli di zoom predefiniti
    public void setZoom25() {
        setZoomLevel(ZOOM_25);
    }

    public void setZoom50() {
        setZoomLevel(ZOOM_50);
    }

    public void setZoom75() {
        setZoomLevel(ZOOM_75);
    }

    public void setZoom100() {
        setZoomLevel(ZOOM_100);
    }

    public void setZoom150() {
        setZoomLevel(ZOOM_150);
    }

    public void setZoom200() {
        setZoomLevel(ZOOM_200);
    }

    /**
     * @return Il fattore di zoom corrente.
     */
    public double getZoomFactor() {
        return currentZoomFactor;
    }

    public Point2D screenToWorld(double screenX, double screenY) {
        if (drawingController == null) { // Controllo di sicurezza
            return new Point2D(screenX / currentZoomFactor, screenY / currentZoomFactor);
        }
        ScrollBar hBar = drawingController.getHorizontalScrollBar();
        ScrollBar vBar = drawingController.getVerticalScrollBar();

        double scrollXWorld = (hBar != null && hBar.isVisible()) ? hBar.getValue() : 0;
        double scrollYWorld = (vBar != null && vBar.isVisible()) ? vBar.getValue() : 0;

        double worldX = (screenX / currentZoomFactor) + scrollXWorld;
        double worldY = (screenY / currentZoomFactor) + scrollYWorld;
        return new Point2D(worldX, worldY);
    }

    public Point2D worldToScreen(double worldX, double worldY) {
        if (drawingController == null) {
            return new Point2D(worldX * currentZoomFactor, worldY * currentZoomFactor);
        }
        ScrollBar hBar = drawingController.getHorizontalScrollBar();
        ScrollBar vBar = drawingController.getVerticalScrollBar();

        double scrollXWorld = (hBar != null && hBar.isVisible()) ? hBar.getValue() : 0;
        double scrollYWorld = (vBar != null && vBar.isVisible()) ? vBar.getValue() : 0;

        double screenX = (worldX - scrollXWorld) * currentZoomFactor;
        double screenY = (worldY - scrollYWorld) * currentZoomFactor;
        return new Point2D(screenX, screenY);
    }

    /**
     * Applica la trasformazione di scala al GraphicsContext fornito.
     * Questo metodo dovrebbe essere chiamato prima di disegnare le forme sul canvas.
     * @param gc Il GraphicsContext da scalare.
     */
    public void applyZoomTransformation(GraphicsContext gc) {
        if (gc != null) {
            gc.scale(currentZoomFactor, currentZoomFactor);
        }
    }
}