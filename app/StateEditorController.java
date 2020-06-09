package app;
import machine.*;

public class StateEditorController extends EditorController<State>
{
    public StateEditorController(StatelyApp app, Editor<State> sed)
    {
        super(app, sed);
    }

    protected boolean stillExists(State st)
    {
        Machine m = app.getMachine();
        if(m == null)
        {
            return false;
        }
        return m.getStates().contains(st);
    }
}
