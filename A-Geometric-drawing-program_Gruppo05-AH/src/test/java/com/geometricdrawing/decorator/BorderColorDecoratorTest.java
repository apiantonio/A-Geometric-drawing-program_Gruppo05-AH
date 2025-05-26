package com.geometricdrawing.decorator;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Rectangle;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class BorderColorDecoratorTest {

    private AbstractShape decoratedShape;
    private Color testBorderColor;
    private BorderColorDecorator borderColorDecorator;

    @BeforeEach
    void setUp() {
        decoratedShape = new Rectangle(10, 10, 50, 50);
        testBorderColor = Color.rgb(255, 0, 0, 0.5); // Rosso con 50% di opacità
        borderColorDecorator = new BorderColorDecorator(decoratedShape, testBorderColor);
    }

    @Test
    void constructorShouldSetColorAndStoreRGBA() throws NoSuchFieldException, IllegalAccessException {
        assertSame(decoratedShape, borderColorDecorator.getInnerShape(), "La forma non é stata decorata correttamente.");

        // Verifica che il colore del bordo transiente sia impostato
        Field borderColorField = BorderColorDecorator.class.getDeclaredField("borderColor");
        borderColorField.setAccessible(true);
        assertEquals(testBorderColor, borderColorField.get(borderColorDecorator), "campo \"border-color\" non settato correttamente.");

        // Verifica che i campi RGBA siano impostati
        Field redField = BorderColorDecorator.class.getDeclaredField("red");
        redField.setAccessible(true);
        assertEquals(testBorderColor.getRed(), (double) redField.get(borderColorDecorator), "Rosso non settato correttamente.");

        Field greenField = BorderColorDecorator.class.getDeclaredField("green");
        greenField.setAccessible(true);
        assertEquals(testBorderColor.getGreen(), (double) greenField.get(borderColorDecorator), "Verde non settato correttamente.");

        Field blueField = BorderColorDecorator.class.getDeclaredField("blue");
        blueField.setAccessible(true);
        assertEquals(testBorderColor.getBlue(), (double) blueField.get(borderColorDecorator), "Blu non settato correttamente");

        Field alphaField = BorderColorDecorator.class.getDeclaredField("alpha");
        alphaField.setAccessible(true);
        assertEquals(testBorderColor.getOpacity(), (double) alphaField.get(borderColorDecorator), "Alpha non settato correttamente.");
    }

    @Test
    void serializationDeserializationShouldPreserveColor() throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        // Serializzazione
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(borderColorDecorator);
        oos.close();

        // Deserializzazione
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        BorderColorDecorator deserializedDecorator = (BorderColorDecorator) ois.readObject();
        ois.close();

        assertNotNull(deserializedDecorator);
        assertNotNull(deserializedDecorator.getInnerShape());

        // Verifica che i campi RGBA siano deserializzati correttamente
        Field redField = BorderColorDecorator.class.getDeclaredField("red");
        redField.setAccessible(true);
        assertEquals(testBorderColor.getRed(), (double) redField.get(deserializedDecorator));

        Field greenField = BorderColorDecorator.class.getDeclaredField("green");
        greenField.setAccessible(true);
        assertEquals(testBorderColor.getGreen(), (double) greenField.get(deserializedDecorator));

        Field blueField = BorderColorDecorator.class.getDeclaredField("blue");
        blueField.setAccessible(true);
        assertEquals(testBorderColor.getBlue(), (double) blueField.get(deserializedDecorator));

        Field alphaField = BorderColorDecorator.class.getDeclaredField("alpha");
        alphaField.setAccessible(true);
        assertEquals(testBorderColor.getOpacity(), (double) alphaField.get(deserializedDecorator));

        // Verifica che il colore del bordo transiente sia ricostruito da readObject
        Field borderColorField = BorderColorDecorator.class.getDeclaredField("borderColor");
        borderColorField.setAccessible(true);
        Color deserializedBorderColor = (Color) borderColorField.get(deserializedDecorator);

        assertNotNull(deserializedBorderColor, "il colore di contorno dovrebbe essere ricostruito dopo la deserializzazione.");
        assertEquals(testBorderColor, deserializedBorderColor, "Il colore di cortorno deserializzato non corrisponde al colore di contorno originale.");
    }

    @Test
    void moveToShouldNotChangeBorderColor() {
        double newX = 100;
        double newY = 200;
        
        borderColorDecorator.moveTo(newX, newY);

        assertEquals(testBorderColor, borderColorDecorator.getBorderColor(), "Il colore del bordo dovrebbe rimanere invariato dopo il movimento.");
    }
}
