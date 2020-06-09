package app;
import machine.*;

public class SignalEditorController extends EditorController<Signal>
{
    public SignalEditorController(StatelyApp app, Editor<Signal> sed)
    {
        super(app, sed);
    }

    protected boolean stillExists(Signal s)
    {
        Machine m = app.getMachine();
        if(m == null)
        {
            return false;
        }
        return m.getSignals().contains(s);
    }
}
