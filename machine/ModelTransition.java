package machine;
import java.util.*;

public class ModelTransition
{
    private List<State> path; // current state, intermediate virtuals, next state
    private Expression condition;
    
    public ModelTransition(List<State> p, Expression e)
    {
        if(p.size() < 2)
        {
            throw new IllegalArgumentException("Paths must have a start and end state.");
        }
        this.path = new ArrayList<>(p);
        this.condition = e;
    }

    public Expression getCondition() { return condition; }
    public List<State> getPath() { return new ArrayList<>(path); }
    
    public String toString()
    {
        String s = path.get(0).getName();
        for(int i = 1; i < path.size(); i++)
        {
            s += " -> " + path.get(i).getName();
        }
        s += " {" + condition.toSExp().toString() + "}";
        return s;
    }
}
