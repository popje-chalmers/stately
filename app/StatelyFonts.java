package app;
import machine.*;
import java.awt.*;

public class StatelyFonts
{
    public static final int LARGE = 24;
    public static final int SMALL = 11;
    public static final int REGULAR = 12;
    public Font title = new Font("Sans", Font.BOLD, LARGE);
    public Font issue_cell = new Font("Sans", Font.PLAIN, REGULAR);
    public Font signal_cell = new Font("Sans", Font.PLAIN, REGULAR);
    public Font text_input = new Font("Sans", Font.PLAIN, REGULAR);
    public Font code = new Font("Courier", Font.PLAIN, REGULAR);
    public Font label = new Font("Sans", Font.PLAIN, SMALL); // Little Left Labels
    public Font compile_info = new Font("Sans", Font.PLAIN, SMALL);
    public Font top_bar = new Font("Sans", Font.PLAIN, REGULAR);
    public Font checkbox = label;
}
