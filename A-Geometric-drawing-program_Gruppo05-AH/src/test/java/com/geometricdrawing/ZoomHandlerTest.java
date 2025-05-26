package com.geometricdrawing;

import javafx.geometry.Point2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ZoomHandlerTest {
    private DrawingController drawingController;
    private ZoomHandler zoomHandler;

    @BeforeEach
    void setUp() {
        drawingController = new DrawingController();
        zoomHandler = new ZoomHandler(drawingController);
    }

    @Test
    void constructorShouldThrowExceptionForNullController() {
        assertThrows(IllegalArgumentException.class, () -> new ZoomHandler(null));
    }

    @Test
    void initialZoomFactorShouldBe100Percent() {
        assertEquals(ZoomHandler.ZOOM_100, zoomHandler.getZoomFactor(), "Il fattore di zoom iniziale dovrebbe essere 1.0 (100%).");
    }

    @Test
    void setZoom25ShouldSetCorrectFactor() {
        zoomHandler.setZoom25();
        assertEquals(ZoomHandler.ZOOM_25, zoomHandler.getZoomFactor());
    }

    @Test
    void setZoom50ShouldSetCorrectFactor() {
        zoomHandler.setZoom50();
        assertEquals(ZoomHandler.ZOOM_50, zoomHandler.getZoomFactor());
    }

    @Test
    void setZoom75ShouldSetCorrectFactor() {
        zoomHandler.setZoom75();
        assertEquals(ZoomHandler.ZOOM_75, zoomHandler.getZoomFactor());
    }

    @Test
    void setZoom100ShouldSetCorrectFactor() {
        zoomHandler.setZoom50();
        zoomHandler.setZoom100();
        assertEquals(ZoomHandler.ZOOM_100, zoomHandler.getZoomFactor());
    }

    @Test
    void setZoom150ShouldSetCorrectFactor() {
        zoomHandler.setZoom150();
        assertEquals(ZoomHandler.ZOOM_150, zoomHandler.getZoomFactor());
    }

    @Test
    void screenToWorldShouldConvertCoordinatesCorrectly() {
        zoomHandler.setZoom50();
        Point2D worldCoords = zoomHandler.screenToWorld(100.0, 200.0);
        assertEquals(200.0, worldCoords.getX(), 0.001, "Coordinata X del mondo errata.");
        assertEquals(400.0, worldCoords.getY(), 0.001, "Coordinata Y del mondo errata.");

        zoomHandler.setZoom150();
        worldCoords = zoomHandler.screenToWorld(150.0, 75.0);
        assertEquals(100.0, worldCoords.getX(), 0.001);
        assertEquals(50.0, worldCoords.getY(), 0.001);
    }
}
