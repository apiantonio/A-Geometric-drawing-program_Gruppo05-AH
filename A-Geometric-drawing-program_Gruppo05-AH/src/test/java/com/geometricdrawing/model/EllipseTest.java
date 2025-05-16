package com.geometricdrawing.model;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import static org.mockito.Mockito.*;

class EllipseTest {
    @Test
    void testDraw() {
        GraphicsContext mockGc = Mockito.mock(GraphicsContext.class);
        Ellipse ellipse = new Ellipse(10, 20, 30, 40);
        ellipse.setFillColor(Color.RED); // Imposta un colore specifico per il test
        ellipse.setBorderColor(Color.BLACK);

        ellipse.draw(mockGc);

        // Verifica che setFill sia chiamato con il colore di riempimento dell'ellisse
        verify(mockGc).setFill(Color.RED);
        // Verifica che fillOval sia chiamato con le coordinate e dimensioni corrette
        verify(mockGc).fillOval(10, 20, 30, 40);
        // Verifica che setStroke sia chiamato con il colore del bordo
        verify(mockGc).setStroke(Color.BLACK);
        // Verifica che strokeOval sia chiamato
        verify(mockGc).strokeOval(10, 20, 30, 40);
    }
}