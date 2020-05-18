import java.util.*;

public class Issue
{
    private IssueCode code;
    private String message; // description of issue
    private List<Signal> signals = new ArrayList<Signal>(); // involved signals
    private List<State> states = new ArrayList<State>(); // involved states
    private boolean signalsCyclic = false; // cyclic vs independent
    private boolean statesCyclic = false; // cyclic vs independent
    private boolean warning = false; // warning vs error
    
    private Issue(IssueCode code, String message, List<Signal> signals, List<State> states)
    {
        this.code = code;
        this.message = message;

        if(signals != null)
        {
            this.signals.addAll(signals);
        }
        
        if(states != null)
        {
            this.states.addAll(states);
        }
    }

    public IssueCode getCode() { return code; }
    public String getMessage() { return message; }
    public List<Signal> getSignals() { return new ArrayList<Signal>(signals); }
    public List<State> getStates() { return new ArrayList<State>(states); }
    public boolean isCyclicWithSignals() { return signalsCyclic; }
    public boolean isCyclicWithStates() { return statesCyclic; }
    public boolean isError() { return !warning; }
    public boolean isWarning() { return warning; }

    public String toString()
    {
        String prefix = isError() ? "ERROR" : "WARNING";

        String signalString = "";
        for(Signal s: signals)
        {
            signalString += " " + s.getName();
        }
        
        String stateString = "";
        for(State st: states)
        {
            stateString += " " + st.getName();
        }
        
        return prefix + " (signals:" + signalString
            + ") (states:" + stateString + ") " + getMessage();
    }
    
    public static Issue uncompiled(List<Signal> signals, List<State> states)
    {
        return new Issue(IssueCode.UNCOMPILED,
                         "uncompiled signals or states",
                         signals, states);
    }

    public static Issue dependencyCycle(List<Signal> signals)
    {
        Issue i = new Issue(IssueCode.DEPENDENCY_CYCLE,
                            "signal dependency cycle",
                            signals, null);
        i.signalsCyclic = true;
        return i;
    }

    public static Issue virtualStateCycle(List<State> states)
    {
        Issue i = new Issue(IssueCode.VIRTUAL_STATE_CYCLE,
                            "virtual state cycle",
                            null, states);
        i.statesCyclic = true;
        return i;
    }

    public static Issue cannotEmit(Signal s, State st)
    {
        List<Signal> signals = new ArrayList<Signal>();
        signals.add(s);
        List<State> states = new ArrayList<State>();
        states.add(st);
        Issue i = new Issue(IssueCode.BAD_EMISSIONS,
                            "cannot emit non-statewise signal",
                            signals, states);
        return i;
    }

    public static Issue overlappingEmit(State st)
    {
        List<State> states = new ArrayList<State>();
        states.add(st);
        Issue i = new Issue(IssueCode.OVERLAPPING_EMIT,
                            "overlapping emit",
                            null, states);
        return i;
    }

    public static Issue overlappingGoto(State st)
    {
        List<State> states = new ArrayList<State>();
        states.add(st);
        Issue i = new Issue(IssueCode.OVERLAPPING_GOTO,
                            "overlapping goto",
                            null, states);
        return i;
    }

    public static Issue virtualNoGoto(State st)
    {
        List<State> states = new ArrayList<State>();
        states.add(st);
        Issue i = new Issue(IssueCode.VIRTUAL_NO_GOTO,
                            "virtual state might not leave",
                            null, states);
        i.warning = true;
        return i;
    }

    public static Issue nameConflicts(List<Signal> signals, List<State> states)
    {
        return new Issue(IssueCode.NAME_CONFLICTS,
                         "name conflict(s)",
                         signals, states);
    }
}
