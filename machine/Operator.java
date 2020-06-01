package machine;
import java.util.*;

public enum Operator
{
    NOT, AND, OR, NAND, NOR, XOR;

    public static Value applyOperator(Operator op, List<Value> args)
    {
        if(op == Operator.NOT)
        {
            if(args.size() != 1)
            {
                throw new Error("Uncaught arity problem - >1");
            }

            return new Value(!args.get(0).getBoolean());
        }
        else if(op == Operator.AND)
        {
            boolean result = true;
            for(Value arg: args)
            {
                result = result && arg.getBoolean();
            }
            return new Value(result);
        }
        else if(op == Operator.OR)
        {
            boolean result = false;
            for(Value arg: args)
            {
                result = result || arg.getBoolean();
            }
            return new Value(result);
        }
        else if(op == Operator.NAND)
        {
            boolean result = true;
            for(Value arg: args)
            {
                result = result && arg.getBoolean();
            }
            return new Value(!result);
        }
        else if(op == Operator.NOR)
        {
            boolean result = false;
            for(Value arg: args)
            {
                result = result || arg.getBoolean();
            }
            return new Value(!result);
        }
        else if(op == Operator.XOR)
        {
            boolean result = false;
            for(Value arg: args)
            {
                boolean b = arg.getBoolean();
                result = (!result && b) || (result && !b);
            }
            return new Value(result);
        }

        throw Misc.impossible();
    }

    public static String toAtom(Operator op)
    {
        switch(op)
        {
        case NOT:
            return "not";
        case AND:
            return "and";
        case OR:
            return "or";
        case NAND:
            return "nand";
        case NOR:
            return "nor";
        case XOR:
            return "xor";
        default:
            throw Misc.impossible();
        }
    }

    public static Operator fromAtom(String a)
    {
        switch(a)
        {
        case "not":
            return NOT;
        case "and":
            return AND;
        case "or":
            return OR;
        case "nand":
            return NAND;
        case "nor":
            return NOR;
        case "xor":
            return XOR;
        default:
            return null;
        }
    }
}
