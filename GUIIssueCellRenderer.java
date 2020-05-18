import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class GUIIssueCellRenderer extends JLabel implements ListCellRenderer<Issue>
{
    private StatelyApp app;
    
    public GUIIssueCellRenderer(StatelyApp app)
    {
        this.app = app;
        setFont(app.fonts.issue_cell);
        int b = app.measures.issue_cell_border;
        setBorder(new EmptyBorder(b,b,b,b));
        setHorizontalAlignment(SwingConstants.LEFT);
        setOpaque(false);
    }

    public Component getListCellRendererComponent(JList<? extends Issue> list, Issue value, int index, boolean isSelected, boolean cellHasFocus)
    {
        Color fg =
            value.isError() ? app.colors.issue_error :
            value.isWarning() ? app.colors.issue_warning :
            app.colors.issue_other;

        setText(value.toString());
        setForeground(fg);

        return this;
    }
}
