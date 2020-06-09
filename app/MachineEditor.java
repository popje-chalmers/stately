package app;
import machine.*;
import java.awt.*;
import javax.swing.*;

// Not an Editor :P
public class MachineEditor extends JPanel
{
    private StatelyApp app;

    private JTabbedPane tabs;
    private StateEditor stateEditor;
    private SignalManager signalManager;
    private IssuesTab issues;

    private SignalEditorController sig;
    private StateEditorController sec;
    
    public MachineEditor(StatelyApp app)
    {
        this.app = app;
        setBackground(app.colors.machine_editor_background);
        setPreferredSize(new Dimension(app.measures.machine_editor_width, 100));
        setLayout(new BorderLayout());
        
        tabs = new JTabbedPane();
        add(tabs, BorderLayout.CENTER);

        signalManager = new SignalManager(app);
        sig = new SignalEditorController(app, signalManager.getEditor());
        tabs.addTab("Signals", signalManager);

        stateEditor = new StateEditor(app);
        sec = new StateEditorController(app, stateEditor);
        tabs.addTab("State editor", stateEditor);
        
        issues = new IssuesTab(app);
        tabs.addTab("Issues", issues);
    }

    public void editSignal(Signal s, boolean agro)
    {
        sig.edit(s);

        if(agro)
        {
            tabs.setSelectedComponent(signalManager);
        }
    }

    public void editState(State st, boolean agro)
    {
        sec.edit(st);
        
        if(agro)
        {
            tabs.setSelectedComponent(stateEditor);
        }
    }

    public void save()
    {
        sig.saveIfReasonable();
        sec.saveIfReasonable();
    }
}
