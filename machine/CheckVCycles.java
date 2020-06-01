package machine;
import java.util.*;

public class CheckVCycles
{
    // Find at least one virtual-state cycle issue if one exists.
    public static List<Issue> checkVCycles(Machine m)
    {
        List<Issue> issues = new ArrayList<Issue>();
        List<List<State>> cycles = findSomeCycles(m);
        
        for(List<State> cycle: cycles)
        {
            issues.add(Issue.virtualStateCycle(cycle));
        }
        
        return issues;
    }
    
    // Find at least one virtual-state cycle if one exists.
    // Does NOT catch the situation where a virtual state doesn't transition.
    // DOES catch the situation where it explicitly GOTOs itself.
    // Precondition: all states are compiled.
    private static List<List<State>> findSomeCycles(Machine m)
    {
        // Graph only of virtual states
        Map<State, Set<State>> vgraph = new HashMap<State, Set<State>>();

        for(State st: m.getStates())
        {
            if(st.isVirtual())
            {
                Set<State> vnext = new HashSet<State>();
                for(State st2: st.getRootStatement().getGOTOs())
                {
                    if(st2.isVirtual())
                    {
                        vnext.add(st2);
                    }
                }
                vgraph.put(st, vnext);
            }
        }

        GraphUtil<State> g = new GraphUtil<State>(vgraph);
        return g.findCycles();
    }
}
