package ac.il.afeka.fsm;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

public class NDFSM {

	protected TransitionMapping transitions;
	protected Set<State> states;
	protected Set<State> acceptingStates;
	protected State initialState;
	protected Alphabet alphabet;
	// Epsilon transitions in NDFSM from State i
	protected Map<Integer, Set<State>> eps = new HashMap<>();

	/**
	 * Builds a NDFSM from a string representation (encoding)
	 * 
	 * @param encoding
	 *            the string representation of a NDFSM
	 * @throws Exception
	 *             if the encoding is incorrect or if the transitions contain
	 *             invalid states or symbols
	 */
	public NDFSM(String encoding) throws Exception {
		parse(encoding);

		transitions.verify(states, alphabet);
	}

	/**
	 * Build a NDFSM from its components
	 * 
	 * @param states
	 *            the set of states for this machine
	 * @param alphabet
	 *            this machine's alphabet
	 * @param transitions
	 *            the transition mapping of this machine
	 * @param initialState
	 *            the initial state (must be a member of states)
	 * @param acceptingStates
	 *            the set of accepting states (must be a subset of states)
	 * @throws Exception
	 *             if the components do not represent a valid non deterministic
	 *             machine
	 */
	public NDFSM(Set<State> states, Alphabet alphabet, Set<Transition> transitions, State initialState,
			Set<State> acceptingStates) throws Exception {

		initializeFrom(states, alphabet, transitions, initialState, acceptingStates);
		this.transitions.verify(this.states, alphabet);
	}

	protected void initializeFrom(Set<State> states, Alphabet alphabet, Set<Transition> transitions, State initialState,
			Set<State> acceptingStates) {

		this.states = states;
		this.alphabet = alphabet;
		this.transitions = createMapping(transitions);
		this.initialState = initialState;
		this.acceptingStates = acceptingStates;
	}

	protected NDFSM() {
	}

	/**
	 * Overrides this machine with the machine encoded in string.
	 * 
	 * <p>
	 * Here's an example of the encoding:
	 * </p>
	 * 
	 * <pre>
	0 1/a b/0 , a , 0; 0,b, 1 ;1, a, 0 ; 1, b, 1/0/ 1
	 * </pre>
	 * <p>
	 * This is the encoding of a finite state machine with two states (identified as
	 * 0 and 1), an alphabet that consists of the two characters 'a' and 'b', and
	 * four transitions:
	 * </p>
	 * <ol>
	 * <li>From state 0 on character a it moves to state 0</li>
	 * <li>from state 0 on character b it moves to state 1,</li>
	 * <li>from state 1 on character a it moves to state 0,</li>
	 * <li>from state 1 on character b it moves to state 1.</li>
	 * </ol>
	 * <p>
	 * The initial state of this machine is 0, and the set of accepting states
	 * consists of just one state 1. Here is the format in general:
	 * </p>
	 * 
	 * <pre>
	 {@code
	<states> / <alphabet> / <transitions> / <initial state> / <accepting states>
	}
	 * </pre>
	 * 
	 * where:
	 * 
	 * <pre>
	{@code
	<alphabet> is <char> <char> ...
	
	<transitions> is <transition> ; <transition> ...
	
	<transition> is from , char, to
	
	<initial state> is an integer
	
	<accepting states> is <state> <state> ...
	
	<state> is an integer
	}
	 * </pre>
	 * 
	 * @param string
	 *            the string encoding
	 * @throws Exception
	 *             if the string encoding is invalid
	 */
	public void parse(String string) throws Exception {

		Scanner scanner = new Scanner(string);

		scanner.useDelimiter("\\s*/");

		Map<Integer, State> states = new HashMap<Integer, State>();

		for (Integer stateId : IdentifiedState.parseStateIdList(scanner.next())) {
			states.put(stateId, new IdentifiedState(stateId));
		}

		Alphabet alphabet = Alphabet.parse(scanner.next());

		Set<Transition> transitions = new HashSet<Transition>();

		for (TransitionTuple t : TransitionTuple.parseTupleList(scanner.next())) {
			transitions.add(new Transition(states.get(t.fromStateId()), t.symbol(), states.get(t.toStateId())));
		}

		State initialState = states.get(scanner.nextInt());

		Set<State> acceptingStates = new HashSet<State>();

		if (scanner.hasNext())
			for (Integer stateId : IdentifiedState.parseStateIdList(scanner.next())) {
				acceptingStates.add(states.get(stateId));
			}

		scanner.close();

		initializeFrom(new HashSet<State>(states.values()), alphabet, transitions, initialState, acceptingStates);
		this.transitions.verify(this.states, alphabet);
	}

