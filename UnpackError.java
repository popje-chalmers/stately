public class UnpackError extends Error
{
    public UnpackError(String msg)
    {
        super(msg);
    }

    public static UnpackError badMap()
    {
        return new UnpackError("Bad map");
    }
    
    public static UnpackError badField(String fieldName)
    {
        return new UnpackError("Bad/missing field '" + fieldName + "'");
    }
}
