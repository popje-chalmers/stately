package app;

public abstract class EditorController<T> implements StatelyListener
{
    protected StatelyApp app;
    private Editor<T> editor;
    private T beingEdited = null;

    public EditorController(StatelyApp app, Editor<T> ed)
    {
        this.app = app;
        editor = ed;

        app.addStatelyListener(this);
    }

    // Abstract

    protected abstract boolean stillExists(T t);

    // Public

    public void edit(T t)
    {
        if(t == beingEdited)
        {
            return;
        }

        saveIfReasonable();
        load(t);
    }

    public T getEditing()
    {
        return beingEdited;
    }

    public void saveIfReasonable()
    {
        if(beingEdited != null && stillExists(beingEdited) && editor.hasUnsavedChanges())
        {
            editor.save(beingEdited);
            app.reportMachineModification(this);
        }
    }
    
    // Private
    
    private void load(T t)
    {
        editor.load(t);
        beingEdited = t;
    }
    
    // StatelyListener
    
    public void machineModified(MachineEvent e)
    {
        if(beingEdited == null)
        {
            return;
        }
        
        if(stillExists(beingEdited))
        {
            if(!editor.hasUnsavedChanges())
            {
                load(beingEdited);
            }
        }
        else
        {
            load(null);
        }
    }
    
    public void machineSwapped(MachineEvent e)
    {
        load(null);
    }
    
    public void selectionModified() {}
}
