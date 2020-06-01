package machine;
public class Value
{
    private boolean bool;

    public Value(boolean b)
    {
        this.bool = b;
    }

    public boolean getBoolean()
    {
        return bool;
    }

    public static Value getDefault()
    {
        return new Value(false);
    }
}
