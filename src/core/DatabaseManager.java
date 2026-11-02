package core;

/**
 * SINGLETON PATTERN
 * Only one DatabaseManager exists in the whole application.
 * The constructor is private, so the only way to get it is getInstance().
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private String connectionString;

    private DatabaseManager() {
        this.connectionString = "jdbc:zakatmisr://localhost:3306/zakat_db";
        System.out.println("[DB] DatabaseManager created -> " + connectionString);
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void executeQuery(String sql) {
        System.out.println("[DB] " + sql);
    }

    public String getConnectionString() {
        return connectionString;
    }
}
