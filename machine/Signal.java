package machine;
import java.util.*;

public class Signal implements Named
{
    private String name;
    private SignalKind kind;
    private String description = "";
    private ExpressionCode code; // exists for all, only used for EXPRESSION signals
    private boolean internal = false; // only relevant to STATEWISE and EXPRESSION signals (since INPUT signals are never internal, obviously)

    public Signal(String name, SignalKind kind, Machine m)
    {
        this.name = name;
        this.kind = kind;
        this.code = new ExpressionCode(m);
    }
    
    public ExpressionCode getCode() { return code; }
    public String getDescription() { return description; }
    
    public Expression getExpression()
    {
        if(kind != SignalKind.EXPRESSION)
        {
            throw new Error("Signal " + name + " isn't of type expression, so cannot getExpression().");
        }
        
        Expression e = code.getCompiled();

        if(e == null)
        {
            throw new Error("Signal " + name + ": can't get expression from non-compiled code.");
        }

        return e;
    }

    public boolean getInternal() { return internal; } // only for sw/expr
    public SignalKind getKind() { return kind; }
    public String getName() { return name; }
    public boolean isInternal() { return internal && kind != SignalKind.INPUT; }
    public void setDescription(String s) { description = s; }
    public void setName(String s) { name = s; }
    public void setInternal(boolean b) { internal = b; } // only for sw/expr
    
    public void setKind(SignalKind k)
    {
        if(kind != k)
        {
            code.reset();
        }
        kind = k;
    }

    public SExp toSExp()
    {
        Map<String,SExp> content = new TreeMap<String,SExp>();
        content.put("name", SExp.mkString(name));
        content.put("kind", SExp.mkAtom(SignalKind.toAtom(kind)));
        content.put("description", SExp.mkString(description));
        content.put("code", SExp.mkString(code.getSource()));
        content.put("internal", SExp.mkBoolAsInt(internal));
        return SExp.stringMapToExp(content);
    }

    public static Signal fromSExp(SExp exp, Machine m)
    {
        Map<String,SExp> content = SExp.expToStringMap(exp);
        if(content == null)
        {
            throw UnpackError.badMap();
        }

        String name = Unpack.getStringItem(content, "name", false, null);
        String kindAtom = Unpack.getAtomItem(content, "kind", false, null);
        SignalKind kind = SignalKind.fromAtom(kindAtom);
        if(kind == null)
        {
            throw UnpackError.badField("kind");
        }

        String description = Unpack.getStringItem(content, "description", false, null);
        String code = Unpack.getStringItem(content, "code", false, null);

        // Optional parameters
        boolean internal = Unpack.getBooleanItem(content, "internal", true, false);

        Signal s = new Signal(name, kind, m);
        s.setDescription(description);
        s.getCode().setSource(code);
        s.setInternal(internal);
        return s;
    }

    public String toString()
    {
        return name;
    }
}
