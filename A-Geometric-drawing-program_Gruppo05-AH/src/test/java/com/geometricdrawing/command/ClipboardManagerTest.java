package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Rectangle;
import com.geometricdrawing.model.Ellipse;
import com.geometricdrawing.decorator.FillColorDecorator; // Assumendo che sia serializzabile
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClipboardManagerTest {

    private ClipboardManager clipboardManager;
    private AbstractShape testRectangle;
    private AbstractShape testEllipse;

    @BeforeEach
    void setUp() {
        clipboardManager = new ClipboardManager();
        testRectangle = new Rectangle(10, 10, 50, 50);
        // Applica un decoratore per testare la clonazione profonda dei decoratori
        testEllipse = new FillColorDecorator(new Ellipse(20, 20, 30, 30), Color.RED);
    }

    @Test
    void initiallyClipboardShouldBeEmpty() {
        assertNull(clipboardManager.getFromClipboard(), "La clipboard dovrebbe essere inizialmente vuota.");
        assertFalse(clipboardManager.hasContent(), "hasContent() dovrebbe restituire false per una clipboard vuota.");
    }

    @Test
    void copyToClipboardShouldStoreShape() {
        clipboardManager.copyToClipboard(testRectangle);
        assertTrue(clipboardManager.hasContent(), "hasContent() dovrebbe restituire true dopo la copia.");
        AbstractShape retrievedShape = clipboardManager.getFromClipboard();
        assertNotNull(retrievedShape, "La forma recuperata non dovrebbe essere nulla.");
        assertNotSame(testRectangle, retrievedShape, "La forma recuperata dovrebbe essere un clone, non la stessa istanza.");
        assertEquals(testRectangle.getX(), retrievedShape.getX());
        assertEquals(testRectangle.getY(), retrievedShape.getY());
        assertEquals(testRectangle.getWidth(), retrievedShape.getWidth());
        assertEquals(testRectangle.getHeight(), retrievedShape.getHeight());
        assertTrue(retrievedShape instanceof Rectangle, "La forma recuperata dovrebbe essere un'istanza di Rectangle.");
    }

    @Test
    void copyToClipboardWithDecoratedShape() {
        clipboardManager.copyToClipboard(testEllipse);
        assertTrue(clipboardManager.hasContent());
        AbstractShape retrievedShape = clipboardManager.getFromClipboard();
        assertNotNull(retrievedShape);
        assertNotSame(testEllipse, retrievedShape);
        assertTrue(retrievedShape instanceof FillColorDecorator, "La forma recuperata dovrebbe essere decorata.");
        assertEquals(testEllipse.getX(), retrievedShape.getX()); // I metodi delegati dovrebbero funzionare
    }

    @Test
    void getFromClipboardShouldReturnNewCloneEachTime() {
        clipboardManager.copyToClipboard(testRectangle);
        AbstractShape s1 = clipboardManager.getFromClipboard();
        AbstractShape s2 = clipboardManager.getFromClipboard();

        assertNotNull(s1);
        assertNotNull(s2);
        assertNotSame(s1, s2, "Ogni chiamata a getFromClipboard dovrebbe restituire un nuovo clone.");
    }

    @Test
    void copyToClipboardWithNullShouldClearClipboard() {
        clipboardManager.copyToClipboard(testRectangle); // Contenuto iniziale
        assertTrue(clipboardManager.hasContent());

        clipboardManager.copyToClipboard(null); // Copia null
        assertNull(clipboardManager.getFromClipboard(), "getFromClipboard dovrebbe restituire null dopo aver copiato null.");
        assertFalse(clipboardManager.hasContent(), "hasContent() dovrebbe restituire false dopo aver copiato null.");
    }

    @Test
    void clearClipboardShouldEmptyClipboard() {
        clipboardManager.copyToClipboard(testRectangle);
        assertTrue(clipboardManager.hasContent());

        clipboardManager.clearClipboard();
        assertNull(clipboardManager.getFromClipboard(), "getFromClipboard dovrebbe restituire null dopo clearClipboard.");
        assertFalse(clipboardManager.hasContent(), "hasContent() dovrebbe restituire false dopo clearClipboard.");
    }
}