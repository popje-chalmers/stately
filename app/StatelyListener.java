package app;
import machine.*;
public interface StatelyListener
{
    public void machineModified(MachineEvent e);
    public void machineSwapped(MachineEvent e);
    public void selectionModified();
}
