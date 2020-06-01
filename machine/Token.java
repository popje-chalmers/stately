package machine;
public class Token
{
    public TokenKind kind;
    public String s; // atoms, strings, comments
    public int i; // ints

    public String source;
    public int loc;
    
    public Token(String source, int loc, TokenKind k)
    {
        this.source = source;
        this.loc = loc;
        kind = k;
    }

    public String toString()
    {
        String before = "<" + kind.toString() + "@" + loc;
        String after = ">";
        String middle = "";

        if(kind == TokenKind.ATOM)
        {
            middle = s;
        }
        else if(kind == TokenKind.INT)
        {
            middle = "" + i;
        }
        else if(kind == TokenKind.STRING)
        {
            middle = SExpPrinter.quote(s);
        }

        if(middle != "")
        {
            middle = " " + middle;
        }

        return before + middle + after;
    }

    public static Token open(String source, int loc)
    {
        return new Token(source, loc, TokenKind.OPEN);
    }

    public static Token close(String source, int loc)
    {
        return new Token(source, loc, TokenKind.CLOSE);
    }

    public static Token begin(String source, int loc)
    {
        return new Token(source, loc, TokenKind.BEGIN);
    }

    public static Token end(String source, int loc)
    {
        return new Token(source, loc, TokenKind.END);
    }

    public static Token atom(String source, int loc, String s)
    {
        Token t = new Token(source, loc, TokenKind.ATOM);
        t.s = s;
        return t;
    }
    
    public static Token integer(String source, int loc, int i)
    {
        Token t = new Token(source, loc, TokenKind.INT);
        t.i = i;
        return t;
    }

    public static Token string(String source, int loc, String s)
    {
        Token t = new Token(source, loc, TokenKind.STRING);
        t.s = s;
        return t;
    }

    public static Token comment(String source, int loc, String s)
    {
        Token t = new Token(source, loc, TokenKind.COMMENT);
        t.s = s;
        return t;
    }
}

