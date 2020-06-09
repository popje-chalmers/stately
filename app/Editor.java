package app;

public interface Editor<T>
{
    public boolean hasUnsavedChanges();
    public void load(T t); // *without* saving
    public void save(T t);
}
