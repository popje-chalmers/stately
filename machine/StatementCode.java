package machine;
import java.util.*;

public class StatementCode extends Code<Statement>
{
    public StatementCode(Machine m)
    {
        super(m);
    }
    
    protected Statement compileSource()
    {
        String bracketified = Bracketer.bracketify(getSource());
        Tokenizer tz = new Tokenizer(bracketified);
        List<Token> tokens = tz.tokenize();

        List<Statement> topLevelStms = new ArrayList<>();
        while(!tokens.isEmpty())
        {
            topLevelStms.add(parseStatement(tokens, true));
        }
        Statement topLevel = Statement.group(topLevelStms);

        return simplify(topLevel);
    }

    private Statement parseStatement(List<Token> tokens, boolean top)
    {
        if(tokens.isEmpty())
        {
            if(top)
            {
                return null;
            }
            else
            {
                throw new ParseError("Unexpected end of code");
            }
        }

        
        if(tokens.get(0).kind == TokenKind.END)
        {
            if(top)
            {
                throw new ParseError("Unexpected " + TokenKind.END);
            }

            return null;
        }

        Token t = tokens.remove(0);

        if(t.kind == TokenKind.ATOM)
        {
            String keyword = t.s;
            Statement stm;

            if(keyword.equals("emit"))
            {
                Signal signal = parseSignal(tokens);

                stm = Statement.emit(signal, new Expression(new Value(true)));
            }
            else if(keyword.equals("let"))
            {
                Signal signal = parseSignal(tokens);
                Expression exp = parseExpression(tokens);

                stm = Statement.emit(signal, exp);
            }
            else if(keyword.equals("goto"))
            {
                if(tokens.isEmpty())
                {
                    throw unexpectedEOF();
                }

                State destination = parseState(tokens);

                stm = Statement.gotoState(destination);
            }
            else if(keyword.equals("if"))
            {
                stm = parseIf(tokens);
            }
            else
            {
                throw new ParseError("Unknown keyword " + t.s, t);
            }

            return stm;
        }
        else if(t.kind == TokenKind.COMMENT)
        {
            Statement stm = Statement.emptyGroup();
            stm.setComment(t.s);
            return stm;
        }
        else
        {
            System.out.println(t.kind);
            throw new ParseError("Unexpected token", t);
        }
    }

    private Statement parseGroup(List<Token> tokens)
    {
        if(tokens.isEmpty())
        {
            throw unexpectedEOF();
        }

        Token t = tokens.remove(0);
        
        if(t.kind == TokenKind.BEGIN)
        {
            List<Statement> subs = new ArrayList<>();

            Statement foo = parseStatement(tokens, false);
            while(foo != null)
            {
                subs.add(foo);
                foo = parseStatement(tokens, false);
            }

            t = tokens.remove(0);
            if(t.kind != TokenKind.END)
            {
                throw new Error("Internal error: this should be a END token");
            }

            return Statement.group(subs);
        }
        else
        {
            throw new ParseError("Expected indented group", t);
        }
    }

    private Statement parseIf(List<Token> tokens)
    {
        Expression condition = parseExpression(tokens);
        Statement trueBranch = parseGroup(tokens);
        Statement falseBranch = Statement.emptyGroup();

        if(!tokens.isEmpty())
        {
            Token peek = tokens.get(0);
        
            if(peek.kind == TokenKind.ATOM && peek.s.equals("elif"))
            {
                tokens.remove(0);
                falseBranch = parseIf(tokens);
            }
            else if(peek.kind == TokenKind.ATOM && peek.s.equals("else"))
            {
                tokens.remove(0);
                falseBranch = parseGroup(tokens);
            }
        }
        
        return Statement.conditional(condition, trueBranch, falseBranch);
    }

    private Expression parseExpression(List<Token> tokens)
    {
        if(tokens.isEmpty())
        {
            throw unexpectedEOF();
        }
        SExpParser p = new SExpParser(tokens);
        SExp sexp = p.parse();
        ExpressionConverter c = new ExpressionConverter(getMachine());
        return c.convert(sexp);
    }

    private Signal parseSignal(List<Token> tokens)
    {
        if(tokens.isEmpty())
        {
            throw unexpectedEOF();
        }

        Token tok = tokens.remove(0);
        
        if(tok.kind != TokenKind.STRING && tok.kind != TokenKind.ATOM)
        {
            throw new ParseError("Expected signal name as string or atom", tok);
        }
        
        Signal s = getMachine().findSignal(tok.s);

        if(s == null)
        {
            throw new ParseError("Cannot find signal " + SExpPrinter.quote(tok.s), tok);
        }
        
        return s;
    }
    
    private State parseState(List<Token> tokens)
    {
        if(tokens.isEmpty())
        {
            throw unexpectedEOF();
        }

        Token tok = tokens.remove(0);
        
        if(tok.kind != TokenKind.STRING && tok.kind != TokenKind.ATOM)
        {
            throw new ParseError("Expected state name as string or atom", tok);
        }
        
        State st = getMachine().findState(tok.s);

        if(st == null)
        {
            throw new ParseError("Cannot find state " + SExpPrinter.quote(tok.s), tok);
        }
        
        return st;
    }

    private static ParseError unexpectedEOF()
    {
        return new ParseError("Unexpected end of code");
    }

    private static Statement simplify(Statement stm)
    {
        if(stm.getKind() == StatementKind.GROUP)
        {
            List<Statement> statements = stm.getStatements();
            if(statements.size() == 1)
            {
                return simplify(statements.get(0));
            }
            
            List<Statement> newStatements = new ArrayList<>();
            for(Statement stm2 : statements)
            {
                newStatements.add(simplify(stm2));
            }
            return Statement.group(newStatements);
        }
        else
        {
            return stm;
        }
    }
}
