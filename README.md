# Stately
*The less worse way of making FSMs.*

## Compatability, dependencies, and compilation
Tested only on Linux machines so far.
The only dependency is Java, for example via the openjdk-11-jdk package on Ubuntu.
After installing that, simply clone the repository and run `make`inside to compile it.
The result is `Stately.jar`, which can be run via `java -jar Stately.jar`.

## Ideas

### Inside-out outputs
Needing to express output signals as state-dependent expressions is unhelpful when designing an FSM!
Given that the machine operates in one state at a time, the question "in which states is signal Q enabled?" is usually fairly useless compared to the question "what happens in state S?", especially when signals need to act in harmony.
As well as making it almost impossible to see what a state does at a glance---as one needs to hunt through all the output signal equations---this approach also means editing the expressions for all relevant signals whenever a state is added/removed, which is time-consuming and error-prone.
So Stately allows outputs to be expressed one of two ways: as a single expression (which may or may not depend on the state, `output <- ...` thinking), or as a signal that is zero unless specified otherwise *inside each state* (`state -> ...` thinking).

### Describing states
A simple (but painful) way to describe states is to break them into transitions and outputs, with each rule subject to a condition.
This is not very convenient because (1) transitions and outputs often stem from the same cause and therefore share common conditions (for example latching a value and transitioning to another state in response to an input signal), and (2) conditions are often hierarchical (like nested if-statements).

In Stately, states are instead described by little programs that are built out of (nested) if-else statements and intermingled commands to output signals or make state transitions.
This matches the mental model of the state ("if x happens then output y and go to state z") and results in concise states with very clear behavior.

### Virtual states
When designing complicated state machines it is common that one ends up with states that serve as entry points to subsections of the machine, dispatching to one state or another based on some conditions.
These entry-point states often don't need to exist as the dispatching logic could just as well be put into whichever state(s) precede them, thereby saving a clock cycle, but that would be error-prone due to the duplication of logic (which one might want to change later, for example), especially if there are many such preceding states.
The solution Stately opts for is to encourage the use of such entry-point states for the sake of machine clarity, but allow them to be marked as "virtual", which means that transitions through them will be short-circuited when the machine is compiled.

### Scope limitation: control signals
To avoid reimplementing half of FL, signals are all "control signals", i.e. single bits.

## Making it go

### Compiling and running
* Compile: make
* Run: java -jar Stately.jar
* Compile and run: make run

### Examples
There are several example FSMs in the "examples" directory.

### Mouse and keybindings
See controls.md.

### Menu options (incomplete)
- Help > Help: shows some keybindings
- File > Export FL: creates an FL version of the state machine and saves it to `<current fsm file>.fl`
- Remainder of file menu: self-explanatory
- Transform > Edit with external program: writes the machine (in a handy representation) to a file for editing, and reads it back in; see the "Transform" section for more details
- Debug > Make some signals: makes a few hw/iot related signals
- Debug > Print machine to terminal: this is what the save file looks like
- Debug > Print model to terminal: this is what the simmered-down model looks like; see the "Model" section
- Debug > Print TL to terminal: this is what the machine looks like in the representation used by Transform > ...

### Editing things
On the right side there are editors for signals and states. Changes to the signal/state being edited are effected by clicking "Apply changes", or automatically in various circumstances (for example when you try to edit a different signal/state).

## More about signals
There are three kinds of signals: input, expression, and statewise.
1. Input signals are exactly what they sound like; the machine cannot change them. They are denoted symbolically with a "<".
2. Expression signals are the typical internal/output signals one uses in FL: the signal's value is always equal to some expression (which may or may not depend on the current state). They are denoted by "=".
States cannot override the values of expression-signals.
3. Statewise signals are a new kind: a statewise signal is controlled by code in each state, defaulting to 0 unless otherwise specified by the current state.
This is the preferred way of implementing state-dependent signals, for the reasons described in the Ideas section. They are denoted by ">".

Statewise and expression signals can be marked as "internal", which means that that they won't become outputs in the eventually generated FL code. Marking input signals as internal has no effect.

## (Boolean) expressions
Expressions are composed of atomic values (0, 1, or a signal name---recall that signals represent single bits) and operator applications (lisp-ishly written `(f x y z ...)`).
Operators:
- not (unary)
- and/or/nand/nor/xor (variadic)
The semantics of those are as expected.
There is also a special form `(is_state state1 state2 ...)`, which is true iff the current state is among the listed states.
Names, both of signals and states, may be written in double quotes (like `"mem req 37"`) or without quotes if the name isn't too weird (like `mem_req_37`).

