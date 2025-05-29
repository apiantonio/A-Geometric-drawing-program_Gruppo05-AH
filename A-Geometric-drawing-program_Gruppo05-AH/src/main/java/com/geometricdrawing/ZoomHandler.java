package com.geometricdrawing;

import com.geometricdrawing.DrawingController;
import javafx.beans.property.DoubleProperty; // Importa DoubleProperty
import javafx.beans.property.SimpleDoubleProperty; // Importa SimpleDoubleProperty
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
    public static final double ZOOM_200 = 2.0;

    private DoubleProperty currentZoomFactor = new SimpleDoubleProperty(ZOOM_100); // Usa DoubleProperty
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
        this.currentZoomFactor.set(zoomFactor); // Imposta il valore della proprietà
        // Non richiamare redrawCanvas qui, sarà il listener nel controller a farlo.
        // this.drawingController.redrawCanvas(); // Rimosso: il listener su currentZoomFactor lo farà.
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
        return currentZoomFactor.get();
    }

    // NUOVO: Getter per la proprietà zoomFactor
    public DoubleProperty zoomFactorProperty() {
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
        return new Point2D(screenX / currentZoomFactor.get(), screenY / currentZoomFactor.get());
    }

    /**
     * NUOVO: Converte le coordinate dallo spazio del mondo logico del canvas
     * allo spazio dello schermo (visualizzazione).
     * @param worldX Coordinata X del mondo.
     * @param worldY Coordinata Y del mondo.
     * @return Un oggetto Point2D con le coordinate dello schermo.
     */
    public Point2D worldToScreen(double worldX, double worldY) {
        return new Point2D(worldX * currentZoomFactor.get(), worldY * currentZoomFactor.get());
    }

    /**
     * Applica la trasformazione di scala al GraphicsContext fornito.
     * Questo metodo dovrebbe essere chiamato prima di disegnare le forme sul canvas.
     * @param gc Il GraphicsContext da scalare.
     */
    public void applyZoomTransformation(GraphicsContext gc) {
        if (gc != null) {
            gc.scale(currentZoomFactor.get(), currentZoomFactor.get());
        }
    }
}