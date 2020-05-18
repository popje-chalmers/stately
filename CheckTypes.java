import java.util.*;

/*
 * Check the types of anything that's compiled (and ignore things that aren't).
 * If everything's compiled, this class must provide a complete type check.
 *
 * Type issues checked:
 * - Emitting a signal that isn't of kind STATEWISE.
 */
public class CheckTypes
{
    public static List<Issue> checkTypes(Machine m)
    {
        List<Issue> issues = new ArrayList<Issue>();

        for(State st: m.getStates())
        {
            if(st.getCode().isCompiled())
            {
                check(st.getRootStatement(), st, issues);
            }
        }
        
        return issues;
    }

    private static void check(Statement stm, State st, List<Issue> issues)
    {
        StatementKind k = stm.getKind();

        if(k == StatementKind.GROUP)
        {
            for(Statement stm2: stm.getStatements())
            {
                check(stm2, st, issues);
            }
        }
        else if(k == StatementKind.EMIT)
        {
            Signal s = stm.getSignal();
            if(s.getKind() != SignalKind.STATEWISE)
            {
                issues.add(Issue.cannotEmit(s, st));
            }
        }
        else if(k == StatementKind.COND)
        {
            check(stm.getTrueBranch(), st, issues);
            check(stm.getFalseBranch(), st, issues);
        }
        else if(k == StatementKind.GOTO)
        {
            // nothing
        }
        else
        {
            throw Misc.impossible();
        }
    }
}
