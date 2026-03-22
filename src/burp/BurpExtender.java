package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.http.message.HttpRequestResponse;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BurpExtender implements BurpExtension {
    public static final String NAME = "Logic Mapper";
    public static final String VERSION = "1.0.0";
    
    private MontoyaApi api;
    private LogicMapperTab logicMapperTab;
    
    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        
        api.extension().setName(NAME);
        
        api.logging().logToOutput(NAME + " v" + VERSION + " extension loaded successfully!");
        
        // Create Logic Mapper tab
        logicMapperTab = new LogicMapperTab(api);
        
        // Add custom tab
        api.userInterface().registerSuiteTab(NAME, logicMapperTab.getUiComponent());
        
        // Register context menu provider
        api.userInterface().registerContextMenuItemsProvider(new ContextMenuItemsProvider() {
            @Override
            public List<Component> provideMenuItems(ContextMenuEvent event) {
                List<HttpRequestResponse> selectedMessages = new ArrayList<>();
                
                // Case 1: Multiple messages selected (e.g., in Proxy history or Target tab)
                List<HttpRequestResponse> multiSelection = event.selectedRequestResponses();
                if (multiSelection != null && !multiSelection.isEmpty()) {
                    selectedMessages.addAll(multiSelection);
                } 
                // Case 2: Individual message in an editor (e.g., Repeater or Proxy interception)
                else {
                    event.messageEditorRequestResponse().ifPresent(editorContext -> {
                        selectedMessages.add(editorContext.requestResponse());
                    });
                }
                
                if (selectedMessages.isEmpty()) {
                    return null;
                }
                
                List<Component> menuItems = new ArrayList<>();
                JMenuItem menuItem = new JMenuItem("Send to " + NAME);
                
                menuItem.addActionListener(e -> {
                    for (HttpRequestResponse message : selectedMessages) {
                        logicMapperTab.addRequest(message);
                    }
                });
                
                menuItems.add(menuItem);
                return menuItems;
            }
        });
        
        api.logging().logToOutput(NAME + " initialized and ready to use!");
    }
}


