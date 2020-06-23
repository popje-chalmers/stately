package app;
import machine.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

// Also shows signals.
public class Inputter extends JPanel implements InputSource, StatelyListener, SimulationListener
{
    public static final int COLUMNS = 10;
    private StatelyApp app;
    private JPanel panel;
    private Map<Signal, Value> inputValues = new HashMap<>();
    private ArrayList<SignalWidget> signalWidgets = new ArrayList<>();
    private Simulator sim;
    
    public Inputter(StatelyApp app)
    {
        this.app = app;
        app.addStatelyListener(this);
        sim = app.getSimulator();
        sim.addSimulationListener(this);
        
        setBackground(app.colors.sim_signals_background);
        setPreferredSize(new Dimension(100, app.measures.inputter_height));

        panel = new JPanel();
        panel.setBackground(app.colors.sim_signals_background);
        
        setLayout(new BorderLayout());
        add(new JScrollPane(panel), BorderLayout.CENTER);

        rebuild();
    }

    private void rebuild()
    {
        panel.removeAll();
        signalWidgets.clear();

        Machine m = app.getMachine();
        if(m == null)
        {
            inputValues.clear();
            return;
        }

        java.util.List<Signal> signals = m.getSignals();
        java.util.List<Signal> remove = new ArrayList<>();
        for(Signal s: inputValues.keySet())
        {
            if(!signals.contains(s) || s.getKind() != SignalKind.INPUT)
            {
                remove.add(s);
            }
        }
        for(Signal s: remove)
        {
            inputValues.remove(s);
        }
        
        int items = signals.size();
        int rows = (items + COLUMNS - 1) / COLUMNS;
        int slots = rows * COLUMNS;

        if(rows != 0)
        {
            panel.setLayout(new GridLayout(rows, COLUMNS));

            for(Signal s: signals)
            {
                if(s.getKind() == SignalKind.INPUT)
                {
                    SignalWidget w = new SignalWidget(app, this, s, getInputValue(s));
                    panel.add(w);
                    signalWidgets.add(w);
                }
            }

            for(Signal s: signals)
            {
                if(s.getKind() != SignalKind.INPUT)
                {
                    SignalWidget w = new SignalWidget(app, this, s, new Value(false));
                    panel.add(w);
                    signalWidgets.add(w);
                }
            }
            
            for(int i = items; i < slots; i++)
            {
                panel.add(new JLabel("-"));
            }
        }

        simulationUpdated();
    }

    public void setInputValue(Signal s, Value v)
    {
        inputValues.put(s, v);
        sim.recompute();
    }

    // InputSource

    public Value getInputValue(Signal input)
    {
        Value v = inputValues.get(input);
        if(v == null)
        {
            return new Value(false);
        }
        return v;
    }

    // SimulationListener

    public void simulationUpdated()
    {
        Environment env = null;

        if(sim != null)
        {
            env = sim.getEnvironment();
        }
        
        if(env == null)
        {
            for(SignalWidget w: signalWidgets)
            {
                w.setSimulatedValue(null);
            }
        }
        else
        {
            for(SignalWidget w: signalWidgets)
            {
                Signal s = w.getSignal();
                if(env.hasValueForSignal(s))
                {
                    w.setSimulatedValue(env.getValue(s));
                }
                else
                {
                    //w.setSimulatedValue(null);
                    throw new Error("Internal error: environment missing signal for widget " + s.getName());
                }
            }
        }

        repaint();
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

    public void selectionModified()
    {

    }
}
