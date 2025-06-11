package wordageddon;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application class for the Wordageddon text analysis game.
 * 
 * Wordageddon is an educational JavaFX application that challenges users
 * to read and analyze text documents, then answer questions about word
 * frequencies.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class Wordageddon extends Application {
    
    /**
     * Starts the JavaFX application by loading the initial StartView.
     * Sets up the primary stage with minimum dimensions and displays
     * the login/registration interface.
     * 
     * @param stage the primary stage for this application
     * @throws Exception if the FXML file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws Exception {
        // l'entry point dell'applicazione sarà la StartView che prevede login/registrazione
        Parent root = FXMLLoader.load(getClass().getResource("view/StartView.fxml"));
        
        Scene scene = new Scene(root);

        // imposto il titolo dello stage (finestra)
        stage.setTitle("Benvenuto in Wordageddon");

        // imposto delle dimensioni minime per la finestra
        stage.setMinWidth(800);
        stage.setMinHeight(600);

        // "si va in scena"
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Main entry point for the application.
     * Launches the JavaFX application.
     * 
     * @param args the command line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
