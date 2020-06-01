package app;
import machine.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class SignalEditor extends JPanel implements StatelyListener, ActionListener
{
    private StatelyApp app;

    public SignalEditor(StatelyApp app)
    {
        this.app = app;
        app.addStatelyListener(this);

        setBackground(app.colors.editor_background);
    }

    // StatelyListener

    public void machineModified(MachineEvent e)
    {
        
    }

    public void machineSwapped(MachineEvent e)
    {
        
    }

    public void selectionModified() {}
    
    // ActionListener

    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();

        
    }
}
