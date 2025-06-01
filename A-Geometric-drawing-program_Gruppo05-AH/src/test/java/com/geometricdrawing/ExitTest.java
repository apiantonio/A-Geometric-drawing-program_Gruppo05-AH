package com.geometricdrawing;

import com.geometricdrawing.controller.DrawingController;
import com.geometricdrawing.controller.Exit;
import com.geometricdrawing.strategy.FileOperationContext;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel; // Per inizializzare il toolkit JavaFX
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*; // Per SwingUtilities se si usa JFXPanel in modo più robusto
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExitTest {
    @Mock
    private DrawingController drawingControllerMock;
    @Mock
    private FileOperationContext fileOperationContextMock;
    private Exit exitUnderTest;
    private MockedStatic<Platform> platformMockedStatic;

    private Alert alertMockInstance;
    private ButtonType capturedSaveButton;
    private ButtonType capturedDontSaveButton;
    private ButtonType capturedCancelButton;
    private ObservableList<ButtonType> mockedButtonListFromAlert;


    // Inizializza il Toolkit JavaFX una volta per tutti i test della classe
    @BeforeAll
    static void initJavaFX() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                new JFXPanel();
            });
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
            fail("Impossibile inizializzare il toolkit JavaFX.", e);
        }
    }

    @BeforeEach
    void setUp() {
        exitUnderTest = new Exit(drawingControllerMock);
        when(drawingControllerMock.getFileOperationContext()).thenReturn(fileOperationContextMock);
        platformMockedStatic = Mockito.mockStatic(Platform.class);
    }

    @AfterEach
    void tearDown() {
        platformMockedStatic.close();
    }

    // Metodo helper per configurare il mock dell'Alert
    private MockedConstruction<Alert> setupAlertConstructionAndStubbing(UserChoice choice) {
        return Mockito.mockConstruction(Alert.class, (mock, context) -> {
            alertMockInstance = mock; // Cattura l'istanza dell'Alert mockato

            // getButtonTypes() restituisce una ObservableList. Mockiamola.
            mockedButtonListFromAlert = mock(ObservableList.class);
            when(alertMockInstance.getButtonTypes()).thenReturn(mockedButtonListFromAlert);

            /* Quando setAll è chiamato sulla lista di bottoni mockata:
             1. Cattura i bottoni effettivi creati dalla classe Exit.
             2. Imposta il risultato di showAndWait() in base alla scelta utente simulata.*/
            doAnswer(invocation -> {
                capturedSaveButton = invocation.getArgument(0);
                capturedDontSaveButton = invocation.getArgument(1);
                capturedCancelButton = invocation.getArgument(2);

                // Configura showAndWait() sull'istanza mockata di Alert
                switch (choice) {
                    case SAVE:
                        when(alertMockInstance.showAndWait()).thenReturn(Optional.of(capturedSaveButton));
                        break;
                    case DONT_SAVE:
                        when(alertMockInstance.showAndWait()).thenReturn(Optional.of(capturedDontSaveButton));
                        break;
                    case CANCEL:
                        when(alertMockInstance.showAndWait()).thenReturn(Optional.of(capturedCancelButton));
                        break;
                    case NO_CHOICE:
                        when(alertMockInstance.showAndWait()).thenReturn(Optional.empty());
                        break;
                }
                return null; // Il metodo setAll di ObservableList è void
            }).when(mockedButtonListFromAlert).setAll(
                    any(ButtonType.class), any(ButtonType.class), any(ButtonType.class)
            );
        });
    }


    @Test
    void exit_whenUserChoosesSaveAndFocIsNotNull_shouldSaveAndExit() {
        // Usa try-with-resources per assicurare che il mockConstruction sia chiuso correttamente
        try (MockedConstruction<Alert> ignored = setupAlertConstructionAndStubbing(UserChoice.SAVE)) {
            exitUnderTest.exit(); // Qui viene creato l'Alert e scatta il mock

            // Verifica che i metodi dell'alert siano stati chiamati come previsto
            verify(alertMockInstance).setTitle("Chiudi Applicazione");
            verify(alertMockInstance).setContentText("Vuoi salvare le modifiche prima di chiudere?");

            // Verifica che getButtonTypes().setAll(...) sia stato chiamato sulla lista mockata
            verify(mockedButtonListFromAlert).setAll(
                    isA(ButtonType.class), isA(ButtonType.class), isA(ButtonType.class)
            );
            // Verifica che showAndWait sia stato chiamato
            verify(alertMockInstance).showAndWait();

            // Verifica le interazioni con il controller e il contesto di file
            verify(drawingControllerMock).getFileOperationContext();
            verify(fileOperationContextMock).executeSave();
            // Verifica che Platform.exit() sia stato chiamato una volta
            platformMockedStatic.verify(Platform::exit, times(1));
        }
    }

    @Test
    void exit_whenUserChoosesSaveAndFocIsNull_shouldNotSaveAndNotExitFromSavePath() {
        when(drawingControllerMock.getFileOperationContext()).thenReturn(null);

        try (MockedConstruction<Alert> ignored = setupAlertConstructionAndStubbing(UserChoice.SAVE)) {

            exitUnderTest.exit();

            // Assert
            verify(alertMockInstance).showAndWait(); // L'alert viene comunque mostrato
            verify(drawingControllerMock).getFileOperationContext(); // Il contesto viene richiesto
            verify(fileOperationContextMock, never()).executeSave(); // Ma non si salva
            platformMockedStatic.verify(Platform::exit, never()); // E non si esce da questo specifico percorso logico
        }
    }

    @Test
    void exit_whenUserChoosesDontSave_shouldExitWithoutSaving() {
        try (MockedConstruction<Alert> ignored = setupAlertConstructionAndStubbing(UserChoice.DONT_SAVE)) {
            exitUnderTest.exit();

            verify(alertMockInstance).showAndWait();
            verify(fileOperationContextMock, never()).executeSave();
            platformMockedStatic.verify(Platform::exit, times(1));
        }
    }

    @Test
    void exit_whenUserChoosesCancel_shouldDoNothing() {
        try (MockedConstruction<Alert> ignored = setupAlertConstructionAndStubbing(UserChoice.CANCEL)) {
            exitUnderTest.exit();

            verify(alertMockInstance).showAndWait();
            verify(fileOperationContextMock, never()).executeSave();
            platformMockedStatic.verify(Platform::exit, never());
        }
    }

    // Enum ausiliario per la leggibilità dei test
    private enum UserChoice {
        SAVE, DONT_SAVE, CANCEL, NO_CHOICE
    }
}