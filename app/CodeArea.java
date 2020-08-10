package app;
import machine.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class CodeArea extends JPanel
{
    private StatelyApp app;

    private JTextArea codeArea;
    
    public CodeArea(StatelyApp app)
    {
        this.app = app;

        setLayout(new BorderLayout());

        codeArea = new JTextArea();
        codeArea.setBackground(app.colors.code_area_background);
        codeArea.setForeground(app.colors.code_area_foreground);
        codeArea.setFont(app.fonts.code);
        codeArea.setCaretColor(app.colors.caret_color);
        Helper.wrapWord(codeArea);

        JScrollPane scroller = Helper.scroll(codeArea, false, true);
        add(scroller, BorderLayout.CENTER);
    }

    public JTextArea getTextArea() { return codeArea; }
}
