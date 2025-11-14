# Logic Mapper - Burp Suite Extension

**Visual Business Logic Flow Mapper for Burp Suite**

A powerful Burp Suite extension that helps security testers visualize and map complex business logic flows during penetration testing. Drag-and-drop requests from Proxy or Repeater to create interactive flow diagrams with automatic connections, notes, and export capabilities.

---

## 1. Extension Overview

### What is Logic Mapper?

Logic Mapper is a visual diagramming tool integrated into Burp Suite that allows security professionals to map and visualize complex business logic flows during security testing. When testing multi-step processes like checkout flows, password reset sequences, or authentication workflows, it becomes challenging to track all the requests involved and understand their relationships.

### How It Helps

- **Visual Understanding**: Transform abstract request sequences into clear, visual flow diagrams
- **Documentation**: Create visual documentation of application workflows for reports and audits
- **Analysis**: Identify patterns, dependencies, and potential security issues in business logic
- **Collaboration**: Share logic maps with team members via JSON export/import
- **Efficiency**: Quickly map complex flows without external diagramming tools

### Use Cases

- **Multi-step Checkout Processes**: Map the entire checkout flow from cart to payment
- **Authentication Flows**: Visualize login, registration, and password reset sequences
- **API Workflows**: Document complex API interactions and dependencies
- **Business Logic Testing**: Track state changes and request dependencies
- **Security Audits**: Create visual documentation for penetration test reports

---

## 2. Key Features

### 🎯 Visual Flow Mapping
- Create interactive diagrams with drag-and-drop functionality
- Visual canvas with grid background for easy alignment
- Nodes represent individual HTTP requests with method and URL information

### 🔗 Automatic & Manual Connections
- **Auto-Connection**: New requests automatically connect to the previous request in sequence
- **Quick Connect**: Ctrl+Click (or Cmd+Click on Mac) to quickly connect nodes
- **Context Menu**: Right-click to connect nodes via menu
- Visual arrows show the flow direction between requests

### 📝 Notes & Documentation
- Add detailed notes to each request node
- Yellow "!" indicator shows which nodes have notes
- Click the indicator to view notes in a popup
- Double-click node to edit notes in a detailed dialog

### 🎨 Color-Coded Visualization
- **GET**: Green badge
- **POST**: Blue badge
- **PUT**: Orange badge
- **DELETE**: Red badge
- **PATCH**: Purple badge
- Easy visual identification of HTTP methods

### 🔄 Export & Import
- Export logic maps to JSON format
- Import previously saved maps
- Choose to replace or append to existing nodes
- Preserves all node positions, connections, and notes

### ⚡ Interactive Controls
- **Drag Nodes**: Click and drag to reposition nodes
- **Delete Connections**: Shift+Click on connection line to delete
- **Context Menus**: Right-click for quick actions
- **Connection Management**: Add or remove connections easily

### 📊 Request Details
- View full request information in detailed dialog
- See headers, body, method, URL, and all request details
- Tabbed interface for organized information display

### 🎛️ Canvas Features
- Large scrollable canvas (2000x1500)
- Grid background for alignment
- Zoom and pan capabilities
- Status bar showing node count

---

## 3. Usage Instructions

### Installation

#### Step 1: Build the Extension
```bash
# Navigate to project directory
cd "Logic Mapper"

# Build using Ant
ant dist

# Output: dist/LogicMapper-1.0.jar
```

#### Step 2: Load in Burp Suite
1. Open Burp Suite
2. Go to **Extender** → **Extensions** → **Add**
3. Select **Extension type**: Java
4. Click **Select file** and choose `dist/LogicMapper-1.0.jar`
5. Click **Next**
6. Verify no errors appear in the output
7. The "Logic Mapper" tab will appear in Burp Suite

### Basic Usage

#### Adding Requests to Logic Mapper

**Method 1: Context Menu (Recommended)**
1. In **Proxy** or **Repeater** tab, select one or more requests
2. **Right-click** on the selected request(s)
3. Choose **"Send to Logic Mapper"**
4. The request will appear as a node in the Logic Mapper tab
5. New requests automatically connect to the previous request

**Method 2: Multiple Selection**
- You can select multiple requests at once
- All selected requests will be added sequentially
- Each will automatically connect to the previous one

#### Connecting Nodes

**Method 1: Quick Connect (Ctrl+Click)**
1. **Ctrl+Click** (or **Cmd+Click** on Mac) on the first node
   - Node will be highlighted
   - Visual line appears from node to mouse position
   - Small indicator circle appears on the first node
