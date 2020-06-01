package app;
import machine.*;
import java.util.*;

public class SelectionManager<T>
{
    private Set<T> selected = new HashSet<>();

    public SelectionManager()
    {

    }

    public void clear()
    {
        selected.clear();
    }
    
    public void deselect(T t)
    {
        selected.remove(t);
    }

    public Set<T> getSelected()
    {
        return new HashSet<>(selected);
    }

    public boolean isSelected(T t)
    {
        return selected.contains(t);
    }

    public void select(T t)
    {
        selected.add(t);
    }
    
    public void toggle(T t)
    {
        if(isSelected(t))
        {
            deselect(t);
        }
        else
        {
            select(t);
        }
    }
}
