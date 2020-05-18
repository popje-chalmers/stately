import java.util.*;

/*
 * Builds the dependency graph of signals for a machine.
 * Virtual states are effectively inlined, per their semantics.
 * Virtual states are also analyzed alone, out of caution.
 */
public class DependencyGraph
{
    private Machine m;

    private Map<Signal, Set<Signal>> graph;
    private List<List<Signal>> cycles; // hopefully none
    private List<Signal> order;
    
    public DependencyGraph(Machine m)
    {
        this.m = m;
        compute();
    }

    private void compute()
    {
        computeGraph();
        computeProperties();
    }

    public List<List<Signal>> getCycles()
    {
        List<List<Signal>> cyclesOut = new ArrayList<List<Signal>>();
        for(List<Signal> cycle: cycles)
        {
            cyclesOut.add(new ArrayList<Signal>(cycle));
        }
        return cyclesOut;
    }

    // null if no order possible
    public List<Signal> getOrder()
    {
        if(order == null)
        {
            return null;
        }
        return new ArrayList<Signal>(order);
    }
    
    
    private void computeGraph()
    {
        graph = new HashMap<Signal, Set<Signal>>();
        for(Signal s: m.getSignals())
        {
            graph.put(s, new HashSet<Signal>());
        }
        
        for(Signal s: m.getSignals())
        {
            SignalKind k = s.getKind();
            if(k == SignalKind.EXPRESSION)
            {
                for(Signal s2: s.getExpression().getDependencies())
                {
                    addDep(s, s2);
                }
            }
        }

        for(State st: m.getStates())
        {
            exploreStatement(st.getRootStatement(), new HashSet<Signal>());
        }
    }

    // Recursively discover dependencies, keeping track of conditionals
    // we're inside of (they are dependencies for emitted signals too).
    private void exploreStatement(Statement stm, Set<Signal> context)
    {
        StatementKind k = stm.getKind();

        if(k == StatementKind.GROUP)
        {
            for(Statement stm2: stm.getStatements())
            {
                exploreStatement(stm2, context);
            }
        }
        else if(k == StatementKind.EMIT)
        {
            // If a signal is being emitted with some expression as its value,
            // it depends on the signals in that expression and also the
            // conditionals that this emission depends upon (the "context").
            Signal s = stm.getSignal();
            Expression e = stm.getEmittedExp();
            for(Signal s2: e.getDependencies())
            {
                addDep(s, s2);
            }
            for(Signal s3: context)
            {
                addDep(s, s3);
            }
        }
        else if(k == StatementKind.COND)
        {
            Set<Signal> newContext = new HashSet<Signal>(context);
            for(Signal s: stm.getCondition().getDependencies())
            {
                newContext.add(s);
            }
            exploreStatement(stm.getTrueBranch(), newContext);
            exploreStatement(stm.getFalseBranch(), newContext);
        }
        else if(k == StatementKind.GOTO)
        {
            State st = stm.getDestination();
            if(st.isVirtual())
            {
                exploreStatement(st.getRootStatement(), context);
            }
            // Nothing to do for GOTO to normal states; no information flows.
        }
        else
        {
            throw Misc.impossible();
        }
    }

    private void addDep(Signal post, Signal pre)
    {
        graph.get(post).add(pre);
    }

    private void computeProperties()
    {
        GraphUtil<Signal> g = new GraphUtil<Signal>(graph);

        cycles = g.findCycles();

        if(cycles.isEmpty())
        {
            order = g.toposortNoCycles();

            // Sanity check
            if(order.size() != m.getSignals().size())
            {
                throw new Error("Toposorted signals missing some signals!");
            }
        }
        else
        {
            order = null;
        }
    }
}
