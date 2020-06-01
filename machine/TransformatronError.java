package machine;
public class TransformatronError extends Error
{
    public TransformatronError(String msg)
    {
        super(msg);
    }

    public static TransformatronError bad(SExp exp)
    {
        return new TransformatronError("Bad: " + exp.toString());
    }
}
