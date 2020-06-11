package app;

public interface Editor<T>
{
    public boolean hasUnappliedChanges();
    public void load(T t); // *without* saving
    public void apply(T t);
}
