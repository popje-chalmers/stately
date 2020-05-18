import java.util.*;

public class SExpParser
{
    private List<Token> tokens;
    private int i;

    // REMOVES ELEMENTS FROM "tokens" AS IT PARSES
    public SExpParser(List<Token> tokens)
    {
        this.tokens = tokens;
    }

    public SExp parse()
    {
        return parseSExp(true);
    }

    private SExp parseSExp(boolean top)
    {
        if(tokens.isEmpty())
        {
            if(top)
            {
                return null;
            }
            else
            {
                throw new ParseError("Unexpected end of code!");
            }
        }
        
        Token t = tokens.get(0);

        if(t.kind == TokenKind.CLOSE)
        {
            if(top)
            {
                throw new ParseError("Unexpected " + Tokenizer.CLOSE, t);
            }
            else
            {
                return null;
            }
        }

        tokens.remove(0);
        
        if(t.kind == TokenKind.OPEN)
        {
            List<SExp> subs = new ArrayList<>();

            SExp foo = parseSExp(false);
            while(foo != null)
            {
                subs.add(foo);
                foo = parseSExp(false);
            }

            t = tokens.remove(0);
            if(t.kind != TokenKind.CLOSE)
            {
                throw new Error("Internal error: this should be a CLOSE token");
            }

            return SExp.mkList(subs);
        }
        else if(t.kind == TokenKind.ATOM)
        {
            return SExp.mkAtom(t.s);
        }
        else if(t.kind == TokenKind.INT)
        {
            return SExp.mkInt(t.i);
        }
        else if(t.kind == TokenKind.STRING)
        {
            return SExp.mkString(t.s);
        }
        else
        {
            throw new ParseError("Unexpected token", t);
        }
    }
}

