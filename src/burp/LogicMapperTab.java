package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogicMapperTab {
    
    private final MontoyaApi api;
    private final JPanel mainPanel;
    private final LogicMapperCanvas canvas;
    private final List<RequestNode> nodes;
    private JLabel statusLabel;
    
    public LogicMapperTab(MontoyaApi api) {
        this.api = api;
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
                getBurpFrame(),
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
    
    public void addRequest(HttpRequestResponse message) {
        SwingUtilities.invokeLater(() -> {
            // Create new node
            RequestNode newNode = new RequestNode(message, api);
            
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
            
            canvas.revalidate();
            canvas.repaint();
            mainPanel.revalidate();
            mainPanel.repaint();
            
            // Show notification
            HttpRequest req = message.request();
            String url = (req != null && req.url() != null) ? req.url() : "unknown URL";
            api.logging().logToOutput("Added request to Logic Mapper: " + url);
        });
    }
    
    public Component getUiComponent() {
        return mainPanel;
    }
    
    private void showHelpDialog() {
        JDialog helpDialog = new JDialog(getBurpFrame(), "Logic Mapper - How To Use", true);
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
        helpDialog.setLocationRelativeTo(getBurpFrame());
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
            JOptionPane.showMessageDialog(getBurpFrame(), 
                "No nodes to export!", 
                "Export", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Logic Mapper to JSON");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files (*.json)", "json"));
        fileChooser.setSelectedFile(new java.io.File("logic_mapper_export.json"));
        
        int result = fileChooser.showSaveDialog(getBurpFrame());
        if (result == JFileChooser.APPROVE_OPTION) {
            final File fileToSave = fileChooser.getSelectedFile();
            
            // Run export in background thread to avoid blocking UI
            new Thread(() -> {
                try {
                    File file = fileToSave;
                    String filePath = file.getAbsolutePath();
                    if (!filePath.toLowerCase().endsWith(".json")) {
                        file = new File(filePath + ".json");
                    }
                    
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    
                    JsonObject root = new JsonObject();
                    root.addProperty("version", BurpExtender.VERSION);
                    
                    JsonArray nodesArray = new JsonArray();
                    for (RequestNode node : nodes) {
                        JsonObject nodeObj = new JsonObject();
                        nodeObj.addProperty("id", node.getNodeId());
                        nodeObj.addProperty("x", node.getX());
                        nodeObj.addProperty("y", node.getY());
                        nodeObj.addProperty("notes", node.getNotes() != null ? node.getNotes() : "");
                        nodeObj.addProperty("displayName", node.getDisplayName());
                        
                        HttpRequest request = node.getMessage().request();
                        nodeObj.addProperty("method", request.method());
                        nodeObj.addProperty("url", request.url());
                        
                        JsonArray headersArray = new JsonArray();
                        for (burp.api.montoya.http.message.HttpHeader header : request.headers()) {
                            headersArray.add(header.name() + ": " + header.value());
                        }
                        nodeObj.add("headers", headersArray);
                        
                        nodeObj.addProperty("body", java.util.Base64.getEncoder().encodeToString(request.body().getBytes()));
                        
                        JsonArray connectionsArray = new JsonArray();
                        for (String connId : node.getConnectionIds(nodes)) {
                            connectionsArray.add(connId);
                        }
                        nodeObj.add("connections", connectionsArray);
                        
                        nodesArray.add(nodeObj);
                    }
                    root.add("nodes", nodesArray);
                    
                    String jsonString = gson.toJson(root);
                    
                    // Write to file
                    try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                        writer.write(jsonString);
                    }
                    
                    final File finalFile = file;
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(getBurpFrame(), 
                            "Successfully exported " + nodes.size() + " nodes to:\n" + finalFile.getAbsolutePath(), 
                            "Export Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                    });
                    
                    api.logging().logToOutput("Logic Mapper: Exported " + nodes.size() + " nodes to " + file.getAbsolutePath());
                    
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(getBurpFrame(), 
                            "Error exporting: " + ex.getMessage(), 
                            "Export Error", 
                            JOptionPane.ERROR_MESSAGE);
                    });
                    api.logging().logToError("Logic Mapper export error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }).start();
        }
    }
    
    private void importFromJson() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Logic Mapper from JSON");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files (*.json)", "json"));
        
        int result = fileChooser.showOpenDialog(getBurpFrame());
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            // Run import in background thread to avoid blocking UI
            new Thread(() -> {
                try {
                    importFromFile(selectedFile);
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(getBurpFrame(), 
                            "Error importing: " + e.getMessage(), 
                            "Import Error", 
                            JOptionPane.ERROR_MESSAGE);
                    });
                    api.logging().logToError("Logic Mapper import error: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    private void importFromFile(File file) throws IOException, InterruptedException, java.lang.reflect.InvocationTargetException {
        // Read JSON file
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        
        JsonElement root = JsonParser.parseString(sb.toString());
        
        if (root == null || !root.isJsonObject()) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(getBurpFrame(), 
                "Invalid or empty JSON file!", 
                "Import Error", 
                JOptionPane.ERROR_MESSAGE));
            return;
        }
        
        JsonObject rootObj = root.getAsJsonObject();
        JsonArray nodesArray = rootObj.getAsJsonArray("nodes");
        
        if (nodesArray == null || nodesArray.isEmpty()) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(getBurpFrame(), 
                "No nodes found in JSON file!", 
                "Import Error", 
                JOptionPane.INFORMATION_MESSAGE));
            return;
        }
        
        // Ask user: replace or append
        final int[] choice = new int[1];
        SwingUtilities.invokeAndWait(() -> {
            choice[0] = JOptionPane.showConfirmDialog(getBurpFrame(),
                "Do you want to replace existing nodes or append to them?\n\n" +
                "Yes = Replace all existing nodes\n" +
                "No = Append to existing nodes\n" +
                "Cancel = Abort import",
                "Import Options",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        });
        
        if (choice[0] == JOptionPane.CANCEL_OPTION) {
            return;
        }
        
        if (choice[0] == JOptionPane.YES_OPTION) {
            // Replace
            nodes.clear();
        }
        // else: append (NO_OPTION)
        
        // Create nodes from data
        Map<String, RequestNode> nodeMap = new HashMap<>();
        List<JsonObject> nodeDataList = new ArrayList<>();
        for (JsonElement el : nodesArray) {
            if (el.isJsonObject()) {
                nodeDataList.add(el.getAsJsonObject());
            }
        }
        
        // Counters for import status
        final int[] successCount = {0};
        final int[] failCount = {0};
        
        // First pass: create all nodes
        for (JsonObject nodeData : nodeDataList) {
            try {
                String method = nodeData.get("method").getAsString();
                String url = nodeData.get("url").getAsString();
                String bodyBase64 = nodeData.get("body").getAsString();
                byte[] bodyBytes = java.util.Base64.getDecoder().decode(bodyBase64);
                ByteArray body = ByteArray.byteArray(bodyBytes);
                
                List<burp.api.montoya.http.message.HttpHeader> headers = new ArrayList<>();
                JsonArray headersArray = nodeData.getAsJsonArray("headers");
                if (headersArray != null) {
                    for (JsonElement h : headersArray) {
                        String hStr = h.getAsString();
                        int colon = hStr.indexOf(":");
                        if (colon > 0) {
                            headers.add(burp.api.montoya.http.message.HttpHeader.httpHeader(hStr.substring(0, colon).trim(), hStr.substring(colon + 1).trim()));
                        }
                    }
                }
                
                // Reconstruct HttpRequest
                HttpRequest request = HttpRequest.httpRequestFromUrl(url).withMethod(method).withAddedHeaders(headers).withBody(body);
                
                // Make HTTP request to get a valid HttpRequestResponse.
                HttpRequestResponse message = api.http().sendRequest(request);
                
                if (message == null) {
                    api.logging().logToError("Logic Mapper: sendRequest returned null for: " + url);
                    failCount[0]++;
                    continue;
                }
                
                RequestNode node = new RequestNode(message, api);
                node.setX(nodeData.get("x").getAsInt());
                node.setY(nodeData.get("y").getAsInt());
                node.setNotes(nodeData.get("notes").getAsString());
                node.setNodeId(nodeData.get("id").getAsString());
                
                nodeMap.put(node.getNodeId(), node);
                SwingUtilities.invokeLater(() -> {
                    nodes.add(node);
                    canvas.repaint();
                });
                successCount[0]++;
                
                api.logging().logToOutput("Logic Mapper: Imported node: " + nodeData.get("displayName").getAsString() + " at (" + nodeData.get("x").getAsInt() + ", " + nodeData.get("y").getAsInt() + ")");
                
            } catch (Exception e) {
                api.logging().logToError("Logic Mapper: Error creating node from import: " + e.getMessage());
                e.printStackTrace();
                failCount[0]++;
            }
        }
        
        if (successCount[0] == 0) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(getBurpFrame(), 
                "Failed to import any nodes. Check error log for details.", 
                "Import Failed", 
                JOptionPane.ERROR_MESSAGE));
            return;
        }
        
        if (failCount[0] > 0) {
            api.logging().logToOutput("Logic Mapper: Imported " + successCount[0] + " nodes successfully, " + failCount[0] + " failed");
        }
        
        // Second pass: restore connections
        SwingUtilities.invokeLater(() -> {
            for (JsonObject nodeData : nodeDataList) {
                RequestNode source = nodeMap.get(nodeData.get("id").getAsString());
                if (source != null && nodeData.has("connections")) {
                    JsonArray connectionsArray = nodeData.getAsJsonArray("connections");
                    if (connectionsArray != null) {
                        for (JsonElement connIdEl : connectionsArray) {
                            RequestNode target = nodeMap.get(connIdEl.getAsString());
                            if (target != null) {
                                source.addConnection(target);
                            }
                        }
                    }
                }
            }
        });
        
        // Update UI on EDT - ensure it happens
        SwingUtilities.invokeLater(() -> {
            api.logging().logToOutput("Logic Mapper: Updating UI - nodes count: " + nodes.size());
            updateStatus();
            canvas.revalidate();
            canvas.repaint();
            mainPanel.revalidate();
            mainPanel.repaint();
            api.logging().logToOutput("Logic Mapper: UI update complete");
        });
        
        final String message = "Successfully imported " + successCount[0] + " node(s)!";
        final String finalMessage = failCount[0] > 0 ? 
            message + "\n" + failCount[0] + " node(s) failed to import." : message;
        final int messageType = failCount[0] > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
        
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(getBurpFrame(), 
                finalMessage, 
                "Import Success", 
                messageType);
        });
        
        api.logging().logToOutput("Logic Mapper: Imported " + successCount[0] + " nodes from " + file.getAbsolutePath());
    }
    
    private Frame getBurpFrame() {
        for (Frame frame : Frame.getFrames()) {
            if (frame.isVisible() && frame.getTitle().contains("Burp Suite")) {
                return frame;
            }
        }
        return null;
    }
}
