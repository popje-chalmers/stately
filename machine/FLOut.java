package machine;
import java.util.*;

// This is a horrible mess, by the way.

public class FLOut
{
    public static final String BIT = "bit";
    public static final String[] LIBS = new String[]{"ste.fl"};

    public static final String CLOCK = "clk"; // clock signal name
    public static final String STATE = "state"; // state variable name;
    public static final String STATE_ENUM_TYPE_SUFFIX = "_state";
    public static final String MOORE_SUFFIX = "_moore";

    public static String generateFL(Machine m)
    {
        if(m.getStatus() != MachineStatus.HAPPY)
        {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        sb.append("// " + m.getName() + " (autogenerated by Stately)\n\n");

        for(String lib: LIBS)
        {
            sb.append("cload " + quote(lib) + ";\n");
        }
        sb.append("\n");

        for(String line: generateStateEnum(m))
        {
            sb.append(line + "\n");
        }
        sb.append("\n");
        for(String line: generateModule(m))
        {
            sb.append(line + "\n");
        }

        return sb.toString();
    }

    private static List<String> generateStateEnum(Machine m)
    {
        List<String> out = new ArrayList<>();

        out.add("ENUM " + quote(stateEnumType(m)));

        List<String> items = new ArrayList<>();
        for(State st: m.getStates())
        {
            items.add(quote(stateFLName(m, st)));
        }
        out.addAll(indentAll(1, list(items, true)));

        return out;
    }

    private static List<String> generateModule(Machine m)
    {
        List<String> out = new ArrayList<>();
        out.add("let " + machineFLName(m) + " =");
        out.addAll(indentAll(1, generateSignals(m)));
        out.addAll(indentAll(1, generateCell(m)));
        //out.add(";");
        return out;
    }

    private static List<String> generateSignals(Machine m)
    {
        List<String> out = new ArrayList<>();

        out.add("// clock");
        out.add(generateInput(BIT, CLOCK));
        out.add("// inputs");
        for(Signal s: m.getSignals())
        {
            if(s.getKind() == SignalKind.INPUT)
            {
                out.add(generateInput(BIT, signalFLName(m, s)));
            }
        }
        out.add("// outputs");
        for(Signal s: m.getSignals())
        {
            if(s.getKind() != SignalKind.INPUT && !s.isInternal())
            {
                out.add(generateOutput(BIT, signalFLName(m, s)));
            }
        }
        out.add("// internals");
        for(Signal s: m.getSignals())
        {
            if(s.getKind() != SignalKind.INPUT && s.isInternal())
            {
                out.add(generateInternal(BIT, signalFLName(m, s)));
            }
        }
        out.add("// state");
        out.add(generateInternal(stateEnumType(m), STATE));

        return out;
    }

    private static List<String> generateCell(Machine m)
    {
        List<String> out = new ArrayList<>();

        out.add("CELL " + quote("draw_hier " + machineFLName(m)));
        out.addAll(indentAll(1, listBlocks(generateCellInternals(m), true)));

        return out;
    }

    private static List<List<String>> generateCellInternals(Machine m)
    {
        List<List<String>> out = new ArrayList<>();
        List<String> moore = generateMooreFSM(m);
        if(!moore.isEmpty())
        {
            out.add(moore);
        }
        for(ModelSignalComputation msc: m.getModel().getSignalComputations())
        {
            List<String> foo = new ArrayList<>();
            foo.add(generateSignalComputation(m, msc));
            out.add(foo);
        }
        return out;
    }

    private static List<String> generateMooreFSM(Machine m)
    {
        List<String> out = new ArrayList<>();
        Model model = m.getModel();
        out.add("Moore_FSM " + quote(mooreName(m))
                + " " + CLOCK
                + " " + STATE
                + " ("
                + generateExpression(m, model.getResetCondition())
                + ", "
                + stateFLName(m, model.getInitialState())
                + ")");
        out.addAll(indentAll(1, list(generateTransitions(m), false)));
        return out;
    }

    private static List<String> generateTransitions(Machine m)
    {
        List<String> out = new ArrayList<>();
        for(State st: m.getStates())
        {
            if(st.isVirtual())
            {
                continue;
            }

            for(ModelTransition mt: m.getModel().getTransitionsFromState(st))
            {
                List<State> path = mt.getPath();
                State from = path.get(0);
                State to = path.get(path.size() - 1);
                Expression condition = mt.getCondition();
                out.add(stateFLName(m, from)
                        + " --- "
                        + generateExpression(m, condition)
                        + " --- "
                        + stateFLName(m, to));
            }
        }
        return out;
    }

    private static String generateSignalComputation(Machine m, ModelSignalComputation msc)
    {
        String lhs = signalFLName(m, msc.getSignal());
        String rhs = generateExpression(m, msc.getExpression());
        return lhs + " <- " + rhs;
    }

    private static String generateExpression(Machine m, Expression e)
    {
        Expression e2 = flPrepExp(Simplifier.simplify(e));
        return generateExpressionRec(m, e2);
    }

    private static String generateExpressionRec(Machine m, Expression e)
    {
        ExpressionKind k = e.getKind();
        if(k == ExpressionKind.CONSTANT)
        {
            return e.getConstant().getBoolean() ? "'1" : "'0";
        }
        else if(k == ExpressionKind.SIGNAL)
        {
            return signalFLName(m, e.getSignal());
        }
        else if(k == ExpressionKind.OPERATION)
        {
            Operator operator = e.getOperator();
            List<Expression> operands = e.getOperands();
            String floperator = getFLOperator(operator);

            List<String> floperands = new ArrayList<>();
            for(Expression e2: operands)
            {
                floperands.add(generateExpressionRec(m, e2));
            }

            if(operator == Operator.NOT)
            {
                if(floperands.size() == 1)
                {
                    return "(" + floperator + " " + floperands.get(0) + ")";
                }
                else
                {
                    throw Misc.impossible();
                }
            }
            else
            {
                if(floperands.isEmpty())
                {
                    return operator == Operator.AND ? "'1" : "'0";
                }

                if(floperands.size() == 1)
                {
                    return floperands.get(0);
                }

                String line = "(";
                boolean first = true;
                for(String floperand: floperands)
                {
                    if(!first)
                    {
                        line += " " + floperator + " ";
                    }
                    first = false;
                    line += floperand;
                }
                line += ")";
                return line;
            }
        }
        else if(k == ExpressionKind.STATE_IS)
        {
            Collection<State> states = e.getStates();
            if(states.size() != 1)
            {
                throw Misc.impossible();
            }
            State st = states.iterator().next();
            return "(is_" + stateFLName(m, st) + " " + STATE + ")";
        }
        else
        {
            throw Misc.impossible();
        }
    }



    // Make a list where elements are multi-line blocks.
    private static List<String> listBlocks(List<List<String>> items, boolean semicolon)
    {
        List<String> lines = new ArrayList<>();
        if(items.isEmpty())
        {
            lines.add(semicolon ? "[];" : "[]");
        }
        else
        {
            boolean first = true;
            for(List<String> block: items)
            {
                String blockPrefix = first ? "[ " : ", ";
                first = false;
                boolean firstLineInBlock = true;
                for(String s: block)
                {
                    String linePrefix = firstLineInBlock ? blockPrefix : "  ";
                    firstLineInBlock = false;
                    lines.add(linePrefix + s);
                }
            }
            lines.add(semicolon ? "];" : "]");
        }
        return lines;
    }

    private static List<String> list(List<String> items, boolean semicolon)
    {
        List<String> lines = new ArrayList<>();
        if(items.isEmpty())
        {
            lines.add(semicolon ? "[];" : "[]");
        }
        else
        {
            boolean first = true;
            for(String s: items)
            {
                String prefix = first ? "[ " : ", ";
                first = false;
                lines.add(prefix + s);
            }
            lines.add(semicolon ? "];" : "]");
        }
        return lines;
    }

    private static String indent(int levels, String s)
    {
        for(int i = 0; i < levels; i++)
        {
            s = "    " + s;
        }
        return s;
    }

    private static List<String> indentAll(int levels, List<String> lines)
    {
        List<String> out = new ArrayList<>();
        for(String s: lines)
        {
            out.add(indent(levels, s));
        }

        return out;
    }



    private static String getFLOperator(Operator op)
    {
        switch(op)
        {
        case NOT:
            return "'~'";
        case AND:
            return "'&'";
        case OR:
            return "'|'";
        case XOR:
            return "'^'";
        default:
            throw new Error("Internal error: no FL operator for " + op);
        }
    }

    private static String generateInput(String t, String name)
    {
        return t + "_input " + name + ".";
    }

    private static String generateInternal(String t, String name)
    {
        return t + "_internal " + name + ".";
    }

    private static String generateOutput(String t, String name)
    {
        return t + "_output " + name + ".";
    }

    private static String quote(String s)
    {
        return "\"" + s.replace("\\", "\\\\").replace("\"","\\\"") + "\"";
    }

    private static String stateEnumType(Machine m)
    {
        return machineFLName(m) + STATE_ENUM_TYPE_SUFFIX;
    }

    private static String mooreName(Machine m)
    {
        return machineFLName(m) + MOORE_SUFFIX;
    }

    public static String machineFLName(Machine m)
    {
        return fixChars(m.getName());
    }

    public static String signalFLName(Machine m, Signal s)
    {
        return fixChars(s.getName());
    }

    public static String stateFLName(Machine m, State st)
    {
        //return machineFLName(m) + "__" + fixChars(st.getName());
        return fixChars(st.getName()).toUpperCase();
    }

    private static String fixChars(String s)
    {
        return s.replace(' ', '_');
    }

    public static List<String> getReservedNames(Machine m)
    {
        List<String> names = new ArrayList<>();
        names.add(STATE);
        names.add(CLOCK);
        names.add(stateEnumType(m));
        names.add(mooreName(m));
        return names;
    }

    // Transform an expression into one ready to be output as FL.
    // - Turn NAND and NOR into NOT+AND and NOT+OR.
    // - Make every IS_STATE unary.
    // - NOT should be unary already?
    // Operators other than IS_STATE and NOT may still be degenerate (nullary, unary).
    private static Expression flPrepExp(Expression exp)
    {
        ExpressionKind k = exp.getKind();
        if(k == ExpressionKind.OPERATION)
        {
            List<Expression> operands = new ArrayList<>();
            for(Expression oldOperand: exp.getOperands())
            {
                operands.add(flPrepExp(oldOperand));
            }
            Operator op = exp.getOperator();
            if(op == Operator.NAND)
            {
                List<Expression> tmp = new ArrayList<>();
                tmp.add(new Expression(Operator.AND, operands));
                return new Expression(Operator.NOT, tmp);
            }
            else if(op == Operator.NOR)
            {
                List<Expression> tmp = new ArrayList<>();
                tmp.add(new Expression(Operator.OR, operands));
                return new Expression(Operator.NOT, tmp);
            }
            else
            {
                return new Expression(op, operands);
            }
        }
        else if(k == ExpressionKind.STATE_IS)
        {
            Collection<State> states = exp.getStates();
            if(states.isEmpty())
            {
                return new Expression(new Value(false));
            }
            else if(states.size() == 1)
            {
                return exp;
            }
            else
            {
                List<Expression> operands = new ArrayList<>();
                for(State st: states)
                {
                    List<State> tmp = new ArrayList<>();
                    tmp.add(st);
                    operands.add(new Expression(tmp));
                }
                return new Expression(Operator.OR, operands);
            }
        }
        else
        {
            return exp;
        }
    }
}
