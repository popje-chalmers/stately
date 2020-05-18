import java.util.*;

/*
 * - Compile all signals and states. (Analysis easiest when everything is compiled.)
 * - Check for virtual state cycles. (Semantics of states undefined when there are cycles of virtual states).
 * - Check some properies of states, with virtual states inlined:
 *   - Emitted signals are of kind STATEWISE. (Could check earlier.)
 *   - Unambiguous next state.
 *   - Unambiguous signal emission.
 * 
 * Extra niceties:
 * - Warning if a virtual state doesn't always transition.
 */

public class Machine
{
    private String name;
    private List<Signal> signals = new ArrayList<>();
    private List<State> states = new ArrayList<>();
    private MachineStatus status = MachineStatus.UNCOMPILED;

    private List<Issue> issues = new ArrayList<>();
    private Map<State, Set<State>> coarseGraph = new HashMap<State, Set<State>>(); // Valid (though incomplete) even when some things aren't compiled.
    
    // Only valid when status > UNCOMPILED
    private DependencyGraph dgraph;

    

    public Machine(String name)
    {
        this.name = name;
    }

    // Warning: the resulting set should not be mutated!
    public Set<State> accessCoarseGraph(State st) { return coarseGraph.get(st); }
    
    public DependencyGraph getDependencyGraph() { return dgraph; }
    public List<Issue> getIssues() { return new ArrayList<>(issues); }
    public String getName() { return name; }
    public List<Signal> getSignals() { return new ArrayList<>(signals); }
    public List<State> getStates() { return new ArrayList<>(states); }
    public MachineStatus getStatus() { return status; }
    public void setName(String n) { name = n; }
    
    public void addSignal(Signal s)
    {
        signals.add(s);
    }

    public void addState(State s)
    {
        states.add(s);
    }

    public Signal findSignal(String name)
    {
        // TODO make more efficient
        for(Signal s: signals)
        {
            if(s.getName().equals(name))
            {
                return s;
            }
        }
        return null;
    }

    public State findState(String name)
    {
        // TODO make more efficient
        for(State st: states)
        {
            if(st.getName().equals(name))
            {
                return st;
            }
        }
        return null;
    }
    
    public void removeSignal(Signal s)
    { 
        breakSignal(s);

        signals.remove(s);
    }

    public void removeState(State st)
    {
        breakState(st);
        
        states.remove(st);
    }

    public void renameSignal(Signal s, String newName)
    {
        // TODO: fix links to it
        breakSignal(s);
        s.setName(newName);
    }

    public void renameState(State st, String newName)
    {
        // TODO: fix links to it
        breakState(st);
        st.setName(newName);
    }

    // Important to call after any changes to the signals/states.
    // Try to compile everything, and then...
    // Check compilation status of signals/states,
    // build dependency graph, and update status.
    public void analyze()
    {
        status = MachineStatus.UNCOMPILED;
        issues.clear();
        coarseGraph.clear();
        dgraph = null;

        compileAll();
        
        // Phase 0: generate coarse graph for display purposes
        coarseGraph.clear();
        for(State st: states)
        {
            if(st.getCode().isCompiled())
            {
                coarseGraph.put(st, st.getRootStatement().getGOTOs());
            }
        }

        // Phase 0.5: check for name conflicts
        if(registerIssues(CheckNameConflicts.checkNameConflicts(this),
                          MachineStatus.UNCOMPILED))
        {
            return;
        }

        // Phase 1: check for type errors and uncompiled things
        List<Issue> compilationIssues = new ArrayList<Issue>();
        compilationIssues.addAll(CheckTypes.checkTypes(this));
        compilationIssues.addAll(CheckUncompiled.checkUncompiled(this));
        
        if(registerIssues(compilationIssues,
                          MachineStatus.UNCOMPILED))
        {
            return;
        }

        // Phase 2: check for cycles in virtual states
        if(registerIssues(CheckVCycles.checkVCycles(this),
                          MachineStatus.TROUBLESOME))
        {
            return;
        }
        
        // Phase 3: determine signal dependencies, check for cycles there
        dgraph = new DependencyGraph(this);
        List<List<Signal>> cycles = dgraph.getCycles();
        if(!cycles.isEmpty())
        {
            for(List<Signal> cycle: cycles)
            {
                issues.add(Issue.dependencyCycle(cycle));
            }
            status = MachineStatus.TROUBLESOME;
            return;
        }

        // Phase 4: check for overlap issues
        if(registerIssues(CheckOverlap.checkOverlap(this),
                          MachineStatus.TROUBLESOME))
        {
            return;
        }

        // Phase 5: warnings for suspicious choices, don't abort
        registerIssues(CheckWarnings.checkWarnings(this), status);

        // Debugging: make sure no error was missed.
        for(Issue is: issues)
        {
            if(is.isError())
            {
                throw Misc.impossible();
            }
        }
        
        status = MachineStatus.HAPPY;
        return;
    }

