package it.unisa.diem.ja.g10.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 *
 * @author fp
 */
public interface WordageddonDAO {
    
    public void initializeDatabase();
    public void truncateTables() throws Exception;
    
}
