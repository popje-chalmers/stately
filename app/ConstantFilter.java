package app;
import machine.*;

public class ConstantFilter<T> implements Filter<T>
{
    private boolean match;
    
    public ConstantFilter(boolean b)
    {
        this.match = b;
    }
    
    public boolean matches(T t)
    {
        return match;
    }
}
