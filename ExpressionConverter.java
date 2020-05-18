import java.util.*;

public class ExpressionConverter
{
    // String constants for parsing
    public static final String STATE_IS = "state_is";
    
    private Machine m;
    
    public ExpressionConverter(Machine m)
    {
        this.m = m;
    }

    public Expression convert(SExp sexp)
    {
        SExpKind k = sexp.getKind();

        if(k == SExpKind.LIST)
        {
            List<SExp> list = sexp.getList();

            if(list.isEmpty())
            {
                throw new ExpressionError("The empty list isn't a valid boolean expression.");
            }

            SExp head = list.get(0);

            if(head.getKind() != SExpKind.ATOM)
            {
                throw new ExpressionError("Bad function application.");
            }

            String headAtom = head.getAtom();

            if(headAtom.equals(STATE_IS))
            {
                List<State> states = new ArrayList<>();
                for(int i = 1; i < list.size(); i++)
                {
                    states.add(getState(list.get(i)));
                }
                return new Expression(states);
            }
            else
            {
                Operator op = Operator.fromAtom(headAtom);

                if(op == null)
                {
                    throw new ExpressionError("Not an operator: " + headAtom);
                }
                
                List<Expression> operands = new ArrayList<>();
                for(int i = 1; i < list.size(); i++)
                {
                    operands.add(convert(list.get(i)));
                }

                if(op == Operator.NOT && operands.size() != 1)
                {
                    throw new ExpressionError("The negation operator is unary-only.");
                }

                return new Expression(op, operands);
            }
        }
        else if(k == SExpKind.ATOM || k == SExpKind.STRING)
        {
            return new Expression(getSignal(sexp));
        }
        else if(k == SExpKind.INT)
        {
            int i = sexp.getInt();
            if(i != 0 && i != 1)
            {
                throw new ExpressionError("Only values 0 and 1 are allowed.");
            }
            return new Expression(new Value(i != 0));
        }

        throw Misc.impossible();
    }

    public Signal getSignal(SExp sexp)
    {
        if(sexp.getKind() != SExpKind.ATOM && sexp.getKind() != SExpKind.STRING)
        {
            throw new ExpressionError("Signal names must be atoms or strings");
        }
        String name = sexp.getKind() == SExpKind.ATOM ? sexp.getAtom() : sexp.getString();
        Signal s = m.findSignal(name);
        if(s == null)
        {
            throw new ExpressionError("Cannot find signal " + name);
        }
        return s;
    }
    
    public State getState(SExp sexp)
    {
        if(sexp.getKind() != SExpKind.ATOM && sexp.getKind() != SExpKind.STRING)
        {
            throw new ExpressionError("State names must be atoms or strings");
        }
        String name = sexp.getKind() == SExpKind.ATOM ? sexp.getAtom() : sexp.getString();
        State st = m.findState(name);
        if(st == null)
        {
            throw new ExpressionError("Cannot find state " + name);
        }
        return st;
    }
}
