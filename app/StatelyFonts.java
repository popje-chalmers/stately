package app;
import machine.*;
import java.awt.*;

public class StatelyFonts
{
    public static final int SZ_LARGE = 24;
    public static final int SZ_SMALL = 11;
    public static final int SZ_REGULAR = 12;
    public static final int SZ_CODE = 13;
    public static final String SANS = Font.SANS_SERIF;
    public static final String MONO = Font.MONOSPACED;
    public Font title        = new Font(SANS, Font.BOLD,  SZ_LARGE);
    public Font issue_cell   = new Font(SANS, Font.PLAIN, SZ_REGULAR);
    public Font signal_cell  = new Font(SANS, Font.PLAIN, SZ_REGULAR);
    public Font text_input   = new Font(SANS, Font.PLAIN, SZ_REGULAR);
    public Font code         = new Font(MONO, Font.PLAIN, SZ_CODE);
    public Font label        = new Font(SANS, Font.PLAIN, SZ_SMALL); // Little Left Labels
    public Font compile_info = new Font(SANS, Font.PLAIN, SZ_SMALL);
    public Font top_bar      = new Font(SANS, Font.PLAIN, SZ_REGULAR);
    public Font checkbox     = label;
}
