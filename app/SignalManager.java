package app;
import machine.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class SignalManager extends JPanel implements ActionListener, ListSelectionListener, StatelyListener
{
    private StatelyApp app;

    private boolean ignoreSelect = false;
    private Set<Signal> selected = new HashSet<>();
    private ArrayList<Signal> listed = new ArrayList<>();
    private JList<Signal> signalList;
    
    private ListCellRenderer<Signal> signalRenderer;
    private JPanel signalListPanel;
    private SignalEditor editor;
    private JButton addButton;
    private JButton removeButton;
    
    public SignalManager(StatelyApp app)
    {
        this.app = app;
        app.addStatelyListener(this);

        editor = new SignalEditor(app);
        
        setBackground(app.colors.editor_background);
        
        signalList = new JList<>();
        signalList.addListSelectionListener(this);
        signalRenderer = new SignalCellRenderer(app);
        
        signalListPanel = new JPanel();
        signalListPanel.setLayout(new BorderLayout());
        signalListPanel.setBackground(app.colors.signal_list_background);
        signalListPanel.setOpaque(true);
        signalListPanel.add(signalList);

        addButton = new JButton("Create");
        addButton.addActionListener(this);
        removeButton = new JButton("Delete selected");
        removeButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridLayout(1,2));
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        
        JScrollPane signalListScroller = new JScrollPane(
            signalListPanel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JPanel managementPanel = new JPanel();
        managementPanel.setOpaque(false);
        managementPanel.setLayout(new BorderLayout());
        managementPanel.add(signalListScroller, BorderLayout.CENTER);
        managementPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        JSplitPane divide = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                           editor,
                                           managementPanel);
        divide.setResizeWeight(0.0f);

        setLayout(new BorderLayout());
        add(divide, BorderLayout.CENTER);

        rebuild();
    }

    public SignalEditor getEditor()
    {
        return editor;
    }
    
    private void rebuild()
    {
        listed.clear();
        Machine m = app.getMachine();
        if(m != null)
        {
            listed.addAll(m.getSignals());
        }

        ignoreSelect = true;
        signalList.setListData(listed.toArray(new Signal[listed.size()]));
        signalList.setOpaque(false);
        signalList.setCellRenderer(signalRenderer);

        pushSelection();
        ignoreSelect = false;
    }

    private void pullSelection()
    {
        selected.clear();
        int[] indices = signalList.getSelectedIndices();
        for(int i: indices)
        {
            selected.add(listed.get(i));
        }
    }

    private void pushSelection()
    {
        // Fix selection and signal being edited
        ArrayList<Integer> selectedIndices = new ArrayList<>();
        ArrayList<Signal> newSelected = new ArrayList<>();
        for(Signal s: selected)
        {
            int index = listed.indexOf(s);
            if(index >= 0)
            {
                selectedIndices.add(index);
                newSelected.add(s);
            }
        }
        selected.clear();
        selected.addAll(newSelected);
        int[] indices = new int[selectedIndices.size()];
        for(int i = 0; i < selectedIndices.size(); i++)
        {
            indices[i] = selectedIndices.get(i);
        }
        signalList.setSelectedIndices(indices);
    }

    private void removeSignals()
    {
        Machine m = app.getMachine();
        int n = selected.size();
        if(n == 0 || m == null)
        {
            return;
        }
        
        int resp = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + n + " signals?", "Delete signals", JOptionPane.OK_CANCEL_OPTION);
        if(resp == JOptionPane.YES_OPTION)
        {
            ArrayList<Signal> tmp = new ArrayList<>(selected);
            selected.clear();
            pushSelection();
            for(Signal s: tmp)
            {
                m.removeSignal(s);
            }
            app.reportMachineModification(this);
        }
    }

    // ActionListener

    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();

        if(source == addButton)
        {
            app.makeSignals();
        }
        else if(source == removeButton)
        {
            removeSignals();
        }
    }
    
    // ListSelectionListener

    public void valueChanged(ListSelectionEvent e)
    {
        if(ignoreSelect)
        {
            return;
        }
        
        pullSelection();

        if(selected.isEmpty())
        {
            app.editSignal(null, false);
        }
        else if(selected.size() == 1)
        {
            app.editSignal(selected.iterator().next(), false);
        }
    }

    // StatelyListener

    public void machineModified(MachineEvent e)
    {
        rebuild();
    }

    public void machineSwapped(MachineEvent e)
    {
        rebuild();
    }

    public void selectionModified() {}
}
