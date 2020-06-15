package machine;
import java.util.*;

// Evaluation environment (see Expression.evaluate).
public class Environment
{
    private State state;
    private Map<Signal,Value> values = new HashMap<Signal,Value>();
    private List<State> path = null; // current, intermediate virtuals, next. null means "remain in same state".
    
    public Environment(State current)
    {
        state = current;
    }

    public List<State> getPath()
    {
        return new ArrayList<>(path);
    }
    
    public State getState()
    {
        return state;
    }
    
    public Value getValue(Signal s)
    {
        Value v = values.get(s);
        if(v == null)
        {
            throw new Error("Internal error: environment missing signal " + s.getName());
        }
        return v;
    }

    public boolean hasValueForSignal(Signal s)
    {
        return values.get(s) != null;
    }

    public void setPath(List<State> path)
    {
        if(path == null)
        {
            this.path = null;
        }
        else
        {
            this.path = new ArrayList<>(path);
        }
    }

    public void setValue(Signal s, Value v)
    {
        values.put(s,v);
    }

    
}
