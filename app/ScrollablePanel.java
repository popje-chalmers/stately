package app;
import java.awt.*;
import javax.swing.*;

// TODO: fix minimum size
public class ScrollablePanel extends JPanel implements Scrollable
{
    private boolean trackWidth, trackHeight;

    // Which dimensions should match ("track") the parent scroll panel's?
    // E.g. if trackWidth is true, then the width of the panel will always
    // match the width of the parent, so there will be no horizontal scroll.
    public ScrollablePanel(boolean trackWidth, boolean trackHeight)
    {
        this.trackWidth = trackWidth;
        this.trackHeight = trackHeight;
    }
    
    public Dimension getPreferredScrollableViewportSize()
    {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return 1;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return 1;
    }

    public boolean getScrollableTracksViewportWidth()
    {
        return trackWidth;
    }

    public boolean getScrollableTracksViewportHeight()
    {
        return trackHeight;
    }
}
