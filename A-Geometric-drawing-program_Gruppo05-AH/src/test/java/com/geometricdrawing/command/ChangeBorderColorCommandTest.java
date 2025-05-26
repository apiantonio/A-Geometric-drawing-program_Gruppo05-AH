package com.geometricdrawing.command;

import com.geometricdrawing.decorator.BorderColorDecorator;
import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChangeBorderColorCommandTest {

    private DrawingModel model;
    private BorderColorDecorator decorator;
    private AbstractShape shape;

    @BeforeEach
    void setUp() {
        model = new DrawingModel();
        shape = new Rectangle(10, 10, 50, 50);
        decorator = new BorderColorDecorator(shape, Color.TRANSPARENT);
        model.addShape(shape);
    }

    @Test
    void executeShouldChangeBorderColorOnModel() {
        Color initialColor = decorator.getBorderColor();
        Color newColor = Color.RED;
        ChangeBorderColorCommand command = new ChangeBorderColorCommand(model, decorator, newColor);

        
        command.execute();

        
        assertEquals(newColor, decorator.getBorderColor(), "Il colore del bordo dovrebbe essere cambiato al nuovo colore");
        assertNotEquals(initialColor, decorator.getBorderColor(), "Il colore del bordo dovrebbe essere diverso dal colore iniziale");
    }

    @Test
    void undoShouldRestoreOldColor() {
        Color initialColor = decorator.getBorderColor();
        Color newColor = Color.RED;
        ChangeBorderColorCommand command = new ChangeBorderColorCommand(model, decorator, newColor);

        
        command.execute();
        command.undo();

        
        assertEquals(initialColor, decorator.getBorderColor(), "Il colore del bordo dovrebbe essere tornato al colore originale");
    }

    @Test
    void executeWithNullModelShouldDoNothing() {
        Color initialColor = decorator.getBorderColor();
        ChangeBorderColorCommand command = new ChangeBorderColorCommand(null, decorator, Color.RED);

        command.execute();

        assertEquals(initialColor, decorator.getBorderColor(), "Il colore non dovrebbe cambiare con model null");
    }

    @Test
    void executeWithNullDecoratorShouldDoNothing() {
        ChangeBorderColorCommand command = new ChangeBorderColorCommand(model, null, Color.RED);
        
        assertDoesNotThrow(() -> command.execute(), "L'esecuzione con decorator null non dovrebbe lanciare eccezioni");
    }
}
