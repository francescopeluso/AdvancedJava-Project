package it.unisa.diem.ja.g10.database;

/**
 *
 * @author fp
 */
public class TestDatabase {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        WordageddonDAOSQLite database = new WordageddonDAOSQLite();
        database.initializeDatabase();
        Thread.sleep(10000);    // aspetto 10 secondi per visualizzare DB su DataGrip
        database.truncateTables();
    }

}
