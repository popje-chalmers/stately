package machine;
import java.util.*;

public class SExp
{
    private SExpKind kind;

    private List<SExp> list;
    private String atom;
    private int intValue;
    private String stringValue;
    
    private SExp(SExpKind k)
    {
        kind = k;
    }
    
    public SExpKind getKind() { return kind; }
    public List<SExp> getList() { return new ArrayList<SExp>(list); }
    public String getAtom() { return atom; }
    public int getInt() { return intValue; }
    public String getString() { return stringValue; }
    public String toString() { return SExpPrinter.print(this); }
    public boolean getBoolFromInt() { return intValue != 0; }
    
    public static SExp mkList(List<SExp> l)
    {
        SExp e = new SExp(SExpKind.LIST);
        e.list = l;
        return e;
    }

    public static SExp mkAtom(String a)
    {
        SExp e = new SExp(SExpKind.ATOM);
        e.atom = a;
        return e;
    }

    public static SExp mkInt(int i)
    {
        SExp e = new SExp(SExpKind.INT);
        e.intValue = i;
        return e;
    }

    public static SExp mkString(String s)
    {
        SExp e = new SExp(SExpKind.STRING);
        e.stringValue = s;
        return e;
    }

    public static SExp mkBoolAsInt(boolean b)
    {
        return mkInt(b ? 1 : 0);
    }

    // Convenience stuff
    
    // Make a 2-element list
    public static SExp pair(SExp exp1, SExp exp2)
    {
        List<SExp> pair = new ArrayList<>();
        pair.add(exp1);
        pair.add(exp2);
        return mkList(pair);
    }
    
    // Convert a map to a list of pairs
    public static SExp stringMapToExp(Map<String,SExp> map)
    {
        List<SExp> list = new ArrayList<>();
        for(Map.Entry<String,SExp> entry: map.entrySet())
        {
            list.add(pair(mkString(entry.getKey()),entry.getValue()));
        }
        return mkList(list);
    }

    // Convert a list of pairs to a map (or return null if malformed)
    public static Map<String,SExp> expToStringMap(SExp exp)
    {
        if(exp.getKind() != SExpKind.LIST)
        {
            return null;
        }

        Map<String,SExp> map = new TreeMap<String,SExp>();
        
        for(SExp pair: exp.getList())
        {
            if(pair.getKind() != SExpKind.LIST)
            {
                return null;
            }
            
            List<SExp> pairList = pair.getList();
            
            if(pairList.size() != 2
               || pairList.get(0).getKind() != SExpKind.STRING)
            {
                return null;
            }

            map.put(pairList.get(0).getString(), pairList.get(1));
        }

        return map;
    }
}

