package app;
import machine.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class SignalWidget extends JPanel implements ActionListener
{
    private StatelyApp app;
    private Inputter inp;
    private Signal signal;
    private JCheckBox box; // for choosing the value
    private JPanel surround;
    
    public SignalWidget(StatelyApp app, Inputter inp, Signal s, Value chosenValue)
    {
        this.app = app;
        this.inp = inp;
        signal = s;

        boolean isInput = s.getKind() == SignalKind.INPUT;
        box = Helper.checkBox(app, signal.getName());
        box.setSelected(isInput && chosenValue.getBoolean());
        box.setEnabled(isInput);
        box.addActionListener(this);
        box.setFocusable(false);
        int b = app.measures.sim_signal_border;
        box.setBorder(new EmptyBorder(b,b,b,b));

        surround = new JPanel();
        surround.setLayout(new FlowLayout());
        surround.add(box);
        
        setOpaque(false);
        setLayout(new FlowLayout());
        add(surround, BorderLayout.CENTER);

        setSimulatedValue(null);
    }

    public Signal getSignal() { return signal; }

    public void setSimulatedValue(Value v)
    {
        //System.out.println(signal.getName() + " := " + (v == null ? "null" : (v.getBoolean() ? "1" : "0")));
        Color bg =
            v == null ? app.colors.sim_signal_unknown :
            !v.getBoolean() ? app.colors.sim_signal_off :
            signal.getKind() == SignalKind.INPUT ? app.colors.sim_signal_on_input :
            app.colors.sim_signal_on_other;
        surround.setBackground(bg);

        boolean on = v != null && v.getBoolean();
        if(signal.getKind() != SignalKind.INPUT)
        {
            box.setSelected(on);
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == box && signal.getKind() == SignalKind.INPUT)
        {
            inp.setInputValue(signal, new Value(box.isSelected()));
        }
    }
}
