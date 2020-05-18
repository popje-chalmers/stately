import java.util.*;

public class Model
{
    private List<ModelSignalComputation> signalComputations; // in computation order, only generated signals (STATEWISE and EXPRESSION)
    private Map<State,List<ModelTransition>> transitions;

    private Machine m;
    
    public Model(Machine m)
    {
        this.m = m;
        extract();
    }

    public List<ModelSignalComputation> getSignalComputations()
    {
        return new ArrayList<>(signalComputations);
    }

    public List<ModelTransition> getTransitionsFromState(State st)
    {
        List<ModelTransition> ts = transitions.get(st);
        if(ts == null)
        {
            return null;
        }
        return new ArrayList<>(ts);
    }

    // Preconditions: environment has current state, and values for all INPUTs.
    public void fillEnvironment(Environment e)
    {
        for(ModelSignalComputation msc: signalComputations)
        {
            e.setValue(msc.getSignal(), msc.getExpression().evaluate(e));
        }

        ModelTransition trans = null;
        for(ModelTransition t: transitions.get(e.getState()))
        {
            if(t.getCondition().evaluate(e).getBoolean())
            {
                if(trans == null)
                {
                    trans = t;
                }
                else
                {
                    throw new Error("Internal error: multiple transitions from state " + e.getState().getName());
                }
            }
        }
        
        if(trans != null)
        {
            e.setPath(trans.getPath());
        }
        else
        {
            e.setPath(null);
        }
    }
    
    // Inefficient, mostly for debugging
    public String toString()
    {
        String s = "== signal computations ==\n";
        for(ModelSignalComputation msc: signalComputations)
        {
            s += msc.toString() + "\n";
        }
        s += "== state transitions ==\n";
        for(Map.Entry<State,List<ModelTransition>> entry: transitions.entrySet())
        {
            for(ModelTransition t: entry.getValue())
            {
                s += t.toString() + "\n";
            }
        }
        return s;
    }

    private void extract()
    {
        if(m.getStatus() != MachineStatus.HAPPY)
        {
            throw new IllegalArgumentException("Can't model a non-happy machine.");
        }

        signalComputations = new ArrayList<>();
        transitions = new HashMap<>();
        
        for(Signal s: m.getDependencyGraph().getOrder())
        {
            SignalKind k = s.getKind();
            if(k == SignalKind.INPUT)
            {
                // Skip
            }
            else if(k == SignalKind.STATEWISE)
            {
                signalComputations.add(new ModelSignalComputation(s, getStatewise(s)));
            }
            else if(k == SignalKind.EXPRESSION)
            {
                signalComputations.add(new ModelSignalComputation(s, s.getExpression()));
            }
            else
            {
                throw Misc.impossible();
            }
        }
        
        for(State st: m.getStates())
        {
            if(!st.isVirtual())
            {
                transitions.put(st, getTransitions(st));
            }
        }
    }

    private Expression getStatewise(Signal s)
    {
        List<Expression> disjuncts = new ArrayList<>();
        
        for(State st: m.getStates())
        {
            if(!st.isVirtual())
            {
                Expression e = getSignalInState(s, st);
                if(e != null)
                {
                    List<Expression> conjuncts = new ArrayList<>();
                    List<State> states = new ArrayList<>();
                    states.add(st);
                    conjuncts.add(new Expression(states)); // in state st...
                    conjuncts.add(e); // and this is true...
                    disjuncts.add(new Expression(Operator.AND, conjuncts));
                }
            }
        }

        // disjuncts might be empty but that's alright
        return Simplifier.simplify(new Expression(Operator.OR, disjuncts));
    }

    private Expression getSignalInState(Signal s, State st)
    {
        return getSignalInStatement(s, st.getRootStatement(), new ArrayList<>());
    }

