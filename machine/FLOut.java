package machine;
import java.util.*;

public class FLOut
{
    // Transform an expression into one ready to be output as FL.
    // - Turn NAND and NOR into NOT+AND and NOT+OR.
    // - Make every IS_STATE unary.
    public static Expression flPrepExp(Expression exp)
    {
        ExpressionKind k = exp.getKind();
        if(k == ExpressionKind.OPERATION)
        {
            List<Expression> operands = new ArrayList<>();
            for(Expression oldOperand: exp.getOperands())
            {
                operands.add(flPrepExp(oldOperand));
            }
            Operator op = exp.getOperator();
            if(op == Operator.NAND)
            {
                List<Expression> tmp = new ArrayList<>();
                tmp.add(new Expression(Operator.AND, operands));
                return new Expression(Operator.NOT, tmp);
            }
            else if(op == Operator.NOR)
            {
                List<Expression> tmp = new ArrayList<>();
                tmp.add(new Expression(Operator.OR, operands));
                return new Expression(Operator.NOT, tmp);
            }
            else
            {
                return new Expression(op, operands);
            }
        }
        else if(k == ExpressionKind.STATE_IS)
        {
            Collection<State> states = exp.getStates();
            if(states.isEmpty())
            {
                return new Expression(new Value(false));
            }
            else if(states.size() == 1)
            {
                return exp;
            }
            else
            {
                List<Expression> operands = new ArrayList<>();
                for(State st: states)
                {
                    List<State> tmp = new ArrayList<>();
                    tmp.add(st);
                    operands.add(new Expression(tmp));
                }
                return new Expression(Operator.OR, operands);
            }
        }
        else
        {
            return exp;
        }
    }
}
