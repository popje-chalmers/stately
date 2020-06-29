package machine;
import java.util.*;

public class CascadingComparator<T> implements Comparator<T>
{
    private Comparator<T> first, second;

    public CascadingComparator(Comparator<T> first, Comparator<T> second)
    {
        this.first = first;
        this.second = second;
    }
    
    public int compare(T t1, T t2)
    {
        int result = first.compare(t1, t2);
        if(result == 0)
        {
            return second.compare(t1, t2);
        }
        return result;
    }
}