	protected TransitionMapping createMapping(Set<Transition> transitions) {
		return new TransitionRelation(transitions);
	}

	/**
	 * Returns a version of this state machine with all the unreachable states
	 * removed.
	 * 
	 * @return NDFSM that recognizes the same language as this machine, but has no
	 *         unreachable states.
	 */
	public NDFSM removeUnreachableStates() {

		Set<State> reachableStates = reachableStates();

		Set<Transition> transitionsToReachableStates = new HashSet<Transition>();

		for (Transition t : transitions.transitions()) {
			if (reachableStates.contains(t.fromState()) && reachableStates.contains(t.toState()))
				transitionsToReachableStates.add(t);
		}

		Set<State> reachableAcceptingStates = new HashSet<State>();
		for (State s : acceptingStates) {
			if (reachableStates.contains(s))
				reachableAcceptingStates.add(s);
		}

		NDFSM aNDFSM = (NDFSM) create();

		aNDFSM.initializeFrom(reachableStates, alphabet, transitionsToReachableStates, initialState,
				reachableAcceptingStates);

		return aNDFSM;
	}

	protected NDFSM create() {
		return new NDFSM();
	}

	// returns a set of all states that are reachable from the initial state

	private Set<State> reachableStates() {

		List<Character> symbols = new ArrayList<Character>();

		symbols.add(Alphabet.EPSILON);

		for (Character c : alphabet) {
			symbols.add(c);
		}

		Alphabet alphabetWithEpsilon = new Alphabet(symbols);

		Set<State> reachable = new HashSet<State>();

		Set<State> newlyReachable = new HashSet<State>();

		newlyReachable.add(initialState);

		while (!newlyReachable.isEmpty()) {
			reachable.addAll(newlyReachable);
			newlyReachable = new HashSet<State>();
			for (State state : reachable) {
				for (Character symbol : alphabetWithEpsilon) {
					for (State s : transitions.at(state, symbol)) {
						if (!reachable.contains(s))
							newlyReachable.add(s);
					}
				}
			}
		}

		return reachable;
	}

	/**
	 * Encodes this state machine as a string
	 * 
	 * @return the string encoding of this state machine
	 */
	public String encode() {
		return State.encodeStateSet(states) + "/" + alphabet.encode() + "/" + transitions.encode() + "/"
				+ initialState.encode() + "/" + State.encodeStateSet(acceptingStates);
	}

	/**
	 * Prints a set notation description of this machine.
	 * 
	 * <p>
	 * To see the Greek symbols on the console in Eclipse, go to Window -&gt;
	 * Preferences -&gt; General -&gt; Workspace and change
	 * <tt>Text file encoding</tt> to <tt>UTF-8</tt>.
	 * </p>
	 * 
	 * @param out
	 *            the output stream on which the description is printed.
	 */
	public void prettyPrint(PrintStream out) {
		out.print("K = ");
		State.prettyPrintStateSet(states, out);
		out.println("");

		out.print("\u03A3 = ");
		alphabet.prettyPrint(out);
		out.println("");

		out.print(transitions.prettyName() + " = ");
		transitions.prettyPrint(out);
		out.println("");

		out.print("s = ");
		initialState.prettyPrint(out);
		out.println("");

		out.print("A = ");
		State.prettyPrintStateSet(acceptingStates, out);
		out.println("");
	}

	/**
	 * Returns a canonic version of this machine.
	 * 
	 * <p>
	 * The canonic encoding of two minimal state machines that recognize the same
	 * language is identical.
	 * </p>
	 * 
	 * @return a canonic version of this machine.
	 */

	public NDFSM toCanonicForm() {

		Set<Character> alphabetAndEpsilon = new HashSet<Character>();

		for (Character symbol : alphabet) {
			alphabetAndEpsilon.add(symbol);
		}
		alphabetAndEpsilon.add(Alphabet.EPSILON);

		Set<Transition> canonicTransitions = new HashSet<Transition>();
		Stack<State> todo = new Stack<State>();
		Map<State, State> canonicStates = new HashMap<State, State>();
		Integer free = 0;

		todo.push(initialState);
		canonicStates.put(initialState, new IdentifiedState(free));
		free++;
		while (!todo.isEmpty()) {
			State top = todo.pop();
			for (Character symbol : alphabetAndEpsilon) {
				for (State nextState : transitions.at(top, symbol)) {
					if (!canonicStates.containsKey(nextState)) {
						canonicStates.put(nextState, new IdentifiedState(free));
						todo.push(nextState);
						free++;
					}
					canonicTransitions
							.add(new Transition(canonicStates.get(top), symbol, canonicStates.get(nextState)));
				}
			}
		}

		Set<State> canonicAcceptingStates = new HashSet<State>();
		for (State s : acceptingStates) {
			if (canonicStates.containsKey(s)) // unreachable accepting states will not appear in the canonic form of the
												// state machine
				canonicAcceptingStates.add(canonicStates.get(s));
		}

		NDFSM aNDFSM = create();

		aNDFSM.initializeFrom(new HashSet<State>(canonicStates.values()), alphabet, canonicTransitions,
				canonicStates.get(initialState), canonicAcceptingStates);

		return aNDFSM;
	}

