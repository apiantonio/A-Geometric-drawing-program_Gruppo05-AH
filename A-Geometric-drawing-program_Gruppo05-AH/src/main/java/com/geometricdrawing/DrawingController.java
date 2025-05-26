package com.geometricdrawing;

import com.geometricdrawing.command.*;
import com.geometricdrawing.decorator.BorderColorDecorator;
import com.geometricdrawing.decorator.FillColorDecorator;
import com.geometricdrawing.decorator.ShapeDecorator;
import com.geometricdrawing.factory.EllipseFactory;
import com.geometricdrawing.factory.LineFactory;
import com.geometricdrawing.factory.RectangleFactory;
import com.geometricdrawing.factory.ShapeFactory;
import com.geometricdrawing.templateMethod.*;
import com.geometricdrawing.strategy.*;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import com.geometricdrawing.model.DrawingModel;
import javafx.scene.paint.Color;
import com.geometricdrawing.model.Line;
import com.geometricdrawing.model.AbstractShape;

import javafx.stage.Window;
import javafx.util.Duration;

import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * @Autore: Gruppo05
 * @Scopo: Controller principale, gestisce UI e interazioni.
 */
public class DrawingController {

    @FXML private AnchorPane rootPane;
    @FXML private Canvas drawingCanvas;
    @FXML private AnchorPane canvasContainer;

    @FXML private Button deleteButton;
    @FXML private Button copyButton;
    @FXML private Button pasteButton;
    @FXML private Button undoButton;
    @FXML private Button cutButton;
    @FXML private Button foregroundButton;
    @FXML private Button backgroundButton;
    @FXML private ColorPicker fillPicker;
    @FXML private ColorPicker borderPicker;
    @FXML private Spinner<Double> heightSpinner;
    @FXML private Spinner<Double> widthSpinner;
    // aggiunte per la gestione degli appunti e migliorare la user experience
    @FXML private Label cutCopyLabel;
    @FXML private Label emptyClipboardLabel;

    private ContextMenu shapeMenu; // Menu contestuale per le figure
    private ContextMenu canvasContextMenu; // Menu contestuale per il canvas (es. "Incolla qui")
    private boolean firstTime = true;   // per gestire la non comparsa della label appunti svuotati all'avvio

    // Costanti per la selezione e l'evidenziazione
    private static final double HANDLE_RADIUS = 3.0; // Raggio maniglie di selezione
    private static final double SELECTION_THRESHOLD = 5.0; // Tolleranza per la selezione (in unità del mondo)
    private static final double RESET_DRAG = -1; // Valore per indicare nessun trascinamento in corso

    // Componenti principali del controller
    private DrawingModel model;
    private GraphicsContext gc;
    private ShapeFactory currentShapeFactory; // Factory per la creazione della prossima figura
    private AbstractShape currentShape; // Figura attualmente selezionata
    private CommandManager commandManager; // Gestore comandi per undo/redo
    private ClipboardManager clipboardManager; // Gestore appunti per copia/incolla
    private FileOperationContext fileOperationContext; // Contesto per operazioni su file (salva/carica)
    private ZoomHandler zoomHandler; // Gestore per i livelli di zoom

    // Variabili per il trascinamento
    private double dragOffsetX;
    private double dragOffsetY;
    private double startDragX = RESET_DRAG;
    private double startDragY = RESET_DRAG;

    // Coordinate per "Incolla qui" (locali al canvas)
    private double lastCanvasMouseX;
    private double lastCanvasMouseY;