    private boolean registerIssues(List<Issue> maybeIssues, MachineStatus ifSo)
    {
        issues.addAll(maybeIssues);
        if(!maybeIssues.isEmpty())
        {
            status = ifSo;
            return true;
        }
        return false;
    }

    // Try to compile any uncompiled signals/states.
    private boolean compileAll()
    {
        boolean success = true;

        for(Signal s: signals)
        {
            if(s.getKind() == SignalKind.EXPRESSION
               && !s.getCode().isCompiled())
            {
                success = s.getCode().compile() && success;
            }
        }
        
        for(State st: states)
        {
            if(!st.getCode().isCompiled())
            {
                success = st.getCode().compile() && success;
            }
        }

        return success;
    }

    private void breakSignal(Signal s)
    {
        for(Signal s2: signals)
        {
            if(s2 != s && s2.getCode().isCompiled()
               && s2.getExpression().getReferencedSignals().contains(s))
            {
                s2.getCode().reset();
            }
        }

        for(State st: states)
        {
            if(st.getCode().isCompiled()
               && st.getRootStatement().getReferencedSignals().contains(s))
            {
                st.getCode().reset();
            }
        }
    }
    
    private void breakState(State st)
    {
        for(Signal s2: signals)
        {
            if(s2.getCode().isCompiled()
               && s2.getExpression().getReferencedStates().contains(st))
            {
                s2.getCode().reset();
            }
        }
        
        for(State st2: states)
        {
            if(st2 != st && st2.getCode().isCompiled()
               && st2.getRootStatement().getReferencedStates().contains(st))
            {
                st2.getCode().reset();
            }
        }
    }

    public SExp toSExp()
    {
        Map<String,SExp> content = new TreeMap<String,SExp>();
        content.put("name", SExp.mkString(name));

        List<SExp> signalSExps = new ArrayList<>();
        for(Signal s: signals)
        {
            signalSExps.add(s.toSExp());
        }
        content.put("signals", SExp.mkList(signalSExps));

        List<SExp> stateSExps = new ArrayList<>();
        for(State st: states)
        {
            stateSExps.add(st.toSExp());
        }
        content.put("states", SExp.mkList(stateSExps));
        
        return SExp.stringMapToExp(content);
    }

    public static Machine fromSExp(SExp exp)
    {
        Map<String,SExp> content = SExp.expToStringMap(exp);
        if(content == null)
        {
            throw UnpackError.badMap();
        }

        SExp nameExp = content.get("name");
        if(nameExp == null || nameExp.getKind() != SExpKind.STRING)
        {
            throw UnpackError.badField("name");
        }
        String name = nameExp.getString();

        Machine m = new Machine(name);
        
        SExp signalsExp = content.get("signals");
        if(signalsExp == null || signalsExp.getKind() != SExpKind.LIST)
        {
            throw UnpackError.badField("signals");
        }
        for(SExp signalExp: signalsExp.getList())
        {
            m.addSignal(Signal.fromSExp(signalExp, m));
        }

        SExp statesExp = content.get("states");
        if(statesExp == null || statesExp.getKind() != SExpKind.LIST)
        {
            throw UnpackError.badField("states");
        }
        for(SExp stateExp: statesExp.getList())
        {
            m.addState(State.fromSExp(stateExp, m));
        }
        
        return m;
    }
}
