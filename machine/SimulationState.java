package machine;

import java.util.*;

public class SimulationState
{
    private State state;
    private Environment environment;
    
    public SimulationState(State state, Environment environment)
    {
        this.state = state;
        this.environment = environment;
    }

    public Environment getEnvironment()
    {
        return environment;
    }

    public State getState()
    {
        return state;
    }
}
