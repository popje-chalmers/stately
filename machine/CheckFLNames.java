package machine;
import java.util.*;

public class CheckFLNames
{
    // Does a half-arsed attempt to check that the resulting FL names will be ok-ish.
    public static List<Issue> checkFLNames(Machine m)
    {
        List<Issue> issues = new ArrayList<>();

        Set<String> reserved = new HashSet<>(FLOut.getReservedNames(m));
        Map<String, List<Object>> flNameMap = new HashMap<>();
        Map<Object, String> badNames = new HashMap<>();

        check(reserved, flNameMap, badNames, m, FLOut.machineFLName(m));
        for(Signal s: m.getSignals())
        {
            String flName = FLOut.signalFLName(m, s);
            check(reserved, flNameMap, badNames, s, flName);
        }
        for(State st: m.getStates())
        {
            String flName = FLOut.stateFLName(m, st);
            check(reserved, flNameMap, badNames, st, flName);
        }

        boolean conflict = false;
        List<Signal> conflictingSignals = new ArrayList<>();
        List<State> conflictingStates = new ArrayList<>();
        for(Map.Entry<String, List<Object>> entry: flNameMap.entrySet())
        {
            List<Object> obs = entry.getValue();
            if(obs.size() > 1)
            {
                conflict = true;
                for(Object ob: obs)
                {
                    if(ob instanceof Signal)
                    {
                        conflictingSignals.add((Signal)ob);
                    }
                    else if(ob instanceof State)
                    {
                        conflictingStates.add((State)ob);
                    }
                    // Could also be the machine, but there will always be at least one other item
                }
            }
        }

        if(conflict)
        {
            issues.add(Issue.flNameConflicts(conflictingSignals, conflictingStates));
        }

        if(!badNames.isEmpty())
        {
            for(Map.Entry<Object, String> entry: badNames.entrySet())
            {
                Object ob = entry.getKey();
                String flName = entry.getValue();

                if(ob instanceof Signal)
                {
                    issues.add(Issue.flNameBadSignal((Signal)ob, flName));
                }
                else if(ob instanceof State)
                {
                    issues.add(Issue.flNameBadState((State)ob, flName));
                }
                else if(ob instanceof Machine)
                {
                    issues.add(Issue.flNameBadMachine(flName));
                }
                else
                {
                    throw Misc.impossible();
                }
            }
        }
        
        return issues;
    }

    private static void check(Set<String> reserved, Map<String, List<Object>> flNameMap, Map<Object, String> badNames, Object object, String flName)
    {
        addEntry(flNameMap, flName, object);

        if(isBad(flName) || reserved.contains(flName))
        {
            badNames.put(object, flName);
        }
    }

    private static boolean isBad(String flName)
    {
        if(flName.equals(""))
        {
            return true;
        }

        char first = flName.charAt(0);
        if(!(isAlpha(first) || first == '_'))
        {
            return true;
        }
        
        for(int i = 0; i < flName.length(); i++)
        {
            char c = flName.charAt(i);
            if(!(isAlpha(c) || isDigit(c) || c == '_'))
            {
                return true;
            }
        }

        return false;
    }

    private static boolean isAlpha(char c)
    {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isDigit(char c)
    {
        return c >= '0' && c <= '9';
    }

    private static <A, B> void addEntry(Map<A, List<B>> map, A a, B b)
    {
        List<B> bs = map.get(a);
        if(bs == null)
        {
            bs = new ArrayList<>();
            bs.add(b);
            map.put(a, bs);
        }
        else
        {
            bs.add(b);
        }
    }
}
