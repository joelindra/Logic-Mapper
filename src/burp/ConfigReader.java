package burp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple YAML config reader for Logic Mapper extension
 */
public class ConfigReader {
    
    private static ConfigReader instance;
    private Map<String, String> config;
    
    private ConfigReader() {
        config = new HashMap<>();
        loadConfig();
    }
    
    public static ConfigReader getInstance() {
        if (instance == null) {
            instance = new ConfigReader();
        }
        return instance;
    }
    
    private void loadConfig() {
        try {
            InputStream is = getClass().getResourceAsStream("/config.yaml");
            if (is == null) {
                // Fallback to default values
                config.put("extension.name", "Logic Mapper");
                config.put("extension.version", "1.0");
                config.put("extension.description", "Visualizer Alur Bisnis untuk Burp Suite");
                return;
            }
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                String currentKey = "";
                int indentLevel = 0;
                
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    
                    // Skip comments and empty lines
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    
                    // Calculate indent
                    int lineIndent = getIndentLevel(line);
                    String trimmed = line.trim();
                    
                    if (trimmed.contains(":")) {
                        String[] parts = trimmed.split(":", 2);
                        String key = parts[0].trim();
                        String value = parts.length > 1 ? parts[1].trim() : "";
                        
                        // Remove quotes if present
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        
                        // Build full key path
                        if (lineIndent == 0) {
                            currentKey = key;
                        } else if (lineIndent == 2) {
                            // Nested under extension
                            currentKey = "extension." + key;
                        }
                        
                        config.put(currentKey, value);
                    }
                }
            }
        } catch (Exception e) {
            // Fallback to default values on error
            config.put("extension.name", "Logic Mapper");
            config.put("extension.version", "1.0");
            config.put("extension.description", "Visualizer Alur Bisnis untuk Burp Suite");
        }
    }
    
    private int getIndentLevel(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                count++;
            } else {
                break;
            }
        }
        return count;
    }
    
    public String getVersion() {
        return config.getOrDefault("extension.version", "1.0");
    }
    
    public String getName() {
        return config.getOrDefault("extension.name", "Logic Mapper");
    }
    
    public String getDescription() {
        return config.getOrDefault("extension.description", "Visualizer Alur Bisnis untuk Burp Suite");
    }
    
    public String get(String key) {
        return config.get(key);
    }
}

