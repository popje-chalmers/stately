import java.util.*;

/*
 * Check for things that aren't necessarily problems, but might be.
 * 
 * Right now:
 * - Virtual states that don't necessarily "GOTO" another state.
 */
public class CheckWarnings
{
    public static List<Issue> checkWarnings(Machine m)
    {
        List<Issue> issues = new ArrayList<Issue>();

        for(State st: m.getStates())
        {
            if(st.isVirtual() && !alwaysGotos(st.getRootStatement(), st))
            {
                issues.add(Issue.virtualNoGoto(st));
            }
        }
        
        return issues;
    }

    // Always GOTOs something other than itself
    private static boolean alwaysGotos(Statement stm, State st)
    {
        StatementKind k = stm.getKind();
        
        if(k == StatementKind.GROUP)
        {
            for(Statement stm2: stm.getStatements())
            {
                if(alwaysGotos(stm2, st))
                {
                    return true;
                }
            }
            return false;
        }
        else if(k == StatementKind.EMIT)
        {
            return false;
        }
        else if(k == StatementKind.GOTO)
        {
            return stm.getDestination() != st;
        }
        else if(k == StatementKind.COND)
        {
            return alwaysGotos(stm.getTrueBranch(), st)
                && alwaysGotos(stm.getFalseBranch(), st);
        }

        throw Misc.impossible();
    }
}