2. **Release Ctrl** and move mouse
   - Visual line follows the mouse
   - No need to hold Ctrl
3. **Click** on the second node
   - Connection is created automatically
   - Connection mode ends
4. **(Optional)** Click on empty space to cancel connection mode

**Method 2: Context Menu**
1. **Right-click** on a node
2. Select **"Connect to..."**
3. Choose target node from submenu
4. Connection is created

#### Removing Connections

**Method 1: Quick Delete**
1. **Shift+Click** on a connection line
2. Connection is immediately deleted

**Method 2: Context Menu**
1. **Right-click** on a connection line
2. Select **"Remove Connection"**
3. Or right-click on a node → **"Remove Connection to..."** → select target

#### Managing Nodes

**Moving Nodes**
1. **Click and drag** a node to move it
2. Release mouse to drop at new position

**Editing Notes**
1. **Double-click** a node
2. Go to **"Notes"** tab
3. Enter or edit your notes
4. Click **"Save Notes"**
5. Yellow "!" indicator appears when notes exist

**Viewing Notes**
1. **Click** the yellow "!" indicator on a node
2. Notes popup appears next to the indicator
3. Popup auto-closes after 10 seconds or when clicked

**Viewing Request Details**
1. **Double-click** a node
2. View information in three tabs:
   - **Request Info**: Method, URL, headers, body info
   - **Request Body**: Full request body preview
   - **Notes**: Add/edit notes

**Deleting Nodes**
1. **Right-click** on a node
2. Select **"Delete Node"**
3. Node and all its connections are removed

#### Export & Import

**Exporting Logic Map**
1. Click **"Export"** button in toolbar
2. Choose save location (default: `logic_mapper_export.json`)
3. All nodes, positions, connections, and notes are saved
4. Success message shows number of exported nodes

**Importing Logic Map**
1. Click **"Import"** button in toolbar
2. Select JSON file to import
3. Choose import option:
   - **Yes**: Replace all existing nodes
   - **No**: Append to existing nodes
   - **Cancel**: Abort import
4. Nodes are restored with positions and connections
5. Success message shows number of imported nodes

### Advanced Usage

#### Creating Complex Flow Diagrams

1. **Start with Base Flow**
   - Add initial requests in sequence
   - They will auto-connect

2. **Add Branching Paths**
   - Use Ctrl+Click to create alternative paths
   - Connect nodes to show different flows

3. **Document with Notes**
   - Add notes to explain each step
   - Document conditions and state changes

4. **Organize Layout**
   - Drag nodes to create logical groupings
   - Use grid for alignment

5. **Export for Documentation**
   - Export completed maps
   - Include in penetration test reports

#### Tips & Best Practices

- **Naming**: Use descriptive notes to identify each step
- **Organization**: Group related requests together visually
- **Documentation**: Add notes explaining business logic at each step
- **Version Control**: Export maps regularly to track changes
- **Collaboration**: Share JSON files with team members
- **Backup**: Keep exported JSON files as backups

### Keyboard Shortcuts

- **Ctrl+Click**: Start connection mode
- **Shift+Click**: Delete connection
- **Double-Click**: Edit node / View details
- **Right-Click**: Context menu

### Visual Indicators

- **Node Colors**: HTTP method (GET=Green, POST=Blue, etc.)
- **Yellow "!"**: Node has notes
- **Blue Arrows**: Connections between nodes
- **Red Line (Hover)**: Connection selected for deletion
- **Highlighted Node**: Selected or in connection mode

---

## Configuration

Edit `config.yaml` to customize:
- Extension name
- Version number
- Description

All version references throughout the application read from this file.

---

## Troubleshooting

### Extension Not Loading
- Ensure Java 8+ is installed
- Check that `burp.jar` is in `lib/` directory
- Verify JAR file was built successfully
- Check Extender → Extensions for error messages

### Nodes Not Appearing
- Verify requests were sent successfully
- Check Extender → Output for error messages
- Ensure canvas is visible (scroll if needed)

### Import Not Working
- Verify JSON file is valid
- Check that file was exported from Logic Mapper
- Ensure JSON structure matches expected format
- Check Extender → Output for specific error messages

---

## License

Created for security testing and audit purposes.

---

## Contributing

Contributions, issues, and feature requests are welcome!

---

## Support

For issues or questions, please check the Help dialog in the Logic Mapper tab or review the Extender → Output for error messages.
