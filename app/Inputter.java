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
    private StatelyApp app;
    private JPanel panel;
    private JScrollPane scroller;
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

        panel = new ScrollablePanel(true, false);
        //panel = new JPanel();
        panel.setBackground(app.colors.sim_signals_background);
        panel.setLayout(new LRLayout());
        
        setLayout(new BorderLayout());
        scroller = Helper.scroll(panel, false, true);
        scroller.getViewport().setBackground(app.colors.sim_signals_background);
        add(scroller, BorderLayout.CENTER);

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
                    //This can happen when a signal is removed.
                    w.setSimulatedValue(null);
                    //throw new Error("Internal error: environment missing signal for widget " + s.getName());
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
