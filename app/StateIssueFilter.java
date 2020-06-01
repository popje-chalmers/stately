package app;
import machine.*;
import java.util.*;

// Any issue that pertains to a state or at least one of a collection of states.
public class StateIssueFilter implements Filter<Issue>
{
    private List<State> states = new ArrayList<>();
    
    public StateIssueFilter(State st)
    {
        states.add(st);
    }

    public StateIssueFilter(Collection<State> sts)
    {
        states.addAll(sts);
    }
    
    public boolean matches(Issue i)
    {
        List<State> istates = i.getStates();
        for(State st: states)
        {
            if(istates.contains(st))
            {
                return true;
            }
        }
        return false;
    }
}
