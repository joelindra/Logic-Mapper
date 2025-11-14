package burp;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class RequestNode {
    
    private final IHttpRequestResponse message;
    private final IExtensionHelpers helpers;
    private int x, y;
    private String notes;
    private final List<RequestNode> connections;
    private String displayName;
    
    private static final int NODE_WIDTH = 200;
    private static final int NODE_HEIGHT = 80;
    private static final int CORNER_RADIUS = 10;
    
    public RequestNode(IHttpRequestResponse message, IExtensionHelpers helpers) {
        this.message = message;
        this.helpers = helpers;
        this.connections = new ArrayList<>();
        this.notes = "";
        this.x = 50;
        this.y = 100;
        
        // Generate display name from request
        IRequestInfo requestInfo = helpers.analyzeRequest(message);
        String method = requestInfo.getMethod();
        String url = requestInfo.getUrl().toString();
        
        // Extract short URL
        try {
            java.net.URL urlObj = new java.net.URL(url);
            String path = urlObj.getPath();
            if (path.length() > 30) {
                path = path.substring(0, 27) + "...";
            }
            this.displayName = method + " " + path;
        } catch (Exception e) {
            this.displayName = method + " " + url.substring(0, Math.min(30, url.length()));
        }
    }
    
    public boolean contains(Point p) {
        return p.x >= x && p.x <= x + NODE_WIDTH &&
               p.y >= y && p.y <= y + NODE_HEIGHT;
    }
    
    public boolean isNotesIndicatorClicked(Point p) {
        if (notes == null || notes.trim().isEmpty()) {
            return false;
        }
        // Notes indicator is at: x + NODE_WIDTH - 20, y + 5, size 12x12
        int indicatorX = x + NODE_WIDTH - 20;
        int indicatorY = y + 5;
        int indicatorSize = 12;
        return p.x >= indicatorX && p.x <= indicatorX + indicatorSize &&
               p.y >= indicatorY && p.y <= indicatorY + indicatorSize;
    }
    
    public Point getNotesIndicatorPosition() {
        // Returns the center position of the notes indicator
        return new Point(x + NODE_WIDTH - 14, y + 11);
    }
    
    public void paint(Graphics2D g2d, boolean selected) {
        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.fill(new RoundRectangle2D.Float(
            x + 3, y + 3, NODE_WIDTH, NODE_HEIGHT, CORNER_RADIUS, CORNER_RADIUS
        ));
        
        // Draw node background
        if (selected) {
            g2d.setColor(new Color(220, 235, 255));
        } else {
            g2d.setColor(new Color(255, 255, 255));
        }
        g2d.fill(new RoundRectangle2D.Float(
            x, y, NODE_WIDTH, NODE_HEIGHT, CORNER_RADIUS, CORNER_RADIUS
        ));
        
        // Draw border
        if (selected) {
            g2d.setStroke(new BasicStroke(3));
            g2d.setColor(new Color(70, 130, 180));
        } else {
            g2d.setStroke(new BasicStroke(2));
            g2d.setColor(new Color(200, 200, 200));
        }
        g2d.draw(new RoundRectangle2D.Float(
            x, y, NODE_WIDTH, NODE_HEIGHT, CORNER_RADIUS, CORNER_RADIUS
        ));
        
        // Draw method badge
        IRequestInfo requestInfo = helpers.analyzeRequest(message);
        String method = requestInfo.getMethod();
        Color methodColor = getMethodColor(method);
        
        g2d.setColor(methodColor);
        g2d.fill(new RoundRectangle2D.Float(
            x + 5, y + 5, 50, 20, 5, 5
        ));
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2d.getFontMetrics();
        int methodWidth = fm.stringWidth(method);
        g2d.drawString(method, x + 5 + (50 - methodWidth) / 2, y + 18);
        
        // Draw URL/path
        g2d.setColor(new Color(50, 50, 50));
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        
        String displayText = displayName;
        int maxWidth = NODE_WIDTH - 15;
        if (fm.stringWidth(displayText) > maxWidth) {
            displayText = truncateString(displayText, fm, maxWidth);
        }
        g2d.drawString(displayText, x + 10, y + 40);
        
        // Draw notes indicator
        if (notes != null && !notes.trim().isEmpty()) {
            g2d.setColor(new Color(255, 200, 0));
            g2d.fillOval(x + NODE_WIDTH - 20, y + 5, 12, 12);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 9));
            g2d.drawString("!", x + NODE_WIDTH - 16, y + 13);
        }
        
        // Draw connection count
        if (!connections.isEmpty()) {
            g2d.setColor(new Color(100, 150, 255));
            g2d.setFont(new Font("Arial", Font.PLAIN, 9));
            g2d.drawString("→ " + connections.size(), x + 10, y + NODE_HEIGHT - 5);
        }
    }
    
    private Color getMethodColor(String method) {
        switch (method.toUpperCase()) {
            case "GET":
                return new Color(67, 160, 71);
            case "POST":
                return new Color(33, 150, 243);
            case "PUT":
                return new Color(255, 152, 0);
            case "DELETE":
                return new Color(244, 67, 54);
            case "PATCH":
                return new Color(156, 39, 176);
            default:
                return new Color(158, 158, 158);
        }
    }
    
    private String truncateString(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        String truncated = text + "...";
        while (fm.stringWidth(truncated) > maxWidth && truncated.length() > 3) {
            truncated = truncated.substring(0, truncated.length() - 4) + "...";
        }
        return truncated;
    }
    
    public void showNotesPopup(Component parent) {
        if (notes == null || notes.trim().isEmpty()) {
            return;
        }
        
        Point indicatorPos = getNotesIndicatorPosition();
        
        // Create popup window
        JWindow popup = new JWindow((Frame) SwingUtilities.getWindowAncestor(parent));
        popup.setLayout(new BorderLayout());
        
        // Create content panel
        JPanel contentPanel = new JPanel(new BorderLayout(8, 8));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        contentPanel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Notes");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(new Color(70, 130, 180));
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Notes text
        JTextArea notesArea = new JTextArea(notes);
        notesArea.setEditable(false);
        notesArea.setFont(new Font("Arial", Font.PLAIN, 11));
        notesArea.setBackground(new Color(250, 250, 250));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        // Calculate optimal size
        FontMetrics fm = notesArea.getFontMetrics(notesArea.getFont());
        int maxWidth = 300;
        int lineHeight = fm.getHeight();
        int textWidth = fm.stringWidth(notes);
        int lines = Math.max(1, (int) Math.ceil((double) textWidth / maxWidth));
        int height = Math.min(200, lines * lineHeight + 20);
        
        JScrollPane scrollPane = new JScrollPane(notesArea);
        scrollPane.setPreferredSize(new Dimension(maxWidth, height));
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        popup.add(contentPanel);
        popup.pack();
        
        // Position popup next to the indicator
        Point screenPos = new Point(indicatorPos.x + 15, indicatorPos.y - popup.getHeight() / 2);
        SwingUtilities.convertPointToScreen(screenPos, parent);
        popup.setLocation(screenPos);
        
        // Make popup disappear when clicking outside or after 10 seconds
        popup.setAlwaysOnTop(true);
        popup.setVisible(true);
        
        // Auto-close after 10 seconds
        javax.swing.Timer timer = new javax.swing.Timer(10000, e -> popup.dispose());
        timer.setRepeats(false);
        timer.start();
        
        // Close on click anywhere (including on popup itself)
        java.awt.event.MouseAdapter closeListener = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                popup.dispose();
                timer.stop();
            }
        };
        
        popup.addMouseListener(closeListener);
        
        // Close when clicking on canvas (one-time listener)
        java.awt.event.MouseAdapter canvasCloseListener = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (popup.isVisible()) {
                    popup.dispose();
                    timer.stop();
                    parent.removeMouseListener(this);
                }
            }
        };
        parent.addMouseListener(canvasCloseListener);
    }
    
    public void showEditDialog() {
        JDialog dialog = new JDialog((Frame) null, "Edit Request Node - " + displayName, true);
        dialog.setLayout(new BorderLayout());
        
        IRequestInfo requestInfo = helpers.analyzeRequest(message);
        
        // Main content panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // ========== TAB 1: Request Information ==========
        JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Request details section
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            "Request Details",
            0, 0,
            new Font("Arial", Font.BOLD, 13)
        ));
        
        // Method
        JPanel methodPanel = createInfoRow("HTTP Method:", requestInfo.getMethod(), 
            getMethodColor(requestInfo.getMethod()));
        detailsPanel.add(methodPanel);
        detailsPanel.add(Box.createVerticalStrut(10));
        
        // URL
        String fullUrl = requestInfo.getUrl().toString();
        JPanel urlPanel = createInfoRow("URL:", fullUrl, new Color(50, 50, 50));
        detailsPanel.add(urlPanel);
        detailsPanel.add(Box.createVerticalStrut(10));
        
        // Host
        String host = requestInfo.getUrl().getHost();
        JPanel hostPanel = createInfoRow("Host:", host, new Color(50, 50, 50));
        detailsPanel.add(hostPanel);
        detailsPanel.add(Box.createVerticalStrut(10));
        
        // Path
        String path = requestInfo.getUrl().getPath();
        if (path.isEmpty()) path = "/";
        JPanel pathPanel = createInfoRow("Path:", path, new Color(50, 50, 50));
        detailsPanel.add(pathPanel);
        detailsPanel.add(Box.createVerticalStrut(10));
        
        // Port
        int port = requestInfo.getUrl().getPort();
        if (port == -1) {
            port = requestInfo.getUrl().getProtocol().equals("https") ? 443 : 80;
        }
        JPanel portPanel = createInfoRow("Port:", String.valueOf(port), new Color(50, 50, 50));
        detailsPanel.add(portPanel);
        detailsPanel.add(Box.createVerticalStrut(10));
        
        // Headers count
        JPanel headersPanel = createInfoRow("Headers:", 
            requestInfo.getHeaders().size() + " headers", new Color(50, 50, 50));
        detailsPanel.add(headersPanel);
        detailsPanel.add(Box.createVerticalStrut(10));
        
        // Body info
        byte[] requestBytes = message.getRequest();
        int bodyOffset = requestInfo.getBodyOffset();
        int bodyLength = requestBytes.length - bodyOffset;
        String bodyInfo = bodyLength > 0 ? bodyLength + " bytes" : "No body";
        JPanel bodyPanel = createInfoRow("Body:", bodyInfo, new Color(50, 50, 50));
        detailsPanel.add(bodyPanel);
        
        infoPanel.add(detailsPanel, BorderLayout.NORTH);
        
        // Headers section
        JPanel headersSection = new JPanel(new BorderLayout());
        headersSection.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            "Request Headers",
            0, 0,
            new Font("Arial", Font.BOLD, 13)
        ));
        
        JTextArea headersArea = new JTextArea();
        headersArea.setEditable(false);
        headersArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        headersArea.setBackground(new Color(250, 250, 250));
        StringBuilder headersText = new StringBuilder();
        for (String header : requestInfo.getHeaders()) {
            headersText.append(header).append("\n");
        }
        headersArea.setText(headersText.toString());
        JScrollPane headersScroll = new JScrollPane(headersArea);
        headersScroll.setPreferredSize(new Dimension(0, 150));
        headersSection.add(headersScroll, BorderLayout.CENTER);
        
        infoPanel.add(headersSection, BorderLayout.CENTER);
        
        tabbedPane.addTab("Request Info", infoPanel);
        
        // ========== TAB 2: Request Body ==========
        JPanel bodyPanelTab = new JPanel(new BorderLayout());
        bodyPanelTab.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        if (bodyLength > 0) {
            byte[] bodyBytes = new byte[bodyLength];
            System.arraycopy(requestBytes, bodyOffset, bodyBytes, 0, bodyLength);
            
            JTextArea bodyArea = new JTextArea();
            bodyArea.setEditable(false);
            bodyArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
            bodyArea.setBackground(new Color(250, 250, 250));
            
            // Enable word wrap and line wrap to avoid horizontal scrolling
            bodyArea.setLineWrap(true);
            bodyArea.setWrapStyleWord(true);
            
            // Try to display as text, otherwise show base64
            String bodyText = helpers.bytesToString(bodyBytes);
            // Try to detect if it's printable text
            boolean isPrintable = true;
            for (byte b : bodyBytes) {
                if (b < 32 && b != 9 && b != 10 && b != 13) {
                    isPrintable = false;
                    break;
                }
            }
            if (!isPrintable && bodyBytes.length < 10000) {
                // For binary data, show base64
                bodyText = helpers.base64Encode(bodyBytes);
            }
            bodyArea.setText(bodyText);
            
            JScrollPane bodyScroll = new JScrollPane(bodyArea);
            bodyScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            bodyScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            bodyPanelTab.add(new JLabel("Request Body (" + bodyLength + " bytes):"), BorderLayout.NORTH);
            bodyPanelTab.add(bodyScroll, BorderLayout.CENTER);
        } else {
            JLabel noBodyLabel = new JLabel("No request body", JLabel.CENTER);
            noBodyLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            noBodyLabel.setForeground(new Color(150, 150, 150));
            bodyPanelTab.add(noBodyLabel, BorderLayout.CENTER);
        }
        
        tabbedPane.addTab("Request Body", bodyPanelTab);
        
        // ========== TAB 3: Notes ==========
        JPanel notesPanel = new JPanel(new BorderLayout(10, 10));
        notesPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel notesLabel = new JLabel("Notes (for documentation and audit):");
        notesLabel.setFont(new Font("Arial", Font.BOLD, 13));
        notesLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JTextArea notesArea = new JTextArea(notes != null ? notes : "", 15, 50);
        notesArea.setFont(new Font("Arial", Font.PLAIN, 12));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            "Your Notes",
            0, 0,
            new Font("Arial", Font.BOLD, 12)
        ));
        
        // Hint label
        JLabel hintLabel = new JLabel("<html><i>Tip: Add notes to document the purpose of this request in the business flow</i></html>");
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        hintLabel.setForeground(new Color(120, 120, 120));
        hintLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        notesPanel.add(notesLabel, BorderLayout.NORTH);
        notesPanel.add(notesScroll, BorderLayout.CENTER);
        notesPanel.add(hintLabel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Notes", notesPanel);
        
        // ========== Buttons ==========
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton saveBtn = new JButton("Save Notes");
        saveBtn.setFont(new Font("Arial", Font.BOLD, 12));
        saveBtn.setPreferredSize(new Dimension(120, 30));
        saveBtn.setBackground(new Color(70, 130, 180));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> {
            notes = notesArea.getText();
            dialog.dispose();
        });
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        cancelBtn.setPreferredSize(new Dimension(100, 30));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        
        // Final layout
        dialog.add(tabbedPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setSize(700, 600);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
    
    private JPanel createInfoRow(String label, String value, Color valueColor) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.BOLD, 12));
        labelComp.setPreferredSize(new Dimension(120, 20));
        
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Arial", Font.PLAIN, 12));
        valueComp.setForeground(valueColor);
        
        // If value is too long, make it selectable text area
        if (value.length() > 80) {
            JTextArea valueArea = new JTextArea(value);
            valueArea.setEditable(false);
            valueArea.setFont(new Font("Arial", Font.PLAIN, 12));
            valueArea.setForeground(valueColor);
            valueArea.setBackground(new Color(250, 250, 250));
            valueArea.setLineWrap(true);
            valueArea.setWrapStyleWord(true);
            valueArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)
            ));
            panel.add(labelComp, BorderLayout.WEST);
            panel.add(new JScrollPane(valueArea), BorderLayout.CENTER);
        } else {
            panel.add(labelComp, BorderLayout.WEST);
            panel.add(valueComp, BorderLayout.CENTER);
        }
        
        return panel;
    }
    
    public void addConnection(RequestNode node) {
        if (!connections.contains(node) && node != this) {
            connections.add(node);
        }
    }
    
    public void removeConnection(RequestNode node) {
        connections.remove(node);
    }
    
    public List<RequestNode> getConnections() {
        return new ArrayList<>(connections);
    }
    
    // Getters and Setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    
    public int getCenterX() { return x + NODE_WIDTH / 2; }
    public int getCenterY() { return y + NODE_HEIGHT / 2; }
    
    public IHttpRequestResponse getMessage() { return message; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getDisplayName() { return displayName; }
    
    // For serialization
    private String nodeId;
    
    public String getNodeId() {
        if (nodeId == null) {
            nodeId = "node_" + System.identityHashCode(this);
        }
        return nodeId;
    }
    
    public void setNodeId(String id) {
        this.nodeId = id;
    }
    
    public List<String> getConnectionIds(List<RequestNode> allNodes) {
        List<String> ids = new ArrayList<>();
        for (RequestNode conn : connections) {
            ids.add(conn.getNodeId());
        }
        return ids;
    }
}

