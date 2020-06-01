package app;
import machine.*;
import java.awt.*;
import javax.swing.*;

public class MachineEditor extends JPanel
{
    private StatelyApp app;
    private SignalEditor signalEditor;
    private StateEditor stateEditor;
    
    public MachineEditor(StatelyApp app)
    {
        this.app = app;
        setBackground(app.colors.machine_editor_background);
        setLayout(new BorderLayout());
        
        JTabbedPane tabs = new JTabbedPane();
        add(tabs, BorderLayout.CENTER);

        signalEditor = new SignalEditor(app);
        tabs.addTab("Signals", signalEditor);

        stateEditor = new StateEditor(app);
        tabs.addTab("State editor", stateEditor);
        
        IssuesTab issues = new IssuesTab(app);
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
