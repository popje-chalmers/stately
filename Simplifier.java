import java.util.*;

public class Simplifier
{
    public static Expression simplify(Expression exp)
    {
        ExpressionKind k = exp.getKind();

        if(k == ExpressionKind.OPERATION)
        {
            Operator op = exp.getOperator();
            List<Expression> simplifiedOperands = new ArrayList<>();
            List<Expression> withoutTrivial = new ArrayList<>();
            boolean hasZero = false;
            boolean hasOne = false;
            for(Expression exp2: exp.getOperands())
            {
                Expression simple = simplify(exp2);
                simplifiedOperands.add(simple);
                if(simple.getKind() == ExpressionKind.CONSTANT)
                {
                    Value v = simple.getConstant();
                    if(v.getBoolean())
                    {
                        hasOne = true;
                    }
                    else
                    {
                        hasZero = true;
                    }
                }
                else
                {
                    withoutTrivial.add(simple);
                }
                
            }

            Expression zero = new Expression(new Value(false));
            Expression one = new Expression(new Value(true));

            if(op == Operator.NOT)
            {
                // original arity is sensible
                if(simplifiedOperands.size() == 1)
                {
                    if(hasZero)
                    {
                        return one;
                    }
                    else if(hasOne)
                    {
                        return zero;
                    }
                    else
                    {
                        return new Expression(op, simplifiedOperands);
                    }
                }
                
                // arity error to be caught later
                return new Expression(op, simplifiedOperands);
            }
            else if(op == Operator.AND)
            {
                if(hasZero)
                {
                    return zero;
                }

                // had no operands or just ones
                if(withoutTrivial.isEmpty())
                {
                    return one;
                }

                if(withoutTrivial.size() == 1)
                {
                    return withoutTrivial.get(0);
                }

                return new Expression(op, withoutTrivial);
            }
            else if(op == Operator.OR)
            {
                if(hasOne)
                {
                    return one;
                }

                // had no operands or just zeroes
                if(withoutTrivial.isEmpty())
                {
                    return zero;
                }

                if(withoutTrivial.size() == 1)
                {
                    return withoutTrivial.get(0);
                }

                return new Expression(op, withoutTrivial);
            }
            else if(op == Operator.NAND)
            {
                if(hasZero)
                {
                    return one;
                }

                // had no operands or just ones
                if(withoutTrivial.isEmpty())
                {
                    return zero;
                }

                if(withoutTrivial.size() == 1)
                {
                    return new Expression(Operator.NOT, withoutTrivial);
                }
                
                return new Expression(op, withoutTrivial);
            }
            else if(op == Operator.NOR)
            {
                if(hasOne)
                {
                    return zero;
                }

                // had no operands or just zeroes
                if(withoutTrivial.isEmpty())
                {
                    return one;
                }

                if(withoutTrivial.size() == 1)
                {
                    return new Expression(Operator.NOT, withoutTrivial);
                }

                return new Expression(op, withoutTrivial);
            }
            else
            {
                return new Expression(op, simplifiedOperands);
            }
        }
        else
        {
            return exp;
        }
    }
}
