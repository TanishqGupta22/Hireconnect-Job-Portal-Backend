import java.sql.*;

public class DbCheck {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/hireconnect_profiles?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String password = "root";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to database!");
            
            checkColumns(conn, "candidate_profiles");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void checkColumns(Connection conn, String tableName) throws SQLException {
        System.out.println("\nColumns in " + tableName + ":");
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String typeName = rs.getString("TYPE_NAME");
                int nullable = rs.getInt("NULLABLE");
                System.out.println("- " + columnName + " (" + typeName + "), nullable: " + (nullable == DatabaseMetaData.columnNullable));
            }
        }
        
        System.out.println("\nChecking for nulls in job_alerts_enabled:");
        String sql = "SELECT COUNT(*) FROM candidate_profiles WHERE job_alerts_enabled IS NULL";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                System.out.println("Found " + rs.getInt(1) + " rows with NULL job_alerts_enabled");
            }
        } catch (SQLException e) {
            System.out.println("Column job_alerts_enabled might not exist: " + e.getMessage());
        }
    }
}
