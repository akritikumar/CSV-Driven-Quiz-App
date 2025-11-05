package quiz;

import java.sql.*;
import java.sql.Connection;
import java.sql.SQLException;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:identifier.sqlite";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}

