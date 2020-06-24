package app;
import machine.*;

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
    public static final String FL_EXTENSION = "fl";
    public static final String TRANSFORM_TMP = "/tmp/stately_tmp";
    public static final FileNameExtensionFilter FILE_EXTENSION_FILTER = new FileNameExtensionFilter("FSM files", EXTENSION);
    public static final double STATE_CREATE_DX = 80;
    public static final double STATE_CREATE_DY = 0;
    public static final int STATE_HISTORY_LIMIT = 100;
    
    public StatelyConfig config;
    public StatelyColors colors = new StatelyColors();
    public StatelyFonts fonts = new StatelyFonts();
    public StatelyMeasures measures = new StatelyMeasures();

    private Inputter inputter;
    private Viewer viewer;
    private MachineEditor machineEditor;
    private SelectionManager<State> selectedStates = new SelectionManager<>();
    private Set<Signal> erroneousSignals = new HashSet<>();
    private Set<Signal> warneousSignals = new HashSet<>();
    private Simulator simulator;
    
    private Machine machine;
    private ArrayList<StatelyListener> listeners = new ArrayList<>();

    private JMenuItem menuNew, menuOpen, menuSave, menuSaveAs, menuFLExport, menuQuit;
    private JMenuItem menuHelp;
    private JMenuItem menuRename, menuEditExternal;

    private JMenuItem menuSimForward, menuSimBackward, menuSimPrintRecord;
    
    private JMenuItem menuDebugMakeSignals, menuDebugPrintMachine, menuDebugPrintModel, menuDebugPrintTL, menuDebugPrintFL;

    private File lastSaveFile;
    private LocalTime lastSaveTime;
    private LocalTime lastExportTime;

    private Historian historian;
    private JLabel historyStepIndicator;
    
    public StatelyApp()
    {
        loadConfig();
        
        setSize(config.win_width, config.win_height);
        setLocation(config.win_x, config.win_y);
        if(config.win_maximize)
        {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.setBackground(colors.background);

        simulator = new Simulator(null);
        newFSM("MyFSM");

        inputter = new Inputter(this);
        simulator.setInputSource(inputter);
        historyStepIndicator = Helper.makeLLL(this, "");
        fixHistoryStepIndicator();
        viewer = new Viewer(this);
        machineEditor = new MachineEditor(this);

        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(colors.background);
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(historyStepIndicator, BorderLayout.NORTH);
        leftPanel.add(viewer, BorderLayout.CENTER);
        JSplitPane leftDivide = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                               leftPanel, inputter);
        leftDivide.setResizeWeight(1.0f);
        JSplitPane divide = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                           leftDivide, machineEditor);
        divide.setResizeWeight(1.0f);
        c.add(divide, BorderLayout.CENTER);
        c.add(new TopBar(this), BorderLayout.NORTH);

        menuNew = new JMenuItem("New FSM...");
        menuNew.addActionListener(this);
        menuNew.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed N"));
        menuOpen = new JMenuItem("Open FSM...");
        menuOpen.addActionListener(this);
        menuOpen.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed O"));
        menuSave = new JMenuItem("Save FSM...");
        menuSave.addActionListener(this);
        menuSave.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed S"));
        menuSaveAs = new JMenuItem("Save FSM as...");
        menuSaveAs.addActionListener(this);
        menuSaveAs.setAccelerator(KeyStroke.getKeyStroke("ctrl shift pressed S"));
        menuFLExport = new JMenuItem("Export FL");
        menuFLExport.addActionListener(this);
        menuFLExport.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed E"));
        menuQuit = new JMenuItem("Quit");
        menuQuit.addActionListener(this);
        menuQuit.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed Q"));

        menuRename = new JMenuItem("Rename FSM");
        menuRename.addActionListener(this);
        menuEditExternal = new JMenuItem("Edit with external program");
        menuEditExternal.addActionListener(this);

        menuSimForward = new JMenuItem("Step simulation forward");
        menuSimForward.addActionListener(this);
        menuSimForward.setAccelerator(KeyStroke.getKeyStroke("ctrl RIGHT"));
        menuSimBackward = new JMenuItem("Wind simulation backward");
        menuSimBackward.addActionListener(this);
        menuSimBackward.setAccelerator(KeyStroke.getKeyStroke("ctrl LEFT"));
        menuSimPrintRecord = new JMenuItem("Print recording");
        menuSimPrintRecord.addActionListener(this);
        menuSimPrintRecord.setAccelerator(KeyStroke.getKeyStroke("ctrl R"));

        menuDebugMakeSignals = new JMenuItem("Make some signals");
        menuDebugMakeSignals.addActionListener(this);
        menuDebugPrintMachine = new JMenuItem("Print machine to terminal");
        menuDebugPrintMachine.addActionListener(this);
        menuDebugPrintModel = new JMenuItem("Print model to terminal");
        menuDebugPrintModel.addActionListener(this);
        menuDebugPrintModel.setAccelerator(KeyStroke.getKeyStroke("ctrl pressed M"));
        menuDebugPrintTL = new JMenuItem("Print TL to terminal");
        menuDebugPrintTL.addActionListener(this);
        menuDebugPrintFL = new JMenuItem("Print FL to terminal");
        menuDebugPrintFL.addActionListener(this);

        menuHelp = new JMenuItem("Help");
        menuHelp.addActionListener(this);

        JMenu fileMenu = new JMenu("File");
        fileMenu.add(menuNew);
        fileMenu.add(menuOpen);
        fileMenu.addSeparator();
        fileMenu.add(menuSave);
        fileMenu.add(menuSaveAs);
        fileMenu.addSeparator();
        fileMenu.add(menuFLExport);
        fileMenu.addSeparator();
        fileMenu.add(menuQuit);

        JMenu fsmMenu = new JMenu("FSM");
        fsmMenu.add(menuRename);
        fsmMenu.add(menuEditExternal);

        JMenu simMenu = new JMenu("Simulation");
        simMenu.add(menuSimForward);
        simMenu.add(menuSimBackward);
        simMenu.add(menuSimPrintRecord);

        JMenu debugMenu = new JMenu("Debug");
        debugMenu.add(menuDebugMakeSignals);
        debugMenu.addSeparator();
        debugMenu.add(menuDebugPrintMachine);
        debugMenu.add(menuDebugPrintModel);
        debugMenu.add(menuDebugPrintTL);
        debugMenu.add(menuDebugPrintFL);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(menuHelp);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(fsmMenu);
        menuBar.add(simMenu);
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
        if(lastExportTime != null)
        {
            title += " - last exported " +
                String.format("%02d:%02d:%02d",
                              lastExportTime.getHour(),
                              lastExportTime.getMinute(),
                              lastExportTime.getSecond());
        }
        setTitle(title);
    }

    private void fixHistoryStepIndicator()
    {
        if(historyStepIndicator != null)
        {
            if(historian != null)
            {
                int cycle = historian.getHistorySize();
                if(cycle >= 0)
                {
                    historyStepIndicator.setText("Cycle: " + cycle);
                }
                else
                {
                    historyStepIndicator.setText("---");
                }
            }
            else
            {
                historyStepIndicator.setText("---");
            }
        }
    }

    private void newFSM(String name)
    {
        Machine m = new Machine(name);
        m.addState(new State("foo", m));
        m.addSignal(new Signal("reset", SignalKind.INPUT, m));
        setMachine(m);
    }

    public void addStatelyListener(StatelyListener l) { listeners.add(l); }
    
    public Machine getMachine() { return machine; }
    public MachineEditor getMachineEditor() { return machineEditor; }
    public Simulator getSimulator() { return simulator; }

    // Call this after non-cosmetic properties of the machine have changed.
    public void reportMachineModification(Object source)
    {
        fixSelection();
        historian.clear();
        fixHistoryStepIndicator();
        
        if(machine == null)
        {
            return;
        }

        analyze();

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
        simulator.setMachine(machine);
        analyze();
        simulator.setState(null);
        historian = new Historian(machine);
        fixHistoryStepIndicator();
        selectedStates.clear();
        MachineEvent e = new MachineEvent(this, m);
        for(StatelyListener l: listeners)
        {
            l.machineSwapped(e);
        }
        fixTitle();
        reportMachineModification(this);
    }



    

    // Selection services


    public void deselectAllStates()
    {
        selectedStates.clear();
        reportSelectionModification();
    }

    public void deselectState(State st)
    {
        selectedStates.deselect(st);
        reportSelectionModification();
    }

    public Set<State> getSelectedStates()
    {
        return selectedStates.getSelected();
    }
    
    public boolean isStateSelected(State st)
    {
        return selectedStates.isSelected(st);
    }

    public void selectAllStates()
    {
        if(machine != null)
        {
            setSelectedStates(machine.getStates());
        }
    }

    public void selectState(State st)
    {
        selectedStates.select(st);
        reportSelectionModification();
    }

    public void setSelectedStates(Collection<State> sel)
    {
        selectedStates.clear();
        selectedStates.select(sel);
        reportSelectionModification();
    }

    public void toggleSelectState(State st)
    {
        selectedStates.toggle(st);
        reportSelectionModification();
    }

    // Simulation and history/recording services

    public void printRecord()
    {
        String s = historian.makeWaveform();
        System.out.println(s);
    }

    public void record()
    {
        State cur = simulator.getState();

        if(cur != null)
        {
            SimulationState now = new SimulationState(cur, simulator.getEnvironment());
            historian.record(now);
        }

        fixHistoryStepIndicator();
    }

    public void stepBackward()
    {
        SimulationState prev = historian.unrecord();
        
        if(prev != null && prev.getState() != null && machine != null && machine.getStates().contains(prev.getState()))
        {
            simulator.setState(prev.getState());
        }

        fixHistoryStepIndicator();
    }
    
    public void stepForward()
    {
        
        State next = simulator.getNextState();
        if(next != null)
        {
            record();
            simulator.setState(next);
        }
    }
    

    // Misc services

    public void editSignal(Signal s, boolean agro)
    {
        machineEditor.editSignal(s, agro);
    }
    
    public void editState(State st, boolean agro)
    {
        machineEditor.editState(st, agro);
    }

    public void gotoState(State st)
    {
        if(st != null && st.isVirtual())
        {
            return;
        }
        
        simulator.setState(st);
        historian.clear();

        fixHistoryStepIndicator();
    }

    public boolean isSignalErroneous(Signal s)
    {
        return erroneousSignals.contains(s);
    }

    public boolean isSignalWarneous(Signal s)
    {
        return warneousSignals.contains(s);
    }

    public void makeState(Pt loc)
    {
        double x = loc.getX();
        double y = loc.getY();
        
        if(machine == null)
        {
            return;
        }
        
        String input = JOptionPane.showInputDialog(this, "Enter state name(s), separated by commas and optional spaces.\nA name may be prefixed with . to make it virtual.", "Create states", JOptionPane.QUESTION_MESSAGE);

        if(input == null)
        {
            return;
        }

        StringTokenizer st = new StringTokenizer(input, ",");
        ArrayList<State> added = new ArrayList<>();
        while(st.hasMoreTokens())
        {
            String token = st.nextToken();
            boolean virtual = false;
            
            while(token.length() != 0)
            {
                char c = token.charAt(0);

                if(c == '.')
                {
                    virtual = true;
                }
                else if(c != ' ')
                {
                    break;
                }

                token = token.substring(1);
            }

            String name = token.trim();
            State state = new State(name, machine);
            state.setVirtual(virtual);
            state.setPosition(x,y);
            x += STATE_CREATE_DX;
            y += STATE_CREATE_DY;
            machine.addState(state);
            added.add(state);
        }

        if(!added.isEmpty())
        {
            reportMachineModification(this);
            editState(added.get(added.size()-1), true);
            setSelectedStates(added);
        }
    }

    public void makeSignals()
    {
        if(machine == null)
        {
            return;
        }
        
        String input = JOptionPane.showInputDialog(this, "Enter signal names, separated by commas and optional spaces.\nA name may be prefixed with <, >, or = to set its kind, and . to make it internal.", "Create signals", JOptionPane.QUESTION_MESSAGE);

        if(input == null)
        {
            return;
        }

        StringTokenizer st = new StringTokenizer(input, ",");
        ArrayList<Signal> added = new ArrayList<>();
        while(st.hasMoreTokens())
        {
            String token = st.nextToken();
            SignalKind kind = SignalKind.STATEWISE; // most common
            boolean internal = false;
            
            while(token.length() != 0)
            {
                char c = token.charAt(0);

                if(c == '<' || c == '>' || c == '=')
                {
                    kind = SignalKind.fromSymbol("" + c);
                }
                else if(c == '.')
                {
                    internal = true;
                }
                else if(c != ' ')
                {
                    break;
                }

                token = token.substring(1);
            }

            String name = token.trim();
            Signal signal = new Signal(name, kind, machine);
            signal.setInternal(internal);
            machine.addSignal(signal);
            added.add(signal);
        }

        if(!added.isEmpty())
        {
            reportMachineModification(this);
            editSignal(added.get(added.size()-1), true);
        }
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

        reportMachineModification(this);
    }
    



    // Private

    private void analyze()
    {
        machine.analyze();
        erroneousSignals.clear();
        warneousSignals.clear();
        for(Issue i: machine.getIssues())
        {
            if(i.isWarning())
            {
                warneousSignals.addAll(i.getSignals());
            }
            else
            {
                erroneousSignals.addAll(i.getSignals());
            }
        }

        if(machine.getStatus() == MachineStatus.HAPPY)
        {
            simulator.setModel(new Model(machine));
        }
        else
        {
            simulator.setModel(null);
        }
    }
    
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
    
    private void loadConfig()
    {
        // TODO
        config = new StatelyConfig();
    }

    private void reportSelectionModification()
    {
        fixSelection();
        for(StatelyListener l: listeners)
        {
            l.selectionModified();
        }
    }

    // ActionListener

    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();

        if(source == menuNew)
        {
            mkNew();
        }
        else if(source == menuOpen)
        {
            open();
        }
        else if(source == menuSave)
        {
            save();
        }
        else if(source == menuSaveAs)
        {
            saveAs();
        }
        else if(source == menuFLExport)
        {
            exportFL();
        }
        else if(source == menuQuit)
        {
            quit();
        }
        else if(source == menuRename)
        {
            rename();
        }
        else if(source == menuEditExternal)
        {
            transform();
        }
        else if(source == menuSimForward)
        {
            stepForward();
        }
        else if(source == menuSimBackward)
        {
            stepBackward();
        }
        else if(source == menuSimPrintRecord)
        {
            printRecord();
        }
        else if(source == menuHelp)
        {
            help();
        }
        else if(source == menuDebugMakeSignals)
        {
            if(machine != null)
            {
                machineEditor.apply(); // apply any unsaved edits
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
                reportMachineModification(this);
            }
        }
        else if(source == menuDebugPrintMachine)
        {
            if(machine != null)
            {
                machineEditor.apply(); // apply any unsaved edits
                System.out.println();
                System.out.println(machine.toSExp());
                System.out.println();
            }
        }
        else if(source == menuDebugPrintModel)
        {
            if(machine != null)
            {
                machineEditor.apply(); // apply any unsaved edits
                if(machine.getStatus() == MachineStatus.HAPPY)
                {
                    System.out.println();
                    System.out.println(machine.getModel());
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
                machineEditor.apply(); // apply any unsaved edits
                java.util.List<SExp> dbg = Transformatron.dumpMachine(machine, null);
                System.out.println();
                for(SExp exp: dbg)
                {
                    System.out.println(exp.toString());
                }
                System.out.println();
            }
        }
        else if(source == menuDebugPrintFL)
        {
            if(machine != null)
            {
                machineEditor.apply(); // apply any unsaved edits
                if(machine.getStatus() == MachineStatus.HAPPY)
                {
                    System.out.println();
                    System.out.println(FLOut.generateFL(machine));
                    System.out.println();
                }
                else
                {
                    System.out.println("\nMachine not happy (outstanding errors), cannot generate FL.\n");
                }
            }
        }
    }

    // Actions driven by above

    private void help()
    {
        String message =
            "A few controls (see controls.md for more):\n" +
            "ctrl + left-click: add state\n" +
            "ctrl + del: delete selected state(s)\n" +
            "middle-drag: pan view\n" +
            "scroll wheel: zoom";

        JOptionPane.showMessageDialog(this, message);
    }

    // "New" in the menu
    private void mkNew()
    {
        String name = JOptionPane.showInputDialog(this, "Name for new FSM?\n(WARNING: you will lose any unsaved changes to the current file.)");
        if(name == null || name.equals(""))
        {
            return;
        }

        lastSaveFile = null;
        lastSaveTime = null;
        lastExportTime = null;
        newFSM(name);
    }

    // "Open" in the menu
    private void open()
    {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileFilter(FILE_EXTENSION_FILTER);
        int res = jfc.showOpenDialog(this);
        if(res == JFileChooser.APPROVE_OPTION)
        {
            File f = jfc.getSelectedFile();
            if(readFSMFromFile(f))
            {
                lastSaveFile = f;
                lastSaveTime = null;
                lastExportTime = null;
                fixTitle();
            }
        }
    }

    // "Save" in the menu
    private void save()
    {
        if(machine == null)
        {
            return;
        }
        
        if(lastSaveFile == null)
        {
            saveAs();
            return;
        }

        saveFSMToFile(lastSaveFile);
    }

    // "Save as" in the menu, or "save" when file not yet specified
    private void saveAs()
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
            saveFSMToFile(chosenExt);
        }
    }

    // Helper for "open"
    private boolean readFSMFromFile(File f)
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

    // Helper for "save" and "save as"
    private void saveFSMToFile(File f)
    {
        machineEditor.apply(); // apply any unsaved edits
        
        try
        {
            
            if(f.getName().equals(""))
            {
                throw new IllegalArgumentException("Bad file name");
            }
            File tmp = new File(f.getParent(), f.getName() + "~");
            writeFileSafe(f, tmp, machine.toSExp().toString());
            System.out.println("Wrote successfully to: " + f.getPath());
            lastSaveFile = f;
            lastSaveTime = LocalTime.now();
        }
        catch(Throwable t)
        {
            t.printStackTrace();
            JOptionPane.showMessageDialog(this, t.getMessage());
        }

        fixTitle();
    }

    private void exportFL()
    {
        machineEditor.apply();

        if(machine == null)
        {
            return;
        }

        if(lastSaveFile == null)
        {
            JOptionPane.showMessageDialog(this, "Cannot export without knowing where.\nSave the FSM somewhere first.");
            return;
        }

        if(machine.getStatus() != MachineStatus.HAPPY)
        {
            JOptionPane.showMessageDialog(this, "Cannot export unhappy machine.");
            return;
        }

        String parent = lastSaveFile.getParent();
        String fsmFileName = lastSaveFile.getName();
        if(fsmFileName.endsWith("." + EXTENSION))
        {
            String flFileName = fsmFileName.substring(0, fsmFileName.length() - EXTENSION.length()) + FL_EXTENSION;

            File f = new File(parent, flFileName);
            try
            {
                String fl = FLOut.generateFL(machine);
                if(fl == null)
                {
                    throw Misc.impossible();
                }
                writeFile(f, fl);
                lastExportTime = LocalTime.now();
                System.out.println("Exported successfully to: " + f.getPath());
            }
            catch(Throwable t)
            {
                t.printStackTrace();
                JOptionPane.showMessageDialog(this, t.getMessage());
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "Save file doesn't end with ." + EXTENSION + ",\nrefusing to export just in case.");
        }

        fixTitle();
    }

    private void rename()
    {
        if(machine == null)
        {
            return;
        }
        
        String newName = JOptionPane.showInputDialog(this, "Enter new FSM name (note: unrelated to filename)", machine.getName());
        if(newName != null && !newName.equals(""))
        {
            machine.setName(newName);
            reportMachineModification(this);
        }
    }

    private boolean transform()
    {
        File f = new File(TRANSFORM_TMP);
        boolean first = true;

        machineEditor.apply(); // apply any unsaved edits
        
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

    private void quit()
    {
        int res = JOptionPane.showConfirmDialog(this, "Are you sure you want to quit?", "Quit", JOptionPane.OK_CANCEL_OPTION);
        if(res == JOptionPane.YES_OPTION)
        {
            System.exit(0);
        }
    }

    // Static methods

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
    
    private static void transform_out(File f, Machine m) throws IOException
    {
        String out = "";
        out += "-- Cheatsheet:\n";
        out += "-- (name \"fsmname\")\n";
        out += "-- (translate <deltaX> <deltaY>)\n";
        out += "-- (signal \"name\" <kind> <internal> \"description\"" + " \"expression code\")\n";
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

    // Writes to a second file, deletes the destination (if it exists), then renames.
    private static void writeFileSafe(File f, File tmp, String s) throws IOException
    {
        writeFile(tmp, s);
        if(f.exists())
        {
            if(!f.delete())
            {
                throw new Error("Could not delete " + f.getPath());
            }
        }
        if(!tmp.renameTo(f))
        {
            throw new Error("Could not rename " + tmp.getPath() + " to " + f.getPath());
        }
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