	public boolean compute(String input) {
		return toDFSM().compute(input);
	}

	public DFSM toDFSM() {
		ArrayList<State> newStates = new ArrayList<>();
		Set<Transition> newTransitions = new HashSet<>();
		ArrayList<State> newAcceptingStates = new ArrayList<>();
		State newInitialState;

		Map<Integer, Set<State>> newStateGroups = new HashMap<>();
		CalculateEpsTransitions();
		int nextStateCounter = 0;
		//initial state is the group of states epsilon(initialState)
		newInitialState = new IdentifiedState(nextStateCounter);
		newStates.add(newInitialState);
		newStateGroups.put(nextStateCounter++, eps(initialState.getId()));
		
		//this loop iterates over all groups of different states, with all the alphabets to build transitions from group to group
		for (int i = 0; i < nextStateCounter; i++) {

			Set<State> currentGroup = newStateGroups.get(i);

			for (Character alphabetChar : this.alphabet) {
				Set<State> nextGroup = new HashSet<>();
				//for each state in the group, checks transition group with the alphabet
				for (State s : currentGroup) {
					nextGroup.addAll(transitions.at(s, alphabetChar));

				}
				
				//add epsilon transition to group
				nextGroup = groupSetWithEps(nextGroup);
				
				if (!newStateGroups.containsValue(nextGroup)) {
					//if group does not already exist in the set, add it 
					//and create a new state for the DFSM
					newStates.add(new IdentifiedState(nextStateCounter));
					newStateGroups.put(nextStateCounter, nextGroup);

					//next unique group will be assigned the following id
					nextStateCounter++;
				}
				//create transition with FromState(represented by group with id i), with char from alphabet, toState(represented by nextGroup, id retrieved in function)
				newTransitions.add(new Transition(newStates.get(i), alphabetChar,
						newStates.get(getSetIndex(nextGroup, newStateGroups))));
			}
		}
		//checks which of the new states is accepting by checking if each of the unique groups includes an accepting state
		//sets the DFSM state to accepting if yes
		for (int i = 0; i < nextStateCounter; i++)
			if (SetIfGroupIsAccepting(newStateGroups.get(i)))
				newAcceptingStates.add(newStates.get(i));
		try {

			return new DFSM(new HashSet<State>(newStates), this.alphabet, newTransitions, newInitialState,
					new HashSet<State>(newAcceptingStates));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		/*
		 * new DFSM(Set<State> states, Alphabet alphabet, Set<Transition> transitions,
		 * State initialState,Set<State> acceptingStates)
		 */
	}

	private int getSetIndex(Set<State> currentSet, Map<Integer, Set<State>> allSets) {
		/*
		 * value is ensured to be contained in the set thus no need to return a value if
		 * not found
		 * 
		 */
		for (Integer index : allSets.keySet()) {
			if (allSets.get(index).equals(currentSet))
				return index;
		}
		return 0;
	}

	private boolean SetIfGroupIsAccepting(Set<State> set) {
		/*
		 * checks if a group of states contains an accepting state return true if yes
		 */
		for (State s : acceptingStates) {
			if (set.contains(s))
				return true;
		}
		return false;
	}

	private Set<State> groupSetWithEps(Set<State> set) {
		/*
		 * method adds all epsilon transitions from a state set
		 */
		Set<State> newSet = new HashSet<>();
		newSet.addAll(set);
		for (State s : set) {
			newSet.addAll(eps(s.getId()));
		}
		return newSet;
	}

	private void CalculateEpsTransitions() {
		/*
		 * transition is of class TransitionRelation, which returns all states from
		 * state s with symbol epsilon
		 */

		for (State s : states) {
			AddEps(s.getId(), calculateEps(s));
		}

	}

	private Set<State> calculateEps(State s) {
		// for a given state, method calculates all epsilon transitions possible

		Set<State> set = new HashSet<>();
		set.add(s);
		for (State t : transitions.at(s, Alphabet.EPSILON))
			set.addAll(calculateEps(t));
		return set;
	}

	private Set<State> eps(int id) {
		return eps.get(id);
	}

	private void AddEps(int id, Set<State> set) {
		eps.put(id, set);
	}
}
