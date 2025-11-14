package burp;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogicMapperTab implements ITab {
    
    private final IBurpExtenderCallbacks callbacks;
    private final IExtensionHelpers helpers;
    private final JPanel mainPanel;
    private final LogicMapperCanvas canvas;
    private final List<RequestNode> nodes;
    private JLabel statusLabel;
    
    public LogicMapperTab(IBurpExtenderCallbacks callbacks, IExtensionHelpers helpers) {
        this.callbacks = callbacks;
        this.helpers = helpers;
        this.nodes = new ArrayList<>();
        
        // Create main panel
        mainPanel = new JPanel(new BorderLayout());
        
        // Create toolbar
        JToolBar toolbar = createToolbar();
        mainPanel.add(toolbar, BorderLayout.NORTH);
        
        // Create canvas
        canvas = new LogicMapperCanvas(nodes);
        JScrollPane scrollPane = new JScrollPane(canvas);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create status bar
        JPanel statusBar = createStatusBar();
        mainPanel.add(statusBar, BorderLayout.SOUTH);
    }
    
    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        // Info label - using plain text with better formatting
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        infoPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Connection Controls:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 11));
        titleLabel.setForeground(new Color(80, 80, 80));
        infoPanel.add(titleLabel);
        
        JLabel infoLabel = new JLabel("Ctrl+Click to connect | Shift+Click on line to delete | Right-click for menu");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        infoLabel.setForeground(new Color(100, 100, 100));
        infoPanel.add(infoLabel);
        
        toolbar.add(infoPanel);
        toolbar.addSeparator();
        
        // Help button
        JButton helpBtn = new JButton("Help");
        helpBtn.addActionListener(e -> showHelpDialog());
        toolbar.add(helpBtn);
        
        // Clear button
        JButton clearBtn = new JButton("Clear All");
        clearBtn.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                mainPanel,
                "Are you sure you want to clear all nodes?",
                "Clear Logic Mapper",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (result == JOptionPane.YES_OPTION) {
                nodes.clear();
                updateStatus();
                canvas.repaint();
            }
        });
        toolbar.add(clearBtn);
        
        toolbar.addSeparator();
        
        // Export button
        JButton exportBtn = new JButton("Export");
        exportBtn.addActionListener(e -> exportToJson());
        toolbar.add(exportBtn);
        
        // Import button
        JButton importBtn = new JButton("Import");
        importBtn.addActionListener(e -> importFromJson());
        toolbar.add(importBtn);
        
        return toolbar;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        
        statusLabel = new JLabel("Ready");
        statusBar.add(statusLabel);
        
        updateStatus();
        
        return statusBar;
    }
    
    private void updateStatus() {
        if (statusLabel != null) {
            int count = nodes.size();
            statusLabel.setText("Nodes: " + count);
        }
    }
    
    public void addRequest(IHttpRequestResponse message) {
        SwingUtilities.invokeLater(() -> {
            // Create new node
            RequestNode newNode = new RequestNode(message, helpers);
            
            // Position new node to the right of the last node
            if (!nodes.isEmpty()) {
                RequestNode lastNode = nodes.get(nodes.size() - 1);
                newNode.setX(lastNode.getX() + 250);
                newNode.setY(lastNode.getY());
                
                // Automatically connect to the previous node
                lastNode.addConnection(newNode);
            } else {
                newNode.setX(50);
                newNode.setY(100);
            }
            
            nodes.add(newNode);
            updateStatus();
            canvas.repaint();
            
            // Show notification
            callbacks.printOutput("Added request to Logic Mapper: " + 
                helpers.analyzeRequest(message).getUrl().toString());
        });
    }
    
    @Override
    public String getTabCaption() {
        return "Logic Mapper";
    }
    
    @Override
    public Component getUiComponent() {
        return mainPanel;
    }
    
    private void showHelpDialog() {
        JDialog helpDialog = new JDialog((Frame) null, "Logic Mapper - How To Use", true);
        helpDialog.setLayout(new BorderLayout());
        
        // Create content panel with scroll
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Logic Mapper - How To Use");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        contentPanel.add(titleLabel);
        
        // Section: Adding Requests
        addSection(contentPanel, "Adding Requests to Logic Mapper", new String[]{
            "1. In Proxy or Repeater tab, select one or more requests",
            "2. Right-click on the selected request(s)",
            "3. Choose \"Send to Logic Mapper\"",
            "4. The request will appear as a node in the Logic Mapper tab",
            "5. New requests are automatically connected to the previous request"
        });
        
        // Section: Connecting Nodes
        addSection(contentPanel, "Connecting Nodes", new String[]{
            "Method 1 - Quick Connect (Ctrl+Click):",
            "  • Ctrl+Click (or Cmd+Click on Mac) on the first node",
            "    - Node will be highlighted",
            "    - Visual line appears from node to mouse position",
            "    - Small indicator circle appears on the first node",
            "  • Release Ctrl and move mouse",
            "    - Visual line follows the mouse",
            "    - No need to hold Ctrl",
            "  • Click on the second node",
            "    - Connection is created automatically",
            "    - Connection mode ends",
            "  • (Optional) Click on empty space to cancel connection mode",
            "",
            "Method 2 - Context Menu:",
            "  • Right-click on a node",
            "  • Select \"Connect to...\"",
            "  • Choose target node from submenu"
        });
        
        // Section: Removing Connections
        addSection(contentPanel, "Removing Connections", new String[]{
            "Method 1 - Quick Delete:",
            "  • Shift+Click on a connection line to delete it",
            "",
            "Method 2 - Context Menu:",
            "  • Right-click on a connection line → \"Remove Connection\"",
            "  • Or right-click on a node → \"Remove Connection to...\" → select target"
        });
        
        // Section: Managing Nodes
        addSection(contentPanel, "Managing Nodes", new String[]{
            "• Drag: Click and drag a node to move it",
            "• Edit Notes: Double-click a node to add/edit notes",
            "• Delete: Right-click a node → \"Delete Node\"",
            "• View Details: Double-click a node to see full request information"
        });
        
        // Section: Export/Import
        addSection(contentPanel, "Export & Import", new String[]{
            "• Export: Click \"Export\" button to save your logic map as JSON",
            "• Import: Click \"Import\" button to load a previously saved logic map",
            "• Choose to replace existing nodes or append to them"
        });
        
        // Section: Visual Indicators
        addSection(contentPanel, "Visual Indicators", new String[]{
            "• Node colors indicate HTTP methods:",
            "  - GET: Green",
            "  - POST: Blue",
            "  - PUT: Orange",
            "  - DELETE: Red",
            "  - PATCH: Purple",
            "• Yellow \"!\" indicator: Node has notes",
            "• Blue arrows: Connections between nodes",
            "• Red line when hovering: Connection is selected for deletion"
        });
        
        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(600, 500));
        
        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> helpDialog.dispose());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(closeBtn);
        
        helpDialog.add(scrollPane, BorderLayout.CENTER);
        helpDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        helpDialog.setSize(650, 600);
        helpDialog.setLocationRelativeTo(mainPanel);
        helpDialog.setVisible(true);
    }
    
    private void addSection(JPanel parent, String title, String[] items) {
        // Section title
        JLabel sectionTitle = new JLabel(title);
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle.setForeground(new Color(70, 130, 180));
        sectionTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        parent.add(sectionTitle);
        
        // Section items
        for (String item : items) {
            if (item.isEmpty()) {
                parent.add(Box.createVerticalStrut(5));
                continue;
            }
            
            JLabel itemLabel = new JLabel(item);
            itemLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            itemLabel.setBorder(BorderFactory.createEmptyBorder(2, 20, 2, 0));
            parent.add(itemLabel);
        }
        
        parent.add(Box.createVerticalStrut(10));
    }
    
    private void exportToJson() {
        if (nodes.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, 
                "No nodes to export!", 
                "Export", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Logic Mapper to JSON");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files (*.json)", "json"));
        fileChooser.setSelectedFile(new java.io.File("logic_mapper_export.json"));
        
        int result = fileChooser.showSaveDialog(mainPanel);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                String filePath = file.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".json")) {
                    file = new File(filePath + ".json");
                }
                
                LogicMapperData data = new LogicMapperData();
                
                // Convert nodes to data
                for (RequestNode node : nodes) {
                    LogicMapperData.NodeData nodeData = new LogicMapperData.NodeData();
                    nodeData.id = node.getNodeId();
                    nodeData.x = node.getX();
                    nodeData.y = node.getY();
                    nodeData.notes = node.getNotes() != null ? node.getNotes() : "";
                    nodeData.displayName = node.getDisplayName();
                    
                    // Extract request info
                    IRequestInfo requestInfo = helpers.analyzeRequest(node.getMessage());
                    nodeData.method = requestInfo.getMethod();
                    nodeData.url = requestInfo.getUrl().toString();
                    nodeData.headers = new ArrayList<>(requestInfo.getHeaders());
                    
                    // Extract body
                    byte[] requestBytes = node.getMessage().getRequest();
                    int bodyOffset = requestInfo.getBodyOffset();
                    if (bodyOffset < requestBytes.length) {
                        byte[] bodyBytes = new byte[requestBytes.length - bodyOffset];
                        System.arraycopy(requestBytes, bodyOffset, bodyBytes, 0, bodyBytes.length);
                        nodeData.body = helpers.base64Encode(bodyBytes);
                    } else {
                        nodeData.body = "";
                    }
                    
                    // Get connection IDs
                    nodeData.connections = node.getConnectionIds(nodes);
                    
                    data.nodes.add(nodeData);
                }
                
                // Convert to JSON (simple manual JSON generation)
                String json = convertToJson(data);
                
                // Write to file
                try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                    writer.write(json);
                }
                
                JOptionPane.showMessageDialog(mainPanel, 
                    "Successfully exported " + nodes.size() + " nodes to:\n" + file.getAbsolutePath(), 
                    "Export Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                callbacks.printOutput("Logic Mapper: Exported " + nodes.size() + " nodes to " + file.getAbsolutePath());
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Error exporting: " + ex.getMessage(), 
                    "Export Error", 
                    JOptionPane.ERROR_MESSAGE);
                callbacks.printError("Logic Mapper export error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
    private void importFromJson() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Logic Mapper from JSON");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files (*.json)", "json"));
        
        int result = fileChooser.showOpenDialog(mainPanel);
        if (result == JFileChooser.APPROVE_OPTION) {
            // Run import in background thread to avoid blocking UI
            new Thread(() -> {
                try {
                    importFromJsonFile(fileChooser.getSelectedFile());
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(mainPanel, 
                            "Error importing: " + e.getMessage(), 
                            "Import Error", 
                            JOptionPane.ERROR_MESSAGE);
                    });
                    callbacks.printError("Logic Mapper import error: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    private void importFromJsonFile(File file) {
        try {
                // Read JSON file
                StringBuilder json = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        json.append(line).append("\n");
                    }
                }
                
                // Parse JSON
                LogicMapperData data = parseJson(json.toString());
                
                if (data == null || data.nodes == null || data.nodes.isEmpty()) {
                    JOptionPane.showMessageDialog(mainPanel, 
                        "Invalid or empty JSON file!", 
                        "Import Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Ask user: replace or append
                int choice = JOptionPane.showConfirmDialog(mainPanel,
                    "Do you want to replace existing nodes or append to them?\n\n" +
                    "Yes = Replace all existing nodes\n" +
                    "No = Append to existing nodes\n" +
                    "Cancel = Abort import",
                    "Import Options",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                
                if (choice == JOptionPane.CANCEL_OPTION) {
                    return;
                }
                
                if (choice == JOptionPane.YES_OPTION) {
                    // Replace
                    nodes.clear();
                }
                // else: append (NO_OPTION)
                
                // Create nodes from data
                Map<String, RequestNode> nodeMap = new HashMap<>();
                List<LogicMapperData.NodeData> nodeDataList = data.nodes;
                
                // Counters for import status
                final int[] successCount = {0};
                final int[] failCount = {0};
                
                // First pass: create all nodes
                
                for (LogicMapperData.NodeData nodeData : nodeDataList) {
                    try {
                        // Reconstruct request
                        byte[] requestBytes = reconstructRequest(nodeData);
                        if (requestBytes == null || requestBytes.length == 0) {
                            callbacks.printError("Logic Mapper: Failed to reconstruct request for node: " + nodeData.displayName);
                            failCount[0]++;
                            continue;
                        }
                        
                        java.net.URL urlObj = new java.net.URL(nodeData.url);
                        IHttpService httpService = helpers.buildHttpService(
                            urlObj.getHost(),
                            urlObj.getPort() == -1 ? (urlObj.getProtocol().equals("https") ? 443 : 80) : urlObj.getPort(),
                            urlObj.getProtocol().equals("https")
                        );
                        
                        // Make HTTP request to get a valid IHttpRequestResponse
                        // Note: makeHttpRequest will actually send the request, but we need it for the IHttpRequestResponse object
                        IHttpRequestResponse message = callbacks.makeHttpRequest(httpService, requestBytes);
                        
                        if (message == null) {
                            callbacks.printError("Logic Mapper: makeHttpRequest returned null for: " + nodeData.url);
                            failCount[0]++;
                            continue;
                        }
                        
                        // Verify the request was set correctly
                        if (message.getRequest() == null || message.getRequest().length == 0) {
                            callbacks.printError("Logic Mapper: Request is null or empty for: " + nodeData.url);
                            failCount[0]++;
                            continue;
                        }
                        
                        RequestNode node = new RequestNode(message, helpers);
                        node.setX(nodeData.x);
                        node.setY(nodeData.y);
                        node.setNotes(nodeData.notes != null ? nodeData.notes : "");
                        node.setNodeId(nodeData.id);
                        
                        nodes.add(node);
                        nodeMap.put(nodeData.id, node);
                        successCount[0]++;
                        
                        callbacks.printOutput("Logic Mapper: Imported node: " + nodeData.displayName + " at (" + nodeData.x + ", " + nodeData.y + ")");
                        
                    } catch (Exception e) {
                        callbacks.printError("Logic Mapper: Error creating node from import: " + e.getMessage());
                        e.printStackTrace();
                        failCount[0]++;
                    }
                }
                
                if (successCount[0] == 0) {
                    JOptionPane.showMessageDialog(mainPanel, 
                        "Failed to import any nodes. Check error log for details.", 
                        "Import Failed", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (failCount[0] > 0) {
                    callbacks.printOutput("Logic Mapper: Imported " + successCount[0] + " nodes successfully, " + failCount[0] + " failed");
                }
                
                // Second pass: restore connections
                // Find nodes by their IDs
                Map<String, RequestNode> idToNodeMap = new HashMap<>();
                for (RequestNode node : nodes) {
                    idToNodeMap.put(node.getNodeId(), node);
                }
                
                for (LogicMapperData.NodeData nodeData : nodeDataList) {
                    RequestNode node = idToNodeMap.get(nodeData.id);
                    if (node != null && nodeData.connections != null) {
                        for (String targetId : nodeData.connections) {
                            RequestNode targetNode = idToNodeMap.get(targetId);
                            if (targetNode != null) {
                                node.addConnection(targetNode);
                            }
                        }
                    }
                }
                
                // Update UI on EDT - ensure it happens
                SwingUtilities.invokeLater(() -> {
                    callbacks.printOutput("Logic Mapper: Updating UI - nodes count: " + nodes.size());
                    updateStatus();
                    canvas.revalidate();
                    canvas.repaint();
                    mainPanel.revalidate();
                    mainPanel.repaint();
                    callbacks.printOutput("Logic Mapper: UI update complete");
                });
                
                final String message = "Successfully imported " + successCount[0] + " node(s)!";
                final String finalMessage = failCount[0] > 0 ? 
                    message + "\n" + failCount[0] + " node(s) failed to import." : message;
                final int messageType = failCount[0] > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
                
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(mainPanel, 
                        finalMessage, 
                        "Import Success", 
                        messageType);
                });
                
                callbacks.printOutput("Logic Mapper: Imported " + successCount[0] + " nodes from " + file.getAbsolutePath());
                
        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Error importing: " + ex.getMessage(), 
                    "Import Error", 
                    JOptionPane.ERROR_MESSAGE);
            });
            callbacks.printError("Logic Mapper import error: " + ex.getMessage());
            ex.printStackTrace();
            // Don't rethrow - error already handled
        }
    }
    
    private byte[] reconstructRequest(LogicMapperData.NodeData nodeData) {
        // Build request line
        StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append(nodeData.method).append(" ");
        
        try {
            java.net.URL url = new java.net.URL(nodeData.url);
            String path = url.getPath();
            if (path.isEmpty()) path = "/";
            String query = url.getQuery();
            if (query != null && !query.isEmpty()) {
                path += "?" + query;
            }
            requestBuilder.append(path);
        } catch (Exception e) {
            requestBuilder.append("/");
        }
        
        requestBuilder.append(" HTTP/1.1\r\n");
        
        // Add headers
        if (nodeData.headers != null) {
            for (String header : nodeData.headers) {
                requestBuilder.append(header).append("\r\n");
            }
        }
        
        requestBuilder.append("\r\n");
        
        // Add body if exists
        byte[] headerBytes = requestBuilder.toString().getBytes(StandardCharsets.UTF_8);
        if (nodeData.body != null && !nodeData.body.isEmpty()) {
            try {
                byte[] bodyBytes = helpers.base64Decode(nodeData.body);
                byte[] fullRequest = new byte[headerBytes.length + bodyBytes.length];
                System.arraycopy(headerBytes, 0, fullRequest, 0, headerBytes.length);
                System.arraycopy(bodyBytes, 0, fullRequest, headerBytes.length, bodyBytes.length);
                return fullRequest;
            } catch (Exception e) {
                // If decode fails, just return headers
            }
        }
        
        return headerBytes;
    }
    
    private String convertToJson(LogicMapperData data) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"version\": \"").append(escapeJson(data.version)).append("\",\n");
        json.append("  \"nodes\": [\n");
        
        for (int i = 0; i < data.nodes.size(); i++) {
            LogicMapperData.NodeData node = data.nodes.get(i);
            json.append("    {\n");
            json.append("      \"id\": \"").append(escapeJson(node.id)).append("\",\n");
            json.append("      \"x\": ").append(node.x).append(",\n");
            json.append("      \"y\": ").append(node.y).append(",\n");
            json.append("      \"notes\": \"").append(escapeJson(node.notes)).append("\",\n");
            json.append("      \"displayName\": \"").append(escapeJson(node.displayName)).append("\",\n");
            json.append("      \"method\": \"").append(escapeJson(node.method)).append("\",\n");
            json.append("      \"url\": \"").append(escapeJson(node.url)).append("\",\n");
            json.append("      \"headers\": [\n");
            
            if (node.headers != null) {
                for (int j = 0; j < node.headers.size(); j++) {
                    json.append("        \"").append(escapeJson(node.headers.get(j))).append("\"");
                    if (j < node.headers.size() - 1) json.append(",");
                    json.append("\n");
                }
            }
            
            json.append("      ],\n");
            json.append("      \"body\": \"").append(escapeJson(node.body != null ? node.body : "")).append("\",\n");
            json.append("      \"connections\": [\n");
            
            if (node.connections != null) {
                for (int j = 0; j < node.connections.size(); j++) {
                    json.append("        \"").append(escapeJson(node.connections.get(j))).append("\"");
                    if (j < node.connections.size() - 1) json.append(",");
                    json.append("\n");
                }
            }
            
            json.append("      ]\n");
            json.append("    }");
            if (i < data.nodes.size() - 1) json.append(",");
            json.append("\n");
        }
        
        json.append("  ]\n");
        json.append("}\n");
        return json.toString();
    }
    
    private LogicMapperData parseJson(String json) {
        try {
            LogicMapperData data = new LogicMapperData();
            
            // Simple JSON parser (manual parsing)
            int nodesStart = json.indexOf("\"nodes\": [");
            if (nodesStart == -1) return null;
            
            int bracketStart = json.indexOf("[", nodesStart);
            int bracketEnd = findMatchingBracket(json, bracketStart);
            String nodesJson = json.substring(bracketStart + 1, bracketEnd);
            
            // Parse each node
            List<LogicMapperData.NodeData> nodes = new ArrayList<>();
            int pos = 0;
            while (pos < nodesJson.length()) {
                int nodeStart = nodesJson.indexOf("{", pos);
                if (nodeStart == -1) break;
                int nodeEnd = findMatchingBracket(nodesJson, nodeStart);
                String nodeJson = nodesJson.substring(nodeStart + 1, nodeEnd);
                
                LogicMapperData.NodeData node = parseNodeJson(nodeJson);
                if (node != null) {
                    nodes.add(node);
                }
                
                pos = nodeEnd + 1;
            }
            
            data.nodes = nodes;
            return data;
            
        } catch (Exception e) {
            callbacks.printError("JSON parse error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private LogicMapperData.NodeData parseNodeJson(String nodeJson) {
        try {
            LogicMapperData.NodeData node = new LogicMapperData.NodeData();
            
            node.id = extractJsonString(nodeJson, "id");
            node.x = extractJsonInt(nodeJson, "x");
            node.y = extractJsonInt(nodeJson, "y");
            node.notes = extractJsonString(nodeJson, "notes");
            node.displayName = extractJsonString(nodeJson, "displayName");
            node.method = extractJsonString(nodeJson, "method");
            node.url = extractJsonString(nodeJson, "url");
            node.body = extractJsonString(nodeJson, "body");
            
            // Parse headers array
            node.headers = extractJsonArray(nodeJson, "headers");
            
            // Parse connections array
            List<String> connections = extractJsonArray(nodeJson, "connections");
            node.connections = connections;
            
            return node;
        } catch (Exception e) {
            callbacks.printError("Node parse error: " + e.getMessage());
            return null;
        }
    }
    
    private String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\": \"";
        int start = json.indexOf(pattern);
        if (start == -1) return "";
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "";
        return unescapeJson(json.substring(start, end));
    }
    
    private int extractJsonInt(String json, String key) {
        String pattern = "\"" + key + "\": ";
        int start = json.indexOf(pattern);
        if (start == -1) return 0;
        start += pattern.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (Exception e) {
            return 0;
        }
    }
    
    private List<String> extractJsonArray(String json, String key) {
        List<String> result = new ArrayList<>();
        String pattern = "\"" + key + "\": [";
        int start = json.indexOf(pattern);
        if (start == -1) return result;
        start += pattern.length();
        int bracketEnd = findMatchingBracket(json, start - 1);
        String arrayContent = json.substring(start, bracketEnd);
        
        int pos = 0;
        while (pos < arrayContent.length()) {
            int quoteStart = arrayContent.indexOf("\"", pos);
            if (quoteStart == -1) break;
            quoteStart++;
            int quoteEnd = arrayContent.indexOf("\"", quoteStart);
            if (quoteEnd == -1) break;
            result.add(unescapeJson(arrayContent.substring(quoteStart, quoteEnd)));
            pos = quoteEnd + 1;
        }
        
        return result;
    }
    
    private int findMatchingBracket(String str, int startPos) {
        int depth = 1;
        char open = str.charAt(startPos);
        char close = (open == '[') ? ']' : '}';
        
        for (int i = startPos + 1; i < str.length(); i++) {
            if (str.charAt(i) == open) depth++;
            else if (str.charAt(i) == close) {
                depth--;
                if (depth == 0) return i;
            }
        }
        return str.length();
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    private String unescapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\\"", "\"")
                  .replace("\\\\", "\\")
                  .replace("\\n", "\n")
                  .replace("\\r", "\r")
                  .replace("\\t", "\t");
    }
}

