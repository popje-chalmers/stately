package app;
import machine.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class SignalCellRenderer extends JLabel implements ListCellRenderer<Signal>
{
    private StatelyApp app;
    
    public SignalCellRenderer(StatelyApp app)
    {
        this.app = app;
        setFont(app.fonts.signal_cell);
        int b = app.measures.signal_cell_border;
        setBorder(new EmptyBorder(b,b,b,b));
        setHorizontalAlignment(SwingConstants.LEFT);
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList<? extends Signal> list, Signal value, int index, boolean isSelected, boolean cellHasFocus)
    {
        Color fg = isSelected ? app.colors.signal_selected : app.colors.signal_not_selected;
        Color bg =
            app.isSignalErroneous(value) ? app.colors.signal_bg_error :
            app.isSignalWarneous(value) ? app.colors.signal_bg_warning :
            app.colors.signal_bg;

        String beforeName = value.isInternal() ? "." : "";
        setText(SignalKind.toSymbol(value.getKind()) + " " + beforeName + value.getName());
        setBackground(bg);
        setForeground(fg);

        return this;
    }
}
