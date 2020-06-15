package machine;

import java.util.*;

public class Simulator
{
    private Machine machine;
    private Model model;
    private InputSource source;

    private State current;
    private State next;
    private ModelTransition transition;
    private Environment environment;

    private List<SimulationListener> listeners = new ArrayList<>();

    public Simulator(Machine m)
    {
        machine = m;
    }
    
    public void addSimulationListener(SimulationListener l)
    {
        listeners.add(l);
    }

    public Environment getEnvironment()
    {
        return environment;
    }
    
    public State getNextState()
    {
        return next;
    }
    
    public State getState()
    {
        return current;
    }
    
    public ModelTransition getTransition()
    {
        return transition;
    }

    private void notifyListeners()
    {
        for(SimulationListener l: listeners)
        {
            l.simulationUpdated();
        }
    }
    
    public void recompute()
    {
        if(machine == null || !machine.getStates().contains(current))
        {
            current = null;
        }
        
        if(machine == null || model == null || current == null || source == null || !model.hasState(current))
        {
            environment = null;
            transition = null;
            next = null;
            notifyListeners();
            return;
        }
        
        environment = new Environment(current);
        
        for(Signal s: model.getInputs())
        {
            environment.setValue(s, source.getInputValue(s));
        }

        model.fillEnvironment(environment);

        boolean reset = model.getResetCondition().evaluate(environment).getBoolean();

        if(reset)
        {
            transition = null;
            next = model.getInitialState();
        }
        else
        {
            List<ModelTransition> matches = new ArrayList<>();
            for(ModelTransition t: model.getTransitionsFromState(current))
            {
                if(t.getCondition().evaluate(environment).getBoolean())
                {
                    matches.add(t);
                }
            }

            if(matches.isEmpty())
            {
                transition = null;
                next = current;
            }
            else if(matches.size() == 1)
            {
                transition = matches.get(0);
                next = transition.getPath().get(transition.getPath().size() - 1);
            }
            else
            {
                String details = "";
                for(ModelTransition t: matches)
                {
                    details += t + "\n";
                }
                throw new Error("Very bad internal error: multiple transitions matched:\n" + details);
            }
        }

        notifyListeners();
    }

    public void removeSimulationListener(SimulationListener l)
    {
        listeners.remove(l);
    }
    
    public void reset()
    {
        if(model != null)
        {
            setState(model.getInitialState());
        }
        else
        {
            setState(null);
        }
    }

    public void setInputSource(InputSource s)
    {
        source = s;
        recompute();
    }

    public void setMachine(Machine m)
    {
        machine = m;
        recompute();
    }
    
    public void setModel(Model m)
    {
        model = m;
        recompute();
    }
    
    public void setState(State st)
    {
        current = st;
        recompute();
    }
    
}

