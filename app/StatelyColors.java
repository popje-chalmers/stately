package app;
import machine.*;
import java.awt.Color;

public class StatelyColors
{
    public Color background = Color.GRAY;
    public Color viewer_background = Color.BLACK;
    public Color viewer_focus = new Color(127,127,255);
    public Color machine_editor_background = Color.LIGHT_GRAY;
    public Color editor_background = Color.DARK_GRAY;
    public Color editor_foreground = Color.WHITE;
    
    public Color title = Color.WHITE;

    public Color state_color = new Color(200,200,200);
    public Color state_virtual_color = new Color(64, 64, 64);
    public Color state_text = Color.BLACK;
    public Color state_warning = new Color(224,168,0);
    public Color state_error = new Color(168,0,0);
    public Color state_cycle_warning = new Color(255,200,0,127);
    public Color state_cycle_error = new Color(200,0,0,127);

    public Color transition_arrow = state_color;
    public Color initial_arrow = state_color;

    public Color selection_box_outline = Color.WHITE;
    public Color selection_box_fill = new Color(255,255,255,32);
    
    public Color issue_list_background = Color.BLACK;
    public Color issue_none = Color.GRAY;
    public Color issue_warning = Color.YELLOW;
    public Color issue_error = Color.RED;
    public Color issue_other = Color.BLUE;

    public Color signal_list_background = Color.BLACK;
    public Color signal_selected = new Color(127,127,255);
    public Color signal_not_selected = Color.WHITE;
    public Color signal_bg = signal_list_background;
    public Color signal_bg_error = new Color(127,0,0);

    public Color text_field_background = Color.BLACK;
    public Color text_field_foreground = Color.WHITE;

    public Color text_area_background = Color.BLACK;
    public Color text_area_foreground = Color.WHITE;

    public Color code_area_background = Color.BLACK;
    public Color code_area_foreground = new Color(127,255,127);

    public Color label_text = Color.WHITE;

    public Color compile_info_background = Color.BLACK;
    public Color compile_info_none = Color.GRAY;
    public Color compile_info_compiled = new Color(127,255,127);
    public Color compile_info_error = Color.RED;

    public Color top_bar_text = Color.WHITE;
    public Color top_bar_background_none = Color.DARK_GRAY;
    public Color top_bar_background_error = new Color(127, 0, 0);
    public Color top_bar_background_warning = new Color(127, 127, 0);
    public Color top_bar_background_happy = new Color(0, 127, 0);

    public Color checkbox_background = Color.BLACK;
    public Color checkbox_foreground = Color.WHITE;
}
