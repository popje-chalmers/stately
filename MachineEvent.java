public class MachineEvent
{
    private Object source;
    private Machine newMachine;

    // A modification
    public MachineEvent(Object source)
    {
        this.source = source;
    }

    // A swap to a new machine
    public MachineEvent(Object source, Machine newMachine)
    {
        this.source = source;
        this.newMachine = newMachine;
    }

    // Only non-null if swapped to a new machine.
    public Machine getNewMachine() { return newMachine; }
}
