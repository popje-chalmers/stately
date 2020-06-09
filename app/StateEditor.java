package app;
import machine.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class StateEditor extends CommonEditor<State> implements ActionListener, DocumentListener
{
    private JTextField nameField;
    private JButton renameButton;
    private JTextArea descriptionArea;
    private JCheckBox virtualCheckBox;
    private CodeArea codeArea;
    private JButton saveButton;

    private JLabel compileStatusLabel;
    private JLabel compileErrorLabel;
    private IssuesListing issues;
    
    public StateEditor(StatelyApp app)
    {
        super(app);
    }

    // Filling in abstract methods
    
    protected void fillEditorPanel(JPanel editorPanel)
    {
        nameField = Helper.textField(app);
        renameButton = Helper.button(app, "Rename...");
        renameButton.addActionListener(this);
        descriptionArea = Helper.textArea(app);
        virtualCheckBox = Helper.checkBox(app, "Virtual state");
        codeArea = new CodeArea(app);
        saveButton = Helper.button(app, "Save changes");
        
        virtualCheckBox.addActionListener(this);
        descriptionArea.getDocument().addDocumentListener(this);
        codeArea.getTextArea().getDocument().addDocumentListener(this);
        saveButton.addActionListener(this);
        
        JPanel namePanel = Helper.transparentPanel();
        namePanel.setLayout(new BorderLayout());
        namePanel.add(nameField, BorderLayout.CENTER);
        namePanel.add(renameButton, BorderLayout.EAST);

        descriptionArea.setRows(4);
        Helper.wrapWord(descriptionArea);
        JScrollPane descriptionScroll = Helper.scroll(descriptionArea, false, true);
        
        editorPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.BOTH;

        editorPanel.add(Helper.makeLLL(app, "Name:"), c);
        editorPanel.add(namePanel, c);
        editorPanel.add(Helper.makeLLL(app, "Description:"), c);
        editorPanel.add(descriptionScroll, c);
        editorPanel.add(Helper.makeLLL(app, "Properties:"), c);
        editorPanel.add(virtualCheckBox, c);
        editorPanel.add(Helper.makeLLL(app, "Code:"), c);
        c.weighty = 1;
        editorPanel.add(codeArea, c);
        c.weighty = 0;
        editorPanel.add(saveButton, c);
    }

    protected Code getCode()
    {
        return beingEdited.getCode();
    }

    protected int getIssueDividerLocation()
    {
        return app.measures.state_issue_divider;
    }

    protected Filter<Issue> getIssueFilter()
    {
        return new StateIssueFilter(beingEdited);
    }

    protected void indicateChanged()
    {
        saveButton.setEnabled(true);
    }

    protected void indicateUnchanged()
    {
        saveButton.setEnabled(false);
    }

    protected void loadStuff(State state)
    {        
        boolean valid = false;
        String name = "";
        String description = "";
        boolean virtual = false;
        String source = "";
        
        if(state != null)
        {
            valid = true;
            name = state.getName();
            description = state.getDescription();
            virtual = state.isVirtual();
            source = state.getCode().getSource();
        }
        
        nameField.setText(name);
        descriptionArea.setText(description);
        virtualCheckBox.setSelected(virtual);
        codeArea.getTextArea().setText(source);

        nameField.setEnabled(false);
        renameButton.setEnabled(valid);
        descriptionArea.setEnabled(valid);
        virtualCheckBox.setEnabled(valid);
        codeArea.getTextArea().setEnabled(valid);
        saveButton.setEnabled(valid);
    }

    protected void saveStuff(State state)
    {
        state.setDescription(descriptionArea.getText());
        state.setVirtual(virtualCheckBox.isSelected());
        state.getCode().setSource(codeArea.getTextArea().getText());
    }

    // ActionListener

    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();

        if(source == saveButton)
        {
            if(beingEdited != null)
            {
                save(beingEdited);
                app.reportMachineModification(this);
            }
        }
        else if(source == renameButton)
        {
            if(beingEdited != null)
            {
                save(beingEdited);
                rename();
                app.reportMachineModification(this);
            }
        }
        else if(source == virtualCheckBox)
        {
            changed();
        }
    }

    // DocumentListener

    public void changedUpdate(DocumentEvent e)
    {
        changed();
    }

    public void insertUpdate(DocumentEvent e)
    {
        changed();
    }

    public void removeUpdate(DocumentEvent e)
    {
        changed();
    }

    // Private helpers
    
    private void rename()
    {   
        Machine m = app.getMachine();
        if(m == null || beingEdited == null)
        {
            return;
        }

        String newName = JOptionPane.showInputDialog(this, "Enter new name", beingEdited.getName());
        if(newName != null)
        {
            m.renameState(beingEdited, newName);
        }
    }
}