    public void setModel(DrawingModel model) {
        this.model = model;
        // Listener per ridisegnare il canvas quando le figure nel modello cambiano
        if (this.model != null && this.model.getShapes() != null) {
            this.model.getShapes().addListener((ListChangeListener.Change<? extends AbstractShape> c) -> redrawCanvas());
        }
    }

    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * Metodo chiamato dopo il caricamento del FXML.
     * Inizializza i componenti e imposta i gestori di eventi.
     */
    @FXML
    public void initialize() {
        if (drawingCanvas != null) {
            gc = drawingCanvas.getGraphicsContext2D();
            // Inizializza componenti mancanti
            if (this.model == null) this.model = new DrawingModel();
            if (this.commandManager == null) this.commandManager = new CommandManager();
            if (this.clipboardManager == null) this.clipboardManager = new ClipboardManager();

            setModel(this.model); // Imposta il modello e aggiunge listener

            this.fileOperationContext = new FileOperationContext(this);
            this.zoomHandler = new ZoomHandler(this, drawingCanvas);

            // Imposta i gestori per gli eventi del mouse sul canvas
            drawingCanvas.setOnMouseClicked(new MouseClickedHandler(drawingCanvas, this)::handleMouseEvent);
            drawingCanvas.setOnMousePressed(new MousePressedHandler(drawingCanvas, this)::handleMouseEvent);
            drawingCanvas.setOnMouseDragged(new MouseDraggedHandler(drawingCanvas, this)::handleMouseEvent);
            drawingCanvas.setOnMouseReleased(new MouseReleasedHandler(drawingCanvas, this)::handleMouseEvent);
            drawingCanvas.setOnMouseMoved(new MouseMovedHandler(drawingCanvas, this)::handleMouseEvent);

            createShapeContextMenu(); // Crea il menu contestuale per le figure
            createCanvasContextMenu(); // Crea il menu contestuale per il canvas (es. "Incolla qui")

            // Gestore per mostrare il menu contestuale del canvas (click destro su area vuota)
            drawingCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getButton() == MouseButton.SECONDARY) { // Click destro
                    AbstractShape clickedShape = null;
                    // Converte coordinate del click (locali al canvas) in coordinate del mondo
                    Point2D worldClickedPoint = zoomHandler.screenToWorld(event.getX(), event.getY());

                    // Verifica se il click è avvenuto su una figura esistente
                    for (AbstractShape shape : model.getShapesOrderedByZ()) { // Itera dalle figure più in alto (Z maggiore)
                        if (shape.containsPoint(worldClickedPoint.getX(), worldClickedPoint.getY(), SELECTION_THRESHOLD)) {
                            clickedShape = shape;
                            break;
                        }
                    }

                    if (clickedShape == null) { // Click su area vuota
                        if (clipboardManager.hasContent()) { // Se c'è contenuto negli appunti
                            lastCanvasMouseX = event.getX(); // Memorizza coordinate X (locali al canvas)
                            lastCanvasMouseY = event.getY(); // Memorizza coordinate Y (locali al canvas)

                            if (canvasContextMenu != null) {
                                updatePasteControlsState(); // Assicura che la voce "Incolla qui" sia abilitata/disabilitata correttamente
                                canvasContextMenu.show(drawingCanvas, event.getScreenX(), event.getScreenY()); // Mostra menu
                            }
                            if (shapeMenu != null) shapeMenu.hide(); // Nasconde l'altro menu se visibile
                        }
                    } else { // Click destro su una figura
                        // MousePressedHandler è responsabile di mostrare shapeMenu.
                        // Nasconde canvasContextMenu per sicurezza.
                        if (canvasContextMenu != null) canvasContextMenu.hide();
                    }
                } else { // Click non secondario
                    if (shapeMenu != null) shapeMenu.hide();
                    if (canvasContextMenu != null) canvasContextMenu.hide();
                }
            });

            // Colori iniziali per i color picker
            fillPicker.setValue(Color.LIGHTGREEN);
            borderPicker.setValue(Color.ORANGE);

            updateControlState(null); // Imposta stato iniziale controlli UI
            firstTime = false; // Dopo il primo avvio la label Appunti svuotati deve essere visibile quando Incolla viene disabilitato

            // Gestione focus e scorciatoie da tastiera
            rootPane.setFocusTraversable(true);
            rootPane.setOnKeyPressed(this::onRootKeyPressed);

            // Binding dimensioni canvas al contenitore
            drawingCanvas.widthProperty().bind(canvasContainer.widthProperty());
            drawingCanvas.heightProperty().bind(canvasContainer.heightProperty());

            // Listener per ridisegnare se le dimensioni del canvas cambiano
            drawingCanvas.widthProperty().addListener((obs, oldVal, newVal) -> redrawCanvas());
            drawingCanvas.heightProperty().addListener((obs, oldVal, newVal) -> redrawCanvas());
        } else {
            System.err.println("Errore: drawingCanvas non è stato iniettato da FXML!");
        }

        // Inizializzazione Spinner per altezza e larghezza
        if (heightSpinner != null) {
            SpinnerValueFactory<Double> heightFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000.0, ShapeFactory.DEFAULT_HEIGHT, 1.0);
            heightSpinner.setValueFactory(heightFactory);
            heightSpinner.setEditable(true);
            heightSpinner.valueProperty().addListener((obs, oldValue, newValue) -> { // Listener per modifiche
                if (newValue != null) handleDimensionChange(false, newValue); // false indica cambio altezza
            });
            configureSpinnerFocusListener(heightSpinner); // Gestisce commit del valore
            configureNumericTextFormatter(heightSpinner); // Formattatore per input numerico
        }

        if (widthSpinner != null) {
            SpinnerValueFactory<Double> widthFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000.0, ShapeFactory.DEFAULT_WIDTH, 1.0);
            widthSpinner.setValueFactory(widthFactory);
            widthSpinner.setEditable(true);
            widthSpinner.valueProperty().addListener((obs, oldValue, newValue) -> { // Listener per modifiche
                if (newValue != null) handleDimensionChange(true, newValue); // true indica cambio larghezza
            });
            configureSpinnerFocusListener(widthSpinner);
            configureNumericTextFormatter(widthSpinner);
        }

        currentShapeFactory = null; // Nessuna factory attiva all'inizio
        updatePasteControlsState(); // Aggiorna stato bottoni/menu incolla
        redrawCanvas(); // Primo disegno del canvas
    }

    /**
     * Crea il menu contestuale per le figure (Elimina, Copia, Incolla con offset).
     */
    private void createShapeContextMenu(){
        shapeMenu = new ContextMenu();
        MenuItem cutItem = new MenuItem("Taglia");
        MenuItem copyItem = new MenuItem("Copia");
        MenuItem pasteOffsetItem = new MenuItem("Incolla"); // Incolla con offset
        MenuItem deleteItem = new MenuItem("Elimina");
        MenuItem foregroundItem = new MenuItem("Porta in primo piano");
        MenuItem backgroundItem = new MenuItem("Porta in secondo piano");

        // Azioni per le voci di menu
        deleteItem.setOnAction(e -> handleDeleteShape(new ActionEvent()));
        cutItem.setOnAction(e -> handleCutShape(new ActionEvent()));
        copyItem.setOnAction(e -> handleCopyShape(new ActionEvent()));
        pasteOffsetItem.setOnAction(e -> handlePasteShape(new ActionEvent()));
        foregroundItem.setOnAction(e -> handleForegroundShape(new ActionEvent()));
        backgroundItem.setOnAction(e -> handleBackgroundShape(new ActionEvent()));

        // Icone per le voci di menu
        ImageView delimg = new ImageView(new Image(GeometricDrawingApp.class.getResourceAsStream("/icons/delCtxMenu.png")));
        delimg.setFitHeight(20); delimg.setFitWidth(20);
        ImageView cutimg = new ImageView(new Image(GeometricDrawingApp.class.getResourceAsStream("/icons/taglia1.png"))); // ## ICONA PER TAGLIA (DA AGGIUNGERE) ##
        cutimg.setFitHeight(18); cutimg.setFitWidth(18);
        ImageView copyimg = new ImageView(new Image(GeometricDrawingApp.class.getResourceAsStream("/icons/copyCtxMenu.png")));
        copyimg.setFitHeight(18); copyimg.setFitWidth(18);
        ImageView pasteimg = new ImageView(new Image(GeometricDrawingApp.class.getResourceAsStream("/icons/pasteCxtMenu.png")));
        pasteimg.setFitHeight(22); pasteimg.setFitWidth(22);
        ImageView forgrndimg = new ImageView(new Image(GeometricDrawingApp.class.getResourceAsStream("/icons/foreground.png")));
        forgrndimg.setFitHeight(20); forgrndimg.setFitWidth(20);
        ImageView backgrndimg = new ImageView(new Image(GeometricDrawingApp.class.getResourceAsStream("/icons/background.png")));
        backgrndimg.setFitHeight(20); backgrndimg.setFitWidth(20);

        deleteItem.setGraphic(delimg);
        cutItem.setGraphic(cutimg);
        copyItem.setGraphic(copyimg);
        pasteOffsetItem.setGraphic(pasteimg);
        foregroundItem.setGraphic(forgrndimg);
        backgroundItem.setGraphic(backgrndimg);

        shapeMenu.getItems().addAll(cutItem, copyItem, pasteOffsetItem, deleteItem, foregroundItem, backgroundItem);
    }

    /**
     * Crea il menu contestuale per il canvas (es. "Incolla qui").
     */
    private void createCanvasContextMenu() {
        canvasContextMenu = new ContextMenu();
        MenuItem pasteHereItem = new MenuItem("Incolla qui"); // Voce per incollare alle coordinate del click
        ImageView pasteCanvasImg = new ImageView(new Image(GeometricDrawingApp.class.getResourceAsStream("/icons/pasteCxtMenu.png"))); // Riutilizza icona
        pasteCanvasImg.setFitHeight(20);
        pasteCanvasImg.setFitWidth(20);
        pasteHereItem.setGraphic(pasteCanvasImg);

        pasteHereItem.setOnAction(e -> {
            if (clipboardManager.hasContent()) {
                // lastCanvasMouseX/Y sono coordinate locali al canvas, memorizzate dal click destro.
                // handlePasteShape le userà per posizionare la figura.
                handlePasteShape(new ActionEvent(), lastCanvasMouseX, lastCanvasMouseY);
            }
        });
        canvasContextMenu.getItems().add(pasteHereItem);
    }

    /**
     * Configura un TextFormatter per uno Spinner per accettare solo input numerici (double).
     * @param spinner Lo Spinner da configurare.
     */
    private void configureNumericTextFormatter(Spinner<Double> spinner) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            // Regex per double (opzionale negativo, opzionale parte decimale con . o ,)
            if (newText.matches("-?\\d*([.,]\\d*)?")) {
                return change;
            }
            return null; // Rifiuta la modifica se non valida
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        spinner.getEditor().setTextFormatter(textFormatter);

        // Listener per aggiornare il valore dello Spinner quando il testo nel suo editor cambia
        // (es. dopo input diretto dell'utente).
        textFormatter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                try {
                    double value = Double.parseDouble(newVal.replace(',', '.')); // Sostituisce , con . per il parsing
                    // Aggiorna il valore della factory solo se è diverso, per evitare cicli.
                    if (spinner.getValueFactory() != null && spinner.getValueFactory().getValue() != value) {
                        spinner.getValueFactory().setValue(value);
                    }
                } catch (NumberFormatException e) {
                    // Ignora errore di parsing (l'utente potrebbe stare ancora digitando)
                    // Il commit avverrà quando lo spinner perde il focus (vedi configureSpinnerFocusListener)
                }
            }
        });
    }

    /**
     * Configura un listener per lo Spinner che committa il valore del testo
     * quando l'editor dello Spinner perde il focus.
     * @param spinner Lo Spinner da configurare.
     */
    private void configureSpinnerFocusListener(Spinner<Double> spinner) {
        spinner.getEditor().focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused && spinner.getValueFactory() != null) { // Ha perso il focus
                try {
                    String text = spinner.getEditor().getText().replace(',', '.');
                    double value = Double.parseDouble(text);
                    SpinnerValueFactory.DoubleSpinnerValueFactory factory = (SpinnerValueFactory.DoubleSpinnerValueFactory) spinner.getValueFactory();
                    // Assicura che il valore sia nei limiti min/max della factory
                    if (value < factory.getMin()) value = factory.getMin();
                    if (value > factory.getMax()) value = factory.getMax();
                    factory.setValue(value); // Imposta il valore (triggera valueProperty dello spinner)
                } catch (NumberFormatException e) {
                    // Se il parsing fallisce (es. testo non valido), ripristina il testo dell'editor
                    // al valore attuale valido dello spinner.
                    spinner.getEditor().setText(String.valueOf(spinner.getValue()).replace('.', ','));
                }
            }
        });
    }

    /**
     * Gestisce la pressione di tasti a livello del rootPane (per scorciatoie).
     */
    @FXML
    private void onRootKeyPressed(KeyEvent event) {
        // Cancella figura con DEL o BACKSPACE
        if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
            handleDeleteShape(new ActionEvent());
            event.consume(); // Evento consumato
        }
        // Taglia con CTRL+X
        if (KeyCombination.keyCombination("CTRL+X").match(event)) {
            handleCutShape(new ActionEvent());
            event.consume();
        }
        // Copia con CTRL+C
        if (KeyCombination.keyCombination("CTRL+C").match(event)) {
            handleCopyShape(new ActionEvent());
            event.consume();
        }
        // Annulla con CTRL+Z
        if (KeyCombination.keyCombination("CTRL+Z").match(event)) {
            handleUndo(new ActionEvent());
            event.consume();
        }
        // Incolla con CTRL+V (incolla con offset di default)
        if (KeyCombination.keyCombination("CTRL+V").match(event)) {
            if (clipboardManager.hasContent()) {
                handlePasteShape(new ActionEvent()); // Chiama incolla con offset
            }
            event.consume();
        }
    }

    /**
     * Prepara il controller per la creazione di una nuova figura.
     * @param factory La factory per il tipo di figura da creare.
     * @param disableFillPicker Se il fill picker deve essere disabilitato per questa factory.
     * @param disableBorderPickerForFactory Se il border picker deve essere disabilitato per questa factory.
     */
    private void initializeShapeSelection(ShapeFactory factory, boolean disableFillPicker, boolean disableBorderPickerForFactory) {
        currentShape = null; // Deseleziona figura corrente
        updateControlState(null); // Aggiorna stato UI
        redrawCanvas(); // Rimuove evidenziazione precedente

        // Disabilita/Abilita picker in base al tipo di figura in creazione
        fillPicker.setDisable(disableFillPicker);
        if (factory instanceof LineFactory) { // Per la Linea, il bordo è sempre possibile
            borderPicker.setDisable(false);
        } else if (factory instanceof RectangleFactory || factory instanceof EllipseFactory) { // Per Rettangolo/Ellisse, il bordo è possibile
            borderPicker.setDisable(false);
        }
        // Altri stati dei picker (es. dopo la selezione effettiva) sono gestiti da updateControlState.

        currentShapeFactory = factory; // Imposta la factory attiva
        if (drawingCanvas != null) drawingCanvas.setCursor(Cursor.CROSSHAIR); // Cambia cursore
    }

    // Gestori per i bottoni di selezione tipo figura
    @FXML
    public void handleSelectLinea(ActionEvent event) {
        initializeShapeSelection(new LineFactory(), true, false); // Linea: no fill, sì border
    }

    @FXML
    public void handleSelectRettangolo(ActionEvent event) {
        initializeShapeSelection(new RectangleFactory(), false, false); // Rettangolo: sì fill, sì border
    }

    @FXML
    public void handleSelectEllisse(ActionEvent event) {
        initializeShapeSelection(new EllipseFactory(), false, false); // Ellisse: sì fill, sì border
    }

    /**
     * Controlla se la nuova figura, posizionata alle coordinate del mondo specificate,
     * uscirebbe dai limiti del canvas.
     * @param newShape La figura da controllare.
     * @param worldX Coordinata X (del mondo) per il posizionamento.
     * @param worldY Coordinata Y (del mondo) per il posizionamento.
     * @return true se la figura è troppo vicina ai bordi o fuori, false altrimenti.
     */
    public boolean isTooClose(AbstractShape newShape, double worldX, double worldY) {
        if (newShape == null || drawingCanvas == null || zoomHandler == null) return true;

        double shapeWidth = newShape.getWidth();
        double shapeHeight = newShape.getHeight();

        // Dimensioni del canvas in coordinate del mondo
        double worldCanvasWidth = drawingCanvas.getWidth() / zoomHandler.getZoomFactor();
        double worldCanvasHeight = drawingCanvas.getHeight() / zoomHandler.getZoomFactor();

        // Controlla se una qualsiasi parte della figura cade fuori dai limiti
        if (worldX < 0 || worldY < 0 ||
                worldX + shapeWidth > worldCanvasWidth ||
                worldY + shapeHeight > worldCanvasHeight) {
            return true; // Fuori dai limiti
        }
        return false; // All'interno dei limiti
    }

    /**
     * Gestisce la modifica di dimensione (larghezza o altezza) di una figura tramite Spinner.
     * @param isWidth true se si modifica la larghezza, false per l'altezza.
     * @param newValue Il nuovo valore della dimensione.
     */
    private void handleDimensionChange(boolean isWidth, Double newValue) {
        if (currentShape == null || newValue == null || model == null || commandManager == null) {
            return; // Nessuna figura selezionata o valore non valido
        }
        // Impedisce dimensioni non positive
        if (newValue <= 0) {
            // Ripristina il valore precedente nello spinner
            if (isWidth) {
                widthSpinner.getValueFactory().setValue(currentShape.getWidth());
            } else {
                heightSpinner.getValueFactory().setValue(currentShape.getHeight());
            }
            return; // Non esegue il comando
        }

        Command cmd = null; // Comando da eseguire
        if (isWidth) {
            // Crea comando solo se il valore è effettivamente cambiato (evita comandi doppi per piccole fluttuazioni)
            if (Math.abs(currentShape.getWidth() - newValue) > 0.001) { // Tolleranza per double
                cmd = new ChangeWidthCommand(model, currentShape, newValue);
            }
        } else { // Modifica altezza
            if (!(getBaseShape(currentShape) instanceof Line)) { // L'altezza della linea non è modificabile direttamente
                if (Math.abs(currentShape.getHeight() - newValue) > 0.001) {
                    cmd = new ChangeHeightCommand(model, currentShape, newValue);
                }
            }
        }
        if (cmd != null) {
            commandManager.executeCommand(cmd); // Esegue il comando
            redrawCanvas(); // Ridisegna per mostrare la modifica
        }
    }

    /**
     * Gestisce il cambio di colore del bordo della figura selezionata.
     * @param newColor Il nuovo colore del bordo.
     */
    public void handleChangeBorderColor(Color newColor) {
        if (currentShape == null || newColor == null) return;

        AbstractShape shapeNavigator = currentShape;
        BorderColorDecorator bcd = null; // Riferimento al decoratore del bordo

        // Cerca il BorderColorDecorator più esterno (o l'unico)
        while (shapeNavigator instanceof ShapeDecorator) {
            if (shapeNavigator instanceof BorderColorDecorator) {
                bcd = (BorderColorDecorator) shapeNavigator;
                break;
            }
            shapeNavigator = ((ShapeDecorator) shapeNavigator).getInnerShape(); // Scende al decoratore interno
        }
        // Caso in cui currentShape stesso è il BorderColorDecorator (non ulteriormente wrappato per il bordo)
        if (bcd == null && currentShape instanceof BorderColorDecorator) {
            bcd = (BorderColorDecorator) currentShape;
        }

        // Se trovato e il colore è diverso, crea ed esegue il comando
        if (bcd != null && !bcd.getBorderColor().equals(newColor)) {
            ChangeBorderColorCommand cmd = new ChangeBorderColorCommand(model, bcd, newColor);
            commandManager.executeCommand(cmd);
            redrawCanvas();
        }
    }

    /**
     * Gestisce il cambio di colore di riempimento della figura selezionata.
     * @param newColor Il nuovo colore di riempimento.
     */
    public void handleChangeFillColor(Color newColor) {
        if (currentShape == null || newColor == null) return;
        if (getBaseShape(currentShape) instanceof Line) return; // Le linee non hanno riempimento

        AbstractShape shapeNavigator = currentShape;
        FillColorDecorator fcd = null; // Riferimento al decoratore di riempimento

        // Cerca il FillColorDecorator
        while (shapeNavigator instanceof ShapeDecorator) {
            if (shapeNavigator instanceof FillColorDecorator) {
                fcd = (FillColorDecorator) shapeNavigator;
                break;
            }
            shapeNavigator = ((ShapeDecorator) shapeNavigator).getInnerShape();
        }
        if (fcd == null && currentShape instanceof FillColorDecorator) {
            fcd = (FillColorDecorator) currentShape;
        }

        // Se trovato e il colore è diverso, crea ed esegue il comando
        if (fcd != null && !fcd.getFillColor().equals(newColor)) {
            ChangeFillColorCommand cmd = new ChangeFillColorCommand(model, fcd, newColor);
            commandManager.executeCommand(cmd);
            redrawCanvas();
        }
    }

    /**
     * Chiamato quando viene selezionato un colore dal ColorPicker del bordo.
     */
    @FXML
    public void onBorderColorPicked() {
        if (currentShape != null) { // Agisce solo se una figura è selezionata
            handleChangeBorderColor(borderPicker.getValue());
        }
    }

    /**
     * Chiamato quando viene selezionato un colore dal ColorPicker del riempimento.
     */
    @FXML
    public void onFillColorPicked() {
        // Agisce solo se una figura è selezionata e non è una linea
        if (currentShape != null && !(getBaseShape(currentShape) instanceof Line)) {
            handleChangeFillColor(fillPicker.getValue());
        }
    }

    /**
     * Restituisce la forma base (non decorata) da una forma potenzialmente decorata.
     */
    private AbstractShape getBaseShape(AbstractShape shape) {
        AbstractShape base = shape;
        while (base instanceof ShapeDecorator) { // Scende attraverso i decoratori
            base = ((ShapeDecorator) base).getInnerShape();
        }
        return base;
    }

    /**
     * Aggiorna lo stato (abilitato/disabilitato) dei controlli UI in base alla figura selezionata.
     * @param shape La figura attualmente selezionata, o null se nessuna.
     */
    public void updateControlState(AbstractShape shape) {
        boolean enableWidth = false;
        boolean enableHeight = false;
        boolean enableFillPicker = false;
        boolean enableBorderPicker = false;
        boolean enableDelete = false;
        boolean enableCutUi;
        boolean enablePaste = false;
        boolean enableCopy = false;
        boolean enableCut = false;
        boolean enableForeground = false;
        boolean enableBackground = false;

        currentShape = shape;

        // Per gestire il comando di annullamento (undo)
        if (commandManager != null) {
            // Verifico se ci sono comandi nello stack
            boolean hasUndoableCommands = !commandManager.getCommandStack().isEmpty();
            if (undoButton != null) {
                undoButton.setDisable(!hasUndoableCommands);
            }
        }

        // Controllo contenuto appunti per Incolla
        if (clipboardManager != null) {
            enablePaste = clipboardManager.hasContent();
        }

        // se c'è una figura selezionata
        if (shape != null) {
            // capisci il tipo di figura SENZA decorator per gestire riempimento e altezza (che dovrebbero essere disabilitati)
            AbstractShape baseShape = getBaseShape(shape);
            enableWidth = true;
            enableDelete = true;
            enableCutUi = true;
            enableCopy = true;
            enableCut = true;
            enableBackground = true;
            enableForeground = true;
            enableBorderPicker = true;

            if (!(baseShape instanceof Line)) {
                enableHeight = true;
                enableFillPicker = true;
            }
        } else {
            enableCutUi = false;
            // La figura non è selezionata, ma stai procedendo alla creazione di una nuova figura
            if (currentShapeFactory != null) {
                if (currentShapeFactory instanceof LineFactory) {
                    enableFillPicker = false;
                    enableBorderPicker = true;
                } else {
                    enableFillPicker = true;
                    enableBorderPicker = true;
                }
                enableWidth = false;
                enableHeight = false;
            }
        }

        // Imposta lo stato dei controlli
        if (widthSpinner != null) widthSpinner.setDisable(!enableWidth);
        if (heightSpinner != null) heightSpinner.setDisable(!enableHeight);
        if (fillPicker != null) fillPicker.setDisable(!enableFillPicker);
        if (borderPicker != null) borderPicker.setDisable(!enableBorderPicker);
        if (deleteButton != null) deleteButton.setDisable(!enableDelete);
        if (cutButton != null) cutButton.setDisable(!enableCutUi);
        if (copyButton != null) copyButton.setDisable(!enableCopy);
        if (cutButton != null) cutButton.setDisable(!enableCut);
        if (foregroundButton != null) foregroundButton.setDisable(!enableForeground);
        if (backgroundButton != null) backgroundButton.setDisable(!enableBackground);

        // la gestione di incolla è legata anche alla visualizzazione della label degli appunti svuotati
        if (pasteButton != null) {
            boolean wasEnabled = !pasteButton.isDisabled(); // com'era prima
            pasteButton.setDisable(!enablePaste);           // aggiorna stato

            // Se prima era abilitato e ora è disabilitato, mostra label
            if (!firstTime && wasEnabled && !enablePaste) {
                showClipboardEmptyLabel();
            }
        }


        if (shapeMenu != null) {
            shapeMenu.getItems().stream()
                    .filter(item -> "Taglia".equals(item.getText()))
                    .findFirst()
                    .ifPresent(item -> item.setDisable(!enableCutUi));
        }
    }

    /**
     * Aggiorna lo stato dei controlli di Incolla (bottone e voci di menu)
     * in base al contenuto degli appunti.
     */
    private void updatePasteControlsState() {
        boolean hasContent = clipboardManager != null && clipboardManager.hasContent();
        // Bottone Incolla
        if (pasteButton != null) {
            pasteButton.setDisable(!hasContent);
        }
        // Voce "Incolla" (con offset) nel menu contestuale delle figure
        if (shapeMenu != null) {
            shapeMenu.getItems().stream()
                    .filter(item -> "Incolla".equals(item.getText())) // Cerca "Incolla"
                    .findFirst()
                    .ifPresent(item -> item.setDisable(!hasContent));
        }
        // Voce "Incolla qui" nel menu contestuale del canvas
        if (canvasContextMenu != null) {
            canvasContextMenu.getItems().stream()
                    .filter(item -> "Incolla qui".equals(item.getText())) // Cerca "Incolla qui"
                    .findFirst()
                    .ifPresent(item -> item.setDisable(!hasContent));
        }
    }

    /**
     * Gestisce l'azione di cancellazione della figura selezionata.
     */
    @FXML
    public void handleDeleteShape(ActionEvent event) {
        if (currentShape != null && model != null && commandManager != null) {
            if(shapeMenu != null) shapeMenu.hide(); // Nasconde menu se aperto
            DeleteShapeCommand deleteCmd = new DeleteShapeCommand(model, currentShape);
            commandManager.executeCommand(deleteCmd);
            setCurrentShape(null); // Deseleziona la figura
            updateControlState(null); // Aggiorna UI
            redrawCanvas();
        }
    }

    @FXML
    public void handleCutShape(ActionEvent event) {
        if (currentShape != null && model != null && commandManager != null && clipboardManager != null) {
            if (shapeMenu != null) shapeMenu.hide(); // Nasconde menu se aperto

            CutShapeCommand cutCmd = new CutShapeCommand(model, currentShape, clipboardManager);
            commandManager.executeCommand(cutCmd);
            showCutCopyLabel();

            setCurrentShape(null);      // Deseleziona la figura
            updateControlState(null);   // Aggiorna UI
            updatePasteControlsState(); // Aggiorna disponibilità Incolla
            redrawCanvas();
        }
    }

    @FXML
    public void handleForegroundShape(ActionEvent event) {
        if (currentShape != null && model != null && commandManager != null) {
            if(shapeMenu != null) shapeMenu.hide(); // Nasconde il menu contestuale se risulta aperto

            BringToForegroundCommand cmd = new BringToForegroundCommand(model, currentShape);
            commandManager.executeCommand(cmd);
            redrawCanvas();
        }
    }

    @FXML
    public void handleBackgroundShape(ActionEvent event) {
        if (currentShape != null && model != null && commandManager != null) {
            if(shapeMenu != null) shapeMenu.hide(); // Nasconde il menu contestuale se risulta aperto

            BringToBackgroundCommand cmd = new BringToBackgroundCommand(model, currentShape);
            commandManager.executeCommand(cmd);
            redrawCanvas();
        }
    }

    /**
     * Gestisce l'azione di copia della figura selezionata negli appunti.
     */
    @FXML
    public void handleCopyShape(ActionEvent event) {
        if (currentShape != null && commandManager != null && clipboardManager != null) {
            CopyShapeCommand copyCmd = new CopyShapeCommand(currentShape, clipboardManager);
            commandManager.executeCommand(copyCmd);
            showCutCopyLabel();
            updatePasteControlsState(); // Aggiorna disponibilità Incolla
        }
    }

    /**
     * Mostra una label temporanea (1 sec)
     * per indicare la corretta esecuzione delle operazioni di Taglia/Copia.
     */
    public void showCutCopyLabel() {
        cutCopyLabel.setVisible(true);

        // Nasconde la label dopo 2 secondi (o quanto preferisci)
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> {
            cutCopyLabel.setVisible(false);
        });
        delay.play();
    }

    /**
     * Mostra una label temporanea (1 sec)
     * per indicare che la sezione appunti è stata svuotata post undo
     */
    private void showClipboardEmptyLabel() {
        emptyClipboardLabel.setVisible(true);

        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> emptyClipboardLabel.setVisible(false));
        delay.play();
    }

    /**
     * Gestisce l'azione di Incolla dal bottone (o scorciatoia CTRL+V),
     * che incolla con un offset di default.
     */
    @FXML
    public void handlePasteShape(ActionEvent event) {
        // Chiama la versione più specifica con coordinate target -1 per indicare offset di default.
        handlePasteShape(event, -1, -1);
    }

    /**
     * Gestisce l'azione di Incolla, posizionando la figura.
     * Se canvasLocalTargetX/Y sono -1, usa un offset di default.
     * Altrimenti, usa canvasLocalTargetX/Y (coordinate locali al canvas) per posizionare la figura.
     * @param event L'evento che ha scatenato l'azione.
     * @param canvasLocalTargetX Coordinata X locale al canvas per "Incolla qui", o -1.
     * @param canvasLocalTargetY Coordinata Y locale al canvas per "Incolla qui", o -1.
     */
    public void handlePasteShape(ActionEvent event, double canvasLocalTargetX, double canvasLocalTargetY) {
        if (model != null && commandManager != null && clipboardManager != null && clipboardManager.hasContent()) {
            PasteShapeCommand pasteCmd;
            // Ottiene una copia della figura dagli appunti per calcolarne le dimensioni per il centraggio.
            AbstractShape shapeDetailsForPositioning = clipboardManager.getFromClipboard();
            if (shapeDetailsForPositioning == null) return; // Nessuna figura valida da incollare

            if (canvasLocalTargetX != -1 && canvasLocalTargetY != -1) { // Coordinate valide fornite (per "Incolla qui")
                // Converte le coordinate del click (locali al canvas) in coordinate del mondo
                Point2D worldPastedClickPoint = zoomHandler.screenToWorld(canvasLocalTargetX, canvasLocalTargetY);

                // Calcola le coordinate del mondo per l'angolo alto-sinistra della figura
                // in modo che sia centrata sul punto del click.
                double finalWorldX = worldPastedClickPoint.getX() - (shapeDetailsForPositioning.getWidth() / 2.0);
                double finalWorldY = worldPastedClickPoint.getY() - (shapeDetailsForPositioning.getHeight() / 2.0);
                pasteCmd = new PasteShapeCommand(model, clipboardManager, finalWorldX, finalWorldY, true); // true per coordinate assolute
            } else {
                // Incolla con offset di default (es. da bottone Incolla o menu figura)
                pasteCmd = new PasteShapeCommand(model, clipboardManager);
            }

            commandManager.executeCommand(pasteCmd); // Esegue il comando di Incolla

            AbstractShape pastedShape = pasteCmd.getPastedShape(); // Ottiene la figura effettivamente incollata
            if (pastedShape != null) {
                setCurrentShape(pastedShape); // Seleziona la nuova figura incollata
                updateControlState(pastedShape); // Aggiorna UI per la nuova selezione
                updateSpinners(pastedShape); // Aggiorna valori spinner
            }
            redrawCanvas(); // Ridisegna il canvas
            updatePasteControlsState(); // Aggiorna stato controlli Incolla
        }
    }

    /**
     * Gestisce l'azione di Annulla (Undo).
     */
    @FXML
    public void handleUndo(ActionEvent event) {
        if (model != null && commandManager != null) {
            if(shapeMenu != null) shapeMenu.hide(); // Nasconde menu se aperto
            commandManager.undo(); // Esegue undo sull'ultimo comando
            // Dopo l'undo, la figura selezionata potrebbe non esistere più o essere cambiata.
            // È più sicuro deselezionare o rivalutare la selezione.
            setCurrentShape(null); // Deseleziona
            updateControlState(null);
            updateSpinners(null); // Resetta spinner
            redrawCanvas();
        }
    }

    /**
     * gestisce l'azione di creazione di una nuova area di lavoro quando si clicca sul menu File -> Nuovo
     */
    @FXML
    public void handleNewWorkspace(ActionEvent event) {
        this.newWorkspace.handleNewWorkspace(); // Chiama il metodo per creare una nuova area di lavoro
    }

