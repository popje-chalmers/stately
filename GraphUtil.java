import java.util.*;

public class GraphUtil<T>
{
    private Map<T,Set<T>> graph = new HashMap<T,Set<T>>(); // Neighbor set

    public GraphUtil(Map<T,Set<T>> neighbors)
    {
        for(Map.Entry<T,Set<T>> entry: neighbors.entrySet())
        {
            graph.put(entry.getKey(), new HashSet<T>(entry.getValue()));
        }
    }

    // Finds some non-overlapping cycles.
    // Guaranteed to find at least one cycle if any cycles exist.
    // Could be more efficient.
    public List<List<T>> findCycles()
    {
        Set<T> explored = new HashSet<T>();
        List<T> stack = new ArrayList<T>();
        List<List<T>> cycles = new ArrayList<List<T>>();

        for(T node: graph.keySet())
        {
            if(!explored.contains(node))
            {
                cycleExplore(node, explored, stack, cycles);
            }
        }

        return cycles;
    }

    private void cycleExplore(T node, Set<T> explored, List<T> stack, List<List<T>> cycles)
    {
        int index = stack.indexOf(node);
        if(index >= 0)
        {
            List<T> cycle = new ArrayList<T>();
            for(int i = index; i < stack.size(); i++)
            {
                cycle.add(stack.get(i));
            }
            cycles.add(cycle);
            return;
        }

        if(explored.contains(node))
        {
            return;
        }

        explored.add(node);
        stack.add(node);

        for(T neighbor: graph.get(node))
        {
            cycleExplore(neighbor, explored, stack, cycles);
        }
        
        stack.remove(stack.size() - 1);
    }


    // Sort nodes so that all arrows point left.
    // Precondition: no cycles (see findCycles() above).
    // Results will be bogus if there are cycles.
    public List<T> toposortNoCycles()
    {
        Set<T> explored = new HashSet<T>();
        List<T> output = new ArrayList<T>();

        Set<T> roots = new HashSet<>(graph.keySet());
        
        for(Map.Entry<T,Set<T>> entry: graph.entrySet())
        {
            for(T neighbor: entry.getValue())
            {
                roots.remove(neighbor);
            }
        }

        for(T node: roots)
        {
            toposortExplore(node, explored, output);
        }

        return output;
    }

    private void toposortExplore(T node, Set<T> explored, List<T> output)
    {
        if(explored.contains(node))
        {
            return;
        }
        
        explored.add(node);
        
        for(T neighbor: graph.get(node))
        {
            toposortExplore(neighbor, explored, output);
        }

        output.add(node);
    }
}
