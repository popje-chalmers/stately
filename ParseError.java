public class ParseError extends CodeError
{
    public static final int BEFORE = 10;
    public static final int AFTER = 10;

    public ParseError(String msg)
    {
        super("Parser error: " + msg);
    }
    
    public ParseError(String msg, Token tok)
    {
        super("Parse error: " + msg + " around here: " + context(tok.source, tok.loc));
    }

    public ParseError(String msg, String source, int loc)
    {
        super("Parse error: " + msg + " around here: " + context(source, loc));
    }

    private static final String context(String stuff, int loc)
    {
        int start = loc - BEFORE;
        start = start < 0 ? 0 : start;

        int end = loc + 1 + AFTER;
        end = end > stuff.length() ? stuff.length() : end;
        
        return stuff.substring(start, end);
    }
}
