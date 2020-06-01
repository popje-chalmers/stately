package machine;
import java.util.*;

public class Signal implements Named
{
    private String name;
    private SignalKind kind;
    private String description = "";
    private ExpressionCode code; // exists for all, only used for EXPRESSION signals

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
    
    public SignalKind getKind() { return kind; }
    public String getName() { return name; }
    public void setDescription(String s) { description = s; }
    public void setName(String s) { name = s; }
    
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
        return SExp.stringMapToExp(content);
    }

    public static Signal fromSExp(SExp exp, Machine m)
    {
        Map<String,SExp> content = SExp.expToStringMap(exp);
        if(content == null)
        {
            throw UnpackError.badMap();
        }

        SExp nameExp = content.get("name");
        if(nameExp == null || nameExp.getKind() != SExpKind.STRING)
        {
            throw UnpackError.badField("name");
        }
        String name = nameExp.getString();

        SExp kindExp = content.get("kind");
        if(kindExp == null || kindExp.getKind() != SExpKind.ATOM)
        {
            throw UnpackError.badField("kind");
        }
        SignalKind kind = SignalKind.fromAtom(kindExp.getAtom());
        if(kind == null)
        {
            throw UnpackError.badField("kind");
        }

        SExp descriptionExp = content.get("description");
        if(descriptionExp == null || descriptionExp.getKind() != SExpKind.STRING)
        {
            throw UnpackError.badField("description");
        }
        String description = descriptionExp.getString();

        SExp codeExp = content.get("code");
        if(codeExp == null || codeExp.getKind() != SExpKind.STRING)
        {
            throw UnpackError.badField("code");
        }
        String code = codeExp.getString();

        Signal s = new Signal(name, kind, m);
        s.setDescription(description);
        s.getCode().setSource(code);
        return s;
    }
}