    private Expression getSignalInStatement(Signal s, Statement stm, List<Expression> conditions)
    {
        StatementKind k = stm.getKind();

        if(k == StatementKind.GROUP)
        {
            Expression result = null;
            for(Statement stm2: stm.getStatements())
            {
                Expression e = getSignalInStatement(s, stm2, conditions);
                if(e != null)
                {
                    if(result == null)
                    {
                        result = e;
                    }
                    else
                    {
                        throw new Error("Internal error: uncaught ambiguous signal value for signal " + s.getName() + ".");
                    }
                }
            }

            return result;
        }
        else if(k == StatementKind.EMIT)
        {
            if(stm.getSignal() == s)
            {
                List<Expression> operands = new ArrayList<>(conditions);
                operands.add(stm.getEmittedExp());
                return new Expression(Operator.AND, operands);
            }
            else
            {
                return null;
            }
        }
        else if(k == StatementKind.GOTO)
        {
            State dest = stm.getDestination();

            if(dest.isVirtual())
            {
                return getSignalInStatement(s, dest.getRootStatement(), conditions);
            }
            else
            {
                return null;
            }
        }
        else if(k == StatementKind.COND)
        {
            Expression cond = stm.getCondition();

            List<Expression> conditionsTrue = new ArrayList<>(conditions);
            conditionsTrue.add(cond);
            
            List<Expression> conditionsFalse = new ArrayList<>(conditions);
            List<Expression> tmp = new ArrayList<>();
            tmp.add(cond);
            conditionsFalse.add(new Expression(Operator.NOT, tmp));

            Expression caseTrue = getSignalInStatement(s, stm.getTrueBranch(), conditionsTrue);
            Expression caseFalse = getSignalInStatement(s, stm.getFalseBranch(), conditionsFalse);

            if(caseTrue == null && caseFalse == null)
            {
                return null;
            }
            else if(caseFalse == null)
            {
                return caseTrue;
            }
            else if(caseTrue == null)
            {
                return caseFalse;
            }
            else
            {
                List<Expression> operands = new ArrayList<>();
                operands.add(caseTrue);
                operands.add(caseFalse);
                return new Expression(Operator.OR, operands);
            }
        }
        else
        {
            throw Misc.impossible();
        }
    }
    
    private List<ModelTransition> getTransitions(State st)
    {
        List<ModelTransition> ts = new ArrayList<>();
        List<State> path = new ArrayList<>();
        path.add(st);
        createTransitions(st.getRootStatement(), new ArrayList<>(), path, ts);
        return ts;
    }

    private void createTransitions(Statement stm, List<Expression> conditions, List<State> path, List<ModelTransition> out)
    {
        StatementKind k = stm.getKind();

        if(k == StatementKind.GROUP)
        {
            for(Statement stm2: stm.getStatements())
            {
                createTransitions(stm2, conditions, path, out);
            }
        }
        else if(k == StatementKind.EMIT)
        {
            // no transitions
        }
        else if(k == StatementKind.GOTO)
        {
            State dest = stm.getDestination();
            path.add(dest);
            
            if(dest.isVirtual())
            {
                createTransitions(dest.getRootStatement(), conditions, path, out);
            }
            else
            {
                Expression cond = new Expression(Operator.AND, conditions);
                out.add(new ModelTransition(path, Simplifier.simplify(cond)));
            }
            path.remove(path.size()-1);
        }
        else if(k == StatementKind.COND)
        {
            Expression cond = stm.getCondition();

            List<Expression> conditionsTrue = new ArrayList<>(conditions);
            conditionsTrue.add(cond);
            
            List<Expression> conditionsFalse = new ArrayList<>(conditions);
            List<Expression> tmp = new ArrayList<>();
            tmp.add(cond);
            conditionsFalse.add(new Expression(Operator.NOT, tmp));

            createTransitions(stm.getTrueBranch(), conditionsTrue, path, out);
            createTransitions(stm.getFalseBranch(), conditionsFalse, path, out);
        }
        else
        {
            throw Misc.impossible();
        }
    }
}
