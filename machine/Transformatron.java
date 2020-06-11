package machine;
import java.util.*;

public class Transformatron
{
    public static final String CMD_INITIAL = "initial";
    public static final String CMD_NAME = "name";
    public static final String CMD_SIGNAL = "signal";
    public static final String CMD_STATE = "state";
    public static final String CMD_TRANSLATE = "translate";
    
    private Machine m;
    private int shiftX = 0, shiftY = 0;

    public Transformatron(Machine m)
    {
        this.m = m;
    }

    public void interpret(SExp exp)
    {
        if(exp.getKind() != SExpKind.LIST)
        {
            throw TransformatronError.bad(exp);
        }

        List<SExp> list = exp.getList();

        if(list.isEmpty())
        {
            throw TransformatronError.bad(exp);
        }

        SExp cmdExp = list.remove(0);
        if(cmdExp.getKind() != SExpKind.ATOM)
        {
            throw TransformatronError.bad(exp);
        }

        String cmd = cmdExp.getAtom();

        switch(cmd)
        {
        case CMD_INITIAL:
            interpretInitial(exp, list);
        case CMD_NAME:
            interpretName(exp, list);
            break;
        case CMD_SIGNAL:
            interpretSignal(exp, list);
            break;
        case CMD_STATE:
            interpretState(exp, list);
            break;
        case CMD_TRANSLATE:
            interpretTranslate(exp, list);
            break;
        default:
            throw TransformatronError.bad(exp);
        }
    }

    private void interpretInitial(SExp exp, List<SExp> args)
    {
        if(args.size() != 1)
        {
            throw TransformatronError.bad(exp);
        }

        String name = gimme(args, 0, SExpKind.STRING).getString();
        State st = m.findState(name);
        if(st == null)
        {
            throw TransformatronError.bad(exp);
        }
        m.setInitialState(st);
    }

    private void interpretName(SExp exp, List<SExp> args)
    {
        if(args.size() != 1)
        {
            throw TransformatronError.bad(exp);
        }

        String name = gimme(args, 0, SExpKind.STRING).getString();
        m.setName(name);
    }

    private void interpretSignal(SExp exp, List<SExp> args)
    {
        if(args.size() != 5)
        {
            throw TransformatronError.bad(exp);
        }

        String name = gimme(args, 0, SExpKind.STRING).getString();
        SignalKind kind = SignalKind.fromAtom(gimme(args, 1, SExpKind.ATOM).getAtom());
        if(kind == null)
        {
            throw TransformatronError.bad(args.get(1));
        }
        int internalInt = gimme(args, 2, SExpKind.INT).getInt();
        if(internalInt != 0 && internalInt != 1)
        {
            throw TransformatronError.bad(args.get(2));
        }
        boolean internal = internalInt != 0;
        String description = gimme(args, 3, SExpKind.STRING).getString();
        String code = gimme(args, 4, SExpKind.STRING).getString();

        Signal s = new Signal(name, kind, m);
        s.setInternal(internal);
        s.setDescription(description);
        s.getCode().setSource(code);
        m.addSignal(s);
    }

    private void interpretState(SExp exp, List<SExp> args)
    {
        if(args.size() != 6)
        {
            throw TransformatronError.bad(exp);
        }

        String name = gimme(args, 0, SExpKind.STRING).getString();
        String description = gimme(args, 1, SExpKind.STRING).getString();
        int virtualInt = gimme(args, 2, SExpKind.INT).getInt();
        if(virtualInt != 0 && virtualInt != 1)
        {
            throw TransformatronError.bad(args.get(2));
        }
        boolean virtual = virtualInt != 0;
        int x = gimme(args, 3, SExpKind.INT).getInt();
        int y = gimme(args, 4, SExpKind.INT).getInt();
        String code = gimme(args, 5, SExpKind.STRING).getString();

        State st = new State(name, m);
        st.setDescription(description);
        st.setVirtual(virtual);
        st.setPosition(x + shiftX, y + shiftY);
        st.getCode().setSource(code);
        m.addState(st);
    }

    private void interpretTranslate(SExp exp, List<SExp> args)
    {
        if(args.size() != 2)
        {
            throw TransformatronError.bad(exp);
        }

        int x = gimme(args, 0, SExpKind.INT).getInt();
        int y = gimme(args, 1, SExpKind.INT).getInt();

        shiftX += x;
        shiftY += y;
    }

    private SExp gimme(List<SExp> args, int index, SExpKind k)
    {
        SExp exp = args.get(index);
        if(exp.getKind() != k)
        {
            throw TransformatronError.bad(exp);
        }
        return exp;
    }

    public static List<SExp> dumpMachine(Machine m, SExp optionalSeparator)
    {
        List<SExp> out = new ArrayList<>();

        dumpMachineProperties(m, out);

        if(optionalSeparator != null)
        {
            out.add(optionalSeparator);
        }

        for(Signal s: m.getSignals())
        {
            dumpSignal(s, out);
        }

        if(optionalSeparator != null)
        {
            out.add(optionalSeparator);
        }
        
        for(State st: m.getStates())
        {
            dumpState(st, out);
        }

        dumpInitial(m.getInitialState(), out);
        
        return out;
    }

    private static void dumpInitial(State st, List<SExp> out)
    {
        if(st == null)
        {
            return;
        }

        List<SExp> list = new ArrayList<>();
        list.add(SExp.mkAtom(CMD_INITIAL));
        list.add(SExp.mkString(st.getName()));
        out.add(SExp.mkList(list));
    }

    private static void dumpMachineProperties(Machine m, List<SExp> out)
    {
        List<SExp> list = new ArrayList<>();
        list.add(SExp.mkAtom(CMD_NAME));
        list.add(SExp.mkString(m.getName()));
        out.add(SExp.mkList(list));
    }

    private static void dumpSignal(Signal s, List<SExp> out)
    {
        List<SExp> list = new ArrayList<>();
        list.add(SExp.mkAtom(CMD_SIGNAL));
        list.add(SExp.mkString(s.getName()));
        list.add(SExp.mkAtom(SignalKind.toAtom(s.getKind())));
        list.add(SExp.mkBoolAsInt(s.getInternal()));
        list.add(SExp.mkString(s.getDescription()));
        list.add(SExp.mkString(s.getCode().getSource()));
        out.add(SExp.mkList(list));
    }

    private static void dumpState(State st, List<SExp> out)
    {
        List<SExp> list = new ArrayList<>();
        list.add(SExp.mkAtom(CMD_STATE));
        list.add(SExp.mkString(st.getName()));
        list.add(SExp.mkString(st.getDescription()));
        list.add(SExp.mkBoolAsInt(st.isVirtual()));
        list.add(SExp.mkInt((int)st.getX()));
        list.add(SExp.mkInt((int)st.getY()));
        list.add(SExp.mkString(st.getCode().getSource()));
        out.add(SExp.mkList(list));
    }
}
