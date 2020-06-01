package app;
import machine.*;
import java.util.*;

public class Drag
{
    private Map<Positionable,Pt> originalPositions = new HashMap<>();
    private int mouseStartX, mouseStartY;
    private double scale;

    public Drag(Positionable item, int mx, int my, double scale)
    {
        originalPositions.put(item, new Pt(item.getX(), item.getY()));
        mouseStartX = mx;
        mouseStartY = my;
        this.scale = scale;
    }
    
    public Drag(Collection<Positionable> items, int mx, int my)
    {
        for(Positionable it: items)
        {
            originalPositions.put(it, new Pt(it.getX(), it.getY()));
        }
        mouseStartX = mx;
        mouseStartY = my;
    }
    
    public void update(int mx, int my)
    {
        double changeX = (mx - mouseStartX) * scale;
        double changeY = (my - mouseStartY) * scale;
        
        for(Map.Entry<Positionable,Pt> entry: originalPositions.entrySet())
        {
            double oldX = entry.getValue().getX();
            double oldY = entry.getValue().getY();
            
            entry.getKey().setPosition(oldX + changeX, oldY + changeY);
        }
    }

}

