package com.deepmodi.jdbc.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DBConfig {
    private static final Properties props = new Properties();

    static {
        try(InputStream input = DBConfig.class.getClassLoader().getResourceAsStream("db.properties")){
            if(input == null){
                throw new RuntimeException("Cannot find db.properties");
            }

            props.load(input);
        } catch (IOException e){
            throw new RuntimeException("Error loading db.properties", e);
        }
    }

    public static String getUrl(){
        return props.getProperty("db.url");
    }

    public static String getUsername(){
        return props.getProperty("db.username");
    }

    public static String getPassword(){
        return props.getProperty("db.password");
    }
}
