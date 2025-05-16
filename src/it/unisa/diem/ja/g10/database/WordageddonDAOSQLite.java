package it.unisa.diem.ja.g10.database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fp
 */
public class WordageddonDAOSQLite implements WordageddonDAO {
    
    private static final String URL = "jdbc:sqlite:wordageddon.db";
    
    public void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
             
            // Lettura dello script SQL da file
            String sqlScript = readSqlScript("create.sql");
            
            // Invio gli statement raccolti dalla lettura del file .sql (escludendo i commenti)
            String[] statements = sqlScript.split(";");
            for (String statement : statements) {
                if (!statement.trim().isEmpty()) {
                    stmt.execute(statement);
                }
            }
            
            System.out.println("Database inizializzato con successo.");
            
        } catch (Exception e) {
            System.err.println("Errore durante l'inizializzazione del database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void truncateTables() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            
            // disabilito temporaneamente i foreign key constraints, così da non avere errori
            stmt.execute("PRAGMA foreign_keys = OFF;");
            
            // disabilito l'auto commit, in modo da effettuare una transazione
            conn.setAutoCommit(false);
            
            try {
                String[] tables = getTableNames();
                
                for (String table : tables) {
                    stmt.execute("DELETE FROM " + table + ";");
                    System.out.println("Tabella " + table + " resettata.");
                }
                
                conn.commit();
                System.out.println("Tutte le tabelle sono state resettate.");    
            } catch (Exception e) {
                // in caso di errore, faccio un rollback, così non lascio il db inconsistente
                conn.rollback();
                throw e;
            } finally {
                // riabilito check delle fk
                stmt.execute("PRAGMA foreign_keys = ON;");
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            System.err.println("Errore durante il reset delle tabelle: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String readSqlScript(String filePath) throws IOException {
        
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().startsWith("--")) {
                    content.append(line).append("\n");
                }
            }
        }
        
        return content.toString();
    }
    
    private String[] getTableNames() throws Exception {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';")) {
            
            List<String> tables = new ArrayList<>();
            while (rs.next()) {
                tables.add(rs.getString("name"));
            }
            
            // riordino le tabelle - elimino prima il contenuto delle "tabelle relazione" e poi quelle delle tabelle entità
            // eseguo il sorting sapendo che quelle relazione contengono un underscore nel nome, altrimenti vado in ordine alfabetico
            tables.sort((t1, t2) -> {
                boolean t1IsRelation = t1.contains("_");
                boolean t2IsRelation = t2.contains("_");
                
                if (t1IsRelation && !t2IsRelation) return -1;
                if (!t1IsRelation && t2IsRelation) return 1;
                return t1.compareTo(t2);
            });
            
            return tables.toArray(new String[0]);
        }
    }
    
}
