
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalTime;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.*;

public class StatelyApp extends JFrame implements ActionListener
{
    public static final String EXTENSION = "fsm";
    public static final String TRANSFORM_TMP = "/tmp/stately_tmp";
    public static final FileNameExtensionFilter FILE_EXTENSION_FILTER = new FileNameExtensionFilter("FSM files", EXTENSION);
    
    public StatelyConfig config;
    public StatelyColors colors = new StatelyColors();
    public StatelyFonts fonts = new StatelyFonts();
    public StatelyMeasures measures = new StatelyMeasures();

    private GUIViewer viewer;
    private GUIMachineEditor machineEditor;
    private SelectionManager<State> selectedStates = new SelectionManager<>();

    private Machine machine;
    private ArrayList<StatelyListener> listeners = new ArrayList<>();

    private JMenuItem menuOpen, menuSave, menuSaveAs, menuQuit;
    private JMenuItem menuHelp;
    private JMenuItem menuTransform;
    
    private JMenuItem menuDebugMakeSignals, menuDebugPrintMachine, menuDebugPrintModel, menuDebugPrintTL;

    private File lastSaveFile;
    private LocalTime lastSaveTime;
    
    public StatelyApp()
    {
        loadConfig();
        
        setSize(config.width, config.height);
        setLocation(config.x, config.y);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.setBackground(colors.background);

        machine = new Machine("MyFSM");
        machine.analyze();
        
        viewer = new GUIViewer(this);
        machineEditor = new GUIMachineEditor(this);
        
        JSplitPane divide = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                           viewer, machineEditor);
        
        divide.setDividerLocation(config.width - config.machineEditorWidth);
        divide.setResizeWeight(1.0f);
        c.add(divide, BorderLayout.CENTER);

        c.add(new GUITopBar(this), BorderLayout.NORTH);

        menuOpen = new JMenuItem("Open FSM...");
        menuOpen.addActionListener(this);
        menuSave = new JMenuItem("Save FSM...");
        menuSave.addActionListener(this);
        menuSaveAs = new JMenuItem("Save FSM as...");
        menuSaveAs.addActionListener(this);
        menuQuit = new JMenuItem("Quit");
        menuQuit.addActionListener(this);

        menuTransform = new JMenuItem("Edit with external program");
        menuTransform.addActionListener(this);

        menuDebugMakeSignals = new JMenuItem("Make some signals");
        menuDebugMakeSignals.addActionListener(this);
        menuDebugPrintMachine = new JMenuItem("Print machine to terminal");
        menuDebugPrintMachine.addActionListener(this);
        menuDebugPrintModel = new JMenuItem("Print model to terminal");
        menuDebugPrintModel.addActionListener(this);
        menuDebugPrintTL = new JMenuItem("Print TL to terminal");
        menuDebugPrintTL.addActionListener(this);

        menuHelp = new JMenuItem("Help");
        menuHelp.addActionListener(this);

        

        JMenu fileMenu = new JMenu("File");
        fileMenu.add(menuOpen);
        fileMenu.addSeparator();
        fileMenu.add(menuSave);
        fileMenu.add(menuSaveAs);
        fileMenu.addSeparator();
        fileMenu.add(menuQuit);

        JMenu transformMenu = new JMenu("Transform");
        transformMenu.add(menuTransform);
        
        JMenu debugMenu = new JMenu("Debug");
        debugMenu.add(menuDebugMakeSignals);
        debugMenu.addSeparator();
        debugMenu.add(menuDebugPrintMachine);
        debugMenu.add(menuDebugPrintModel);
        debugMenu.add(menuDebugPrintTL);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(menuHelp);
        
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(transformMenu);
        menuBar.add(debugMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        fixTitle();
        setVisible(true);
    }

    private void fixTitle()
    {
        String title = "Stately";
        if(machine != null)
        {
            title += " - " + machine.getName();
        }
        if(lastSaveFile != null)
        {
            title += " - " + lastSaveFile.getName();
        }
        if(lastSaveTime != null)
        {
            title += " - last saved " +
                String.format("%02d:%02d:%02d",
                              lastSaveTime.getHour(),
                              lastSaveTime.getMinute(),
                              lastSaveTime.getSecond());
        }
        setTitle(title);
    }

