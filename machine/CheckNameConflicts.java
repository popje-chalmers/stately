package machine;
import java.util.*;

public class CheckNameConflicts
{
    public static List<Issue> checkNameConflicts(Machine m)
    {
        List<Issue> issues = new ArrayList<>();

        Set<String> taken = new HashSet<>();
        Set<String> duplicates = new HashSet<>();

        for(Signal s: m.getSignals())
        {
            addName(s.getName(), taken, duplicates);
        }
        for(State st: m.getStates())
        {
            addName(st.getName(), taken, duplicates);
        }

        List<Signal> signals = new ArrayList<>();
        List<State> states = new ArrayList<>();

        for(Signal s: m.getSignals())
        {
            if(duplicates.contains(s.getName()))
            {
                signals.add(s);
            }
        }

        for(State st: m.getStates())
        {
            if(duplicates.contains(st.getName()))
            {
                states.add(st);
            }
        }

        if(!signals.isEmpty() || !states.isEmpty())
        {
            issues.add(Issue.nameConflicts(signals, states));
        }

        return issues;
    }

    private static void addName(String n, Set<String> taken, Set<String> duplicates)
    {
        if(taken.contains(n))
        {
            duplicates.add(n);
        }
        else
        {
            taken.add(n);
        }
    }
}
