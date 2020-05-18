import java.util.*;

/*
 * Note: inlines virtual states for proper semantics wrt emission overlap.
 */
public class CheckOverlap
{
    public static List<Issue> checkOverlap(Machine m)
    {
        List<Issue> issues = new ArrayList<Issue>();

        for(State st: m.getStates())
        {
            checkState(st, issues);
        }
        
        return issues;
    }

    private static void checkState(State st, List<Issue> issues)
    {
        Outputs out = tabulate(st.getRootStatement(), new Outputs());
        List<Signal> doublyEmitted = new ArrayList<Signal>();
        for(Map.Entry<Signal,Integer> entry: out.emitCount.entrySet())
        {
            if(entry.getValue() > 1)
            {
                doublyEmitted.add(entry.getKey());
            }
        }
        boolean doubleGoto = out.gotoCount > 1;

        if(!doublyEmitted.isEmpty())
        {
            // TODO report which signals, since we know that
            issues.add(Issue.overlappingEmit(st));
        }

        if(doubleGoto)
        {
            issues.add(Issue.overlappingGoto(st));
        }
    }

    private static Outputs tabulate(Statement stm, Outputs out)
    {
        StatementKind k = stm.getKind();

        if(k == StatementKind.GROUP)
        {
            for(Statement stm2: stm.getStatements())
            {
                tabulate(stm2, out);
            }
        }
        else if(k == StatementKind.EMIT)
        {
            out.addEmit(stm.getSignal(), 1);
        }
        else if(k == StatementKind.COND)
        {
            Outputs outTrue = tabulate(stm.getTrueBranch(), new Outputs());
            Outputs outFalse = tabulate(stm.getFalseBranch(), new Outputs());
            Outputs union = outTrue.union(outFalse);
            out.add(union);
        }
        else if(k == StatementKind.GOTO)
        {
            State dest = stm.getDestination();
            if(dest.isVirtual())
            {
                tabulate(dest.getRootStatement(), out);
            }
            else
            {
                out.addGoto(1);
            }
        }
        else
        {
            throw Misc.impossible();
        }

        return out;
    }
}

class Outputs
{
    public Map<Signal, Integer> emitCount = new HashMap<Signal,Integer>();
    public int gotoCount = 0;

    
    public void addEmit(Signal s, int howMany)
    {
        emitCount.put(s, getEmitCount(s) + howMany);
    }

    public void addGoto(int howMany)
    {
        gotoCount += howMany;
    }

    public int getEmitCount(Signal s)
    {
        Integer count = emitCount.get(s);
        if(count == null)
        {
            return 0;
        }
        else
        {
            return count.intValue();
        }
    }

    public void add(Outputs other)
    {
        for(Map.Entry<Signal, Integer> entry: other.emitCount.entrySet())
        {
            Signal os = entry.getKey();
            int oc = entry.getValue();
            addEmit(os, oc);
        }
        addGoto(other.gotoCount);
    }

    
    /*
    public Outputs duplicate()
    {
        Outputs out = new Outputs();
        for(Map.Entry<Signal, Integer> entry: emitCount)
        {
            out.emitCount.put(entry.getKey(), entry.getValue());
        }
        out.gotoCount = gotoCount;
        return out;
    }
    */
    
    

    public Outputs union(Outputs other)
    {
        Outputs out = new Outputs();

        Set<Signal> signals = new HashSet<Signal>();
        signals.addAll(emitCount.keySet());
        signals.addAll(other.emitCount.keySet());
        
        for(Signal s: signals)
        {
            int count = Integer.max(getEmitCount(s), other.getEmitCount(s));
            out.emitCount.put(s, count);
        }

        out.gotoCount = Integer.max(gotoCount, other.gotoCount);
        return out;
    }
    
}
