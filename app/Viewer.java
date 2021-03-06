package app;
import machine.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;


public class Viewer extends JPanel implements SimulationListener, StatelyListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, FocusListener
{
    // General
    public static final int ORIGIN_RADIUS = 10;

    // States and transitions
    public static final int STATE_RADIUS = 25;
    public static final int STATE_MAX_CHARS_PER_LINE = 8;
    public static final Font STATE_NAME_FONT = new Font("Sans", Font.BOLD, 10);
    public static final double TRANSITION_ARROWHEAD_LENGTH = 8;
    public static final double TRANSITION_ARROWHEAD_WIDTH = 6;
    public static final double TRANSITION_OFFSET = STATE_RADIUS + 4;
    
    // Per-state errors
    public static final double STATE_DOT_ANGLE = -0.25 * Math.PI;
    public static final double STATE_DOT_ANGLE_STEP = 10 * Math.PI / 180;
    public static final int STATE_DOT_FROM_STATE = STATE_RADIUS - 1;
    public static final int STATE_DOT_RADIUS = 7;
    
    // Initial state indicator
    public static final double INITIAL_ARROWHEAD_LENGTH = 8;
    public static final double INITIAL_ARROWHEAD_WIDTH = 6;
    public static final double INITIAL_ARROW_LENGTH = 24;
    public static final double INITIAL_ARROW_OFFSET = 4;

    // Cycle errors
    public static final float CYCLE_STROKE_WIDTH = 3;
    public static final int CYCLE_RING_RADIUS = 30;
    public static final double CYCLE_OFFSET = 32;
    
    // Selection
    public static final int SELECTION_RING_RADIUS = 28;
    public static final int STATE_CLICK_RADIUS = STATE_RADIUS;

    // Simulator
    public static final int SIM_STATE_RING_RADIUS = STATE_RADIUS + 5;
    public static final float SIM_STROKE_WIDTH = 2;
    
    // Focus indicator

    public static final boolean SHOW_FOCUS = false;
    public static final int FOCUS_INSET = 0;
    
    private StatelyApp app;
    private Pt viewCenter = new Pt(0,0); // what world coordinates are in the center of view?
    private boolean showOrigin = false;
    
    // View scale
    private int scaleExponent = 0;
    private double scale = 1.0;

    // Selection box
    private boolean selectionBoxGoing = false;
    private boolean selectionBoxSubtract; // versus add
    private Pt selectionBox1, selectionBox2;
    private double selectionBoxXMin, selectionBoxYMin;
    private double selectionBoxXMax, selectionBoxYMax;
    private Set<State> preselected;

    // Things being dragged by the mouse
    private List<Drag> drags = new ArrayList<>();

    // Caching of issue information for faster lookups
    private ArrayList<Issue> stateCycles = new ArrayList<>();
    private Set<State> statesWithErrors = new HashSet<>();
    private Set<State> statesWithWarnings = new HashSet<>();
    
    public Viewer(StatelyApp app)
    {
        this.app = app;
        app.addStatelyListener(this);
        app.getSimulator().addSimulationListener(this);
        setBackground(app.colors.viewer_background);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);

