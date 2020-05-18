import java.util.*;

public class ExpressionCode extends Code<Expression>
{
    public ExpressionCode(Machine m)
    {
        super(m);
    }
    
    protected Expression compileSource()
    {
        Tokenizer tz = new Tokenizer(getSource());
        List<Token> tokens = tz.tokenize();

        SExpParser p = new SExpParser(tokens);
        SExp sexp = p.parse();

        if(sexp == null)
        {
            throw new ParseError("Missing expression");
        }

        if(!tokens.isEmpty())
        {
            throw new ParseError("Dangling tokens: ", tokens.get(0));
        }

        ExpressionConverter c = new ExpressionConverter(getMachine());
        return c.convert(sexp);
    }
}
