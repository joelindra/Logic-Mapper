package burp;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.util.List;

public class BurpExtender implements IBurpExtender, IContextMenuFactory {
    
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private PrintWriter stdout;
    private PrintWriter stderr;
    private LogicMapperTab logicMapperTab;
    
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        
        // Set extension name from config
        ConfigReader config = ConfigReader.getInstance();
        callbacks.setExtensionName(config.getName());
        
        // Get output streams
        stdout = new PrintWriter(callbacks.getStdout(), true);
        stderr = new PrintWriter(callbacks.getStderr(), true);
        
        stdout.println(config.getName() + " v" + config.getVersion() + " extension loaded successfully!");
        
        // Create Logic Mapper tab
        logicMapperTab = new LogicMapperTab(callbacks, helpers);
        
        // Add custom tab
        callbacks.addSuiteTab(logicMapperTab);
        
        // Register context menu factory
        callbacks.registerContextMenuFactory(this);
        
        stdout.println("Logic Mapper initialized and ready to use!");
    }
    
    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
        JMenuItem menuItem = new JMenuItem("Send to Logic Mapper");
        menuItem.addActionListener(e -> {
            IHttpRequestResponse[] messages = invocation.getSelectedMessages();
            if (messages != null && messages.length > 0) {
                for (IHttpRequestResponse message : messages) {
                    logicMapperTab.addRequest(message);
                }
            }
        });
        
        return java.util.Arrays.asList(menuItem);
    }
}

