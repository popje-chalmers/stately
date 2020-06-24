package app;
import java.awt.*;
import javax.swing.*;

/* Left-to-right wrapping layout. */
public class LRLayout implements LayoutManager
{
    public void addLayoutComponent(String s, Component c) {}

    public void removeLayoutComponent(Component c) {}

    public Dimension preferredLayoutSize(Container parent)
    {
        return layout(parent, false, false);
    }

    public Dimension minimumLayoutSize(Container parent)
    {
        return layout(parent, false, true);
    }

    public void layoutContainer(Container parent)
    {
        layout(parent, true, false);
    }

    private Dimension layout(Container parent, boolean place, boolean minimum)
    {
        Insets insets = parent.getInsets();
        int width = parent.getWidth();
        int left = insets.left;
        int right = width - insets.right;
        int usableWidth = right - left;

        int y = insets.top;
        int ymax = y;
        int x = left;
        
        for(Component c: parent.getComponents())
        {
            Dimension cd = minimum ? c.getMinimumSize() : c.getPreferredSize();

            int cw = (int)cd.getWidth();
            int ch = (int)cd.getHeight();

            if(x + cw > right && x != left)
            {
                y = ymax;
                x = left;
            }

            cw = Integer.min(cw, right - x);
            
            if(place)
            {
                c.setBounds(x,y,cw, ch);
            }
            
            ymax = Integer.max(ymax, y + ch);
            x += cw;
        }

        int height = ymax + insets.bottom;

        return new Dimension(width, height);
    }
}
