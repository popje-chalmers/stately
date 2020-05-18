import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class GUIStateEditor extends JPanel implements StatelyListener, ActionListener
{
    private StatelyApp app;
    private State state;

    private JTextField nameField;
    private JButton renameButton;
    private JTextArea descriptionArea;
    private JCheckBox virtualCheckBox;
    private GUICodeArea codeArea;
    private JButton saveButton;

    private JLabel compileStatusLabel;
    private JLabel compileErrorLabel;
    private GUIIssues issues;
    
    
    public GUIStateEditor(StatelyApp app)
    {
        this.app = app;
        app.addStatelyListener(this);

        createInterface();
        loadState();
    }

    private void createInterface()
    {
        setBackground(app.colors.editor_background);

        // The state editor panel itself
        
        nameField = GUIHelper.textField(app);
        renameButton = GUIHelper.button(app, "Rename");
        renameButton.addActionListener(this);
        descriptionArea = GUIHelper.textArea(app);
        virtualCheckBox = GUIHelper.checkBox(app, "Virtual state");
        codeArea = new GUICodeArea(app);
        saveButton = GUIHelper.button(app, "Save changes");
        saveButton.addActionListener(this);
        
        JPanel namePanel = GUIHelper.transparentPanel();
        namePanel.setLayout(new BorderLayout());
        namePanel.add(nameField, BorderLayout.CENTER);
        namePanel.add(renameButton, BorderLayout.EAST);

        descriptionArea.setRows(8);
        GUIHelper.wrapWord(descriptionArea);
        JScrollPane descriptionScroll = GUIHelper.scroll(descriptionArea, false, true);
        
        JPanel editorPanel = new JPanel();
        editorPanel.setBackground(app.colors.editor_background);
        editorPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.BOTH;

        editorPanel.add(GUIHelper.makeLLL(app, "Name:"), c);
        editorPanel.add(namePanel, c);
        editorPanel.add(GUIHelper.makeLLL(app, "Description:"), c);
        editorPanel.add(descriptionScroll, c);
        editorPanel.add(GUIHelper.makeLLL(app, "Properties:"), c);
        editorPanel.add(virtualCheckBox, c);
        editorPanel.add(GUIHelper.makeLLL(app, "Code:"), c);
        c.weighty = 1;
        editorPanel.add(codeArea, c);
        c.weighty = 0;
        editorPanel.add(saveButton, c);

        // The compile/issue section

        JLabel compileStatusLLL = GUIHelper.makeLLL(app, "Compile status:");
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
        
        issues = new GUIIssues(app);
        JPanel issuesPanel = new JPanel();
        issuesPanel.setOpaque(false);
        issuesPanel.setLayout(new BorderLayout());
        issuesPanel.add(GUIHelper.makeLLL(app, "Issues:"), BorderLayout.NORTH);
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
        divide.setDividerLocation(app.measures.state_issue_divider);

        setLayout(new BorderLayout());
        add(divide, BorderLayout.CENTER);
    }

    public void edit(State st)
    {
        if(state == st)
        {
            return;
        }
        
        saveState();
        switchTo(st);
    }

    private void loadState()
    {
        boolean valid = false;
        String name = "";
        String description = "";
        boolean virtual = false;
        String source = "";
        boolean compiled = false;
        String compileError = "";
        
        if(state == null)
        {
            issues.setFilter(new StateIssueFilter(new ArrayList<>()));
        }
        else
        {
            valid = true;
            name = state.getName();
            description = state.getDescription();
            virtual = state.isVirtual();
            source = state.getCode().getSource();
            issues.setFilter(new StateIssueFilter(state));

            if(state.getCode().isCompiled())
            {
                compiled = true;
            }
            else
            {
                compileError = state.getCode().getError();
            }
        }

        nameField.setText(name);
        descriptionArea.setText(description);
        virtualCheckBox.setSelected(virtual);
        codeArea.getTextArea().setText(source);

        nameField.setEnabled(valid);
        renameButton.setEnabled(valid);
        descriptionArea.setEnabled(valid);
        virtualCheckBox.setEnabled(valid);
        codeArea.getTextArea().setEnabled(valid);
        saveButton.setEnabled(valid);

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
        revalidate();
    }

    private void saveState()
    {
        if(state == null)
        {
            return;
        }

        state.setDescription(descriptionArea.getText());
        state.setVirtual(virtualCheckBox.isSelected());
        state.getCode().setSource(codeArea.getTextArea().getText());
        
        app.machineModified(this);
    }

    private void switchTo(State st)
    {
        state = st;
        loadState();
    }

    private void renameState()
    {
        Machine m = app.getMachine();
        if(m == null || state == null)
        {
            return;
        }

        m.renameState(state, nameField.getText());
        app.machineModified(this);
    }

    // StatelyListener

    public void machineModified(MachineEvent e)
    {
        Machine m = app.getMachine();
        if(m == null || !m.getStates().contains(state))
        {
            switchTo(null);
        }
        else
        {
            loadState();
        }
    }

    public void machineSwapped(MachineEvent e)
    {
        switchTo(null);
    }

    public void selectionModified() {}
    
    // ActionListener

    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();

        if(source == saveButton)
        {
            saveState();
        }
        else if(source == renameButton)
        {
            renameState();
        }
    }
}
