import java.util.*;

// Insert brackets based on indentation.
// Assumes tabs are 4 spaces.
public class Bracketer
{
    public static final int TAB_STOP = 4;
    
    public static String bracketify(String s)
    {
        List<String> lines = splitLines(s);
        List<String> newLines = new ArrayList<>();

        List<Integer> prevIndents = new ArrayList<>();
        int currentIndent = 0;
        for(String line: lines)
        {
            int indent = getIndent(line);
            
            if(indent >= 0)
            {
                if(indent > currentIndent)
                {
                    prevIndents.add(currentIndent);
                    newLines.add(spaces(currentIndent) + Tokenizer.BEGIN);
                }
                else if(indent < currentIndent)
                {
                    while(!prevIndents.isEmpty() && indent <= prevIndents.get(prevIndents.size()-1))
                    {
                        int tmp = prevIndents.get(prevIndents.size()-1);
                        newLines.add(spaces(tmp) + Tokenizer.END);
                        prevIndents.remove(prevIndents.size()-1);
                    }
                }

                currentIndent = indent;
            }

            newLines.add(line);
        }

        while(!prevIndents.isEmpty())
        {
            int tmp = prevIndents.get(prevIndents.size()-1);
            newLines.add(spaces(tmp) + Tokenizer.END);
            prevIndents.remove(prevIndents.size()-1);
        }

        return mergeLines(newLines);
    }

    public static String spaces(int count)
    {
        String s = "";
        for(int i = 0; i < count; i++)
        {
            s += " ";
        }
        return s;
    }

    // Returns -1 for lines that don't have any non-whitespace content.
    public static int getIndent(String line)
    {
        int i = 0;
        while(i < line.length())
        {
            char c = line.charAt(i);
            if(c == ' ')
            {
                i++;
            }
            else if(c == '\t')
            {
                i++;
                while(i % TAB_STOP != 0)
                {
                    i++;
                }
            }
            else if(c == '\r' || c == '\n')
            {
                return -1;
            }
            else
            {
                return i;
            }
        }
        return -1;
    }
    
    public static List<String> splitLines(String s)
    {
        List<String> l = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(s, "\n");
        while(st.hasMoreTokens())
        {
            l.add(st.nextToken());
        }
        return l;
    }

    public static String mergeLines(List<String> l)
    {
        String result = "";
        for(String s: l)
        {
            result += s + "\n";
        }
        return result;
    }
}