Examples:
- `mem_req`
- `(and mem_req enabled (not (xor in1 in2)) mem_okay)`
- `(not (is_state s_idle s_off))`
- `(nor mem_req MEM_ON_FIRE "mem ack")`

## State code
Each state contains code that specifies state transitions and (statewise) outputs.

Branching is accomplished using if-statements (`if` with optional `elif` and `else`), which use expressions as conditions (see above).

Grouping of statements---for example to place several inside an if-statement---is accomplished via increasing the indentation, as opposed to brackets.
Please be consistent with the uses of spaces (yay!) or tabs (boo!) for indentation, otherwise the grouping might not be as you intended.

Besides branching, there are three commands: `goto`, `let`, and `emit`.
- `goto` is for state transitions and takes one argument, the name of the state to transition to for the *next* cycle.
- `let` takes two arguments: the name of a (statewise) signal, and an expression to let it equal in this state (subject to any if-statements the command is inside).
- `emit` is the same as let, but it only takes the signal name---it uses the expression `1`.

Until-end-of-line comments may also be written using `--`.

Example:
    if (and hd_enabled hd_req)
      if hd_chill
        goto read
        emit hd_ack
      elif hd_on fire -- !!!
        emit uhoh
        goto fire
      else
        emit nack
    else
        emit boring

## State code semantics
There is no notion of sequence in the code for a state; one should instead think of the statements as truths that all must hold simultaneously during the state.
To illustrate this:
   if trivial:
     goto somewhere
     emit something
   emit trivial
In the above example, `trivial` and `something` will always be 1 in this state, and it will always transition to `somewhere` for the next cycle.

For this to work and be well-defined, Stately places some strict requirements on the code.
1. There cannot be any cyclic dependencies among signals.
In additional to explicit dependencies introduced by `let`, remember that if-statements create implicit dependencies.
Furthermore, the signal dependencies are required to be acyclic when taken across all states, not merely acyclic within each specific state/signal valuation
(this is necessary for the synthesized circuit not to contain any zero-delay loops, even if they would ideally be okay).
2. Any two `goto` statements must occur mutually exclusively.
Stately does not use a SAT solver for this, but instead errs on the side of caution and requires that they be in mutually-exclusive branches of an if-statement.
3. Any two `let`/`emit` statements for a given signal must occur mutually exclusively.
This is true even if the values would be the same.
Once again, this is strengthened to the requirement that they be in mutually exclusive branches of an if-statement.

### Goto (and virtual states)
The `goto` command normally indicates the next state (the default action being to remain in the current state if a `goto` doesn't occur).
If the destination of a `goto` is a virtual state, however, the latter's code is effectively substituted in place of the `goto`---this is how the desired behavior of short-circuiting transitions through virtual states is obtained.

For example, the state `foo` here:
    [state foo]
    if (nand x y)
      goto bar
    ...
    
    [state bar]
    emit w
    if z
      goto baz
    else
      goto zab

Has semantics as if it were:
    [state foo]
    if (nand x y)
      emit w
      if z
        goto baz
      else
        goto zab
    ... 

The inlinining of virtual states---as well as common sense---mean that virtual states must not form cycles.
Also, if a virtual state might not transition to another state, a warning is raised, since this is likely unintended (and due to the inlining means that the callee states could fail to transition even when it looks like they should, due to the `goto`).
Note that as they are all bypassed, virtual states are not given semantics by themselves.

## Transform

The save file format was designed to be verbose and relatively stable, which unfortunately makes it difficult to edit by hand.
So instead, Stately offers an option `Transform > Edit with external program` which dumps the machine to a file in a convenient-to-edit (but prone to changing) form---namely a series of commands---and then upon confirmation reads it back in.
The file's structure is explained in comments at the beginning of the file.

One of the main uses of this feature is for duplicating parts of the machine; one can copy and paste the relevant state declarations (making sure to change their names), and offset their positions by prepending a `translate` command.

## The "model"

Stately can flatten the machine into a simpler model, which has the following characteristics:
- Non-input signals are turned into single expressions (and conveniently in dependency order).
- State transitions are extracted and given in the form `from -> to {condition}`. Note: virtual intermediates show up with arrows between `from` and `to`; this information is retained for visualization.

## FL output

There is a prototype FL output engine now, either to the terminal via the debug menu or to a file next to the FSM's save file (".fsm" -> ".fsm.fl") via the File menu.
