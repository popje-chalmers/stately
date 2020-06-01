package app;
import machine.*;
public interface Filter<T>
{
    public boolean matches(T t);
}
