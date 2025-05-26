package com.geometricdrawing.command;

import com.geometricdrawing.decorator.FillColorDecorator;
import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChangeFillColorCommandTest {

    private DrawingModel model;
    private FillColorDecorator decorator;
    private AbstractShape shape;

    @BeforeEach
    void setUp() {
        model = new DrawingModel();
        shape = new Rectangle(10, 10, 50, 50);
        decorator = new FillColorDecorator(shape, Color.TRANSPARENT);
        model.addShape(shape);
    }

    @Test
    void executeShouldChangeFillColorOnModel() {
        Color initialColor = decorator.getFillColor();
        Color newColor = Color.GREEN;
        ChangeFillColorCommand command = new ChangeFillColorCommand(model, decorator, newColor);


        command.execute();


        assertEquals(newColor, decorator.getFillColor(), "Il colore di riempimento dovrebbe essere cambiato al nuovo colore");
        assertNotEquals(initialColor, decorator.getFillColor(), "Il colore di riempimento dovrebbe essere diverso dal colore iniziale");
    }

    @Test
    void undoShouldRestoreOldColor() {
        Color initialColor = decorator.getFillColor();
        Color newColor = Color.GREEN;
        ChangeFillColorCommand command = new ChangeFillColorCommand(model, decorator, newColor);


        command.execute();
        command.undo();


        assertEquals(initialColor, decorator.getFillColor(), "Il colore di riempimento dovrebbe essere tornato al colore originale");
    }

    @Test
    void executeWithNullModelShouldDoNothing() {
        Color initialColor = decorator.getFillColor();
        ChangeFillColorCommand command = new ChangeFillColorCommand(null, decorator, Color.GREEN);

        command.execute();

        assertEquals(initialColor, decorator.getFillColor(), "Il colore non dovrebbe cambiare con model null");
    }

    @Test
    void executeWithNullDecoratorShouldDoNothing() {
        ChangeFillColorCommand command = new ChangeFillColorCommand(model, null, Color.RED);

        assertDoesNotThrow(() -> command.execute(), "L'esecuzione con decorator null non dovrebbe lanciare eccezioni");
    }
}