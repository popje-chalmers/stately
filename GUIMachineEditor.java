import java.awt.*;
import javax.swing.*;

public class GUIMachineEditor extends JPanel
{
    private StatelyApp app;
    private GUIStateEditor stateEditor;
    
    public GUIMachineEditor(StatelyApp app)
    {
        this.app = app;
        setBackground(app.colors.machine_editor_background);
        setLayout(new BorderLayout());
        
        JTabbedPane tabs = new JTabbedPane();
        add(tabs, BorderLayout.CENTER);

        stateEditor = new GUIStateEditor(app);
        tabs.addTab("State editor", stateEditor);
        
        GUIIssuesTab issues = new GUIIssuesTab(app);
        tabs.addTab("Issues", issues);
        
 
    }

    public void goEditSignal(Signal s)
    {
        
    }

    public void goEditState(State s)
    {
        stateEditor.edit(s);
    }
}
