import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GUICodeArea extends JPanel
{
    private StatelyApp app;

    private JTextArea codeArea;
    
    public GUICodeArea(StatelyApp app)
    {
        this.app = app;

        setLayout(new BorderLayout());

        codeArea = new JTextArea();
        codeArea.setBackground(app.colors.code_area_background);
        codeArea.setForeground(app.colors.code_area_foreground);
        codeArea.setFont(app.fonts.code);
        GUIHelper.wrapWord(codeArea);

        JScrollPane scroller = GUIHelper.scroll(codeArea, false, true);
        add(scroller, BorderLayout.CENTER);
    }

    public JTextArea getTextArea() { return codeArea; }
}
