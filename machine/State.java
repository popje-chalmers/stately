package machine;
import java.util.*;

public class State implements Named, Positionable
{
    private String name;
    private String description = "";
    private boolean virtual;
    private StatementCode code;
    private int x, y;
    
    public State(String name, Machine m)
    {
        this.name = name;
        this.code = new StatementCode(m);
    }

    public StatementCode getCode() { return code; }
    public String getDescription() { return description; }
    public String getName() { return name; }
    public Set<Signal> getReferencedSignals()
    {
        return getRootStatement().getReferencedSignals();
    }
    public Statement getRootStatement()
    {
        Statement root = code.getCompiled();
        if(root == null)
        {
            throw new Error("State " + name + ": can't get root statement from non-compiled code.");
        }
        return root;
    }
    public double getX() { return (double)x; }
    public double getY() { return (double)y; }
    public boolean isVirtual() { return virtual; }
    public void setVirtual(boolean b) { virtual = b; }
    public void setDescription(String s) { description = s; }
    public void setName(String s) { name = s; }
    public void setPosition(double x, double y)
    {
        this.x = (int)Math.round(x);
        this.y = (int)Math.round(y);
    }

    public SExp toSExp()
    {
        Map<String,SExp> content = new TreeMap<String,SExp>();
        content.put("name", SExp.mkString(name));
        content.put("description", SExp.mkString(description));
        content.put("virtual", SExp.mkBoolAsInt(virtual));
        content.put("x", SExp.mkInt(x));
        content.put("y", SExp.mkInt(y));
        content.put("code", SExp.mkString(code.getSource()));
        return SExp.stringMapToExp(content);
    }
    
    public static State fromSExp(SExp exp, Machine m)
    {
        Map<String,SExp> content = SExp.expToStringMap(exp);
        if(content == null)
        {
            throw UnpackError.badMap();
        }

        String name = Unpack.getStringItem(content, "name", false, null);
        String description = Unpack.getStringItem(content, "description", false, null);
        boolean virtual = Unpack.getBooleanItem(content, "virtual", false, false);
        int x = Unpack.getIntItem(content, "x", false, 0);
        int y = Unpack.getIntItem(content, "y", false, 0);
        String code = Unpack.getStringItem(content, "code", false, null);
        
        State st = new State(name, m);
        st.setDescription(description);
        st.setVirtual(virtual);
        st.getCode().setSource(code);
        st.setPosition(x,y);
        return st;
    }

    public String toString()
    {
        return name;
    }
}
