package com.geometricdrawing;

import com.geometricdrawing.DrawingController;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

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

    private double currentZoomFactor = ZOOM_100;
    private final DrawingController drawingController;
    private final Canvas canvas;

    /**
     * Costruttore per ZoomHandler.
     * @param controller Il DrawingController principale dell'applicazione.
     * @param canvas Il Canvas su cui verrà applicato lo zoom.
     */
    public ZoomHandler(DrawingController controller, Canvas canvas) {
        if (controller == null || canvas == null) {
            throw new IllegalArgumentException("DrawingController e Canvas non possono essere nulli.");
        }
        this.drawingController = controller;
        this.canvas = canvas;
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
        // Richiede un ridisegno del canvas attraverso il controller
        if (this.drawingController != null) {
            this.drawingController.redrawCanvas();
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

    /**
     * @return Il fattore di zoom corrente.
     */
    public double getZoomFactor() {
        return currentZoomFactor;
    }

    /**
     * Converte le coordinate dallo spazio dello schermo (visualizzazione)
     * allo spazio del mondo logico del canvas (scalato).
     * @param screenX Coordinata X dello schermo.
     * @param screenY Coordinata Y dello schermo.
     * @return Un oggetto Point2D con le coordinate del mondo.
     */
    public Point2D screenToWorld(double screenX, double screenY) {
        return new Point2D(screenX / currentZoomFactor, screenY / currentZoomFactor);
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