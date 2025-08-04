package com.deepmodi.app.util;

import com.deepmodi.app.config.DatabaseConfig;

import java.sql.*;

public class DatabaseUtil {

    private static final DatabaseConfig config = DatabaseConfig.getInstance();

    public static Connection getConnection() throws SQLException {
        try{

            // Step 1: Load the PostgreSQL JDBC driver
            Class.forName(config.getDriverClassName());
            System.out.println("✓ PostgreSQL JDBC driver loaded successfully");

            // Step 2:
            Connection connection = DriverManager.getConnection(
                    config.getUrl(),
                    config.getUsername(),
                    config.getPassword()
            );

            // Step 3:
            connection.setAutoCommit(false);
            System.out.println("✓ Database connection established successfully");
            return connection;

        }catch (ClassNotFoundException e){
            String errorMsg = "PostgreSQL JDBC driver not found. Make sure postgresql dependency is in your classpath.";
            System.err.println("✗ " + errorMsg);
            throw new SQLException(errorMsg, e);
        } catch (SQLException e) {
            System.err.println("✗ Failed to connect to database: " + e.getMessage());
            throw e; // Re-throw to let caller handle
        }
    }

    public static boolean testConnection(){
        System.out.println("Testing database connection...");

        try(Connection connection = getConnection()){
            try(Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT version()")){
                if(rs.next()){
                    System.out.println("✓ Connection test successful!");
                    System.out.println("  PostgreSQL version: " + rs.getString(1));
                    connection.commit();
                    return true;
                }
            }
        }catch (SQLException e){
            System.err.println("✗ Connection test failed: " + e.getMessage());
        }

        return false;
    }

    public static void createUserTableIfNotExists(){
        String createTableSQL = """
                CREATE TABLE IF NOT EXISTS users (
                    id BIGSERIAL PRIMARY KEY,
                    first_name VARCHAR(100) NOT NULL,
                    last_name VARCHAR(100) NOT NULL,
                    email VARCHAR(255) UNIQUE NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        try(Connection connection = getConnection();
            Statement statement = connection.createStatement()) {
            // Execute DDL statement
            statement.executeUpdate(createTableSQL);
            connection.commit();

            System.out.println("✓ Users table created or already exists");
        } catch (SQLException e){
            System.err.println("✗ Failed to create users table: " + e.getMessage());
        }
    }


    public static void closeResources(Connection conn, Statement stmt, ResultSet rs){
        // Close in reverse order of creation
        if(rs != null){
            try{
                rs.close();
                System.out.println("✓ ResultSet closed");
            } catch (SQLException e){
                System.err.println("⚠ Error closing ResultSet: " + e.getMessage());
            }
        }

        if(stmt != null){
            try{
                stmt.close();
            }catch (SQLException e){
                System.err.println("⚠ Error closing Statement: " + e.getMessage());
            }
        }

        if(conn != null){
            try{
                // If there's an uncommitted transaction, rollback before closing
                if(!conn.getAutoCommit()){
                    conn.rollback();
                    System.out.println("✓ Uncommitted transaction rolled back");
                }
                conn.close();
                System.out.println("✓ Connection closed");
            }catch (SQLException e){
                System.err.println("⚠ Error closing Connection: " + e.getMessage());
            }
        }
    }
}
