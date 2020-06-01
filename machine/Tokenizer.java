package machine;
import java.util.*;

public class Tokenizer
{
    public static final char OPEN = '(';
    public static final char CLOSE = ')';
    public static final char BEGIN = '{';
    public static final char END = '}';
    private String source;
    private int i;

    public Tokenizer(String s)
    {
        source = s;
    }
    
    public List<Token> tokenize()
    {
        List<Token> tokens = new ArrayList<>();

        String soFar = "";
        boolean inAtom = false;
        boolean inString = false;
        boolean inInt = false;
        boolean inComment = false;
        int start = -1; // appease java
        
        for(i = 0; i < source.length(); i++)
        {
            char c = source.charAt(i);

            if(inAtom)
            {
                if(Character.isWhitespace(c) || isSymbol(c))
                {
                    inAtom = false;
                    tokens.add(Token.atom(source, start, soFar));
                    i--;
                    continue;
                }
                else if(isAtomChar(c))
                {
                    soFar += c;
                }
                else
                {
                    unexpectedChar("atom", c);
                }
            }
            else if(inInt)
            {
                if(Character.isWhitespace(c) || isSymbol(c))
                {
                    inInt = false;
                    tokens.add(Token.integer(source, start, Integer.parseInt(soFar)));
                    i--;
                    continue;
                }
                else if(Character.isDigit(c))
                {
                    soFar += c;
                }
                else
                {
                    unexpectedChar("integer", c);
                }
            }
            else if(inString)
            {
                if(c == '"')
                {
                    inString = false;
                    tokens.add(Token.string(source, start, soFar));
                }
                else if(c == '\\')
                {
                    if(i + 1 == source.length())
                    {
                        unexpectedEndOfString();
                    }
                    i++;
                    c = source.charAt(i);

                    // TODO HEX, UNICODE, OTHER ESCAPE SEQUENCES
                    if(c == '\\')
                    {
                        soFar += "\\";
                    }
                    else if(c == '"')
                    {
                        soFar += "\"";
                    }
                    else if(c == 'n')
                    {
                        soFar += "\n";
                    }
                    else
                    {
                        unexpectedChar("escape sequence", c);
                    }
                }
                else
                {
                    soFar += c;
                }
            }
            else if(inComment)
            {
                if(c == '\n')
                {
                    tokens.add(Token.comment(source, start, soFar));
                    inComment = false;
                }
                else
                {
                    if(soFar.length() == 0)
                    {
                        if(!Character.isWhitespace(c))
                        {
                            start = i;
                            soFar += c;
                        }
                    }
                    else
                    {
                        soFar += c;
                    }
                }
            }
            else
            {
                if(c == OPEN)
                {
                    tokens.add(Token.open(source, i));
                }
                else if(c == CLOSE)
                {
                    tokens.add(Token.close(source, i));
                }
                else if(c == BEGIN)
                {
                    tokens.add(Token.begin(source, i));
                }
                else if(c == END)
                {
                    tokens.add(Token.end(source, i));
                }
                else if(c == '"')
                {
                    soFar = "";
                    inString = true;
                    start = i;
                }
                else if(isAtomStartChar(c))
                {
                    soFar = "" + c;
                    inAtom = true;
                    start = i;
                }
                else if(c == '-' && i + 1 < source.length() && source.charAt(i+1) == '-')
                {
                    soFar = "";
                    inComment = true;
                    start = i + 2;
                    i++;
                }
                else if(Character.isDigit(c) || c == '-')
                {
                    soFar = "" + c;
                    inInt = true;
                    start = i;
                }
                else if(Character.isWhitespace(c))
                {
                    // just keep swimming
                }
                else
                {
                    unexpectedChar("anywhere", c);
                }
            }
        }

        if(inAtom)
        {
            tokens.add(Token.atom(source, start, soFar));
        }
        else if(inInt)
        {
            tokens.add(Token.integer(source, start, Integer.parseInt(soFar)));
        }
        else if(inString)
        {
            unexpectedEndOfString();
        }

        return tokens;
    }

    private void unexpectedChar(String type, char c)
    {
        throw new ParseError("Unexpected character in " + type + ": " + c,
                             source, i);
    }
    
    private void unexpectedEndOfString()
    {
        throw new ParseError("Unexpected end of string", source, i);
    }

    public static boolean isAtomStartChar(char c)
    {
        return Character.isLetter(c) || c == '_';
    }
    
    public static boolean isAtomChar(char c)
    {
        return Character.isLetter(c) || Character.isDigit(c) || c == '_';
    }

    public static boolean isSymbol(char c)
    {
        return c == OPEN || c == CLOSE || c == BEGIN || c == END;
    }
}
