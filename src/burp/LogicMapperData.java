package burp;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class for serializing/deserializing Logic Mapper data to JSON
 */
public class LogicMapperData {
    
    public static class NodeData {
        public String id;
        public int x;
        public int y;
        public String notes;
        public String displayName;
        public String method;
        public String url;
        public List<String> headers;
        public String body; // Base64 encoded
        public List<String> connections; // List of target node IDs
    }
    
    public String version;
    public List<NodeData> nodes;
    
    public LogicMapperData() {
        this.version = ConfigReader.getInstance().getVersion();
        this.nodes = new ArrayList<>();
    }
}

