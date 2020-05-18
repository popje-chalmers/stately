import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GUIIssuesTab extends JPanel implements StatelyListener, ItemListener
{
    private StatelyApp app;
    private GUIIssues issues;
    private JCheckBox filterToSelected;
    private boolean filtering = false;
    
    public GUIIssuesTab(StatelyApp app)
    {
        this.app = app;
        app.addStatelyListener(this);
        
        setBackground(app.colors.editor_background);

        // Create things
        issues = new GUIIssues(app);
        filterToSelected = new JCheckBox("Selected states only", false);
        filterToSelected.addItemListener(this);
        filterToSelected.setOpaque(false);
        filterToSelected.setForeground(app.colors.editor_foreground);

        // Add and layout
        setLayout(new BorderLayout());
        JPanel optionsPanel = new JPanel();
        optionsPanel.setOpaque(false);
        optionsPanel.setLayout(new GridLayout(1,2));
        optionsPanel.add(GUIHelper.makeTitle(app, "Issues", app.colors.title, null));
        optionsPanel.add(filterToSelected);
        add(optionsPanel, BorderLayout.NORTH);
        add(issues, BorderLayout.CENTER);
    }

    private void makeFilter()
    {
        if(filtering)
        {
            issues.setFilter(new StateIssueFilter(app.getSelectedStates()));
        }
        else
        {
            issues.setFilter(null);
        }

        issues.rebuild();
    }

    // ItemListener
    
    public void itemStateChanged(ItemEvent e)
    {
        Object source = e.getSource();

        if(source == filterToSelected)
        {
            if(e.getStateChange() == ItemEvent.SELECTED)
            {
                filtering = true;
                makeFilter();
            }
            else
            {
                filtering = false;
                makeFilter();
            }
        }
    }

    // StatelyListener
    
    public void machineModified(MachineEvent e) { makeFilter(); }
    public void machineSwapped(MachineEvent e) { makeFilter(); }
    public void selectionModified() { makeFilter(); }

    
}
