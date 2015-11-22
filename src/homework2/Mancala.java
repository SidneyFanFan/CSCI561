package homework2;

import homework2.basic.MancalaLogger;
import homework2.basic.MancalaState;
import homework2.basic.Node;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Mancala {

	public static void main(String[] args) {
		args = new String[] { "-i", "hw2/input_0.txt" };

		if (args.length != 2) {
			System.out.println("Input format: -i <filename>");
			return;
		}
		long start = System.currentTimeMillis();
		Mancala m = new Mancala(args[1]);
		m.start(0);
		System.out.println(System.currentTimeMillis() - start);
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
	public Mancala(String stateConfigFile) {
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

	public void start(int i) {
		System.out.println("Start...");
		String dir = "hw2/";
		String nextStatePath = dir
				+ String.format("largecase_answer/prunningAnswer/next_state_%d.txt", i);
		String traverseLogPath = dir
				+ String.format("largecase_answer/prunningAnswer/traverse_log_%d.txt", i);
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
				// stateNode.addChild(node);
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
				// stateNode.addChild(node);
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
				// stateNode.addChild(node);
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
				// stateNode.addChild(node);
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
					Node.changeType(stateNode.nodeType), next, stateNode,
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
		if (size % 300000 == 0) {
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
}
