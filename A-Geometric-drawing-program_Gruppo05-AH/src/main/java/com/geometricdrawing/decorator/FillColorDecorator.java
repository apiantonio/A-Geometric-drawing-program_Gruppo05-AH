package com.geometricdrawing.decorator;

import com.geometricdrawing.model.AbstractShape;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.*;

/**
 * La classe decoratore per il colore del riempimento implementa Serializable
 * per consentire l'esportazione e importazione.
 */
public class FillColorDecorator extends ShapeDecorator {
    private transient Color fillColor;
    private double red, green, blue, alpha;   // i campi serializzati saranno RGBA

    public FillColorDecorator(AbstractShape shape, Color fillColor) {
        super(shape);
        // Imposta il colore e i componenti RGBA
        setFillColorAndUpdateRGBA(fillColor);
    }

    private void setFillColorAndUpdateRGBA(Color color) {
        this.fillColor = color;
        if (this.fillColor != null) {
            this.red = this.fillColor.getRed();
            this.green = this.fillColor.getGreen();
            this.blue = this.fillColor.getBlue();
            this.alpha = this.fillColor.getOpacity();
        } else {
            // Gestisci il caso di un colore nullo, se applicabile (es. nero trasparente o default)
            // Ad esempio, potresti voler impostare un colore di default o trasparente
            this.red = 0; // Esempio: nero
            this.green = 0;
            this.blue = 0;
            this.alpha = 0; // Esempio: completamente trasparente
        }
    }

    /**
     * Metodo per modificare il colore di riempimento dopo la creazione.
     * Assicura che anche i campi RGBA per la serializzazione siano aggiornati.
     * @param newFillColor Il nuovo colore di riempimento.
     */
    public void setFillColor(Color newFillColor) {
        setFillColorAndUpdateRGBA(newFillColor);
    }

    public Color getFillColor() {
        return this.fillColor;
    }

    @Override
    protected void decorateShape(GraphicsContext gc) {
        if (fillColor != null) { // Controlla che fillColor non sia null prima di usarlo
            gc.setFill(fillColor);
        }
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject(); // Serializza red, green, blue, alpha
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); // Deserializza red, green, blue, alpha
        // Ricostruisci il colore transient, gestendo il caso in cui i valori RGBA potrebbero
        // rappresentare un colore nullo se così è stato deciso in setFillColorAndUpdateRGBA
        if (red == 0 && green == 0 && blue == 0 && alpha == 0 && this.fillColor == null) {
            // Potrebbe essere un colore nullo intenzionale o un default.
            // Se hai una logica di default specifica per il colore nullo, applicala qui.
            // Per ora, se fillColor era null e RGBA sono 0, lo lasciamo null o lo impostiamo a trasparente.
            this.fillColor = new Color(0,0,0,0); // o null, a seconda della tua logica di default
        } else {
            this.fillColor = new Color(red, green, blue, alpha);
        }
    }
}