        setFocusable(true);
        if(SHOW_FOCUS)
        {
            addFocusListener(this);
        }
        
    }

    // Misc

    private void generateIssueInfo()
    {
        stateCycles.clear();
        statesWithErrors.clear();
        statesWithWarnings.clear();
        Machine m = app.getMachine();

        if(m == null)
        {
            return;
        }
        
        for(Issue i: m.getIssues())
        {
            if(i.isCyclicWithStates())
            {
                stateCycles.add(i);
            }
            else
            {
                Set<State> dest = i.isWarning() ? statesWithWarnings : statesWithErrors;
                dest.addAll(i.getStates());
            }
        }
    }

    private void processSelectionBox()
    {
        if(!selectionBoxGoing)
        {
            return;
        }

        double xmin = selectionBox1.getX();
        double xmax = selectionBox2.getX();
        if(xmin > xmax)
        {
            double tmp = xmin;
            xmin = xmax;
            xmax = tmp;
        }

        double ymin = selectionBox1.getY();
        double ymax = selectionBox2.getY();
        if(ymin > ymax)
        {
            double tmp = ymin;
            ymin = ymax;
            ymax = tmp;
        }

        selectionBoxXMin = xmin;
        selectionBoxXMax = xmax;
        selectionBoxYMin = ymin;
        selectionBoxYMax = ymax;

        Set<State> selected = new HashSet<>(preselected);
        for(State st: app.getMachine().getStates())
        {
            double x = st.getX();
            double y = st.getY();
            boolean inBox =
                xmin <= x && x <= xmax &&
                ymin <= y && y <= ymax;

            if(inBox)
            {
                if(selectionBoxSubtract)
                {
                    selected.remove(st);
                }
                else
                {
                    selected.add(st);
                }
            }
        }
        app.setSelectedStates(selected);
    }

    public State stateAt(Pt w)
    {
        List<State> states = statesAt(w);
        if(states.isEmpty())
        {
            return null;
        }
        return states.get(states.size()-1);
    }
    
    public List<State> statesAt(Pt w)
    {
        List<State> states = new ArrayList<>();
        
        for(State st: app.getMachine().getStates())
        {
            double dsq = w.distanceSquared(new Pt(st.getX(), st.getY()));
            if(dsq < STATE_CLICK_RADIUS * STATE_CLICK_RADIUS)
            {
                states.add(st);
            }
        }

        return states;
    }

    // SimulationListener

    public void simulationUpdated()
    {
        repaint();
    }
    
    // StatelyListener

    public void machineModified(MachineEvent e)
    {
        generateIssueInfo();
        repaint();
    }

    public void machineSwapped(MachineEvent e)
    {
        generateIssueInfo();
        drags.clear();
        repaint();
    }

    public void selectionModified()
    {
        repaint();
    }

    // MouseListener
    
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    
    public void mousePressed(MouseEvent e)
    {
        grabFocus();
        
        Pt w = componentToWorld(new Pt(e.getX(), e.getY()));

        int button = e.getButton();
        int clicks = e.getClickCount();
        boolean shift = e.isShiftDown();
        boolean control = e.isControlDown();
        boolean alt = e.isAltDown();

        if(button == 1)
        {
            if(clicks == 1)
            {
                if(alt)
                {
                    app.makeSignals();
                }
                else
                {
                    State st = stateAt(w);
                    boolean onState = st != null;
                    boolean onSelectedState = onState && app.isStateSelected(st);

                    if(control && !shift)
                    {
                        app.makeState(w);
                    }
                    else
                    {
                        // Three exhaustive and mutually exclusive possibilities
                        boolean normal   = !shift;
                        boolean add      = shift && !control;
                        boolean subtract = shift && control;

                        if(onState)
                        {
                            // clicked on an existing state

                            if(normal && !onSelectedState)
                            {
                                app.deselectAllStates();
                            }

                            if(normal)
                            {
                                if(!onSelectedState)
                                {
                                    app.deselectAllStates();
                                }

                                app.selectState(st);

                                // begin dragging things
                                for(State st2: app.getSelectedStates())
                                {
                                    drags.add(new Drag(st2, e.getX(), e.getY(), 1/scale));
                                }
                            }
                            else if(add)
                            {
                                app.selectState(st);
                            }
                            else // subtract
                            {
                                app.deselectState(st);
                            }

                            if(!subtract && onState)
                            {
                                app.editState(st, false);
                            }
                        }
                        else 
                        {
                            // clicked in the middle of nowhere

                            if(normal)
                            {
                                app.deselectAllStates();
                            }

                            selectionBox1 = new Pt(w.getX(),w.getY());
                            selectionBox2 = new Pt(w.getX(),w.getY());
                            preselected = app.getSelectedStates();
                            selectionBoxGoing = true;
                            selectionBoxSubtract = subtract;
                            processSelectionBox();
                            drags.add(new Drag(selectionBox2, e.getX(), e.getY(), 1/scale));
                        }
                    }
                }
            }
            else if(clicks == 2)
            {
                State st = stateAt(w);
                app.editState(st, st != null);
            }
        }
        else if(button == 2)
        {
            drags.add(new Drag(viewCenter, e.getX(), e.getY(), -1.0/scale));
        }
        else if(button == 3)
        {
            State st = stateAt(w);
            app.gotoState(st);
        }

        repaint();
    }

    public void mouseReleased(MouseEvent e)
    {
        drags.clear();
        selectionBoxGoing = false;
        repaint();
    }

    // MouseMotionListener

    public void mouseDragged(MouseEvent e)
    {
        for(Drag d: drags)
        {
            d.update(e.getX(), e.getY());
        }
        processSelectionBox();
        repaint();
    }
    
    public void mouseMoved(MouseEvent e) {}

    // MouseWheelListener

    public void mouseWheelMoved(MouseWheelEvent e)
    {
        zoomToward(-e.getWheelRotation(), e.getX(), e.getY());
        repaint();
    }

    // KeyListener

    public void keyPressed(KeyEvent e)
    {
        int k = e.getKeyCode();
        boolean shift = e.isShiftDown();
        boolean control = e.isControlDown();
        
        if(control && k == KeyEvent.VK_DELETE)
        {
            Machine m = app.getMachine();
            if(m != null)
            {
                app.removeStates(app.getSelectedStates());
            }
        }
        else if(k == KeyEvent.VK_A && !shift && !control)
        {
            Machine m = app.getMachine();
            if(m != null)
            {
                if(app.getSelectedStates().size() != m.getStates().size())
                {
                    app.selectAllStates();
                }
                else
                {
                    app.deselectAllStates();
                }
            }
        }
        else if(k == KeyEvent.VK_O && !shift && !control)
        {
            showOrigin = !showOrigin;
            repaint();
        }
        else if(k == KeyEvent.VK_0)
        {
            resetView();
            repaint();
        }
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    // FocusListener

    public void focusGained(FocusEvent e) { repaint(); }
    public void focusLost(FocusEvent e) { repaint(); }
    
    // Graphics

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        if(SHOW_FOCUS && hasFocus())
        {
            g.setColor(app.colors.viewer_focus);
            g.drawRect(FOCUS_INSET,
                       FOCUS_INSET,
                       getWidth() - 1 - 2 * FOCUS_INSET,
                       getHeight() - 1 - 2 * FOCUS_INSET);
        }
        
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.translate(getWidth()/2, getHeight()/2);
        g2d.scale(scale, scale);
        g2d.translate(-viewCenter.getX(), -viewCenter.getY());
        Stroke regularStroke = g2d.getStroke();

        if(showOrigin)
        {
            g.setColor(Color.WHITE);
            g.drawLine(-ORIGIN_RADIUS,0,ORIGIN_RADIUS,0);
            g.drawLine(0,-ORIGIN_RADIUS,0,ORIGIN_RADIUS);
        }

        Machine m = app.getMachine();

        if(m == null)
        {
            return;
        }
        
        for(State st: m.getStates())
        {
            Set<State> sts = m.accessCoarseGraph(st);
            if(sts != null)
            {
                for(State st2: sts)
                {
                    if(st2 != st)
                    {
                        drawTransition(g, st, st2);
                    }
                }
            }
        }

        State initial = m.getInitialState();
        for(State st: m.getStates())
        {
            drawState(g,st, st == initial);
        }

        // Errors
        g2d.setStroke(new BasicStroke(CYCLE_STROKE_WIDTH));
        for(Issue i: stateCycles)
        {
            Color c = i.isWarning() ?
                app.colors.state_cycle_warning :
                app.colors.state_cycle_error;
            g.setColor(c);
            drawStateCycle(g, i.getStates());
        }

        // Simulator
        if(m.getStatus() == MachineStatus.HAPPY)
        {
            g2d.setStroke(new BasicStroke(SIM_STROKE_WIDTH));
            
            Simulator sim = app.getSimulator();
            State cur = sim.getState();
            State next = sim.getNextState();
            ModelTransition trans = sim.getTransition();
            
            if(trans != null)
            {
                g.setColor(app.colors.sim_path);
                
                Pt prev = null;
                for(State st: trans.getPath())
                {
                    Pt p = new Pt(st.getX(), st.getY());
                    if(prev != null)
                    {   
                        drawArrow(g, prev, p, TRANSITION_OFFSET, TRANSITION_OFFSET, TRANSITION_ARROWHEAD_WIDTH, TRANSITION_ARROWHEAD_LENGTH);
                    }

                    prev = p;
                }

                for(State st: trans.getPath())
                {
                    if(st != cur && st != next)
                    {   
                        drawCircle(g, (int)st.getX(), (int)st.getY(), SIM_STATE_RING_RADIUS);
                    }
                }
            }

            
            if(cur != null)
            {
                g.setColor(cur == next ? app.colors.sim_stay : app.colors.sim_state);
                drawCircle(g, (int)cur.getX(), (int)cur.getY(), SIM_STATE_RING_RADIUS);
            }

            if(next != null && next != cur)
            {
                g.setColor(app.colors.sim_next_state);
                drawCircle(g, (int)next.getX(), (int)next.getY(), SIM_STATE_RING_RADIUS);
            }
        }

        g2d.setStroke(regularStroke);
        if(selectionBoxGoing)
        {
            int sx = (int)selectionBoxXMin;
            int sy = (int)selectionBoxYMin;
            int sw = (int)(selectionBoxXMax - selectionBoxXMin);
            int sh = (int)(selectionBoxYMax - selectionBoxYMin);

            if(app.colors.selection_box_fill != null)
            {
                g.setColor(app.colors.selection_box_fill);
                g.fillRect(sx, sy, sw + 1, sh + 1);
            }
            
            g.setColor(app.colors.selection_box_outline);
            g.drawRect(sx, sy, sw, sh);
        }
    }

    public void drawState(Graphics g, State st, boolean isInitial)
    {
        int x = (int)st.getX();
        int y = (int)st.getY();

        Color color = st.isVirtual() ?
            app.colors.state_virtual_color :
            app.colors.state_color;
        g.setColor(color);
        fillCircle(g, x, y, STATE_RADIUS);

        if(app.isStateSelected(st))
        {
            drawCircle(g, x, y, SELECTION_RING_RADIUS);
        }

        g.setColor(app.colors.state_text);
        g.setFont(STATE_NAME_FONT);
        drawCenteredLines(g, linebreak(st.getName(), STATE_MAX_CHARS_PER_LINE), x, y);

        double angle = STATE_DOT_ANGLE;

        if(statesWithWarnings.contains(st))
        {
            g.setColor(app.colors.state_warning);
            fillStateDot(g, x, y, angle);
            angle += STATE_DOT_ANGLE_STEP;
        }
        if(statesWithErrors.contains(st))
        {
            g.setColor(app.colors.state_error);
            fillStateDot(g, x, y, angle);
            angle += STATE_DOT_ANGLE_STEP;
        }

        if(isInitial)
        {
            g.setColor(app.colors.initial_arrow);
            Pt to = new Pt(st.getX(), st.getY() - STATE_RADIUS - INITIAL_ARROW_OFFSET);
            Pt from = new Pt(to.getX(), to.getY() - INITIAL_ARROW_LENGTH);
            drawArrow(g, from, to, 0, 0, INITIAL_ARROWHEAD_WIDTH, INITIAL_ARROWHEAD_LENGTH);
        }
    }

    public void fillStateDot(Graphics g, int sx, int sy, double angle)
    {
        int r = STATE_DOT_FROM_STATE;
        int x = sx + (int)Math.round(r * Math.cos(angle));
        int y = sy + (int)Math.round(r * Math.sin(angle));
        fillCircle(g, x, y, STATE_DOT_RADIUS);
    }

    public void drawTransition(Graphics g, State from, State to)
    {
        Pt fromPt = new Pt(from.getX(), from.getY());
        Pt toPt = new Pt(to.getX(), to.getY());

        g.setColor(app.colors.transition_arrow);
        drawArrow(g, fromPt, toPt, TRANSITION_OFFSET, TRANSITION_OFFSET, TRANSITION_ARROWHEAD_WIDTH, TRANSITION_ARROWHEAD_LENGTH);
    }

    public void drawStateCycle(Graphics g, List<State> cycle)
    {
        if(cycle.isEmpty())
        {
            return;
        }

        if(cycle.size() > 1)
        {
            State previous = cycle.get(cycle.size() - 1);
            Pt previousPt = new Pt(previous.getX(), previous.getY());
            for(State st: cycle)
            {
                if(st != previous)
                {
                    Pt p = new Pt(st.getX(), st.getY());
                    drawOffsetLine(g, previousPt, p, CYCLE_OFFSET, CYCLE_OFFSET);
                    previousPt = p;
                    previous = st;    
                }
                
            }
        }

        for(State st: cycle)
        {
            drawCircle(g, (int)st.getX(), (int)st.getY(), CYCLE_RING_RADIUS);
        }
    }

    public void drawOffsetLine(Graphics g, Pt from, Pt to, double offsetFrom, double offsetTo)
    {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double d = Math.sqrt(dx * dx + dy * dy);
        
        double ux = dx / d;
        double uy = dy / d;

        int x1 = (int)(from.getX() + ux * offsetFrom);
        int y1 = (int)(from.getY() + uy * offsetFrom);
        int x2 = (int)(to.getX() - ux * offsetFrom);
        int y2 = (int)(to.getY() - uy * offsetFrom);

        g.drawLine(x1,y1,x2,y2);
    }

    public void drawArrow(Graphics g, Pt from, Pt to, double offsetFrom, double offsetTo, double width, double length)
    {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double d = Math.sqrt(dx * dx + dy * dy);
        double hw = width * 0.5;
        
        double ux = dx / d;
        double uy = dy / d;
        double vx = -uy;
        double vy = ux;

        int x1 = (int)(from.getX() + ux * offsetFrom);
        int y1 = (int)(from.getY() + uy * offsetFrom);
        int x2 = (int)(to.getX() - ux * offsetFrom);
        int y2 = (int)(to.getY() - uy * offsetFrom);

        int x3 = (int)(x2 - ux * length - vx * hw);
        int y3 = (int)(y2 - uy * length - vy * hw);
        int x4 = (int)(x2 - ux * length + vx * hw);
        int y4 = (int)(y2 - uy * length + vy * hw);

        g.drawLine(x1,y1,x2,y2);
        g.drawLine(x2,y2,x3,y3);
        g.drawLine(x2,y2,x4,y4);
    }

    public void drawCircle(Graphics g, int cx, int cy, int r)
    {
        g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
    }
    
    public void fillCircle(Graphics g, int cx, int cy, int r)
    {
        g.fillOval(cx - r, cy - r, 2 * r + 1, 2 * r + 1);
    }

    public void drawCenteredLine(Graphics g, String s, int x, int y)
    {
        FontMetrics fm = g.getFontMetrics(g.getFont());
        int a = fm.getAscent();
        int w = fm.stringWidth(s);
        g.drawString(s, x - w/2, y + a/2);
    }
    
    public void drawCenteredLines(Graphics g, List<String> lines, int x, int y)
    {
        FontMetrics fm = g.getFontMetrics(g.getFont());
        int a = fm.getAscent();
        int h = fm.getHeight();

        int n = lines.size();
        int y0 = (int)Math.round(y - h * (n - 1) / 2);
        for(int i = 0; i < n; i++)
        {
            int yi = y0 + h * i;
            drawCenteredLine(g, lines.get(i), x, yi);
        }
    }

    public List<String> linebreak(String s, int maxCharsPerLine)
    {
        List<String> lines = new ArrayList<>();

        while(s.length() > maxCharsPerLine)
        {
            lines.add(s.substring(0, maxCharsPerLine));
            s = s.substring(maxCharsPerLine);
        }
        
        lines.add(s);
        return lines;
    }

    // Coordinate transformations and view zooming

    private void resetView()
    {
        scaleExponent = 0;
        scale = 1.0;
        viewCenter = new Pt(0,0);
    }
    
    private void zoomToward(int zoom, int x, int y)
    {
        Pt pComp = new Pt(x,y);
        Pt pWorld = componentToWorld(pComp);
            
        scaleExponent += zoom;
        scale = Math.pow(2.0, 0.5 * scaleExponent);

        Pt now = componentToWorld(pComp);
        double differenceX = pWorld.getX() - now.getX();
        double differenceY = pWorld.getY() - now.getY();
        viewCenter.setPosition(viewCenter.getX() + differenceX,
                               viewCenter.getY() + differenceY);
    }
    
    private Pt componentToWorld(Pt p)
    {
        double x = (p.getX() - 0.5 * getWidth()) / scale + viewCenter.getX();
        double y = (p.getY() - 0.5 * getHeight()) / scale + viewCenter.getY();
        return new Pt((int)Math.round(x), (int)Math.round(y));
    }

    private Pt worldToComponent(Pt p)
    {
        double x = (p.getX() - viewCenter.getX()) * scale + 0.5 * getWidth();
        double y = (p.getY() - viewCenter.getY()) * scale + 0.5 * getHeight();
        return new Pt((int)Math.round(x), (int)Math.round(y));
    }
}
