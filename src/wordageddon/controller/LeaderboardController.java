package wordageddon.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;

import wordageddon.model.GameSessionSummary;
import wordageddon.model.UserLeaderboardEntry;
import wordageddon.model.User;
import wordageddon.service.GameIntegrationService;
import wordageddon.service.UserSession;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.text.SimpleDateFormat;

/**
 * Controller for the leaderboard and game history view.
 * Displays user's game sessions and statistics.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class LeaderboardController implements Initializable {

    @FXML private VBox leaderboardPane;
    @FXML private TabPane tabPane;
    @FXML private Label userWelcomeLabel;
    @FXML private Label userStatsLabel;
    
    // Global leaderboard table
    @FXML private TableView<UserLeaderboardEntry> globalLeaderboardTable;
    @FXML private TableColumn<UserLeaderboardEntry, Integer> rankColumn;
    @FXML private TableColumn<UserLeaderboardEntry, String> usernameColumn;
    @FXML private TableColumn<UserLeaderboardEntry, Integer> totalPointsColumn;
    @FXML private Button refreshGlobalButton;
    
    // User game sessions table
    @FXML private TableView<GameSessionSummary> gameSessionsTable;
    @FXML private TableColumn<GameSessionSummary, String> difficultyColumn;
    @FXML private TableColumn<GameSessionSummary, Double> scoreColumn;
    @FXML private TableColumn<GameSessionSummary, Integer> questionsColumn;
    @FXML private TableColumn<GameSessionSummary, Integer> correctColumn;
    @FXML private TableColumn<GameSessionSummary, Double> percentageColumn;
    @FXML private TableColumn<GameSessionSummary, String> dateColumn;
    
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    
    private GameIntegrationService gameIntegrationService;
    private SimpleDateFormat dateFormat;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameIntegrationService = new GameIntegrationService();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        initializeGlobalLeaderboardTable();
        initializeUserHistoryTable();
        loadUserData();
        loadGlobalLeaderboard();
        loadGameSessions();
    }
    
    /**
     * Initializes the global leaderboard table with column bindings.
     */
    private void initializeGlobalLeaderboardTable() {
        if (globalLeaderboardTable == null) return;
        
        rankColumn.setCellValueFactory(cellData -> {
            int index = globalLeaderboardTable.getItems().indexOf(cellData.getValue()) + 1;
            return new SimpleIntegerProperty(index).asObject();
        });
        
        usernameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUsername()));
        
        totalPointsColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getTotalPoints()).asObject());
        
        globalLeaderboardTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    /**
     * Initializes the user game history table with column bindings.
     */
    private void initializeUserHistoryTable() {
        if (gameSessionsTable == null) return;
        
        difficultyColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDifficulty()));
        
        scoreColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getScore()).asObject());
        
        questionsColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getTotalQuestions()).asObject());
        
        correctColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getCorrectAnswers()).asObject());
        
        percentageColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getPercentageScore()).asObject());
        
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(dateFormat.format(cellData.getValue().getCreatedAt())));
        
        gameSessionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    /**
     * Loads and displays current user information.
     */
    private void loadUserData() {
        UserSession userSession = UserSession.getInstance();
        if (userSession.isLoggedIn()) {
            User currentUser = userSession.getCurrentUser();
            if (userWelcomeLabel != null) {
                userWelcomeLabel.setText("Benvenuto, " + currentUser.getFname() + " " + currentUser.getLname());
            }
            
            // carica le statistiche utente usando oggetti GameSessionSummary
            List<GameSessionSummary> summaries = gameIntegrationService.getCurrentUserGameSessionSummaries();
            updateUserStatsFromSummaries(summaries);
        } else {
            if (userWelcomeLabel != null) {
                userWelcomeLabel.setText("Utente non autenticato");
            }
        }
    }
    
    /**
     * Updates user statistics based on game session summaries.
     */
    private void updateUserStatsFromSummaries(List<GameSessionSummary> summaries) {
        if (userStatsLabel == null || summaries.isEmpty()) {
            if (userStatsLabel != null) {
                userStatsLabel.setText("Nessuna partita giocata");
            }
            return;
        }
        
        int totalGames = summaries.size();
        double avgScore = summaries.stream()
            .mapToDouble(GameSessionSummary::getScore)
            .average()
            .orElse(0.0);
        
        double avgPercentage = summaries.stream()
            .mapToDouble(GameSessionSummary::getPercentageScore)
            .average()
            .orElse(0.0);
        
        GameSessionSummary bestGame = summaries.stream()
            .max((s1, s2) -> Double.compare(s1.getPercentageScore(), s2.getPercentageScore()))
            .orElse(null);
        
        StringBuilder stats = new StringBuilder();
        stats.append(String.format("Partite giocate: %d | ", totalGames));
        stats.append(String.format("Punteggio medio: %.2f | ", avgScore));
        stats.append(String.format("Percentuale media: %.1f%% | ", avgPercentage));
        if (bestGame != null) {
            stats.append(String.format("Miglior risultato: %.1f%% (%s)", 
                bestGame.getPercentageScore(), bestGame.getDifficulty()));
        }
        
        userStatsLabel.setText(stats.toString());
    }
    
    /**
     * Loads and displays user's game sessions.
     */
    private void loadGameSessions() {
        if (gameSessionsTable == null) return;
        
        UserSession userSession = UserSession.getInstance();
        if (userSession.isLoggedIn()) {
            List<GameSessionSummary> summaries = gameIntegrationService.getCurrentUserGameSessionSummaries();
            ObservableList<GameSessionSummary> tableData = FXCollections.observableArrayList(summaries);
            gameSessionsTable.setItems(tableData);
        }
    }
    
    /**
     * Loads and displays the global leaderboard.
     */
    private void loadGlobalLeaderboard() {
        if (globalLeaderboardTable == null) return;
        
        List<UserLeaderboardEntry> leaderboard = gameIntegrationService.getGlobalLeaderboard();
        ObservableList<UserLeaderboardEntry> tableData = FXCollections.observableArrayList(leaderboard);
        globalLeaderboardTable.setItems(tableData);
    }
    
    /**
     * Handles the refresh button click to reload data.
     */
    @FXML
    private void handleRefresh(ActionEvent event) {
        loadUserData();
        loadGameSessions();
    }
    
    /**
     * Handles the refresh global leaderboard button click.
     */
    @FXML
    private void handleRefreshGlobal(ActionEvent event) {
        loadGlobalLeaderboard();
    }
    
    /**
     * Handles the back button click to return to dashboard.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            // carica la vista dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/wordageddon/view/DashboardView.fxml"));
            Parent dashboardView = loader.load();
            
            // ottieni la finestra corrente
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // imposta la nuova scena
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
}
