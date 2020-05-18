public class ModelSignalComputation
{
    private Signal signal;
    private Expression expression;

    public ModelSignalComputation(Signal s, Expression e)
    {
        signal = s;
        expression = e;
    }

    public Signal getSignal() { return signal; }
    public Expression getExpression() { return expression; }
    
    public String toString()
    {
        return signal.getName() + " := " + expression.toString();
    }
}
