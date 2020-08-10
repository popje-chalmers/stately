package app;
import machine.*;
import java.awt.*;
import javax.swing.*;

public class Helper
{
    public static JButton button(StatelyApp app, String text)
    {
        JButton button = new JButton(text);
        return button;
    }

    public static JCheckBox checkBox(StatelyApp app, String text)
    {
        JCheckBox box = new JCheckBox(text);
        box.setBackground(app.colors.checkbox_background);
        box.setForeground(app.colors.checkbox_foreground);
        box.setFont(app.fonts.checkbox);
        return box;
    }
    
    public static JLabel makeLLL(StatelyApp app, String text)
    {
        JLabel label = new JLabel(text);
        
        label.setForeground(app.colors.label_text);
        label.setOpaque(false);
        label.setFont(app.fonts.label);
        return label;
    }
    
    public static JLabel makeTitle(StatelyApp app, String title, Color fg, Color bg)
    {
        JLabel label = new JLabel(title);
        label.setFont(app.fonts.title);
        label.setForeground(fg);
        label.setBackground(bg);
        if(bg == null)
        {
            label.setOpaque(false);
        }
        return label;
    }

    public static JTextArea textArea(StatelyApp app)
    {
        JTextArea area = new JTextArea();
        area.setBackground(app.colors.text_area_background);
        area.setForeground(app.colors.text_area_foreground);
        area.setFont(app.fonts.text_input);
        area.setCaretColor(app.colors.caret_color);
        return area;
    }

    
    public static JTextField textField(StatelyApp app)
    {
        JTextField field = new JTextField();
        field.setBackground(app.colors.text_field_background);
        field.setForeground(app.colors.text_field_foreground);
        field.setFont(app.fonts.text_input);
        field.setCaretColor(app.colors.caret_color);
        return field;
    }

    public static JPanel transparentPanel()
    {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }
    
    public static void wrapWord(JTextArea area)
    {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
    }

    public static JScrollPane scroll(JComponent comp, boolean horizontal, boolean vertical)
    {
        JScrollPane pane = new JScrollPane(
            comp,
            (vertical ?
             ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED :
             ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER),
            (horizontal ?
             ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED :
             ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
        return pane;
    }
}
