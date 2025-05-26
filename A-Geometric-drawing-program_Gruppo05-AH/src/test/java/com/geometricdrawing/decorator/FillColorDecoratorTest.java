package com.geometricdrawing.decorator;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Rectangle;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class FillColorDecoratorTest {

    private AbstractShape decoratedShape;
    private Color testFillColor;
    private FillColorDecorator fillColorDecorator;

    @BeforeEach
    void setUp() {
        decoratedShape = new Rectangle(10, 10, 50, 50);
        testFillColor = Color.rgb(0, 255, 0, 0.75); // Verde con 75% di opacità
        fillColorDecorator = new FillColorDecorator(decoratedShape, testFillColor);
    }

    @Test
    void constructorShouldSetColorAndStoreRGBA() throws NoSuchFieldException, IllegalAccessException {
        assertSame(decoratedShape, fillColorDecorator.getInnerShape(), "La forma non é stata decorata correttamente.");

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

    @Test
    void moveToShouldNotChangeFillColor() {
        double newX = 100;
        double newY = 200;
        
        fillColorDecorator.moveTo(newX, newY);
        
        assertEquals(testFillColor, fillColorDecorator.getFillColor(), "Il colore di riempimento dovrebbe rimanere invariato dopo il movimento.");
    }
}
