package com.geometricdrawing;

import com.geometricdrawing.DrawingController;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZoomHandlerTest {

    @Mock
    private DrawingController mockDrawingController;
    @Mock
    private Canvas mockCanvas;
    @Mock
    private GraphicsContext mockGc;

    private ZoomHandler zoomHandler;

    @BeforeEach
    void setUp() {
        zoomHandler = new ZoomHandler(mockDrawingController, mockCanvas);
    }

    @Test
    void constructorShouldThrowExceptionForNullController() {
        assertThrows(IllegalArgumentException.class, () -> new ZoomHandler(null, mockCanvas));
    }

    @Test
    void constructorShouldThrowExceptionForNullCanvas() {
        assertThrows(IllegalArgumentException.class, () -> new ZoomHandler(mockDrawingController, null));
    }

    @Test
    void initialZoomFactorShouldBe100Percent() {
        assertEquals(ZoomHandler.ZOOM_100, zoomHandler.getZoomFactor(), "Il fattore di zoom iniziale dovrebbe essere 1.0 (100%).");
    }


    @Test
    void setZoom25ShouldSetCorrectFactorAndRedraw() {
        zoomHandler.setZoom25();
        assertEquals(ZoomHandler.ZOOM_25, zoomHandler.getZoomFactor());
        verify(mockDrawingController, times(1)).redrawCanvas();
    }

    @Test
    void setZoom50ShouldSetCorrectFactorAndRedraw() {
        zoomHandler.setZoom50();
        assertEquals(ZoomHandler.ZOOM_50, zoomHandler.getZoomFactor());
        verify(mockDrawingController, times(1)).redrawCanvas();
    }

    @Test
    void setZoom75ShouldSetCorrectFactorAndRedraw() {
        zoomHandler.setZoom75();
        assertEquals(ZoomHandler.ZOOM_75, zoomHandler.getZoomFactor());
        verify(mockDrawingController, times(1)).redrawCanvas();
    }

    @Test
    void setZoom100ShouldSetCorrectFactorAndRedraw() {
        // Cambia prima lo zoom per assicurarsi che setZoom100 lo reimposti
        zoomHandler.setZoom50();
        reset(mockDrawingController); // Resetta il mock per contare solo la prossima chiamata

        zoomHandler.setZoom100();
        assertEquals(ZoomHandler.ZOOM_100, zoomHandler.getZoomFactor());
        verify(mockDrawingController, times(1)).redrawCanvas();
    }

    @Test
    void setZoom150ShouldSetCorrectFactorAndRedraw() {
        zoomHandler.setZoom150();
        assertEquals(ZoomHandler.ZOOM_150, zoomHandler.getZoomFactor());
        verify(mockDrawingController, times(1)).redrawCanvas();
    }

    @Test
    void resetZoomShouldSetFactorTo100AndRedraw() {
        // Cambia prima lo zoom
        zoomHandler.setZoom25();
        reset(mockDrawingController); // Resetta il mock

        zoomHandler.setZoom100();
        assertEquals(ZoomHandler.ZOOM_100, zoomHandler.getZoomFactor());
        verify(mockDrawingController, times(1)).redrawCanvas();
    }

    @Test
    void screenToWorldShouldConvertCoordinatesCorrectly() {
        zoomHandler.setZoom50(); // Zoom 0.5 (ingrandimento dimezzato)
        Point2D worldCoords = zoomHandler.screenToWorld(100.0, 200.0);
        assertEquals(200.0, worldCoords.getX(), 0.001, "Coordinata X del mondo errata.");
        assertEquals(400.0, worldCoords.getY(), 0.001, "Coordinata Y del mondo errata.");

        zoomHandler.setZoom150(); // Zoom 1.5
        worldCoords = zoomHandler.screenToWorld(150.0, 75.0);
        assertEquals(100.0, worldCoords.getX(), 0.001);
        assertEquals(50.0, worldCoords.getY(), 0.001);
    }

}