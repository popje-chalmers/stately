package machine;
import java.util.*;

public class PriorityComparator implements Comparator<Signal>
{
    public int compare(Signal s1, Signal s2)
    {
        return Integer.compare(s1.getPriority(), s2.getPriority());
    }
}
