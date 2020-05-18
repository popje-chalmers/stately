import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GUITopBar extends JPanel implements StatelyListener
{
    private StatelyApp app;
    private JLabel infoLabel;
    
    public GUITopBar(StatelyApp app)
    {
        this.app = app;
        app.addStatelyListener(this);

        infoLabel = new JLabel();
        infoLabel.setOpaque(false);
        infoLabel.setForeground(app.colors.top_bar_text);
        infoLabel.setFont(app.fonts.top_bar);

        setLayout(new FlowLayout());
        add(infoLabel);
        update();
    }

    private void update()
    {
        String info = "(no machine)";
        Color color = app.colors.top_bar_background_none;

        Machine m = app.getMachine();
        
        if(m != null)
        {
            info = "Machine status: " + m.getStatus();

            int warnings = 0;
            int errors = 0;
            int issues = 0;
            for(Issue i: m.getIssues())
            {
                issues++;
                if(i.isWarning())
                {
                    warnings++;
                }
                if(i.isError())
                {
                    errors++;
                }
            }
            
            info += " (" + errors + " errors, " + warnings + " warnings)";

            color =
                errors > 0 ? app.colors.top_bar_background_error :
                warnings > 0 ? app.colors.top_bar_background_warning :
                app.colors.top_bar_background_happy;
        }

        setBackground(color);
        infoLabel.setText(info);
        invalidate();
        repaint();
    }

    // StatelyListener

    public void machineModified(MachineEvent e)
    {
        update();
    }

    public void machineSwapped(MachineEvent e)
    {
        update();
    }

    public void selectionModified() {}
}
