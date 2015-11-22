package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main2 {

	public static void main(String[] args) {
		args = new String[] { "-i", "hw2/input_0.txt" };
		if (args.length != 2) {
			System.out.println("Input format: -i <filename>");
			return;
		}
		Main2 m = new Main2(args[1]);
		m.start();
	}

	/** Variables */
	public static final int TASK_GREEDY = 1;
	public static final int TASK_MINIMAX = 2;
	public static final int TASK_ABPRUNING = 3;
	public static final int TASK_COMPETITION = 4;

	private int task;
	private int cutOff;
	private MancalaState initState;
	private int sum;

	MancalaLogger logger;

	/** Functions */
	public Main2(String stateConfigFile) {
		initState = init(stateConfigFile);
		sum = sumOfBoard(initState);
	}

	private MancalaState init(String stateConfigFile) {
		System.out.println("Initialization...");
		MancalaState inputState = null;
		Scanner sc = null;
		try {
			sc = new Scanner(new File(stateConfigFile));
			if (sc.hasNext()) {
				task = sc.nextInt();
				int player = sc.nextInt();
				cutOff = sc.nextInt();
				int[] A, B;
				int As, Bs;
				sc.nextLine(); // format
				A = parseStringToIntegerArray(sc.nextLine());
				B = parseStringToIntegerArray(sc.nextLine());
				As = sc.nextInt();
				Bs = sc.nextInt();
				inputState = new MancalaState(A, B, As, Bs, player);
			} else {
				sc.close();
				throw new IllegalArgumentException("Empty input");
			}
			System.out.println("Initialization finished:");
			if (inputState != null) {
				System.out.println(inputState);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return inputState;
	}

	private int[] parseStringToIntegerArray(String s) {
		String[] strs = s.split(" ");
		int[] arr = new int[strs.length];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = Integer.parseInt(strs[i]);
		}
		return arr;
	}

	public void start() {
		System.out.println("Start...");
		String dir = "hw2/";
		String nextStatePath = dir + "next_state.txt";
		String traverseLogPath = dir + "traverse_log.txt";
		MancalaState next = null;
		switch (task) {
		case TASK_GREEDY:
			logger = new MancalaLogger(traverseLogPath,
					MancalaLogger.MINIMAX_MODE);
			next = greedySearch(initState);
			export(next.toString(), nextStatePath);
			System.out.println(next);
			break;
		case TASK_MINIMAX:
			logger = new MancalaLogger(traverseLogPath,
					MancalaLogger.MINIMAX_MODE);
			next = minimaxSearch(initState);
			export(next.toString(), nextStatePath);
			System.out.println(next);
			logger.writeLog();
			break;
		case TASK_ABPRUNING:
			logger = new MancalaLogger(traverseLogPath,
					MancalaLogger.PRUNING_MODE);
			next = pruningSearch(initState);
			export(next.toString(), nextStatePath);
			System.out.println(next);
			logger.writeLog();
			break;
		case TASK_COMPETITION:
			logger = new MancalaLogger(traverseLogPath,
					MancalaLogger.PRUNING_MODE);
			next = pruningSearch(initState);
			export(next.toString(), nextStatePath);
			System.out.println(next);
			logger.writeLog();
			break;
		default:
			System.out.println("Meaningless task code: " + task);
		}
		logger.close();
	}

	public MancalaState greedySearch(MancalaState state) {
		cutOff = 1;
		return minimaxSearch(state);
	}

	public MancalaState minimaxSearch(MancalaState state) {
		// DFS
		// init state is after opponent finished
		Node<MancalaState> root = new Node<MancalaState>(Node.MAX_NODE, state,
				null, 0);
		root = maxValue(root, -1);
		// find next state
		Node<MancalaState> nextNode = root.getBestMove();
		if (nextNode == null) {// already finished
			return root.getState();
		}
		while (nextNode.hasAddtionalMove()) {
			// System.out.println(nextNode.getState());
			nextNode = nextNode.getBestMove();
		}
		return nextNode.getState();
	}

	public MancalaState pruningSearch(MancalaState state) {
		Node<MancalaState> root = new Node<MancalaState>(Node.MAX_NODE, state,
				null, 0);
		root = maxValueWithPruning(root, -1);
		// find next state
		Node<MancalaState> nextNode = root.getBestMove();
		if (nextNode == null) {// already finished
			return root.getState();
		}
		while (nextNode.hasAddtionalMove()) {
			System.out.println(nextNode.getState());
			nextNode = nextNode.getBestMove();
		}
		return nextNode.getState();
	}

	private Node<MancalaState> maxValueWithPruning(
			Node<MancalaState> stateNode, int fromAction) {
		MancalaState state = stateNode.getState();
		if (state.gameOver()) {
			calFinalState(stateNode);
			logTrace(fromAction, stateNode.getParent(), stateNode);
			return stateNode;
		}
		if (stateNode.getDepth() == cutOff) {
			if (stateNode.hasAddtionalMove()) { // fake terminal
				// stateNode.setVal(heuristic(stateNode.getState()));
			} else {
				stateNode.setVal(heuristic(state));
				logTrace(fromAction, stateNode.getParent(), stateNode);
				return stateNode;
			}
		}
		Node<MancalaState> node;
		logTrace(fromAction, stateNode.getParent(), stateNode);
		for (int action = 0; action < state.N; action++) {
			if (state.getPlayerPits()[action] > 0) {
				node = move(stateNode, action);
				if (node.hasAddtionalMove()) {
					node = maxValueWithPruning(node, action);
				} else {
					node = minValueWithPruning(node, action);
				}
				if (stateNode.getVal() < node.getVal()) {
					stateNode.setVal(node.getVal());
					stateNode.setBestMove(node);
				}
				if (stateNode.hasAddtionalMove()) { // must move
					if (stateNode.getBestMove() == null) {
						stateNode.setVal(node.getVal());
						stateNode.setBestMove(node);
					} else {
						if (stateNode.getBestMove().getVal() < node.getVal()) {
							stateNode.setVal(node.getVal());
							stateNode.setBestMove(node);
						}
					}
				}
				if (stateNode.getVal() >= stateNode.getB()) {
					logTrace(fromAction, stateNode.getParent(), stateNode);
					return stateNode;
				}
				if (stateNode.getA() < stateNode.getVal()) {
					stateNode.setA(stateNode.getVal());
				}
				stateNode.addChild(node);
				logTrace(fromAction, stateNode.getParent(), stateNode);

			}
		}
		return stateNode;
	}

	private Node<MancalaState> minValueWithPruning(
			Node<MancalaState> stateNode, int fromAction) {
		MancalaState state = stateNode.getState();
		if (state.gameOver()) {
			calFinalState(stateNode);
			logTrace(fromAction, stateNode.getParent(), stateNode);
			return stateNode;
		}
		if (stateNode.getDepth() == cutOff) {
			if (stateNode.hasAddtionalMove()) {
				// stateNode.setVal(heuristic(stateNode.getState()));
			} else {
				stateNode.setVal(heuristic(state));
				logTrace(fromAction, stateNode.getParent(), stateNode);
				return stateNode;
			}
		}
		Node<MancalaState> node;
		// log if it is not leaf as first visit
		logTrace(fromAction, stateNode.getParent(), stateNode);
		for (int action = 0; action < state.N; action++) {
			if (state.getPlayerPits()[action] > 0) {
				node = move(stateNode, action);
				if (node.hasAddtionalMove()) {
					node = minValueWithPruning(node, action);
				} else {
					node = maxValueWithPruning(node, action);
				}
				if (stateNode.getVal() > node.getVal()) {
					stateNode.setVal(node.getVal());
					stateNode.setBestMove(node);
				}
				if (stateNode.hasAddtionalMove()) { // must move
					if (stateNode.getBestMove() == null) {
						stateNode.setVal(node.getVal());
						stateNode.setBestMove(node);
					} else {
						if (stateNode.getBestMove().getVal() > node.getVal()) {
							stateNode.setVal(node.getVal());
							stateNode.setBestMove(node);
						}
					}
				}
				if (stateNode.getVal() <= stateNode.getA()) {
					logTrace(fromAction, stateNode.getParent(), stateNode);
					return stateNode;
				}
				if (stateNode.getB() > stateNode.getVal()) {
					stateNode.setB(stateNode.getVal());
				}
				stateNode.addChild(node);
				// log update of value from children
				logTrace(fromAction, stateNode.getParent(), stateNode);

			}
		}
		return stateNode;
	}

	private Node<MancalaState> maxValue(Node<MancalaState> stateNode,
			int fromAction) {
		MancalaState state = stateNode.getState();
		if (state.gameOver()) {
			calFinalState(stateNode);
			logTrace(fromAction, stateNode.getParent(), stateNode);
			return stateNode;
		}
		if (stateNode.getDepth() == cutOff) {
			if (stateNode.hasAddtionalMove()) { // fake terminal
				// stateNode.setVal(heuristic(stateNode.getState()));
			} else {
				stateNode.setVal(heuristic(state));
				logTrace(fromAction, stateNode.getParent(), stateNode);
				return stateNode;
			}
		}
		Node<MancalaState> node;
		logTrace(fromAction, stateNode.getParent(), stateNode);
		for (int action = 0; action < state.N; action++) {
			if (state.getPlayerPits()[action] > 0) {
				node = move(stateNode, action);
				if (node.hasAddtionalMove()) {
					node = maxValue(node, action);
				} else {
					node = minValue(node, action);
				}
				if (stateNode.getVal() < node.getVal()) {
					stateNode.setVal(node.getVal());
					stateNode.setBestMove(node);
				}
				if (stateNode.hasAddtionalMove()) { // must move
					if (stateNode.getBestMove() == null) {
						stateNode.setVal(node.getVal());
						stateNode.setBestMove(node);
					} else {
						if (stateNode.getBestMove().getVal() < node.getVal()) {
							stateNode.setVal(node.getVal());
							stateNode.setBestMove(node);
						}
					}
				}
				stateNode.addChild(node);
				logTrace(fromAction, stateNode.getParent(), stateNode);
			}
		}
		return stateNode;
	}

	private Node<MancalaState> minValue(Node<MancalaState> stateNode,
			int fromAction) {
		MancalaState state = stateNode.getState();
		if (state.gameOver()) {
			calFinalState(stateNode);
			logTrace(fromAction, stateNode.getParent(), stateNode);
			return stateNode;
		}
		if (stateNode.getDepth() == cutOff) {
			if (stateNode.hasAddtionalMove()) {
				// stateNode.setVal(heuristic(stateNode.getState()));
			} else {
				stateNode.setVal(heuristic(state));
				logTrace(fromAction, stateNode.getParent(), stateNode);
				return stateNode;
			}
		}
		Node<MancalaState> node;
		// log if it is not leaf as first visit
		logTrace(fromAction, stateNode.getParent(), stateNode);
		for (int action = 0; action < state.N; action++) {
			if (state.getPlayerPits()[action] > 0) {
				node = move(stateNode, action);
				if (node.hasAddtionalMove()) {
					node = minValue(node, action);
				} else {
					node = maxValue(node, action);
				}
				if (stateNode.getVal() > node.getVal()) {
					stateNode.setVal(node.getVal());
					stateNode.setBestMove(node);
				}
				if (stateNode.hasAddtionalMove()) { // must move
					if (stateNode.getBestMove() == null) {
						stateNode.setVal(node.getVal());
						stateNode.setBestMove(node);
					} else {
						if (stateNode.getBestMove().getVal() > node.getVal()) {
							stateNode.setVal(node.getVal());
							stateNode.setBestMove(node);
						}
					}
				}
				stateNode.addChild(node);
				// log update of value from children
				logTrace(fromAction, stateNode.getParent(), stateNode);
			}
		}
		return stateNode;
	}

	private Node<MancalaState> move(Node<MancalaState> stateNode, int action) {
		// expand
		MancalaState state = stateNode.getState();
		int pitNum = state.getPlayerPits()[action];
		int n = state.getPlayerPits().length;
		MancalaState next = state.copy();
		int pos = action;
		int end = -1;
		int round = pitNum / (2 * n + 1);
		int reminder = pitNum % (2 * n + 1);
		boolean addMove = false, onZero = false;
		next.getPlayerPits()[pos] = 0;
		next.getPlayerPits()[pos] += round;
		int reminderCounter = reminder;
		if (next.who == 1) { // move B >>
			// judge end
			end = (action + reminder) % (2 * n + 1);
			if (round == 1 && reminder == 0) {
				onZero = true;
			}
			if (round == 0 && end < n) {
				if (state.getPlayerPits()[end] == 0) {
					onZero = true;

				}
			}
			pos++;
			for (; pos < n; pos++) {
				next.getPlayerPits()[pos] += round;
				if (reminderCounter > 0) {
					next.getPlayerPits()[pos]++;
					reminderCounter--;
				}
			}
			next.addPlayerScore(round);
			if (reminderCounter > 0) {
				next.addPlayerScore(1);
				reminderCounter--;
				if (reminderCounter == 0) {
					addMove = true;
				}
			}
			for (; pos > 0; pos--) {
				next.getOppoPits()[pos - 1] += round;
				if (reminderCounter > 0) {
					next.getOppoPits()[pos - 1]++;
					reminderCounter--;
				}
			}
			for (int i = 0; i < action; i++) {
				next.getPlayerPits()[i] += round;
				if (reminderCounter > 0) {
					next.getPlayerPits()[i]++;
					reminderCounter--;
				}
			}
		} else { // who == 2 move A <<
			// judge end
			end = (action - reminder) % (2 * n + 1);
			end = end < 0 ? end + (2 * n + 1) : end;
			if (round == 1 && reminder == 0) {
				onZero = true;
			}
			if (round == 0 && end < n) {
				if (state.getPlayerPits()[end] == 0) {
					onZero = true;
				}
			}
			pos--;
			for (; pos >= 0; pos--) {
				next.getPlayerPits()[pos] += round;
				if (reminderCounter > 0) {
					next.getPlayerPits()[pos]++;
					reminderCounter--;
				}
			}
			next.addPlayerScore(round);
			if (reminderCounter > 0) {
				next.addPlayerScore(1);
				reminderCounter--;
				if (reminderCounter == 0) {
					addMove = true;
				}
			}
			for (; pos < n - 1; pos++) {
				next.getOppoPits()[pos + 1] += round;
				if (reminderCounter > 0) {
					next.getOppoPits()[pos + 1]++;
					reminderCounter--;
				}
			}
			for (int i = n - 1; i > action; i--) {
				next.getPlayerPits()[i] += round;
				if (reminderCounter > 0) {
					next.getPlayerPits()[i]++;
					reminderCounter--;
				}
			}
		}
		if (onZero) {
			next.addPlayerScore(next.getPlayerPits()[end]
					+ next.getOppoPits()[end]);
			next.getPlayerPits()[end] = 0;
			next.getOppoPits()[end] = 0;
		}
		Node<MancalaState> nextNode;
		int addDepth = stateNode.hasAddtionalMove() ? 0 : 1;
		if (addMove) {
			nextNode = new Node<MancalaState>(stateNode.nodeType, next,
					stateNode, stateNode.getDepth() + addDepth);
		} else {
			next = next.switchToOppoState();
			nextNode = new Node<MancalaState>(
					stateNode.changeType(stateNode.nodeType), next, stateNode,
					stateNode.getDepth() + addDepth);
		}
		return nextNode;
	}

	private void logTrace(int action, Node<MancalaState> fromNode,
			Node<MancalaState> nextNode) {
		if (fromNode == null) {
			logger.addTrace("root", nextNode.getDepth(), nextNode.getVal(),
					nextNode.getA(), nextNode.getB());
		} else {
			logger.addTrace(
					String.format("%s%d", fromNode.getState().side, action + 2),
					nextNode.getDepth(), nextNode.getVal(), nextNode.getA(),
					nextNode.getB());
		}
		int size = logger.getSize();
		// if (size >= 2070 && size <= 2090) {
		// System.out.printf("%d %s%d\t%s\n", size, fromNode.getState().side,
		// action + 2, nextNode);
		// }
		// if (size >= 0 && size <= 10000 && fromNode != null) {
		// System.out.printf("%d %s%d\t%s\n", size, fromNode.getState().side,
		// action + 2, nextNode);
		// }
		if (size % 10000 == 0) {
			System.out.println(logger.getSize());
			logger.writeLog();
		}
		if (sumOfBoard(nextNode.getState()) != sum) {
			logger.close();
			System.out.println("Error");
			System.exit(0);
		}
	}

	private Node<MancalaState> calFinalState(Node<MancalaState> stateNode) {
		for (int i : stateNode.getState().getPlayerPits()) {
			stateNode.getState().addPlayerScore(i);
		}
		for (int i : stateNode.getState().getOppoPits()) {
			stateNode.getState().addOppoScore(i);
		}
		stateNode.getState().clearBoard();
		stateNode.setVal(heuristic(stateNode.getState()));
		return stateNode;
	}

	public int heuristic(MancalaState state) {
		return state.getScore(initState.who)
				- state.getScore(3 - initState.who);
	}

	public void export(String result, String filePath) {
		try {
			FileWriter fw = new FileWriter(filePath);
			fw.write(result);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int sumOfBoard(MancalaState state) {
		int sum = 0;
		sum = 0;
		for (int a : state.A) {
			sum += a;
		}
		for (int b : state.B) {
			sum += b;
		}
		sum += state.AScore;
		sum += state.BScore;
		return sum;
	}

	public class MancalaLogger {

		public static final int MINIMAX_MODE = 1;
		public static final int PRUNING_MODE = 2;

		List<Trace> logList;
		BufferedWriter bw;
		String exportPath;
		int mode;
		int historySize;

		public MancalaLogger(String exportPath, int mode) {
			this.mode = mode;
			logList = new ArrayList<Trace>();
			historySize = 0;
			try {
				bw = new BufferedWriter(new FileWriter(exportPath, false));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void addTrace(String pit, int depth, double value, double a,
				double b) {
			logList.add(new Trace(pit, depth, value, a, b));
			historySize++;
		}

		public void writeLog() {
			// write size-1 logs
			if (logList.size() == 0) {
				return;
			}
			try {
				bw.append(toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
			logList.clear();
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			if (mode == MINIMAX_MODE) {
				sb.append("Node,Depth,Value\n");
			} else {
				sb.append("Node,Depth,Value,Alpha,Beta\n");
			}
			for (Trace trace : logList) {
				if (mode == MINIMAX_MODE) {
					sb.append(trace.toSimpleString());
				} else {
					sb.append(trace.toString());
				}
				sb.append("\n");
			}
			return sb.toString();
		}

		public void printLog() {
			System.out.println(toString());
		}

		public int getSize() {
			return historySize;
		}

		public void export(String result, String filePath) {
			try {
				bw.write(result);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void close() {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		protected class Trace {
			String pit;
			int depth;
			double value;
			double a;
			double b;

			public Trace(String pit, int depth, double value, double a, double b) {
				super();
				this.pit = pit;
				this.depth = depth;
				this.value = value;
				this.a = a;
				this.b = b;
			}

			public Trace(String pit, int depth, double value) {
				super();
				this.pit = pit;
				this.depth = depth;
				this.value = value;
			}

			public Trace copy() {
				return new Trace(pit, depth, value, a, b);
			}

			@Override
			public String toString() {
				StringBuffer sb = new StringBuffer();
				sb.append(pit);
				sb.append(",");
				sb.append(depth);
				sb.append(",");
				sb.append((int) value);
				sb.append(",");
				sb.append((int) a);
				sb.append(",");
				sb.append((int) b);
				sb.append(",");
				return sb.toString();
			}

			public String toSimpleString() {
				StringBuffer sb = new StringBuffer();
				sb.append(pit);
				sb.append(",");
				sb.append(depth);
				sb.append(",");
				sb.append((int) value);
				return sb.toString();
			}

		}

	}

	public class MancalaState {
		public int[] A;
		public int[] B;
		public int AScore;
		public int BScore;
		public int N;
		public int who;
		public String side;

		public MancalaState(int[] A, int[] B, int AScore, int BScore, int who) {
			super();
			N = A.length;
			this.A = A.clone();
			this.B = B.clone();
			this.AScore = AScore;
			this.BScore = BScore;
			this.who = who;
			side = who == 1 ? "B" : "A";
		}

		public int[] getPlayerPits() {
			if (who == 1) {
				return B;
			} else {
				return A;
			}
		}

		public int[] getOppoPits() {
			if (who == 1) {
				return A;
			} else {
				return B;
			}
		}

		public int getScore(int i) {
			if (i == 1) {
				return BScore;
			} else {
				return AScore;
			}
		}

		public int getPlayerScore() {
			return getScore(who);
		}

		public int getOppoScore() {
			return getScore(3 - who);
		}

		public int addPlayerScore(int i) {
			if (who == 1) {
				BScore += i;
				return BScore;
			} else {
				AScore += i;
				return AScore;
			}
		}

		public int addOppoScore(int i) {
			if (who == 1) {
				AScore += i;
				return AScore;
			} else {
				BScore += i;
				return BScore;
			}
		}

		@Override
		public String toString() {
			String ret = String.format("%s\n%s\n%d\n%d", Arrays.toString(A),
					Arrays.toString(B), AScore, BScore);
			ret = ret.replace("[", "").replace("]", "").replace(",", "");
			return ret;
		}

		public String toStringFlat() {
			String ret = String.format("%s: %d %s %s %d", side, AScore,
					Arrays.toString(A), Arrays.toString(B), BScore);
			ret = ret.replaceAll("[|]", "");
			return ret;
		}

		public MancalaState copy() {
			MancalaState cp = new MancalaState(A.clone(), B.clone(), AScore,
					BScore, who);
			return cp;
		}

		public MancalaState switchToOppoState() {
			MancalaState oppoState = new MancalaState(A.clone(), B.clone(),
					AScore, BScore, 3 - who);
			return oppoState;
		}

		public boolean gameOver() {
			if (emptyPits(A) || emptyPits(B)) {
				return true;
			} else {
				return false;
			}
		}

		private boolean emptyPits(int[] pitsOf) {
			for (int i : pitsOf) {
				if (i != 0)
					return false;
			}
			return true;
		}

		public void clearBoard() {
			A = new int[N];
			B = new int[N];
		}

	}

	public class Node<S extends MancalaState> {
		public static final int MAX_NODE = 1;
		public static final int MIN_NODE = 2;

		public int nodeType;
		S state;
		double val;
		Node<S> parent;
		List<Node<S>> children;
		int depth;
		Node<S> bestMove;
		double a; // alpha
		double b; // beta

		public Node(int nodeType, S state, Node<S> parent, int depth) {
			this(nodeType, state, parent, new ArrayList<Node<S>>(), depth);
		}

		private Node(int nodeType, S state, Node<S> parent,
				List<Node<S>> childs, int depth) {
			super();
			this.nodeType = nodeType;
			this.state = state;
			this.val = nodeType == MAX_NODE ? Double.NEGATIVE_INFINITY
					: Double.POSITIVE_INFINITY;
			this.parent = parent;
			this.children = childs;
			this.depth = depth;
			if (parent == null) {
				a = Double.NEGATIVE_INFINITY;
				b = Double.POSITIVE_INFINITY;
			} else {
				a = parent.a;
				b = parent.b;
			}
		}

		@Override
		public String toString() {
			String typeStr = nodeType == MAX_NODE ? "MAX" : "MIN";
			return String.format("%s depth=%d val=%.0f a=%.0f b=%.0f\t%s",
					typeStr, depth, val, a, b, state.toStringFlat());
		}

		public void printTree() {
			printNode(0, this);
		}

		private void printNode(int numTab, Node<S> root) {
			for (int i = 0; i < numTab; i++) {
				System.out.print("\t");
			}
			System.out.println(root.toString());
			if (root.children != null) {
				for (Node<S> node : root.children) {
					printNode(numTab + 1, node);
				}
			}
		}

		public S getState() {
			return state;
		}

		public void setState(S state) {
			this.state = state;
		}

		public double getVal() {
			return val;
		}

		public void setVal(double val) {
			this.val = val;
		}

		public Node<S> getParent() {
			return parent;
		}

		public void setParent(Node<S> parent) {
			this.parent = parent;
		}

		public void setChildren(List<Node<S>> childs) {
			this.children = childs;
		}

		public int getDepth() {
			return depth;
		}

		public void setDepth(int depth) {
			this.depth = depth;
		}

		public List<Node<S>> getChildren() {
			return children;
		}

		public void oppoNodeType(List<Node<S>> children) {
			this.children = children;
		}

		public int changeType(int t) {
			if (t == MAX_NODE)
				return MIN_NODE;
			else
				return MAX_NODE;
		}

		public boolean hasAddtionalMove() {
			if (parent == null) {
				return false;
			} else if (state.gameOver()) {
				return false;
			} else {
				return parent.nodeType == nodeType;
			}
		}

		public boolean isAddtionalMove() {
			if (parent == null) {
				return false;
			} else {
				return parent.depth == depth;
			}
		}

		public void addChild(Node<S> node) {
			if (children == null) {
				children = new ArrayList<Node<S>>();
			}
			children.add(node);
		}

		public Node<S> getBestMove() {
			return bestMove;
		}

		public void setBestMove(Node<S> bestMove) {
			this.bestMove = bestMove;
		}

		public double getA() {
			return a;
		}

		public void setA(double a) {
			this.a = a;
		}

		public double getB() {
			return b;
		}

		public void setB(double b) {
			this.b = b;
		}

	}

}
