package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class RequestNode {
    
    private final HttpRequestResponse message;
    private final MontoyaApi api;
    private int x, y;
    private String notes;
    private final List<RequestNode> connections;
    private String displayName;
    
    private static final int NODE_WIDTH = 200;
    private static final int NODE_HEIGHT = 80;
    private static final int CORNER_RADIUS = 10;
    
    public RequestNode(HttpRequestResponse message, MontoyaApi api) {
        this.message = message;
        this.api = api;
        this.connections = new ArrayList<>();
        this.notes = "";
        this.x = 50;
        this.y = 100;
        
        // Generate display name from request
        HttpRequest request = message.request();
        String method = request != null ? request.method() : "UNKNOWN";
        if (method == null) method = "UNKNOWN";
        
        String url = (request != null && request.url() != null) ? request.url() : "";
        
        // Extract short URL path
        try {
            String path = request != null ? request.path() : null;
            if (path != null) {
                if (path.length() > 30) {
                    path = path.substring(0, 27) + "...";
                }
                this.displayName = method + " " + path;
            } else {
                this.displayName = method + " " + (url.length() > 30 ? url.substring(0, 27) + "..." : url);
            }
        } catch (Exception e) {
            String urlSafe = (url != null) ? url : "unknown";
            this.displayName = method + " " + (urlSafe.length() > 30 ? urlSafe.substring(0, 27) + "..." : urlSafe);
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
        int indicatorX = x + NODE_WIDTH - 20;
        int indicatorY = y + 5;
        int indicatorSize = 12;
        return p.x >= indicatorX && p.x <= indicatorX + indicatorSize &&
               p.y >= indicatorY && p.y <= indicatorY + indicatorSize;
    }
    
    public Point getNotesIndicatorPosition() {
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
        HttpRequest request = message.request();
        String method = (request != null) ? request.method() : "UNKNOWN";
        if (method == null) method = "UNKNOWN";
        
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
        if (method == null) return new Color(158, 158, 158);
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
        JWindow popup = new JWindow(getBurpFrame());
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
        int textWidth = fm.stringWidth(notes);
        int lines = Math.max(1, (int) Math.ceil((double) textWidth / maxWidth));
        int height = Math.min(200, lines * fm.getHeight() + 20);
        
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
        
        popup.setAlwaysOnTop(true);
        popup.setVisible(true);
        
        javax.swing.Timer timer = new javax.swing.Timer(10000, e -> popup.dispose());
        timer.setRepeats(false);
        timer.start();
        
        java.awt.event.MouseAdapter closeListener = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                popup.dispose();
                timer.stop();
            }
        };
        popup.addMouseListener(closeListener);
        
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
        HttpRequest request = message.request();
        
        JDialog dialog = new JDialog(getBurpFrame(), "Edit Request Node - " + displayName, true);
        dialog.setLayout(new BorderLayout());
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // TAB 1: Request Information
        JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            "Request Details",
            0, 0,
            new Font("Arial", Font.BOLD, 13)
        ));
        
        detailsPanel.add(createInfoRow("HTTP Method:", request.method(), getMethodColor(request.method())));
        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(createInfoRow("URL:", request.url(), new Color(50, 50, 50)));
        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(createInfoRow("Host:", request.httpService().host(), new Color(50, 50, 50)));
        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(createInfoRow("Path:", request.path(), new Color(50, 50, 50)));
        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(createInfoRow("Port:", String.valueOf(request.httpService().port()), new Color(50, 50, 50)));
        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(createInfoRow("Headers:", request.headers().size() + " headers", new Color(50, 50, 50)));
        detailsPanel.add(Box.createVerticalStrut(10));
        
        String bodyInfo = request.body().length() > 0 ? request.body().length() + " bytes" : "No body";
        detailsPanel.add(createInfoRow("Body:", bodyInfo, new Color(50, 50, 50)));
        
        infoPanel.add(detailsPanel, BorderLayout.NORTH);
        
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
        for (burp.api.montoya.http.message.HttpHeader header : request.headers()) {
            headersText.append(header.name()).append(": ").append(header.value()).append("\n");
        }
        headersArea.setText(headersText.toString());
        JScrollPane headersScroll = new JScrollPane(headersArea);
        headersSection.add(headersScroll, BorderLayout.CENTER);
        
        infoPanel.add(headersSection, BorderLayout.CENTER);
        tabbedPane.addTab("Request Info", infoPanel);
        
        // TAB 2: Request Body
        JPanel bodyPanelTab = new JPanel(new BorderLayout());
        bodyPanelTab.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        if (request.body().length() > 0) {
            JTextArea bodyArea = new JTextArea();
            bodyArea.setEditable(false);
            bodyArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
            bodyArea.setBackground(new Color(250, 250, 250));
            bodyArea.setLineWrap(true);
            bodyArea.setWrapStyleWord(true);
            
            String bodyText = request.body().toString();
            bodyArea.setText(bodyText);
            
            JScrollPane bodyScroll = new JScrollPane(bodyArea);
            bodyPanelTab.add(new JLabel("Request Body (" + request.body().length() + " bytes):"), BorderLayout.NORTH);
            bodyPanelTab.add(bodyScroll, BorderLayout.CENTER);
        } else {
            JLabel noBodyLabel = new JLabel("No request body", JLabel.CENTER);
            bodyPanelTab.add(noBodyLabel, BorderLayout.CENTER);
        }
        tabbedPane.addTab("Request Body", bodyPanelTab);
        
        // TAB 3: Notes
        JPanel notesPanel = new JPanel(new BorderLayout(10, 10));
        notesPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JTextArea notesArea = new JTextArea(notes != null ? notes : "", 15, 50);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(BorderFactory.createTitledBorder("Your Notes"));
        
        notesPanel.add(new JLabel("Notes (for documentation and audit):"), BorderLayout.NORTH);
        notesPanel.add(notesScroll, BorderLayout.CENTER);
        tabbedPane.addTab("Notes", notesPanel);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save Notes");
        saveBtn.addActionListener(e -> {
            notes = notesArea.getText();
            dialog.dispose();
        });
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        
        dialog.add(tabbedPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setSize(700, 600);
        dialog.setLocationRelativeTo(getBurpFrame());
        dialog.setVisible(true);
    }
    
    private JPanel createInfoRow(String label, String value, Color valueColor) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.BOLD, 12));
        labelComp.setPreferredSize(new Dimension(120, 20));
        
        if (value.length() > 80) {
            JTextArea valueArea = new JTextArea(value);
            valueArea.setEditable(false);
            valueArea.setLineWrap(true);
            valueArea.setWrapStyleWord(true);
            panel.add(labelComp, BorderLayout.WEST);
            panel.add(new JScrollPane(valueArea), BorderLayout.CENTER);
        } else {
            JLabel valueComp = new JLabel(value);
            valueComp.setForeground(valueColor);
            panel.add(labelComp, BorderLayout.WEST);
            panel.add(valueComp, BorderLayout.CENTER);
        }
        return panel;
    }
    
    private Frame getBurpFrame() {
        for (Frame frame : Frame.getFrames()) {
            if (frame.isVisible() && frame.getTitle().contains("Burp Suite")) {
                return frame;
            }
        }
        return null;
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
    
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getCenterX() { return x + NODE_WIDTH / 2; }
    public int getCenterY() { return y + NODE_HEIGHT / 2; }
    public HttpRequestResponse getMessage() { return message; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getDisplayName() { return displayName; }
    
    private String nodeId;
    public String getNodeId() {
        if (nodeId == null) {
            nodeId = "node_" + System.identityHashCode(this);
        }
        return nodeId;
    }
    public void setNodeId(String id) { this.nodeId = id; }
    
    public List<String> getConnectionIds(List<RequestNode> allNodes) {
        List<String> ids = new ArrayList<>();
        for (RequestNode conn : connections) {
            ids.add(conn.getNodeId());
        }
        return ids;
    }
}


