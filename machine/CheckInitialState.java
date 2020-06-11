package machine;
import java.util.*;

public class CheckInitialState
{
    // Find at least one virtual-state cycle issue if one exists.
    public static List<Issue> checkInitialState(Machine m)
    {
        List<Issue> issues = new ArrayList<Issue>();

        State init = m.getInitialState();
        if(init == null)
        {
            issues.add(Issue.initialMissing());
        }
        else if(init.isVirtual())
        {
            issues.add(Issue.initialVirtual(init));
        }
        
        return issues;
    }
}
