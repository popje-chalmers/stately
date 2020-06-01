package machine;
public class ArityError extends Error
{
    public ArityError(String function, int given)
    {
        super("Function/operator " + function + " not applicable to " + given + " argument(s).");
    }
}
