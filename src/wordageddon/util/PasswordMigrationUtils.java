package wordageddon.util;

/**
 * NOTA: Questa classe non è più utilizzata nel progetto poiché si assume ora che tutte 
 * le password siano hashate al momento della registrazione.
 * 
 * Classe mantenuta solo per compatibilità con il codice esistente.
 * In un contesto di produzione, questa classe dovrebbe essere rimossa
 * o sostituita con una più adeguata se necessario.
 * 
 * @author Francesco Peluso
 * @version 1.0
 * @deprecated Non più necessaria poiché le tabelle vengono ricreate tramite TRUNCATE
 */
@Deprecated
public class PasswordMigrationUtils {
    
    /**
     * Metodo non più utilizzato, sempre restituisce true
     * assumendo che tutte le password siano hashate.
     * 
     * @param password La stringa da controllare
     * @return sempre true
     * @deprecated Non più necessario
     */
    @Deprecated
    public static boolean isHashedPassword(String password) {
        // Tutti gli utenti ora hanno password hashate, quindi questo metodo
        // restituisce sempre true
        return true;
    }
}
