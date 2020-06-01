package machine;
import java.util.*;

public class NameComparator<T extends Named> implements Comparator<T>
{
    public int compare(T t1, T t2)
    {
        return t1.getName().compareTo(t2.getName());
    }
}
