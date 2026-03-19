# Logic Mapper - Burp Suite Extension

**Visual Business Logic Flow Mapper for Burp Suite**

Logic Mapper is a powerful Burp Suite extension that helps security testers visualize and map complex business logic flows during penetration testing. It allows you to drag-and-drop requests from Proxy or Repeater to create interactive flow diagrams with automatic connections, detailed notes, and export/import capabilities.

Built with the **Montoya API**, this extension is designed for performance, stability, and seamless integration with modern Burp Suite versions.

---

## 🚀 Key Features

### 🎯 Visual Flow Mapping
- **Interactive Canvas**: Create diagrams with a responsive drag-and-drop interface.
- **Node-Based Visualization**: Each HTTP request is represented as a node with its method and path.
- **Grid Background**: Built-in alignment for clean and organized logic maps.

### 🔗 Smart Connections
- **Auto-Connection**: New requests automatically connect to the previous one in sequence.
- **Manual Connections**: Use `Ctrl+Click` (or `Cmd+Click`) to create manual links between any two nodes.
- **Multi-Context Support**: Send requests to the mapper from Proxy history, Repeater, or Intercept contexts.

### 📝 Documentation & Notes
- **Detailed Notes**: Attach notes explaining business logic steps to each node.
- **Visual Indicators**: Nodes with notes are marked with a yellow "!" for easy identification.
- **Request Details**: Double-click any node to view the full HTTP request (headers and body).

### 🎨 Color-Coded Interface
- **HTTP Methods**: Automatically color-coded for quick visual auditing (GET=Green, POST=Blue, etc.).
- **Smooth Interaction**: Hover effects and intuitive context menus for deleting nodes or connections.

### 🔄 Multi-Monitor & Performance
- **Burp Frame Parentage**: Popups and dialogs are correctly parented to the Burp Suite window for proper multi-monitor support.
- **Thread Safety**: All intensive operations (File I/O, JSON parsing) are performed on background threads to keep the UI responsive.
- **Export/Import**: Save your maps to JSON and reload them later for audits or team collaboration.

---

## 🛠️ Build and Installation

### Prerequisites
- Java 17 or higher
- Burp Suite Professional or Community Edition

### Option 1: Build from Source (Recommended)
You can build the logic mapper using the included Gradle wrapper:

```bash
# Clone the repository
git clone https://github.com/yourusername/logic-mapper.git
cd "Logic Mapper"

# Build the project
./gradlew clean build

# The compiled JAR will be in:
# build/libs/LogicMapper-1.0.0.jar
```

### Option 2: Load in Burp Suite
1. Download or build the `LogicMapper-1.0.0.jar`.
2. Open Burp Suite.
3. Go to **Extensions** -> **Installed** -> **Add**.
4. Select **Extension type**: `Java`.
5. Select the file and click **Next**.
6. The "Logic Mapper" tab will appear in your top bar.

---

## 📖 Usage Instructions

- **Sending Requests**: Right-click any request in Burp (Proxy, Repeater, etc.) -> **Send to Logic Mapper**.
- **Connecting Nodes**: `Ctrl+Click` the source node, then click the target node.
- **Deleting Nodes/Lines**: `Shift+Click` a connection line or right-click a node and select **Delete**.
- **Viewing Details**: Double-click a node to see the full request and add/edit notes.

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Feel free to check the issues page or submit a pull request.

Created for automated logic analysis and security testing.
