package com.deepmodi.app.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Database configuration management class.
 * Handles loading database credentials from environment variables or configuration files.
 * This approach keeps sensitive information out of source code.
 */
public class DatabaseConfig {
    // Database connection parameters
    private final String url;
    private final String username;
    private final String password;
    private final String driverClassName;

    // Singleton instance
    private static DatabaseConfig instance;

    /**
     * Private constructor to implement the singleton pattern
     * Loads configuration from environment variables first, then falls back to properties file.
     */

    private DatabaseConfig(){
        Properties properties = loadPropertiesFile();
        this.url = properties.getProperty("database.url");
        this.username = properties.getProperty("database.username");
        this.password = properties.getProperty("database.password");
        this.driverClassName = properties.getProperty("database.driver");
        System.out.println("✓ Database configuration loaded from properties file");
    }

    public static synchronized DatabaseConfig getInstance(){
        if(instance == null){
            instance = new DatabaseConfig();
        }
        return instance;
    }

    private Properties loadPropertiesFile(){
        Properties properties = new Properties();

        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties")){
            if(inputStream == null){
                System.out.println("⚠ application.properties file not found, using defaults");
                return properties;
            }

            properties.load(inputStream);
        }catch (IOException e){
            System.out.println("✗ Error loading application.properties: " + e.getMessage());
            // In production, you might want to throw a runtime exception here
        }

        return properties;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    /**
     * Display configuration info (without sensitive data).
     * Useful for debugging connection issues.
     */
    public void printConnectionInfo() {
        System.out.println("Database Connection Info:");
        System.out.println("  URL: " + url);
        System.out.println("  Username: " + username);
        System.out.println("  Driver: " + driverClassName);
        System.out.println("  Password: [HIDDEN]");
    }
}
