package machine;
public abstract class Code<T>
{
    private String source = "";
    private T compiled;
    private boolean compSuccess;
    private String error = "";
    private Machine machine;
    
    public Code(Machine m)
    {
        machine = m;
        reset();
    }

    // Discard compiled version, if any.
    public void reset()
    {
        compiled = null;
        compSuccess = false;
        error = null;
    }

    // If it's already compiled, return true.
    // If not: try to compile it, and save any errors.
    // Return true upon success.
    public boolean compile()
    {
        if(isCompiled())
        {
            return true;
        }
        
        reset();
        
        try
        {
            compiled = compileSource();
            compSuccess = true;
        }
        catch(CodeError t)
        {
            error = t.getMessage();
        }

        return compSuccess;
    }

    public T getCompiled() { return compiled; }
    public String getError() { return error; }
    public Machine getMachine() { return machine; }
    public String getSource() { return source; }
    public boolean isCompiled() { return compSuccess; }
    
    public void setSource(String s)
    {
        reset();
        source = s;
    }

    // Subclass implements this. Throw error on fail.
    protected abstract T compileSource();
}
