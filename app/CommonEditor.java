package app;
import machine.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public abstract class CommonEditor<T> extends JPanel implements Editor<T>, StatelyListener
{
    protected StatelyApp app;
    protected T beingEdited = null;
    private boolean unsavedChanges = false;
    private boolean ignoreChanges = false;

    private JLabel compileStatusLabel;
    private JLabel compileErrorLabel;
    private IssuesListing issues;
    
    public CommonEditor(StatelyApp app)
    {
        this.app = app;
        app.addStatelyListener(this);

        createInterface();
        load(null);
    }

    // Abstract

    protected abstract void fillEditorPanel(JPanel editorPanel);
    protected abstract Code getCode();
    protected abstract int getIssueDividerLocation();
    protected abstract Filter<Issue> getIssueFilter();
    protected abstract void indicateChanged();
    protected abstract void indicateUnchanged();
    protected abstract void loadStuff(T t);
    protected abstract void saveStuff(T t);

    // Protected
    
    protected void changed()
    {
        if(ignoreChanges)
        {
            return;
        }

        if(!unsavedChanges)
        {
            unsavedChanges = true;
            indicateChanged();
        }
    }

    // Editor interface
    
    public boolean hasUnsavedChanges()
    {
        return unsavedChanges;
    }

    public void load(T t)
    {
        beingEdited = t;
        ignoreChanges = true;
        loadStuff(t);
        reloadStatus();
        ignoreChanges = false;
        unchanged();
        revalidate();
    }

    public void save(T t)
    {
        if(t == null)
        {
            return;
        }

        saveStuff(t);
        
        unchanged();
    }

    // StatelyListener interface

    public void machineModified(MachineEvent e)
    {
        reloadStatus();
        revalidate();
    }
    
    public void machineSwapped(MachineEvent e) {}

    public void selectionModified() {}

    // Private
    
    private void createInterface()
    {
        setBackground(app.colors.editor_background);
        JPanel editorPanel = new JPanel();
        editorPanel.setBackground(app.colors.editor_background);
        fillEditorPanel(editorPanel);

        // The compile/issue section

        JLabel compileStatusLLL = Helper.makeLLL(app, "Compile status:");
        compileStatusLLL.setOpaque(true);
        compileStatusLLL.setBackground(app.colors.editor_background);
        
        compileStatusLabel = new JLabel();
        compileStatusLabel.setOpaque(false);
        compileStatusLabel.setFont(app.fonts.compile_info);
        compileStatusLabel.setHorizontalAlignment(SwingConstants.LEFT);

        compileErrorLabel = new JLabel();
        compileErrorLabel.setOpaque(false);
        compileErrorLabel.setFont(app.fonts.compile_info);
        compileErrorLabel.setHorizontalAlignment(SwingConstants.LEFT);
        
        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(app.colors.compile_info_background);
        statusPanel.setLayout(new GridLayout(3,1));
        
        statusPanel.add(compileStatusLLL);
        statusPanel.add(compileStatusLabel);
        statusPanel.add(compileErrorLabel);
        
        issues = new IssuesListing(app);
        JPanel issuesPanel = new JPanel();
        issuesPanel.setOpaque(false);
        issuesPanel.setLayout(new BorderLayout());
        issuesPanel.add(Helper.makeLLL(app, "Issues:"), BorderLayout.NORTH);
        issuesPanel.add(issues, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(app.colors.editor_background);
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(statusPanel, BorderLayout.NORTH);
        bottomPanel.add(issuesPanel, BorderLayout.CENTER);
        
        // Combination

        JSplitPane divide = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                           editorPanel,
                                           bottomPanel);
        divide.setDividerLocation(getIssueDividerLocation());

        setLayout(new BorderLayout());
        add(divide, BorderLayout.CENTER);
    }
    
    private void reloadStatus()
    {
        boolean valid = false;
        boolean compiled = false;
        String compileError = "";
        Code code = null;

        if(beingEdited == null)
        {
            issues.setFilter(new ConstantFilter<>(false));
        }
        else
        {
            issues.setFilter(getIssueFilter());
            code = getCode();
        }
        
        if(code != null)
        {
            valid = true;

            if(code.isCompiled())
            {
                compiled = true;
            }
            else
            {
                compileError = code.getError();
            }
        }

        Color compileColor =
            (!valid) ? app.colors.compile_info_none :
            compiled ? app.colors.compile_info_compiled :
            app.colors.compile_info_error;
        compileStatusLabel.setForeground(compileColor);
        compileErrorLabel.setForeground(compileColor);

        compileStatusLabel.setText(
            (!valid) ? "---" :
            compiled ? "*** compiled ***" :
            "*** not compiled ***");
        compileErrorLabel.setText(compileError);
        
        issues.rebuild();
    }

    private void unchanged()
    {
        unsavedChanges = false;
        indicateUnchanged();
    }
    
    
}