//    /**
//     * Crea una nuova area di lavoro vuota
//     */
//    private void createNewWorkspace() {
//        // Resetta il modello
//        model.clear();
//
//        // Resetta lo stato del controller
//        setCurrentShape(null);
//        setCurrentShapeFactory(null);
//
//        // Aggiorna l'interfaccia
//        updateControlState(null);
//        updateSpinners(null);
//        redrawCanvas();
//    }

    /**
     * Seleziona la figura alle coordinate del mondo specificate (worldX, worldY).
     * @return La figura selezionata, o null se nessuna figura è trovata.
     */
    public AbstractShape selectShapeAt(double worldX, double worldY) {
        if (model == null) return null;
        // SELECTION_THRESHOLD è la tolleranza in unità del mondo.
        AbstractShape selected = null;
        // Itera sulle figure in ordine di Z decrescente (da quella più in alto a quella più in basso)
        for (AbstractShape shape : model.getShapesOrderedByZ()) {
            if (shape.containsPoint(worldX, worldY, SELECTION_THRESHOLD)) {
                selected = shape; // Figura trovata
                break;
            }
        }

        setCurrentShape(selected); // Imposta la figura corrente nel controller
        updateSpinners(selected);    // Aggiorna gli spinner con le dimensioni della figura
        updateControlState(selected); // Aggiorna lo stato generale dei controlli UI

        // Output di debug (opzionale)
        if (selected != null) {
            System.out.println("DEBUG: Figura selezionata: " + selected + " z: " + selected.getZ());
        } else {
            System.out.println("DEBUG: Nessuna figura selezionata.");
        }
        return selected; // Restituisce la figura selezionata (o null)
    }

    /**
     * Aggiorna i valori degli Spinner di larghezza e altezza in base alla figura selezionata.
     * @param shape La figura selezionata, o null.
     */
    public void updateSpinners(AbstractShape shape) {
        if (widthSpinner == null || heightSpinner == null || widthSpinner.getValueFactory() == null || heightSpinner.getValueFactory() == null) return;

        if (shape != null) { // Se una figura è selezionata
            AbstractShape baseShape = getBaseShape(shape);
            widthSpinner.getValueFactory().setValue(baseShape.getWidth()); // Imposta larghezza
            if (baseShape instanceof Line) {
                // Per la Linea, l'altezza non è direttamente modificabile.
                // Lo spinner altezza mostrerà un valore di default (es. il suo minimo)
                // e sarà disabilitato da updateControlState.
                heightSpinner.getValueFactory().setValue(Math.max(1.0, baseShape.getHeight())); // Mostra almeno 1.0 o altezza reale
            } else { // Per Rettangolo o Ellisse
                heightSpinner.getValueFactory().setValue(baseShape.getHeight()); // Imposta altezza
            }
        } else { // Nessuna figura selezionata
            // Resetta gli spinner ai valori di default della factory
            widthSpinner.getValueFactory().setValue(ShapeFactory.DEFAULT_WIDTH);
            heightSpinner.getValueFactory().setValue(ShapeFactory.DEFAULT_HEIGHT);
            // Gli spinner saranno disabilitati da updateControlState se nessuna figura è selezionata.
        }
    }

    /**
     * Mostra il menu contestuale della figura (shapeMenu) alle coordinate dell'evento mouse.
     * Viene chiamato da MousePressedHandler quando si fa click destro su una figura.
     */
    public void showContextMenu(MouseEvent event) {
        // Mostra il menu solo se una figura è effettivamente selezionata
        if (shapeMenu != null && currentShape != null) {
            updatePasteControlsState(); // Assicura che la voce "Incolla" sia aggiornata

            boolean enableCutUi = currentShape != null;
            shapeMenu.getItems().stream()
                    .filter(item -> "Taglia".equals(item.getText()))
                    .findFirst()
                    .ifPresent(item -> item.setDisable(!enableCutUi));

            shapeMenu.show(drawingCanvas, event.getScreenX(), event.getScreenY());
        }
    }

    /**
     * Ridisegna l'intero contenuto del canvas.
     * Cancella il canvas e ridisegna tutte le figure nel modello.
     * Applica la trasformazione di zoom.
     * Evidenzia la figura correntemente selezionata.
     */
    public void redrawCanvas() {
        if (gc == null || drawingCanvas == null || model == null || zoomHandler == null) {
            return; // Componenti non pronti
        }

        // Pulisce il canvas
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());

        gc.save(); // Salva lo stato corrente del GraphicsContext (es. trasformazioni)
        zoomHandler.applyZoomTransformation(gc); // Applica la scala per lo zoom

        // Disegna tutte le figure nel modello (l'ordine di disegno influisce sulla sovrapposizione visiva)
        // model.getShapes() di solito è in ordine di inserimento. Per un controllo Z-order più fine,
        // si potrebbe usare model.getShapesOrderedByZ() se l'ordine di disegno deve seguire Z.
        // Qui, si assume che l'ordine di model.getShapes() sia corretto per il disegno.
        for (AbstractShape shape : model.getShapes()) {
            if (shape != null) {
                shape.draw(gc); // Chiama il metodo draw() di ogni figura
                if (shape == currentShape) { // Se la figura è quella selezionata
                    drawHighlightBorder(shape); // Disegna il bordo di evidenziazione
                }
            }
        }
        gc.restore(); // Ripristina lo stato del GraphicsContext precedente allo save()
    }

    /**
     * Disegna il bordo di evidenziazione e le maniglie per la figura selezionata.
     * @param shape La figura da evidenziare.
     */
    private void drawHighlightBorder(AbstractShape shape) {
        AbstractShape baseShape = getBaseShape(shape); // Ottiene la forma base (non decorata)

        // Impostazioni per il bordo di selezione (tratteggiato, colore specifico)
        double lineWidthOnScreen = 1.0; // Spessore desiderato per la linea di selezione sullo schermo
        double worldLineWidth = lineWidthOnScreen / zoomHandler.getZoomFactor(); // Spessore in coordinate del mondo

        gc.setStroke(Color.SKYBLUE); // Colore del bordo di selezione
        gc.setLineWidth(worldLineWidth); // Spessore linea (scalato per zoom)
        gc.setLineDashes(5 * worldLineWidth); // Linea tratteggiata (tratteggio scalato)

        if (baseShape instanceof Line line) { // Se è una linea, evidenzia la linea stessa e le sue estremità
            gc.strokeLine(line.getX(), line.getY(), line.getEndX(), line.getEndY());
            drawHandle(line.getX(), line.getY()); // Maniglia all'inizio
            drawHandle(line.getEndX(), line.getEndY()); // Maniglia alla fine
        } else { // Se è un rettangolo o ellisse (o altra forma con bounding box)
            // Disegna un rettangolo tratteggiato attorno al bounding box
            gc.strokeRect(baseShape.getX(), baseShape.getY(), baseShape.getWidth(), baseShape.getHeight());
            // Disegna maniglie agli angoli del bounding box
            drawHandle(baseShape.getX(), baseShape.getY()); // Alto-sinistra
            drawHandle(baseShape.getX() + baseShape.getWidth(), baseShape.getY()); // Alto-destra
            drawHandle(baseShape.getX(), baseShape.getY() + baseShape.getHeight()); // Basso-sinistra
            drawHandle(baseShape.getX() + baseShape.getWidth(), baseShape.getY() + baseShape.getHeight()); // Basso-destra
            // Opzionale: maniglie ai punti medi dei lati
        }
        gc.setLineDashes(0); // Ripristina linea continua per disegni successivi
        // gc.setLineWidth(1.0); // Opzionale: ripristina spessore linea default (in unità del mondo)
    }

    /**
     * Disegna una maniglia di selezione (un piccolo cerchio) alle coordinate del mondo specificate.
     * @param worldX Coordinata X (del mondo) del centro della maniglia.
     * @param worldY Coordinata Y (del mondo) del centro della maniglia.
     */
    private void drawHandle(double worldX, double worldY) {
        // HANDLE_RADIUS è il raggio desiderato sullo schermo. Converti in unità del mondo.
        double handleRadiusWorld = HANDLE_RADIUS / zoomHandler.getZoomFactor();
        gc.setFill(Color.SKYBLUE); // Colore della maniglia
        // Disegna un cerchio (ovale con uguali raggioX e raggioY)
        gc.fillOval(worldX - handleRadiusWorld, worldY - handleRadiusWorld,
                handleRadiusWorld * 2, handleRadiusWorld * 2); // x, y, larghezza, altezza dell'ovale
    }

    // --- Gestori per i bottoni del menu File e Zoom ---
    @FXML public void handleSaveSerialized(ActionEvent event) {
        if (fileOperationContext != null) {
            fileOperationContext.setStrategySave(new SerializedSaveStrategy() {
            });
            fileOperationContext.executeSave();
        }
    }
    @FXML public void handleLoadSerialized(ActionEvent event) {
        if (fileOperationContext != null){
            fileOperationContext.setStrategyLoad(new SerializedLoadStrategy() {});
            fileOperationContext.executeLoad();
        } }
    @FXML public void handleSaveAsPng(ActionEvent event) {
        if (fileOperationContext != null){
            fileOperationContext.setStrategySave(new PngSaveStrategy() {});
            fileOperationContext.executeSave();
        } }
    @FXML public void handleSaveAsPdf(ActionEvent event) {
        if (fileOperationContext != null){
            fileOperationContext.setStrategySave(new PdfSaveStrategy() {});
            fileOperationContext.executeSave();
        } }

    @FXML private void handleZoom25() { if (zoomHandler != null) zoomHandler.setZoom25(); }
    @FXML private void handleZoom50() { if (zoomHandler != null) zoomHandler.setZoom50(); }
    @FXML private void handleZoom75() { if (zoomHandler != null) zoomHandler.setZoom75(); }
    @FXML private void handleZoom100() { if (zoomHandler != null) zoomHandler.setZoom100(); }
    @FXML private void handleZoom150() { if (zoomHandler != null) zoomHandler.setZoom150(); }

    @FXML
    private void handleCloseFile() {
        Exit exit = new Exit(this);
        exit.exit();
        }


    // --- Metodi Getter e Setter usati principalmente dai gestori eventi o per test ---
    public Window getWindow() { return (drawingCanvas != null && drawingCanvas.getScene() != null) ? drawingCanvas.getScene().getWindow() : null; }

    public void showAlertDialog(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // Nessun header text
        alert.setContentText(content);
        alert.showAndWait(); // Mostra e attende chiusura
    }

    // Metodi di accesso e modifica per lo stato del controller
    public AbstractShape getCurrentShape() { return currentShape; }

    /** Imposta la figura corrente e gestisce la factory e il cursore. */
    public void setCurrentShape(AbstractShape shape) {
        this.currentShape = shape;
        // Se una figura viene selezionata (o deselezionata), e non si è in fase di creazione,
        // la factory attiva per creare nuove figure va resettata.
        if (shape != null) { // Se si seleziona una figura esistente
            this.currentShapeFactory = null; // Resetta la factory per nuove forme
            if(drawingCanvas != null) drawingCanvas.setCursor(Cursor.DEFAULT); // Cursore di default
        }
        // L'aggiornamento dello stato dei controlli (updateControlState)
        // sarà chiamato da chi invoca setCurrentShape (es. selectShapeAt, handleDeleteShape).
    }

    /**
     * Imposta la factory per la creazione di nuove figure e aggiorna il cursore.
     */
    public void setCurrentShapeFactory(ShapeFactory currentShapeFactory) {
        this.currentShapeFactory = currentShapeFactory;
        if (this.currentShapeFactory == null && drawingCanvas != null) {
            drawingCanvas.setCursor(Cursor.DEFAULT); // Cursore default se nessuna factory
        } else if (this.currentShapeFactory != null && drawingCanvas != null) {
            drawingCanvas.setCursor(Cursor.CROSSHAIR); // Cursore a croce se factory attiva
        }
    }
    public ShapeFactory getCurrentShapeFactory() { return currentShapeFactory; }

    public void resetDrag() { startDragX = RESET_DRAG; startDragY = RESET_DRAG; } // Resetta stato trascinamento
    public boolean isDragging() { return startDragX != RESET_DRAG && startDragY != RESET_DRAG; } // Verifica se in trascinamento

    public ContextMenu getShapeMenu() { return shapeMenu; } // Per MousePressedHandler

    public double getDragOffsetX() { return dragOffsetX; }
    public void setDragOffsetX(double dragOffsetX) { this.dragOffsetX = dragOffsetX; }
    public double getDragOffsetY() { return dragOffsetY; }
    public void setDragOffsetY(double dragOffsetY) { this.dragOffsetY = dragOffsetY; }

    public double getStartDragX() { return startDragX; }
    public void setStartDragX(double startDragX) { this.startDragX = startDragX; }
    public double getStartDragY() { return startDragY; }
    public void setStartDragY(double startDragY) { this.startDragY = startDragY; }

    public Canvas getDrawingCanvas() { return drawingCanvas; }
    public DrawingModel getModel() { return model; }
    public AnchorPane getRootPane() { return rootPane; } // Per MousePressedHandler (richiesta focus)
    public CommandManager getCommandManager() { return commandManager; }

    // Getter per i controlli UI (usati dai gestori eventi del mouse e dai test)
    public ColorPicker getBorderPicker() { return borderPicker; }
    public ColorPicker getFillPicker() { return fillPicker; }
    public Spinner<Double> getHeightSpinner() { return heightSpinner; }
    public Spinner<Double> getWidthSpinner() { return widthSpinner; }

    public ShapeFactory getCurrentShapeFactory() { return currentShapeFactory; }
    /** Imposta la factory per la creazione di nuove figure e aggiorna il cursore. */
    public void setCurrentShapeFactory(ShapeFactory currentShapeFactory) {
        this.currentShapeFactory = currentShapeFactory;
        if (this.currentShapeFactory == null && drawingCanvas != null) {
            drawingCanvas.setCursor(Cursor.DEFAULT); // Cursore default se nessuna factory
        } else if (this.currentShapeFactory != null && drawingCanvas != null) {
            drawingCanvas.setCursor(Cursor.CROSSHAIR); // Cursore a croce se factory attiva
        }
    }
    public ZoomHandler getZoomHandler() { return zoomHandler; }
    public FileOperationContext getFileOperationContext() { return fileOperationContext; }
}