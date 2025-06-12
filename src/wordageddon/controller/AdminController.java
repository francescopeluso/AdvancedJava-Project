package wordageddon.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.application.Platform;
import javafx.stage.FileChooser;

import wordageddon.model.User;
import wordageddon.model.GameSession;
import wordageddon.service.UserSession;
import wordageddon.service.DocumentServices;
import wordageddon.dao.UserDAO;
import wordageddon.dao.GameSessionDAO;
import wordageddon.dao.implementation.UserDAOSQLite;
import wordageddon.dao.implementation.GameSessionDAOSQLite;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Map;

/**
 * Controller for the admin panel interface.
 * Manages user administration, statistics, and administrative functions.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class AdminController implements Initializable {

    // FXML Elements
    @FXML private Label adminTitleLabel;
    @FXML private TableView<User> usersTableView;
    @FXML private TableColumn<User, Integer> userIdColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> firstNameColumn;
    @FXML private TableColumn<User, String> lastNameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, Boolean> isAdminColumn;
    
    @FXML private Button refreshUsersButton;
    @FXML private Button promoteUserButton;
    @FXML private Button demoteUserButton;
    @FXML private Button backButton;
    
    @FXML private Label totalUsersLabel;
    @FXML private Label totalGamesLabel;
    @FXML private Label totalAdminsLabel;
    
    // Document Management Elements
    @FXML private Label documentsCountLabel;
    @FXML private Label stopwordsCountLabel;
    @FXML private Label vocabularyCountLabel;
    @FXML private Button loadDocumentButton;
    @FXML private Button regenerateDtmButton;
    @FXML private TextArea stopwordsTextArea;
    @FXML private Button updateStopwordsButton;
    @FXML private Label stopwordsStatusLabel;
    @FXML private ListView<String> documentsListView;
    @FXML private Button removeDocumentButton;
    @FXML private Label documentStatusLabel;

    // Services and DAOs
    private UserDAO userDAO;
    private GameSessionDAO gameSessionDAO;
    private DocumentServices documentServices;
    
    // Data
    private ObservableList<User> usersList;

    /**
     * Initializes the admin controller.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize DAOs and services
        userDAO = new UserDAOSQLite();
        gameSessionDAO = new GameSessionDAOSQLite();
        documentServices = new DocumentServices();
        
        // Initialize user list
        usersList = FXCollections.observableArrayList();
        
        // Setup table columns
        setupTableColumns();
        
        // Load initial data
        loadData();
        
        // Setup document list selection listener
        setupDocumentListListener();
    }
    
    /**
     * Sets up the table columns for the users table.
     */
    private void setupTableColumns() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("fname"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lname"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        isAdminColumn.setCellValueFactory(new PropertyValueFactory<>("isAdmin"));
        
        // Set the table data
        usersTableView.setItems(usersList);
    }
    
    /**
     * Loads all data for the admin panel.
     */
    private void loadData() {
        loadUsers();
        loadStatistics();
    }
    
    /**
     * Loads all users into the table.
     */
    private void loadUsers() {
        try {
            List<User> users = userDAO.getAllUsers();
            usersList.clear();
            usersList.addAll(users);
            System.out.println("Caricati " + users.size() + " utenti");
        } catch (Exception e) {
            System.err.println("Errore nel caricamento degli utenti: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads and displays statistics.
     */
    private void loadStatistics() {
        try {
            // Count total users
            List<User> allUsers = userDAO.getAllUsers();
            totalUsersLabel.setText(String.valueOf(allUsers.size()));
            
            // Count total admins
            long adminCount = allUsers.stream().filter(user -> 
                user.getIsAdmin() != null && user.getIsAdmin()).count();
            totalAdminsLabel.setText(String.valueOf(adminCount));
            
            // Count total games - use direct SQL query instead of creating GameSession objects
            int totalGames = countTotalGameSessions();
            totalGamesLabel.setText(String.valueOf(totalGames));
            
        } catch (Exception e) {
            System.err.println("Errore nel caricamento delle statistiche: " + e.getMessage());
            e.printStackTrace();
            
            // Set default values
            totalUsersLabel.setText("N/A");
            totalGamesLabel.setText("N/A");
            totalAdminsLabel.setText("N/A");
        }
    }
    
    /**
     * Counts the total number of game sessions without creating GameSession objects.
     * This avoids the "Questions cannot be null or empty" error.
     */
    private int countTotalGameSessions() {
        try {
            // Use direct database query to count sessions
            return ((GameSessionDAOSQLite) gameSessionDAO).countGameSessions();
        } catch (Exception e) {
            System.err.println("Errore nel conteggio delle sessioni di gioco: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Handles the refresh users action.
     */
    @FXML
    private void handleRefreshUsers(ActionEvent event) {
        loadData();
        System.out.println("Dati aggiornati");
    }
    
    /**
     * Handles promoting a user to admin.
     */
    @FXML
    private void handlePromoteUser(ActionEvent event) {
        User selectedUser = usersTableView.getSelectionModel().getSelectedItem();
        
        if (selectedUser == null) {
            showAlert("Nessun utente selezionato", "Seleziona un utente dalla tabella per promuoverlo ad amministratore.");
            return;
        }
        
        if (selectedUser.getIsAdmin() != null && selectedUser.getIsAdmin()) {
            showAlert("Utente già amministratore", "L'utente selezionato è già un amministratore.");
            return;
        }
        
        try {
            // Update user admin status
            userDAO.updateUserAdminStatus(selectedUser.getId(), true);
            
            // Refresh data
            loadData();
            
            showAlert("Promozione completata", "L'utente " + selectedUser.getUsername() + " è stato promosso ad amministratore.");
            System.out.println("Utente " + selectedUser.getUsername() + " promosso ad admin");
            
        } catch (Exception e) {
            System.err.println("Errore nella promozione dell'utente: " + e.getMessage());
            showAlert("Errore", "Errore nella promozione dell'utente: " + e.getMessage());
        }
    }
    
    /**
     * Handles demoting an admin user.
     */
    @FXML
    private void handleDemoteUser(ActionEvent event) {
        User selectedUser = usersTableView.getSelectionModel().getSelectedItem();
        
        if (selectedUser == null) {
            showAlert("Nessun utente selezionato", "Seleziona un utente dalla tabella per rimuovere i privilegi di amministratore.");
            return;
        }
        
        if (selectedUser.getIsAdmin() == null || !selectedUser.getIsAdmin()) {
            showAlert("Utente non amministratore", "L'utente selezionato non è un amministratore.");
            return;
        }
        
        // Check if this is the currently logged user
        UserSession userSession = UserSession.getInstance();
        if (userSession.isLoggedIn() && userSession.getCurrentUser().getId() == selectedUser.getId()) {
            showAlert("Operazione non consentita", "Non puoi rimuovere i privilegi di amministratore dal tuo account.");
            return;
        }
        
        try {
            // Update user admin status
            userDAO.updateUserAdminStatus(selectedUser.getId(), false);
            
            // Refresh data
            loadData();
            
            showAlert("Rimozione completata", "I privilegi di amministratore sono stati rimossi dall'utente " + selectedUser.getUsername() + ".");
            System.out.println("Privilegi admin rimossi dall'utente " + selectedUser.getUsername());
            
        } catch (Exception e) {
            System.err.println("Errore nella rimozione dei privilegi admin: " + e.getMessage());
            showAlert("Errore", "Errore nella rimozione dei privilegi: " + e.getMessage());
        }
    }
    
    /**
     * Handles the back button to return to dashboard.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            // Load dashboard view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/wordageddon/view/DashboardView.fxml"));
            Parent dashboardView = loader.load();
            
            // Get current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // Set new scene
            Scene scene = new Scene(dashboardView);
            stage.setScene(scene);
            stage.setTitle("Wordageddon - Dashboard");
            stage.show();
            
            System.out.println("Tornato alla dashboard");
        } catch (Exception e) {
            System.err.println("Errore nel tornare alla dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Shows an alert dialog with the specified title and message.
     */
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /**
     * Sets up the document list selection listener.
     */
    private void setupDocumentListListener() {
        documentsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                documentStatusLabel.setText("Documento selezionato: " + (newVal.length() > 50 ? newVal.substring(0, 50) + "..." : newVal));
            } else {
                documentStatusLabel.setText("Seleziona un documento per rimuoverlo");
            }
        });
        
        // Load documents and statistics
        loadDocumentData();
    }
    
    /**
     * Loads document data and statistics.
     */
    private void loadDocumentData() {
        try {
            // Update document statistics
            Map<String, Integer> stats = documentServices.getStatistics();
            documentsCountLabel.setText(String.valueOf(stats.get("documents")));
            stopwordsCountLabel.setText(String.valueOf(stats.get("stopwords")));
            vocabularyCountLabel.setText(String.valueOf(stats.get("vocabulary")));
            
            // Update documents list
            List<String> documents = documentServices.getDocuments();
            ObservableList<String> documentItems = FXCollections.observableArrayList();
            for (int i = 0; i < documents.size(); i++) {
                String doc = documents.get(i);
                String preview = "Documento " + (i + 1) + ": " + 
                               (doc.length() > 100 ? doc.substring(0, 100) + "..." : doc);
                documentItems.add(preview);
            }
            documentsListView.setItems(documentItems);
            
            // Load stopwords into TextArea
            String stopwordsText = documentServices.getStopwordsAsText();
            stopwordsTextArea.setText(stopwordsText);
            
        } catch (Exception e) {
            System.err.println("Errore nel caricamento dei dati documenti: " + e.getMessage());
        }
    }
    
    /**
     * Handles loading a new document from file.
     */
    @FXML
    private void handleLoadDocument(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona Documento da Caricare");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("File di Testo", "*.txt"),
            new FileChooser.ExtensionFilter("Tutti i File", "*.*")
        );
        
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            try {
                boolean success = documentServices.loadDocumentFromFile(selectedFile.getAbsolutePath());
                if (success) {
                    loadDocumentData();
                    documentStatusLabel.setText("Documento caricato: " + selectedFile.getName());
                    showAlert("Documento Caricato", "Il documento '" + selectedFile.getName() + "' è stato caricato con successo e salvato.");
                } else {
                    documentStatusLabel.setText("Errore nel caricamento del documento.");
                    showAlert("Errore", "Impossibile caricare il documento. Verifica che il file non sia vuoto.");
                }
            } catch (Exception e) {
                documentStatusLabel.setText("Errore: " + e.getMessage());
                showAlert("Errore", "Errore nel caricamento del documento: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handles updating stopwords from the TextArea.
     */
    @FXML
    private void handleUpdateStopwords(ActionEvent event) {
        String stopwordsText = stopwordsTextArea.getText();
        
        try {
            boolean success = documentServices.updateStopwordsFromText(stopwordsText);
            if (success) {
                loadDocumentData();
                stopwordsStatusLabel.setText("Stopwords aggiornate con successo!");
                showAlert("Stopwords Aggiornate", "Le stopwords sono state aggiornate con successo e la DTM è stata rigenerata.");
            } else {
                stopwordsStatusLabel.setText("Errore nell'aggiornamento delle stopwords.");
                showAlert("Errore", "Impossibile aggiornare le stopwords.");
            }
        } catch (Exception e) {
            stopwordsStatusLabel.setText("Errore: " + e.getMessage());
            showAlert("Errore", "Errore nell'aggiornamento delle stopwords: " + e.getMessage());
        }
    }
    
    /**
     * Handles regenerating the Document Term Matrix.
     */
    @FXML
    private void handleRegenerateDtm(ActionEvent event) {
        try {
            // Force regeneration by calling the service method
            documentServices.regenerateAndSaveDtm(); // Updated call
            loadDocumentData(); // Then refresh UI
            showAlert("DTM Rigenerata", "La Document Term Matrix è stata rigenerata con successo.");
        } catch (Exception e) {
            showAlert("Errore", "Errore nella rigenerazione della DTM: " + e.getMessage());
        }
    }
    
    /**
     * Handles removing a selected document.
     */
    @FXML
    private void handleRemoveDocument(ActionEvent event) {
        int selectedIndex = documentsListView.getSelectionModel().getSelectedIndex();
        
        if (selectedIndex < 0) {
            showAlert("Nessun Documento Selezionato", "Seleziona un documento dalla lista per rimuoverlo.");
            return;
        }
        
        try {
            boolean success = documentServices.removeDocument(selectedIndex);
            if (success) {
                loadDocumentData();
                showAlert("Documento Rimosso", "Il documento è stato rimosso con successo e la DTM è stata rigenerata.");
            } else {
                showAlert("Errore", "Impossibile rimuovere il documento.");
            }
        } catch (Exception e) {
            showAlert("Errore", "Errore nella rimozione del documento: " + e.getMessage());
        }
    }
}
