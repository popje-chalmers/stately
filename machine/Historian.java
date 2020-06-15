package machine;

import java.util.*;

public class Historian
{
    public static final int NO_LIMIT = 0;
    
    private Machine machine;
    private List<SimulationState> history = new ArrayList<>();
    private int limit = NO_LIMIT;
    
    public Historian(Machine m)
    {
        machine = m;
    }

    public void clear()
    {
        history.clear();
    }
    
    public List<SimulationState> getHistory()
    {
        return new ArrayList<>(history);
    }

    public int getHistorySize()
    {
        return history.size();
    }

    public String makeWaveform()
    {
        String out = "";
        for(Signal s: machine.getSignals())
        {
            out += makeWaveformForSignal(s);
        }
        return out;
    }

    private String makeWaveformForSignal(Signal s)
    {
        String line1 = "";
        String line2 = "";

        String paddedName = s.getName() + " ";
        while(paddedName.length() < 24)
        {
            paddedName = " " + paddedName;
        }
            
        line2 = paddedName;
        for(int i = 0; i < line2.length(); i++)
        {
            line1 += " ";
        }
        
        boolean prev = false;
        boolean prevValid = false;
        for(int i = 0; i < history.size(); i++)
        {
            Environment env = history.get(i).getEnvironment();
            if(env != null && env.hasValueForSignal(s))
            {
                boolean cur = env.getValue(s).getBoolean();
                
                if(!prevValid)
                {
                    prev = cur;
                }

                if(prev)
                {
                    if(cur)
                    {
                        line1 += "__";
                        line2 += "  ";
                    }
                    else
                    {
                        line1 += "  ";
                        line2 += "\\_";
                    }
                }
                else
                {
                    if(cur)
                    {
                        line1 += " _";
                        line2 += "/ ";
                    }
                    else
                    {
                        line1 += "  ";
                        line2 += "__";
                    }
                }

                prev = cur;
                prevValid = true;
            }
            else
            {
                line1 += "  ";
                line2 += "??";
            }
        }

        return line1 + "\n" + line2 + "\n";
    }

    public SimulationState peek()
    {
        if(history.isEmpty())
        {
            return null;
        }

        return history.get(history.size() - 1);
    }
    
    public void record(SimulationState sims)
    {
        history.add(sims);
        cleanup();
    }

    public void setLimit(int limit)
    {
        this.limit = limit;
        cleanup();
    }

    public SimulationState unrecord()
    {
        SimulationState toReturn = null;
        if(!history.isEmpty())
        {
            toReturn = history.remove(history.size()-1);
        }
        
        return toReturn;
    }
    
    private void cleanup()
    {
        if(limit <= NO_LIMIT)
        {
            return;
        }

        int tooMuch = history.size() - limit;
        // inefficient for large cleanups, O(n^2), but I'm in a hurry :P
        for(int i = 0; i < tooMuch; i++)
        {
            history.remove(0);
        }
    }
}
