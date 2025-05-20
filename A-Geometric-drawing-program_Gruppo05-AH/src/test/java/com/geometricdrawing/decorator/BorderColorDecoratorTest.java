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
class BorderColorDecoratorTest {

    @Mock
    private AbstractShape mockDecoratedShape;

    @Mock
    private GraphicsContext mockGc;

    private Color testBorderColor;
    private BorderColorDecorator borderColorDecorator;

    @BeforeEach
    void setUp() {
        testBorderColor = Color.rgb(255, 0, 0, 0.5); // Red with 50% opacity
        borderColorDecorator = new BorderColorDecorator(mockDecoratedShape, testBorderColor);
    }

    @Test
    void constructorShouldSetColorAndStoreRGBA() throws NoSuchFieldException, IllegalAccessException {
        assertSame(mockDecoratedShape, borderColorDecorator.getInnerShape(), "La forma non é stata decorata correttamente.");

        // Verify transient borderColor is set (it should be, even if transient, until serialized)
        Field borderColorField = BorderColorDecorator.class.getDeclaredField("borderColor");
        borderColorField.setAccessible(true);
        assertEquals(testBorderColor, borderColorField.get(borderColorDecorator), "campo \"border-color\" non settato correttamente.");

        // Verify RGBA fields are set
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
        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(borderColorDecorator);
        oos.close();

        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        BorderColorDecorator deserializedDecorator = (BorderColorDecorator) ois.readObject();
        ois.close();

        assertNotNull(deserializedDecorator);
        assertNotNull(deserializedDecorator.getInnerShape()); // Mock shape should be serialized/deserialized as well

        // Verify RGBA fields are correctly deserialized
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

        // Verify transient borderColor is reconstructed by readObject
        Field borderColorField = BorderColorDecorator.class.getDeclaredField("borderColor");
        borderColorField.setAccessible(true);
        Color deserializedBorderColor = (Color) borderColorField.get(deserializedDecorator);

        assertNotNull(deserializedBorderColor, "il colore di contorno dovrebbe essere ricostruito dopo la deserializzazione.");
        assertEquals(testBorderColor, deserializedBorderColor, "Il colore di cortorno deserializzato non corrisponde al colore di contorno originale.");
    }

    //verifica che anche spostando la figura il colore di bordo di mantenga
    @Test
    void moveToShouldDelegate() {
        borderColorDecorator.moveTo(100, 200);
        verify(mockDecoratedShape).moveTo(100, 200);
    }
}