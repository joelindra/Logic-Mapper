package burp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class LogicMapperCanvas extends JPanel {
    
    private final List<RequestNode> nodes;
    private RequestNode selectedNode;
    private RequestNode draggedNode;
    private Point dragOffset;
    private Point mousePos;
    private boolean isConnecting;
    private RequestNode connectionStart;
    private Connection hoveredConnection;
    
    private static final int NODE_WIDTH = 200;
    private static final int NODE_HEIGHT = 80;
    private static final int ARROW_SIZE = 10;
    private static final int CONNECTION_HIT_DISTANCE = 8; // Distance in pixels to detect click on connection
    
    // Inner class to represent a connection
    private static class Connection {
        RequestNode from;
        RequestNode to;
        
        Connection(RequestNode from, RequestNode to) {
            this.from = from;
            this.to = to;
        }
    }
    
    public LogicMapperCanvas(List<RequestNode> nodes) {
        this.nodes = nodes;
        this.mousePos = new Point();
        
        setBackground(new Color(245, 245, 250));
        setPreferredSize(new Dimension(2000, 1500));
        
        // Mouse listeners
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClicked(e);
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePos = e.getPoint();
                // Check if hovering over a connection
                Connection oldHovered = hoveredConnection;
                hoveredConnection = getConnectionAt(e.getPoint());
                if (oldHovered != hoveredConnection) {
                    repaint();
                }
            }
        });
    }
    
    private void handleMousePressed(MouseEvent e) {
        // Check if clicking on a connection first
        Connection clickedConnection = getConnectionAt(e.getPoint());
        
        if (e.isPopupTrigger() || (e.getButton() == MouseEvent.BUTTON3)) {
            if (clickedConnection != null) {
                // Show context menu for connection
                showConnectionContextMenu(e, clickedConnection);
            } else {
                RequestNode clickedNode = getNodeAt(e.getPoint());
                showContextMenu(e, clickedNode);
            }
            return;
        }
        
        // Handle connection deletion with Shift+Click
        if (clickedConnection != null && (e.isShiftDown() || e.getButton() == MouseEvent.BUTTON2)) {
            clickedConnection.from.removeConnection(clickedConnection.to);
            repaint();
            return;
        }
        
        RequestNode clickedNode = getNodeAt(e.getPoint());
        
        if (clickedNode != null) {
            // Check for Ctrl+Click or Cmd+Click (Mac) to start connection mode
            if (e.isControlDown() || e.isMetaDown()) {
                // Start connection mode - set the starting node
                isConnecting = true;
                connectionStart = clickedNode;
                selectedNode = clickedNode;
                draggedNode = null; // Prevent dragging when connecting
                repaint();
                return;
            } else if (isConnecting && connectionStart != null) {
                // We're in connection mode and user clicked on a node (without Ctrl)
                // This means they want to connect connectionStart to clickedNode
                if (clickedNode != connectionStart) {
                    // Check if connection already exists
                    if (!connectionStart.getConnections().contains(clickedNode)) {
                        connectionStart.addConnection(clickedNode);
                    }
                }
                // Clear connection mode
                isConnecting = false;
                connectionStart = null;
                selectedNode = clickedNode;
                draggedNode = null;
                repaint();
                return;
            } else {
                // Normal mode - allow dragging
                draggedNode = clickedNode;
                dragOffset = new Point(
                    e.getX() - clickedNode.getX(),
                    e.getY() - clickedNode.getY()
                );
                selectedNode = clickedNode;
                // Clear connection mode if it was active
                isConnecting = false;
                connectionStart = null;
            }
        } else {
            // Clicked on empty space
            if (isConnecting && connectionStart != null) {
                // Cancel connection mode if clicking on empty space
                isConnecting = false;
                connectionStart = null;
            }
            selectedNode = null;
        }
        repaint();
    }
    
    private void handleMouseReleased(MouseEvent e) {
        // Connection is now handled in mousePressed, so we don't need to handle it here
        // But we still need to handle the visual feedback during drag (if user was dragging)
        
        draggedNode = null;
        repaint();
    }
    
    private void handleMouseClicked(MouseEvent e) {
        RequestNode clickedNode = getNodeAt(e.getPoint());
        
        if (clickedNode != null) {
            // Check if notes indicator was clicked
            if (clickedNode.isNotesIndicatorClicked(e.getPoint())) {
                clickedNode.showNotesPopup(this);
                return;
            }
            
            // Double-click to edit
            if (e.getClickCount() == 2) {
                clickedNode.showEditDialog();
                repaint();
            }
        }
    }
    
    private void handleMouseDragged(MouseEvent e) {
        // If in connection mode, don't allow dragging
        if (isConnecting && connectionStart != null) {
            // Just update the visual connection line
            repaint();
            return;
        }
        
        if (draggedNode != null) {
            draggedNode.setX(e.getX() - dragOffset.x);
            draggedNode.setY(e.getY() - dragOffset.y);
            repaint();
        }
    }
    
    private RequestNode getNodeAt(Point p) {
        for (int i = nodes.size() - 1; i >= 0; i--) {
            RequestNode node = nodes.get(i);
            if (node.contains(p)) {
                return node;
            }
        }
        return null;
    }
    
    private void showContextMenu(MouseEvent e, RequestNode node) {
        JPopupMenu menu = new JPopupMenu();
        
        if (node != null) {
            JMenuItem editItem = new JMenuItem("Edit Notes");
            editItem.addActionListener(ev -> {
                node.showEditDialog();
                repaint();
            });
            menu.add(editItem);
            
            menu.addSeparator();
            
            // Connect to submenu
            JMenu connectMenu = new JMenu("Connect to...");
            boolean hasNodesToConnect = false;
            for (RequestNode otherNode : nodes) {
                if (otherNode != node && !node.getConnections().contains(otherNode)) {
                    hasNodesToConnect = true;
                    JMenuItem connectItem = new JMenuItem(otherNode.getDisplayName());
                    connectItem.addActionListener(ev -> {
                        node.addConnection(otherNode);
                        repaint();
                    });
                    connectMenu.add(connectItem);
                }
            }
            if (!hasNodesToConnect) {
                connectMenu.setEnabled(false);
            }
            menu.add(connectMenu);
            
            // Remove connection submenu
            JMenu removeMenu = new JMenu("Remove Connection to...");
            List<RequestNode> connections = node.getConnections();
            if (connections.isEmpty()) {
                removeMenu.setEnabled(false);
            } else {
                for (RequestNode connectedNode : connections) {
                    JMenuItem removeItem = new JMenuItem(connectedNode.getDisplayName());
                    removeItem.addActionListener(ev -> {
                        node.removeConnection(connectedNode);
                        repaint();
                    });
                    removeMenu.add(removeItem);
                }
            }
            menu.add(removeMenu);
            
            menu.addSeparator();
            
            // Quick connect hint
            JMenuItem hintItem = new JMenuItem("Tip: Ctrl+Click to connect quickly");
            hintItem.setEnabled(false);
            menu.add(hintItem);
            
            menu.addSeparator();
            
            JMenuItem deleteItem = new JMenuItem("Delete Node");
            deleteItem.addActionListener(ev -> {
                nodes.remove(node);
                // Remove connections
                for (RequestNode n : nodes) {
                    n.removeConnection(node);
                }
                repaint();
            });
            menu.add(deleteItem);
        } else {
            JMenuItem addNoteItem = new JMenuItem("Add Note");
            addNoteItem.addActionListener(ev -> {
                // TODO: Add standalone note functionality
            });
            menu.add(addNoteItem);
        }
        
        menu.show(this, e.getX(), e.getY());
    }
    
    private void showConnectionContextMenu(MouseEvent e, Connection connection) {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem removeItem = new JMenuItem("Remove Connection");
        removeItem.addActionListener(ev -> {
            connection.from.removeConnection(connection.to);
            repaint();
        });
        menu.add(removeItem);
        
        menu.addSeparator();
        
        JMenuItem infoItem = new JMenuItem("From: " + connection.from.getDisplayName() + 
                                          " → To: " + connection.to.getDisplayName());
        infoItem.setEnabled(false);
        menu.add(infoItem);
        
        menu.show(this, e.getX(), e.getY());
    }
    
    private Connection getConnectionAt(Point p) {
        for (RequestNode node : nodes) {
            for (RequestNode connectedNode : node.getConnections()) {
                if (isPointNearConnection(p, node, connectedNode)) {
                    return new Connection(node, connectedNode);
                }
            }
        }
        return null;
    }
    
    private boolean isPointNearConnection(Point p, RequestNode from, RequestNode to) {
        int x1 = from.getCenterX();
        int y1 = from.getCenterY();
        int x2 = to.getCenterX();
        int y2 = to.getCenterY();
        
        // Calculate intersection points
        Point p1 = getIntersectionPoint(x1, y1, x2, y2, from);
        Point p2 = getIntersectionPoint(x2, y2, x1, y1, to);
        
        // Calculate distance from point to line segment
        double distance = pointToLineDistance(p.x, p.y, p1.x, p1.y, p2.x, p2.y);
        return distance <= CONNECTION_HIT_DISTANCE;
    }
    
    private double pointToLineDistance(double px, double py, double x1, double y1, double x2, double y2) {
        double A = px - x1;
        double B = py - y1;
        double C = x2 - x1;
        double D = y2 - y1;
        
        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        double param = lenSq != 0 ? dot / lenSq : -1;
        
        double xx, yy;
        
        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }
        
        double dx = px - xx;
        double dy = py - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw grid
        drawGrid(g2d);
        
        // Draw connections (arrows)
        for (RequestNode node : nodes) {
            for (RequestNode connectedNode : node.getConnections()) {
                boolean isHovered = (hoveredConnection != null && 
                                    hoveredConnection.from == node && 
                                    hoveredConnection.to == connectedNode);
                drawArrow(g2d, node, connectedNode, isHovered);
            }
        }
        
        // Draw connecting line if in progress (show from connectionStart to mouse or to selected node)
        if (isConnecting && connectionStart != null) {
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(new Color(100, 150, 255, 200));
            
            // Draw line from connectionStart to mouse position
            g2d.drawLine(
                connectionStart.getCenterX(),
                connectionStart.getCenterY(),
                mousePos.x,
                mousePos.y
            );
            
            // Also draw a small indicator circle at connectionStart
            g2d.setColor(new Color(100, 150, 255, 180));
            g2d.fillOval(connectionStart.getCenterX() - 5, connectionStart.getCenterY() - 5, 10, 10);
        }
        
        // Draw nodes
        for (RequestNode node : nodes) {
            node.paint(g2d, node == selectedNode);
        }
        
        g2d.dispose();
    }
    
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(230, 230, 235));
        g2d.setStroke(new BasicStroke(1));
        
        int gridSize = 20;
        int width = getWidth();
        int height = getHeight();
        
        for (int x = 0; x < width; x += gridSize) {
            g2d.drawLine(x, 0, x, height);
        }
        for (int y = 0; y < height; y += gridSize) {
            g2d.drawLine(0, y, width, y);
        }
    }
    
    private void drawArrow(Graphics2D g2d, RequestNode from, RequestNode to, boolean isHovered) {
        int x1 = from.getCenterX();
        int y1 = from.getCenterY();
        int x2 = to.getCenterX();
        int y2 = to.getCenterY();
        
        // Calculate intersection points with node boundaries
        Point p1 = getIntersectionPoint(x1, y1, x2, y2, from);
        Point p2 = getIntersectionPoint(x2, y2, x1, y1, to);
        
        // Draw arrow line with different style if hovered
        if (isHovered) {
            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(new Color(255, 100, 100)); // Red when hovered
        } else {
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(new Color(70, 130, 180));
        }
        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        
        // Draw arrowhead
        drawArrowhead(g2d, p1.x, p1.y, p2.x, p2.y);
    }
    
    private Point getIntersectionPoint(int x1, int y1, int x2, int y2, RequestNode node) {
        int cx = node.getX() + NODE_WIDTH / 2;
        int cy = node.getY() + NODE_HEIGHT / 2;
        int w = NODE_WIDTH / 2;
        int h = NODE_HEIGHT / 2;
        
        // Calculate line equation
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length == 0) return new Point(cx, cy);
        
        dx /= length;
        dy /= length;
        
        // Find intersection with rectangle
        double t = Double.MAX_VALUE;
        
        // Check left/right edges
        if (dx != 0) {
            double t1 = (node.getX() - x1) / dx;
            double t2 = (node.getX() + NODE_WIDTH - x1) / dx;
            if (t1 > 0 && t1 < t) t = t1;
            if (t2 > 0 && t2 < t) t = t2;
        }
        
        // Check top/bottom edges
        if (dy != 0) {
            double t1 = (node.getY() - y1) / dy;
            double t2 = (node.getY() + NODE_HEIGHT - y1) / dy;
            if (t1 > 0 && t1 < t) t = t1;
            if (t2 > 0 && t2 < t) t = t2;
        }
        
        return new Point(
            (int)(x1 + dx * t),
            (int)(y1 + dy * t)
        );
    }
    
    private void drawArrowhead(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        
        xPoints[0] = x2;
        yPoints[0] = y2;
        
        xPoints[1] = (int)(x2 - ARROW_SIZE * Math.cos(angle - Math.PI / 6));
        yPoints[1] = (int)(y2 - ARROW_SIZE * Math.sin(angle - Math.PI / 6));
        
        xPoints[2] = (int)(x2 - ARROW_SIZE * Math.cos(angle + Math.PI / 6));
        yPoints[2] = (int)(y2 - ARROW_SIZE * Math.sin(angle + Math.PI / 6));
        
        g2d.fillPolygon(xPoints, yPoints, 3);
    }
}

