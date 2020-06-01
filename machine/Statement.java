package machine;
import java.util.*;

public class Statement
{
    private StatementKind kind;

    // GROUP
    private List<Statement> statements;
    
    // EMIT
    private Signal signal;
    private Expression emittedExp;

    // GOTO
    private State destination;

    // COND
    private Expression condition;
    private Statement trueBranch, falseBranch;

    // for any kind
    private String commentString;
    
    private Statement()
    {
        // Nothing to do
    }

    public static Statement emptyGroup()
    {
        Statement s = new Statement();
        s.kind = StatementKind.GROUP;
        s.statements = new ArrayList<>();
        return s;
    }

    public static Statement group(List<Statement> stms)
    {
        Statement s = new Statement();
        s.kind = StatementKind.GROUP;
        s.statements = new ArrayList<>(stms);
        return s;
    }

    public static Statement emit(Signal signal, Expression e)
    {
        Statement s = new Statement();
        s.kind = StatementKind.EMIT;
        s.signal = signal;
        s.emittedExp = e;
        return s;
    }

    public static Statement gotoState(State dest)
    {
        Statement s = new Statement();
        s.kind = StatementKind.GOTO;
        s.destination = dest;
        return s;
    }

    public static Statement conditional(Expression cond, Statement trueBranch, Statement falseBranch)
    {
        Statement s = new Statement();
        s.kind = StatementKind.COND;
        s.condition = cond;
        s.trueBranch = trueBranch;
        s.falseBranch = falseBranch;
        return s;
    }

    // All statements
    public StatementKind getKind() { return kind; }
    public String getComment() { return commentString; }
    public void setComment(String s) { commentString = s; }

    // GROUP
    public List<Statement> getStatements() { return new ArrayList<Statement>(statements); }
    
    // EMIT
    public Signal getSignal() { return signal; }
    public Expression getEmittedExp() { return emittedExp; }

    // GOTO
    public State getDestination() { return destination; }
    
    // COND
    public Expression getCondition() { return condition; }
    public Statement getTrueBranch() { return trueBranch; }
    public Statement getFalseBranch() { return falseBranch; }
    
    
    // Directly-next states.
    // Note that this ignores the default situation where no transition is made.
    public Set<State> getGOTOs()
    {
        Set<State> states = new HashSet<State>();
        addGOTOs(states);
        return states;
    }

    private void addGOTOs(Set<State> states)
    {
        StatementKind k = getKind();
        
        if(k == StatementKind.GROUP)
        {
            for(Statement stm: getStatements())
            {
                stm.addGOTOs(states);
            }
        }
        else if(k == StatementKind.EMIT)
        {
            // no gotos
        }
        else if(k == StatementKind.GOTO)
        {
            states.add(getDestination());
        }
        else if(k == StatementKind.COND)
        {
            getTrueBranch().addGOTOs(states);
            getFalseBranch().addGOTOs(states);
        }
        else
        {
            throw Misc.impossible();
        }
    }
    
    // All signals referenced by this statement tree.
    public Set<Signal> getReferencedSignals()
    {
        Set<Signal> deps = new HashSet<Signal>();
        addReferencedSignals(deps);
        return deps;
    }

    private void addReferencedSignals(Set<Signal> signals)
    {
        StatementKind k = getKind();
        
        if(k == StatementKind.GROUP)
        {
            for(Statement stm: getStatements())
            {
                stm.addReferencedSignals(signals);
            }
        }
        else if(k == StatementKind.EMIT)
        {
            signals.add(getSignal());
            signals.addAll(getEmittedExp().getReferencedSignals());
        }
        else if(k == StatementKind.GOTO)
        {
            // no dependencies
        }
        else if(k == StatementKind.COND)
        {
            signals.addAll(getCondition().getReferencedSignals());
            getTrueBranch().addReferencedSignals(signals);
            getFalseBranch().addReferencedSignals(signals);
        }
        else
        {
            throw Misc.impossible();
        }
    }

    // All states referenced by this statement tree, both in GOTOs and exps.
    public Set<State> getReferencedStates()
    {
        Set<State> states = new HashSet<State>();
        addReferencedStates(states);
        return states;
    }

    private void addReferencedStates(Set<State> states)
    {
        StatementKind k = getKind();
        
        if(k == StatementKind.GROUP)
        {
            for(Statement stm: getStatements())
            {
                stm.addReferencedStates(states);
            }
        }
        else if(k == StatementKind.EMIT)
        {
            states.addAll(getEmittedExp().getReferencedStates());
        }
        else if(k == StatementKind.GOTO)
        {
            states.add(destination);
        }
        else if(k == StatementKind.COND)
        {
            states.addAll(getCondition().getReferencedStates());
            getTrueBranch().addReferencedStates(states);
            getFalseBranch().addReferencedStates(states);
        }
        else
        {
            throw Misc.impossible();
        }
    }
}
