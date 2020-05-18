import java.util.*;

// Immutable!
public class Expression
{
    private ExpressionKind kind;
    
    private Value constant;
    private Signal signal;
    private Operator operator;
    private List<Expression> operands;
    private Set<State> onStates;

    public Expression(Value v)
    {
        kind = ExpressionKind.CONSTANT;
        constant = v;
    }
    
    public Expression(Signal s)
    {
        kind = ExpressionKind.SIGNAL;
        signal = s;
    }

    public Expression(Operator oper, List<Expression> ands)
    {
        kind = ExpressionKind.OPERATION;
        operator = oper;
        operands = new ArrayList<>(ands);
    }

    public Expression(Collection<State> states)
    {
        kind = ExpressionKind.STATE_IS;
        onStates = new HashSet<>(states);
    }

    public ExpressionKind getKind() { return kind; }
    public Value getConstant() { return constant; }
    public Signal getSignal() { return signal; }
    public Operator getOperator() { return operator; }
    public List<Expression> getOperands() { return new ArrayList<>(operands); }

    public Value evaluate(Environment e)
    {
        if(kind == ExpressionKind.CONSTANT)
        {
            return constant;
        }
        else if(kind == ExpressionKind.SIGNAL)
        {
            return e.getValue(signal);
        }
        else if(kind == ExpressionKind.OPERATION)
        {
            List<Value> args = new ArrayList<Value>();
            for(Expression exp: operands)
            {
                args.add(exp.evaluate(e));
            }
            return Operator.applyOperator(operator, args);
        }
        else if(kind == ExpressionKind.STATE_IS)
        {
            return new Value(onStates.contains(e.getState()));
        }

        throw Misc.impossible();
    }

    // All referenced signals are dependencies.
    public Set<Signal> getDependencies() { return getReferencedSignals(); }
    
    public Set<Signal> getReferencedSignals()
    {
        Set<Signal> deps = new HashSet<Signal>();
        addReferencedSignals(deps);
        return deps;
    }

    private void addReferencedSignals(Set<Signal> deps)
    {
        if(kind == ExpressionKind.SIGNAL)
        {
            deps.add(signal);
        }
        else if(kind == ExpressionKind.OPERATION)
        {
            for(Expression e: operands)
            {
                e.addReferencedSignals(deps);
            }
        }
    }

    public Set<State> getReferencedStates()
    {
        Set<State> states = new HashSet<State>();
        addReferencedStates(states);
        return states;
    }
    
    private void addReferencedStates(Set<State> states)
    {
        if(kind == ExpressionKind.OPERATION)
        {
            for(Expression e: operands)
            {
                e.addReferencedStates(states);
            }
        }
        else if(kind == ExpressionKind.STATE_IS)
        {
            states.addAll(onStates);
        }
    }

    public SExp toSExp()
    {
        if(kind == ExpressionKind.CONSTANT)
        {
            int v = constant.getBoolean() ? 1 : 0;
            return SExp.mkInt(v);
        }
        else if(kind == ExpressionKind.SIGNAL)
        {
            return SExp.mkAtom(signal.getName());
        }
        else if(kind == ExpressionKind.OPERATION)
        {
            List<SExp> list = new ArrayList<>();
            list.add(SExp.mkAtom(Operator.toAtom(operator)));
            for(Expression e: operands)
            {
                list.add(e.toSExp());
            }
            return SExp.mkList(list);
        }
        else if(kind == ExpressionKind.STATE_IS)
        {
            List<SExp> list = new ArrayList<>();
            list.add(SExp.mkAtom(ExpressionConverter.STATE_IS));
            for(State st: onStates)
            {
                list.add(SExp.mkString(st.getName()));
            }
            return SExp.mkList(list);
        }
        else
        {
            throw Misc.impossible();
        }
    }

    public String toString()
    {
        return toSExp().toString();
    }
}
