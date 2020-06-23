package machine;
import java.util.*;

public class CheckResetSignal
{
    // Check that the reset signal exists and is the correct kind.
    public static List<Issue> checkResetSignal(Machine m)
    {
        List<Issue> issues = new ArrayList<Issue>();

        Signal reset = m.findSignal("reset");
        if(reset == null)
        {
            issues.add(Issue.resetMissing());
        }
        else if(reset.getKind() != SignalKind.INPUT)
        {
            issues.add(Issue.specialSignalWrongKind(reset, SignalKind.INPUT));
        }
        
        return issues;
    }
}
