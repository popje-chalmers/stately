package machine;
public enum SignalKind
{
    INPUT, STATEWISE, EXPRESSION;

    public static String toAtom(SignalKind k)
    {
        switch(k)
        {
        case INPUT:
            return "input";
        case STATEWISE:
            return "statewise";
        case EXPRESSION:
            return "expression";
        default:
            throw Misc.impossible();
        }
    }

    public static SignalKind fromAtom(String a)
    {
        switch(a)
        {
        case "input":
            return INPUT;
        case "statewise":
            return STATEWISE;
        case "expression":
            return EXPRESSION;
        default:
            return null;
        }
    }

    public static String toSymbol(SignalKind k)
    {
        switch(k)
        {
        case INPUT:
            return "<";
        case STATEWISE:
            return ">";
        case EXPRESSION:
            return "=";
        default:
            throw Misc.impossible();
        }
    }

    public static SignalKind fromSymbol(String s)
    {
        switch(s)
        {
        case "<":
            return INPUT;
        case ">":
            return STATEWISE;
        case "=":
            return EXPRESSION;
        default:
            return null;
        }
    }
}
