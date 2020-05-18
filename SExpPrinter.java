public class SExpPrinter
{
    public static String print(SExp e)
    {
        SExpKind k = e.getKind();
        
        if(k == SExpKind.LIST)
        {
            String s = "" + Tokenizer.OPEN;
            boolean space = false;
            for(SExp e2: e.getList())
            {
                if(space)
                {
                    s += " ";
                }
                space = true;

                s += print(e2);
            }
            s += Tokenizer.CLOSE;
            return s;
        }
        else if(k == SExpKind.ATOM)
        {
            return e.getAtom();
        }
        else if(k == SExpKind.INT)
        {
            return "" + e.getInt();
        }
        else if(k == SExpKind.STRING)
        {
            return quote(e.getString());
        }

        throw Misc.impossible();
    }

    public static String quote(String s)
    {
        return "\"" + escape(s) + "\"";
    }

    public static String escape(String s)
    {
        String t = "";

        for(int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if(c == '\\')
            {
                t += "\\\\";
            }
            else if(c == '"')
            {
                t += "\\\"";
            }
            else if(c == '\n')
            {
                t += "\\n";
            }
            else
            {
                t += c;
            }
        }

        return t;
    }
}
