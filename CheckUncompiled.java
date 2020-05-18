import java.util.*;

public class CheckUncompiled
{
    public static List<Issue> checkUncompiled(Machine m)
    {
        List<Issue> issues = new ArrayList<Issue>();
        List<Signal> uncompiledSignals = new ArrayList<Signal>();
        List<State> uncompiledStates = new ArrayList<State>();
        
        for(Signal s: m.getSignals())
        {
            if(s.getKind() == SignalKind.EXPRESSION
               && !s.getCode().isCompiled())
            {
                uncompiledSignals.add(s);
            }
        }

        for(State st: m.getStates())
        {
            if(!st.getCode().isCompiled())
            {
                uncompiledStates.add(st);
            }
        }

        if(!uncompiledSignals.isEmpty() || !uncompiledStates.isEmpty())
        {
            issues.add(Issue.uncompiled(uncompiledSignals, uncompiledStates));
        }

        return issues;
    }
}
