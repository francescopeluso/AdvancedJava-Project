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

import wordageddon.model.User;
import wordageddon.model.GameSession;
import wordageddon.service.UserSession;
import wordageddon.dao.UserDAO;
import wordageddon.dao.GameSessionDAO;
import wordageddon.dao.implementation.UserDAOSQLite;
import wordageddon.dao.implementation.GameSessionDAOSQLite;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;

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

    // Services and DAOs
    private UserDAO userDAO;
    private GameSessionDAO gameSessionDAO;
    
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
        
        // Initialize user list
        usersList = FXCollections.observableArrayList();
        
        // Setup table columns
        setupTableColumns();
        
        // Load initial data
        loadData();
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
}
