package com.geometricdrawing.decorator;

import com.geometricdrawing.model.AbstractShape;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FillColorDecoratorTest {

    @Mock
    private AbstractShape mockDecoratedShape;

    @Mock
    private GraphicsContext mockGc;

    private Color testFillColor;
    private FillColorDecorator fillColorDecorator;

    @BeforeEach
    void setUp() {
        testFillColor = Color.rgb(0, 255, 0, 0.75);
        fillColorDecorator = new FillColorDecorator(mockDecoratedShape, testFillColor);
    }
    //verifica che il costeruttore setti correttamente i colori
    @Test
    void constructorShouldSetColorAndStoreRGBA() throws NoSuchFieldException, IllegalAccessException {
        assertSame(mockDecoratedShape, fillColorDecorator.getInnerShape(), "La forma non é stata decorata correttamente.");

        Field fillColorField = FillColorDecorator.class.getDeclaredField("fillColor");
        fillColorField.setAccessible(true);
        assertEquals(testFillColor, fillColorField.get(fillColorDecorator), "il campo \"fillColor\" non é stato settato correttamente.");

        Field redField = FillColorDecorator.class.getDeclaredField("red");
        redField.setAccessible(true);
        assertEquals(testFillColor.getRed(), (double) redField.get(fillColorDecorator), "Rosso non settato correttamente.");

        Field greenField = FillColorDecorator.class.getDeclaredField("green");
        greenField.setAccessible(true);
        assertEquals(testFillColor.getGreen(), (double) greenField.get(fillColorDecorator), "Verde non settato correttamente.");

        Field blueField = FillColorDecorator.class.getDeclaredField("blue");
        blueField.setAccessible(true);
        assertEquals(testFillColor.getBlue(), (double) blueField.get(fillColorDecorator), "Blue non settato correttamente.");

        Field alphaField = FillColorDecorator.class.getDeclaredField("alpha");
        alphaField.setAccessible(true);
        assertEquals(testFillColor.getOpacity(), (double) alphaField.get(fillColorDecorator), "Alpha non settato correttamente.");
    }

    //verifica che la il salvataggio e caricamento da file mantengano il colore
    @Test
    void serializationDeserializationShouldPreserveColor() throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(fillColorDecorator);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FillColorDecorator deserializedDecorator = (FillColorDecorator) ois.readObject();
        ois.close();

        assertNotNull(deserializedDecorator);
        assertNotNull(deserializedDecorator.getInnerShape());

        Field redField = FillColorDecorator.class.getDeclaredField("red");
        redField.setAccessible(true);
        assertEquals(testFillColor.getRed(), (double) redField.get(deserializedDecorator));

        Field greenField = FillColorDecorator.class.getDeclaredField("green");
        greenField.setAccessible(true);
        assertEquals(testFillColor.getGreen(), (double) greenField.get(deserializedDecorator));

        Field blueField = FillColorDecorator.class.getDeclaredField("blue");
        blueField.setAccessible(true);
        assertEquals(testFillColor.getBlue(), (double) blueField.get(deserializedDecorator));

        Field alphaField = FillColorDecorator.class.getDeclaredField("alpha");
        alphaField.setAccessible(true);
        assertEquals(testFillColor.getOpacity(), (double) alphaField.get(deserializedDecorator));

        Field fillColorField = FillColorDecorator.class.getDeclaredField("fillColor");
        fillColorField.setAccessible(true);
        Color deserializedFillColor = (Color) fillColorField.get(deserializedDecorator);

        assertNotNull(deserializedFillColor, "fillColor should be reconstructed after deserialization.");
        assertEquals(testFillColor, deserializedFillColor, "Deserialized fillColor does not match original color.");
    }
    //verifica che anche spostando la figura il colore di riempimento di mantenga
    @Test
    void moveToShouldDelegate() {
        fillColorDecorator.moveTo(100, 200);
        verify(mockDecoratedShape).moveTo(100, 200);
    }
}