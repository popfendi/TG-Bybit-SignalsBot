package com.popfendi.config;



import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// loads props
public class PropertiesLoader {

    public static Properties instance;

    private static Properties loadProperties() throws IOException {
        Properties configuration = new Properties();
        InputStream inputStream = PropertiesLoader.class
                .getClassLoader()
                .getResourceAsStream("application.properties");
        configuration.load(inputStream);
        inputStream.close();
        return configuration;
    }

    public static Properties getProperties() {
        if(instance == null){
            try {
                instance = loadProperties();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return instance;
    }
}