    public void addStatelyListener(StatelyListener l) { listeners.add(l); }
    
    public Machine getMachine() { return machine; }
    public GUIMachineEditor getMachineEditor() { return machineEditor; }

    // Call this after non-cosmetic properties of the machine have changed.
    public void machineModified(Object source)
    {
        fixSelection();
        
        if(machine == null)
        {
            return;
        }

        machine.analyze();

        MachineEvent e = new MachineEvent(source);
        for(StatelyListener l: listeners)
        {
            l.machineModified(e);
        }

        fixTitle();
    }

    // Change to a new machine.
    public void setMachine(Machine m)
    {
        machine = m;
        machine.analyze();
        selectedStates.clear();
        MachineEvent e = new MachineEvent(this, m);
        for(StatelyListener l: listeners)
        {
            l.machineSwapped(e);
        }
        fixTitle();
        machineModified(this);
    }



    

    // Selection services


    public void deselectAllStates()
    {
        selectedStates.clear();
        selectionModified();
    }

    public void deselectState(State st)
    {
        selectedStates.deselect(st);
        selectionModified();
    }

    public Set<State> getSelectedStates()
    {
        return selectedStates.getSelected();
    }
    
    public boolean isStateSelected(State st)
    {
        return selectedStates.isSelected(st);
    }

    public void selectState(State st)
    {
        selectedStates.select(st);
        selectionModified();
    }

    public void setSelectedStates(Set<State> sel)
    {
        selectedStates.clear();
        for(State st: sel)
        {
            selectedStates.select(st);
        }
        selectionModified();
    }

    public void toggleSelectState(State st)
    {
        selectedStates.toggle(st);
        selectionModified();
    }
    

    // Misc services
    
    public void editState(State st)
    {
        machineEditor.goEditState(st);
    }

    public void makeState(Pt loc)
    {
        String name = JOptionPane.showInputDialog("Name?");
        if(name == null)
        {
            return;
        }
        State st = new State(name, machine);
        st.setPosition(loc.getX(), loc.getY());
        machine.addState(st);

        editState(st);
        machineModified(this);
       
        selectedStates.clear();
        selectedStates.select(st);
        selectionModified();
    }

    public void removeStates(Set<State> states)
    {
        if(machine == null)
        {
            return;
        }
        
        for(State st: states)
        {
            machine.removeState(st);
        }

        machineModified(this);
    }
    



    // Private

    private void fixSelection()
    {
        Set<State> selected = selectedStates.getSelected();
        Set<State> valid = new HashSet<>(machine.getStates());

        for(State st: selected)
        {
            if(!valid.contains(st))
            {
                selectedStates.deselect(st);
            }
        }
    }

    private void selectionModified()
    {
        fixSelection();
        for(StatelyListener l: listeners)
        {
            l.selectionModified();
        }
    }
    
    private void loadConfig()
    {
        // TODO
        config = new StatelyConfig();
    }

    private void saveConfig()
    {
        // TODO
    }

    // ActionListener

    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();

