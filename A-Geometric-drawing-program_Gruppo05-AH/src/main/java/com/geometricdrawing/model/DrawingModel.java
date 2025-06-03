package com.geometricdrawing.model;

import com.geometricdrawing.decorator.BorderColorDecorator;
import com.geometricdrawing.decorator.FillColorDecorator;
import com.geometricdrawing.decorator.ShapeDecorator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Autore: Gruppo05
 * @Scopo: Modello dell'applicazione, contiene le figure e gestisce le operazioni fondamentali ssu di esse.
 */
public class DrawingModel {
    private transient ObservableList<AbstractShape> shapes;

    public DrawingModel() {
        this.shapes = FXCollections.observableArrayList();
    }

    public void addShape(AbstractShape shape) {
        if (shape != null) {
            shape.setZ(shapes.size()); // La nuova figura ha lo Z più alto
            this.shapes.add(shape);
        }
    }

    /**
     * @param shape rimuove una figura dalla lista delle figure del modello
     */
    public void removeShape(AbstractShape shape) {
        if (shape != null) {
            this.shapes.remove(shape);
        }
    }

    /**
     * @param shape è la figura di cui vogliamo cambiare la larghezza
     * @param width è la nuova larghezza che vogliamo assegnare alla figura
     */
    public void setShapeWidth(AbstractShape shape, double width) {
        if (shape != null) {
            shape.setWidth(width);
        }
    }

    /**
     * @param shape è la figura di cui vogliamo cambiare l'altezza
     * @param newHeight è la nuova altezza che vogliamo assegnare alla figura
     */
    public void setShapeHeight(AbstractShape shape, double newHeight) {
        if (shape != null) {
            shape.setHeight(newHeight);
        }
    }

    /**
     *
     * @param shape è la figura di cui vogliamo cambiare la posizione
     * @param newX
     * @param newY
     */
    public void moveShapeTo(AbstractShape shape, double newX, double newY) {
        if (shape != null) {
            shape.moveTo(newX, newY);
        }
    }

    /**
     * Metodo per cambiare il colore di contorno della figura
     * @param decorator
     * @param color
     */
    public void setBorderColor(BorderColorDecorator decorator, Color color) {
        if (decorator != null) {
            decorator.setBorderColor(color);
        }
    }

    /**
     * Metodo per cambiare il colore di riempimento della figura
     * @param decorator
     * @param color
     */
    public void setFillColor(FillColorDecorator decorator, Color color) {
        if (decorator != null) {
            decorator.setFillColor(color);
        }
    }

    /**
     * il metodo gestisce sia porta in primo piano che in secondo piano e undo delle due operazioni
     * @param shape è la figura di cui vogliamo cambiare la disposizione nell'area di disegno
     * @param newZ è l'indice Z che si vuole assegnare alla figura selezionata
     */
    public void changeZOrder(AbstractShape shape, int newZ) {
        // Controllo per prima cosa che il valore passato per Z sia valido
        if(newZ < 0 || newZ >= shapes.size()) {
            throw new IndexOutOfBoundsException("Invalid Z index: " + newZ);
        }

        // se la figura è presente nella lista
        if (shape != null && shapes.contains(shape)) {
            int actualZ = shape.getZ();

            // Solo se gli indici - di partenza e nuovo - per la figura sono diversi eseguo l'operazione
            if(newZ != actualZ) {
                // scorro tutte le figure
                for (AbstractShape s : shapes) {
                    // PER PORTARE IN PRIMO PIANO
                    // se la figura ha uno Z maggiore del nuovo Z, lo decremento
                    if (s.getZ() >= newZ && s.getZ() < actualZ) {
                        s.setZ(s.getZ() + 1);
                    }
                    // PER PORTARE IN SECONDO PIANO
                    // se la figura ha uno Z minore del nuovo Z, lo incremento
                    else if (s.getZ() <= newZ && s.getZ() > actualZ) {
                        s.setZ(s.getZ() - 1);
                    }
                }
                // rimuovo la figura e la reinserisco nella posizione corretta
                removeShape(shape);
                shapes.add(newZ, shape);
                // ne definisco lo Z-index come il parametro passato
                shape.setZ(newZ);
            }
        }
    }

    /**
     * Metodo richiamato dal command che ruota una figura di un certo angolo
     * @param shape figura da ruotare
     * @param deltaAngle angol di rotazione rispetto alla posizione attuale della figura
     */
    public void rotateShape(AbstractShape shape, double deltaAngle) {
        if (shape != null) {
            double scaleX = shape.getScaleX();
            double scaleY = shape.getScaleY();

            // Se c'è specchiatura orizzontale o verticale (per questo la XOR) la rotazione è nel senso opposto
            if (scaleX < 0 ^ scaleY < 0) {
                deltaAngle = -deltaAngle;
            }
            shape.rotateBy(deltaAngle);
        }
    }


    public void mirrorShape(AbstractShape shape, boolean horizontal) {
        if (shape != null) {
            if (horizontal) {
                shape.setScaleX(-shape.getScaleX()); // Inverte sull'asse orizzontale
            } else {
                shape.setScaleY(-shape.getScaleY()); // Inverte sull'asse verticale
            }
        }
    }

    public ObservableList<AbstractShape> getShapes() {
        return this.shapes;
    }

    /**
     * Metodo che ripulisce tutta la lista di figure presenti nell'area di disegno
     */
    public void clear() {
        this.shapes.clear();
    }

    /**
     * Metodo che restituisce le figure in ordine decrescente di z
     */
    public ObservableList<AbstractShape> getShapesOrderedByZ() {
        return FXCollections.observableArrayList(
                shapes.stream()
                        .sorted((s1, s2) -> Integer.compare(((AbstractShape) s2).getZ(), ((AbstractShape) s1).getZ()))
                        .toList()
        );
    }

    /**
     * @param file  per il salvataggio di figure su un file
     */
    public void saveToFile(File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            // Convert ObservableList to ArrayList for serialization
            oos.writeObject(new ArrayList<>(shapes));
        }
    }

    /**
     * @param file  per il caricameento di figure da un file
     */
    @SuppressWarnings("unchecked")
    public void loadFromFile(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<AbstractShape> loadedShapes = (List<AbstractShape>) ois.readObject();
            shapes.clear();
            if (loadedShapes != null) {
                shapes.addAll(loadedShapes);
            }
        }
        if (this.shapes == null) {
            this.shapes = FXCollections.observableArrayList();
        }
    }
    public void setText(TextShape text, String newText) {
        if(text != null) {
            text.setText(newText);
        }
    }
    public void setFontSize(TextShape text, int size) {
        text.setFontSize(size);
    }
}