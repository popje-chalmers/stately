public class TypeError extends Error
{
    public TypeError(String function, String type)
    {
        super("Function/operator " + function + " not applicable to argument of type " + type + " (at least in that position).");
    }
}