        if(source == menuOpen)
        {
            openFile();
        }
        else if(source == menuSave)
        {
            saveFile();
        }
        else if(source == menuSaveAs)
        {
            saveAsFile();
        }
        else if(source == menuQuit)
        {
            System.exit(0);
        }
        else if(source == menuTransform)
        {
            transform();
        }
        else if(source == menuHelp)
        {
            help();
        }
        else if(source == menuDebugMakeSignals)
        {
            if(machine != null)
            {
                System.out.println("\nMaking some signals...");

                String[] inputs = new String[]{"mem_ack","alloc_ack","calculate_ack","door_is_open","window_is_open","user_is_home","keycode_entered"};
                String[] statewise = new String[]{"mem_req","alloc_req","calculate_req","led0","led1","alarm","unlock_door","lock_door","send_sms"};
                    
                for(String name: inputs)
                {
                    if(machine.findSignal(name) == null)
                    {
                        machine.addSignal(new Signal(name, SignalKind.INPUT, machine));
                    }
                }
                for(String name: statewise)
                {
                    if(machine.findSignal(name) == null)
                    {
                        machine.addSignal(new Signal(name, SignalKind.STATEWISE, machine));
                    }
                }
                for(int i = 0 ; i < 3; i++)
                {
                    Signal s = new Signal("in" + i, SignalKind.INPUT, machine);
                    if(machine.findSignal(s.getName()) == null)
                    {
                        machine.addSignal(s);
                    }
                }
                for(int i = 0 ; i < 3; i++)
                {
                    Signal s = new Signal("out" + i, SignalKind.STATEWISE, machine);
                    if(machine.findSignal(s.getName()) == null)
                    {   
                        machine.addSignal(s);
                    }
                }
                for(Signal s: machine.getSignals())
                {
                    System.out.println(s.getName() + " " + s.getKind());
                }
                machineModified(this);
            }
        }
        else if(source == menuDebugPrintMachine)
        {
            if(machine != null)
            {
                System.out.println();
                System.out.println(machine.toSExp());
                System.out.println();
            }
        }
        else if(source == menuDebugPrintModel)
        {
            if(machine != null)
            {
                if(machine.getStatus() == MachineStatus.HAPPY)
                {
                    System.out.println();
                    System.out.println(new Model(machine));
                    System.out.println();
                }
                else
                {
                    System.out.println("\nMachine not happy (outstanding errors), cannot model.\n");
                }
            }
        }
        else if(source == menuDebugPrintTL)
        {
            if(machine != null)
            {
                java.util.List<SExp> dbg = Transformatron.dumpMachine(machine, null);
                System.out.println();
                for(SExp exp: dbg)
                {
                    System.out.println(exp.toString());
                }
                System.out.println();

                setMachine(new Machine(""));
                Transformatron t = new Transformatron(machine);
                for(SExp exp: dbg)
                {
                    t.interpret(exp);
                }
                machineModified(this);
            }
        }
    }

    // Actions driven by GUI

    private void help()
    {
        String message =
            "ctrl-click: add state\n" +
            "ctrl + Del: delete selected state(s)\n" +
            "left-click: select one state\n" +
            "shift + left-click: toggle to selection\n" +
            "shift + left-drag: add rectangle to selection\n" +
            "shift + ctrl + left-drag: subtract rectangle from selection\n" +
            "middle-drag: pan view\n" +
            "scroll wheel: zoom";

        JOptionPane.showMessageDialog(this, message);
    }

    private void openFile()
    {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileFilter(FILE_EXTENSION_FILTER);
        int res = jfc.showOpenDialog(this);
        if(res == JFileChooser.APPROVE_OPTION)
        {
            File f = jfc.getSelectedFile();
            if(readFromFile(f))
            {
                lastSaveFile = f;
                lastSaveTime = null;
                fixTitle();
            }
        }
    }

    private void saveFile()
    {
        if(machine == null)
        {
            return;
        }
        
        if(lastSaveFile == null)
        {
            saveAsFile();
            return;
        }

        saveToFile(lastSaveFile);
    }

    private void saveAsFile()
    {
        if(machine == null)
        {
            return;
        }
        
        JFileChooser jfc = new JFileChooser();
        jfc.setFileFilter(FILE_EXTENSION_FILTER);
        int res = jfc.showSaveDialog(this);
        if(res == JFileChooser.APPROVE_OPTION)
        {
            File chosen = jfc.getSelectedFile();
            String name = chosen.getName();
            if(!name.endsWith("." + EXTENSION))
            {
                name += "." + EXTENSION;
            }
            File chosenExt = new File(chosen.getParent(), name);
            saveToFile(chosenExt);
        }
    }

    private void saveToFile(File f)
    {
        lastSaveFile = f;
        if(writeToFile(f))
        {
            lastSaveTime = LocalTime.now();
        }
        fixTitle();
    }

    private boolean writeToFile(File f)
    {
        try
        {
            writeFile(f, machine.toSExp().toString());
            System.out.println("Wrote successfully to: " + f.getPath());
            return true;
        }
        catch(Throwable t)
        {
            t.printStackTrace();
            JOptionPane.showMessageDialog(this, t.getMessage());
            return false;
        }
    }

    private boolean readFromFile(File f)
    {
        try
        {
            String s = readFile(f);
            Tokenizer t = new Tokenizer(s);
            java.util.List<Token> tokens = t.tokenize();
            SExpParser p = new SExpParser(tokens);
            SExp exp = p.parse();
            if(!tokens.isEmpty())
            {
                System.out.println("Warning - stray tokens left in file...");
            }
            Machine m = Machine.fromSExp(exp);
            System.out.println("Read successfully from: " + f.getPath());
            setMachine(m);
            return true;
        }
        catch(Throwable t)
        {
            t.printStackTrace();
            JOptionPane.showMessageDialog(this, t.getMessage());
            return false;
        }
    }

    private boolean transform()
    {
        File f = new File(TRANSFORM_TMP);
        boolean first = true;
        
        try
        {
            transform_out(f, machine);
            
            while(true)
            {
                try
                {
                    String question = "Read back in?";
                    String prefix = first ? "Wrote to " + f.getPath() + ". " : "";
                    first = false;
                    String message = prefix + question;
                    int result = JOptionPane.showConfirmDialog(this, message, "Transform", JOptionPane.OK_CANCEL_OPTION);

                    if(result == JOptionPane.YES_OPTION)
                    {
                        Machine m = transform_in(f);
                        setMachine(m);
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                catch(Throwable t)
                {
                    JOptionPane.showMessageDialog(this, t.getMessage());
                }
            }
        }
        catch(Throwable t)
        {
            t.printStackTrace();
            JOptionPane.showMessageDialog(this, t.getMessage());
        }

        return false;
    }

    // Static methods

    private static void transform_out(File f, Machine m) throws IOException
    {
        String out = "";
        out += "-- Cheatsheet:\n";
        out += "-- (name \"fsmname\")\n";
        out += "-- (translate <deltaX> <deltaY>)\n";
        out += "-- (signal \"name\" <kind> \"description\" \"expression code\")\n";
        out += "--   n.b. <kind> is in {input, expression, statewise}\n";
        out += "-- (state \"name\" \"description\" <isvirtual> <x> <y> \"state code\")\n";
        out += "\n";
        SExp sep = SExp.mkList(new ArrayList<>());
        java.util.List<SExp> exps = Transformatron.dumpMachine(m, sep);
        for(SExp exp: exps)
        {
            if(exp == sep)
            {
                out += "\n";
            }
            else
            {
                out += exp.toString() + "\n";
            }
        }
        writeFile(f, out);
    }
    
    private static Machine transform_in(File f) throws IOException
    {
        Machine m = new Machine("Untitled");
        Transformatron t = new Transformatron(m);
        String contents = readFile(f);
        Tokenizer tz = new Tokenizer(contents);
        java.util.List<Token> tokens = tz.tokenize();
        SExpParser p =new SExpParser(tokens);
        while(!tokens.isEmpty())
        {
            if(tokens.get(0).kind == TokenKind.COMMENT)
            {
                tokens.remove(0);
            }
            else
            {
                SExp exp = p.parse();
                t.interpret(exp);
            }
        }
        return m;
    }
    

    // Adds a \n
    private static void writeFile(File f, String s) throws IOException
    {
        PrintWriter out = new PrintWriter(new FileWriter(f), true);
        out.println(s);
        out.close();
    }

    // Might add a \n
    private static String readFile(File f) throws IOException
    {
        String text = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        String line = br.readLine();
        while(line != null)
        {
            text += line + "\n";
            line = br.readLine();
        }
        br.close();
        return text;
    }
}
