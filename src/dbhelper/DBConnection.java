package dbhelper;

import java.sql.*;

public class DBConnection {
    private Connection c = null;
    
    public Connection connDb() {
        try {
            this.c = DriverManager.getConnection("jdbc:mysql://localhost:3306/hospital?user=root&password=Ulasece123");
            return c;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return c;
    }
    
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/hospital?user=root&password=Ulasece123");
    }
}