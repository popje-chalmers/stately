package app;
import machine.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class SignalEditor extends CommonEditor<Signal> implements ActionListener, DocumentListener
{
    private static final String INPUT = "Input";
    private static final String STATEWISE = "Statewise";
    private static final String EXPRESSION = "Expression";
    private static final String[] kindChoices = new String[]{INPUT, STATEWISE, EXPRESSION};
    private JTextField nameField;
    private JButton renameButton;
    private JTextArea descriptionArea;
    private JComboBox<String> kindChooser;
    private JCheckBox internalCheckBox;
    private CodeArea codeArea;
    private JButton applyButton;

    public SignalEditor(StatelyApp app)
    {
        super(app);
        setPreferredSize(new Dimension(100, app.measures.signal_editor_height));
    }

    protected void fillEditorPanel(JPanel editorPanel)
    {
        nameField = Helper.textField(app);
        renameButton = Helper.button(app, "Rename...");
        renameButton.addActionListener(this);
        descriptionArea = Helper.textArea(app);
        kindChooser = new JComboBox<>(kindChoices);
        internalCheckBox = Helper.checkBox(app, "Internal? (no effect on inputs)");
        codeArea = new CodeArea(app);
        applyButton = Helper.button(app, "Apply changes");
        
        kindChooser.addActionListener(this);
        internalCheckBox.addActionListener(this);
        descriptionArea.getDocument().addDocumentListener(this);
        codeArea.getTextArea().getDocument().addDocumentListener(this);
        applyButton.addActionListener(this);

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
        editorPanel.add(Helper.makeLLL(app, "Kind:"), c);
        editorPanel.add(kindChooser, c);
        editorPanel.add(Helper.makeLLL(app, "Properties:"), c);
        editorPanel.add(internalCheckBox, c);
        editorPanel.add(Helper.makeLLL(app, "Expression, if applicable:"), c);
        c.weighty = 1;
        editorPanel.add(codeArea, c);
        c.weighty = 0;
        editorPanel.add(applyButton, c);
    }

    protected Code getCode()
    {
        if(beingEdited.getKind() == SignalKind.EXPRESSION)
        {
            return beingEdited.getCode();
        }

        return null;
    }

    protected int getIssueDividerLocation()
    {
        return app.measures.signal_issue_divider;
    }

    protected Filter<Issue> getIssueFilter()
    {
        return null; // TODO
    }

    protected void indicateChanged()
    {
        applyButton.setEnabled(true);
    }

    protected void indicateUnchanged()
    {
        applyButton.setEnabled(false);
    }

    protected void loadStuff(Signal signal)
    {
        boolean valid = false;
        String name = "";
        String description = "";
        SignalKind kind = SignalKind.INPUT;
        boolean internal = false;
        String source = "";
        //boolean hasExpression = false;
        
        if(signal != null)
        {
            valid = true;
            name = signal.getName();
            description = signal.getDescription();
            kind = signal.getKind();
            internal = signal.getInternal();
            //hasExpression = signal.getKind();
            source = signal.getCode().getSource();
        }
        
        nameField.setText(name);
        descriptionArea.setText(description);
        kindChooser.setSelectedItem(stringForKind(kind));
        internalCheckBox.setSelected(internal);
        codeArea.getTextArea().setText(source);

        nameField.setEnabled(false);
        renameButton.setEnabled(valid);
        descriptionArea.setEnabled(valid);
        kindChooser.setEnabled(valid);
        internalCheckBox.setEnabled(valid);
        codeArea.getTextArea().setEnabled(valid);
        applyButton.setEnabled(valid);
    }

    protected void applyStuff(Signal signal)
    {
        signal.setDescription(descriptionArea.getText());
        if(codeArea.getTextArea().isEnabled())
        {
            signal.getCode().setSource(codeArea.getTextArea().getText());
        }
        signal.setKind(kindForString((String)kindChooser.getSelectedItem()));
        signal.setInternal(internalCheckBox.isSelected());
    }
    
    // ActionListener

    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();

        if(source == applyButton)
        {
            if(beingEdited != null)
            {
                apply(beingEdited);
                app.reportMachineModification(this);
            }
        }
        else if(source == renameButton)
        {
            if(beingEdited != null)
            {
                apply(beingEdited);
                rename();
                app.reportMachineModification(this);
            }
        }
        else if(source == kindChooser
                || source == internalCheckBox)
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
            m.renameSignal(beingEdited, newName);
        }
    }

    // Static
    
    private static SignalKind kindForString(String s)
    {
        switch(s)
        {
        case INPUT:
            return SignalKind.INPUT;
        case STATEWISE:
            return SignalKind.STATEWISE;
        case EXPRESSION:
            return SignalKind.EXPRESSION;
        default:
            throw Misc.impossible();
        }
    }

    private static String stringForKind(SignalKind kind)
    {
        switch(kind)
        {
        case INPUT:
            return INPUT;
        case STATEWISE:
            return STATEWISE;
        case EXPRESSION:
            return EXPRESSION;
        default:
            throw Misc.impossible();
        }
    }
}
