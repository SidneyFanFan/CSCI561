package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Main {
	public static int CASE_NUM;
	public static Problem[] PROBLEMS;
	public static final int HOURS_DAY = 24;

	public static boolean SHORTCUT_MODE = false;

	/** Different mode for bfs and dfs */
	public static boolean SHORTCUT_MODE_BFS = true; // TODO
	public static boolean SHORTCUT_MODE_DFS = false; // TODO
	public static boolean IGNORE_TIME = false; // TODO

	public static boolean RECURSIVE_SEARCH = false;
	public static boolean NEXT_VALID_PATH = false;

	public static Comparator<Pipe> PIPE_COMPARATOR = new PipeComparator();

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Please input with format -i <inputFile>");
			return;
		}

		args[1] = "hw1/sampleInput.txt";
		Main m = new Main();
		m.init(args);
		String result = m.startTask();
		m.export(result, "output.txt");
	}

	public void init(String[] args) {

		try {
			System.out.println("Initialization...");
			BufferedReader br = new BufferedReader(new FileReader(args[1]));
			CASE_NUM = Integer.valueOf(br.readLine());
			StringBuffer sb = new StringBuffer();
			while (br.ready()) {
				sb.append(br.readLine());
				sb.append("\n");
			}
			String[] split = sb.toString().split("\n\n");
			PROBLEMS = new Problem[split.length];
			for (int i = 0; i < split.length; i++) {
				PROBLEMS[i] = new Problem(split[i]);
			}
			br.close();
			System.out.println("Initialization finished...");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String startTask() {
		String result = "";
		UninformedSearchAlgorithm usa = null;
		int count = 1;

		for (Problem problem : PROBLEMS) {
			System.out.printf("Start task %s...\n>Algorithm: %s\tSource: %s\n",
					count, problem.getTask(), problem.getSource());
			switch (problem.getTask()) {
			case "BFS":
				SHORTCUT_MODE = SHORTCUT_MODE_BFS;
				IGNORE_TIME = true;
				usa = new BreadFirstSearch(problem);
				break;
			case "DFS":
				SHORTCUT_MODE = SHORTCUT_MODE_DFS;
				IGNORE_TIME = true;
				usa = new DepthFirstSearch(problem);
				break;
			case "UCS":
				IGNORE_TIME = false;
				usa = new UniformCostSearch(problem);
				break;
			}
			if (usa == null) {
				System.out.printf("Task %d error: no algorithm %s\n", count,
						problem.getTask());
				count++;
			} else {
				result += String.format("%s\n", usa.search());
				System.out.printf("Task %d finished...\n", count);
				count++;
			}
		}
		System.out.println(result);
		return result;
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

	public class Problem {

		String task;
		String source;
		String[] destinations;
		String[] middleNodes;
		int pipeNum;
		Pipe[] pipes;
		int startTime;

		public Problem(String string) {
			this(string.split("\n"));
		}

		public Problem(String[] lines) {
			if (lines.length < 7) {
				System.out.println("Incorrect parameter format");
				return;
			}
			this.task = lines[0];
			this.source = lines[1];
			this.destinations = lines[2].split(" ");
			this.middleNodes = lines[3].split(" ");
			this.pipeNum = Integer.valueOf(lines[4]);
			this.pipes = new Pipe[pipeNum];
			List<Pipe> vp = new LinkedList<Pipe>();
			Pipe p;
			// sorted by alpha bet
			for (int i = 0; i < pipeNum; i++) {
				p = new Pipe(lines[5 + i]);
				vp.add(p);
			}
			vp.sort(PIPE_COMPARATOR);
			vp.toArray(pipes);
			this.startTime = Integer.valueOf(lines[5 + pipeNum]);
		}

		public boolean GOAL_TEST(String node) {
			return isDestination(node);
		}

		public List<Pipe> validPipes(String node, int time) {
			List<Pipe> vp = new LinkedList<Pipe>();
			for (Pipe pipe : pipes) {
				if (pipe.start.equals(node)
						&& (pipe.isValidTime(time) || IGNORE_TIME)) {
					vp.add(pipe);
				}
			}
			return vp;
		}

		public boolean isDestination(String node) {
			for (String d : destinations) {
				if (d.equals(node))
					return true;
			}
			return false;
		}

		public String getTask() {
			return task;
		}

		public String getSource() {
			return source;
		}

		public int getStartTime() {
			return startTime;
		}

	}

	public abstract class UninformedSearchAlgorithm {

		Problem problem;
		LinkedList<Trace> frontier;
		Set<Trace> explored;

		public UninformedSearchAlgorithm(Problem problem) {
			this.problem = problem;
			init();
		}

		public abstract Trace insert(List<Pipe> vp, int prevCost,
				LinkedList<Trace> queue);

		private void init() {
			frontier = new LinkedList<Trace>();
			explored = new HashSet<Trace>();
			frontier.add(new Trace(problem.getSource(), "ROOT", 0));
		}

		public String search() {
			String dst = "None";
			// String child;
			Trace nodeTrace;
			List<Pipe> vp;
			while (!frontier.isEmpty()) {
				// System.out.println("FR: " + frontier);
				// System.out.println("EX: " + explored);
				nodeTrace = frontier.pop();
				explored.add(nodeTrace);
				if (problem.isDestination(nodeTrace.node)) {
					dst = String.format("%s %d", nodeTrace.node,
							(nodeTrace.cost + problem.getStartTime())
									% HOURS_DAY);
					break;
				}
				vp = problem.validPipes(nodeTrace.node, nodeTrace.cost
						+ problem.getStartTime());
				Trace shortcut = insert(vp, nodeTrace.cost, frontier);
				// short cut function
				if (shortcut != null && SHORTCUT_MODE) {
					dst = String.format("%s %d", shortcut.node,
							(shortcut.cost + problem.getStartTime())
									% HOURS_DAY);
					break;
				}
			}
			return String.format("%s", dst);
		}

		protected Trace collectionContainsTrace(String node, Collection<Trace> c) {
			for (Trace trace : c) {
				if (trace.node.toString().equals(node))
					return trace;
			}
			return null;
		}

		class Trace {
			String node;
			String parent;
			int cost;

			public Trace(String node, String parent, int cost) {
				super();
				this.node = node;
				this.parent = parent;
				this.cost = cost;
			}

			@Override
			public String toString() {
				return String.format("%s %s", node, parent, cost);
			}

		}
	}

	public class BreadFirstSearch extends UninformedSearchAlgorithm {

		public BreadFirstSearch(Problem problem) {
			super(problem);
		}

		public Trace insertPipe(Pipe pipe, int prevCost, LinkedList<Trace> queue) {
			String child = pipe.getEnd();
			Trace t = new Trace(pipe.getEnd(), pipe.getStart(), prevCost + 1);
			if (collectionContainsTrace(child, frontier) == null
					&& collectionContainsTrace(child, explored) == null) {
				if (problem.isDestination(child) && SHORTCUT_MODE) {
					// visited a destination
					return t;
				}
				queue.add(t);
			}
			return null;
		}

		@Override
		public Trace insert(List<Pipe> vp, int prevCost, LinkedList<Trace> queue) {
			Pipe pipe;
			Trace shortcut = null;
			for (int i = 0; i < vp.size(); i++) {
				pipe = vp.get(i);
				shortcut = insertPipe(pipe, prevCost, queue);
				if (shortcut != null && SHORTCUT_MODE) {
					break;
				}
			}
			if (IGNORE_TIME) // TODO resort without time check
				queue.sort(new Comparator<Trace>() {
					@Override
					public int compare(Trace o1, Trace o2) {
						return o1.cost - o2.cost == 0 ? o1.node
								.compareTo(o2.node) : o1.cost - o2.cost;
					}
				});
			return shortcut;
		}

	}

	public class DepthFirstSearch extends UninformedSearchAlgorithm {

		public DepthFirstSearch(Problem problem) {
			super(problem);
		}

		public Trace insertPipe(Pipe pipe, int prevCost, LinkedList<Trace> queue) {
			String child = pipe.getEnd();
			Trace t = new Trace(pipe.getEnd(), pipe.getStart(), prevCost + 1);
			// check frontier = expand but not explore
			// no check frontier = recursive version
			if (collectionContainsTrace(child, frontier) == null
					&& collectionContainsTrace(child, explored) == null) {
				if (problem.isDestination(child) && SHORTCUT_MODE) {
					// visited a destination
					return t;
				}
				queue.push(t);
			}
			return null;
		}

		@Override
		public Trace insert(List<Pipe> vp, int prevCost, LinkedList<Trace> queue) {
			Pipe pipe;
			Trace shortcut = null;
			for (int i = vp.size() - 1; i >= 0; i--) {
				pipe = vp.get(i);
				shortcut = insertPipe(pipe, prevCost, queue);
				if (shortcut != null && SHORTCUT_MODE) {
					break;
				}
			}
			return shortcut;
		}

		// recursive search
		public String search() {
			if (RECURSIVE_SEARCH) {
				System.out.println("recursive search...");
				Trace result = recursiveSearch(new Trace(problem.getSource(),
						"ROOT", 0));
				String dst;
				if (result == null) {
					return "None";
				} else {
					dst = String.format("%s %d", result.node,
							(result.cost + problem.getStartTime()) % HOURS_DAY);
					return dst;
				}
			} else {
				return super.search();
			}
		}

		private Trace recursiveSearch(Trace nodeTrace) {
			if (problem.isDestination(nodeTrace.node)) {
				return nodeTrace;
			} else {
				explored.add(nodeTrace);
				System.out.println("explored: " + explored.toString());
				List<Pipe> vp = problem.validPipes(nodeTrace.node,
						nodeTrace.cost + problem.getStartTime());
				Pipe pipe;
				Trace t;
				Trace result;
				for (int i = 0; i < vp.size(); i++) {
					pipe = vp.get(i);
					t = new Trace(pipe.getEnd(), pipe.getStart(),
							nodeTrace.cost + 1);
					if (collectionContainsTrace(t.node, explored) == null) {
						result = recursiveSearch(t);
						if (result != null) {
							return result;
						}
					}
				}
				return null;
			}

		}
	}

	public class UniformCostSearch extends UninformedSearchAlgorithm {

		public UniformCostSearch(Problem problem) {
			super(problem);
		}

		public void insertPipe(Pipe pipe, int prevCost, LinkedList<Trace> queue) {
			String child = pipe.getEnd();
			Trace trace;
			Trace t = new Trace(pipe.getEnd(), pipe.getStart(), prevCost
					+ pipe.getLength());
			if (collectionContainsTrace(child, frontier) == null
					&& containsTraceWithTime(t, explored) == null) {
				int i;
				for (i = 0; i < queue.size(); i++) {
					trace = queue.get(i);
					if (t.cost <= trace.cost
							&& t.node.compareTo(trace.node) <= 0) {
						break;
					} else if (t.cost < trace.cost) {
						break;
					}
				}
				queue.add(i, t);
			}
			if (collectionContainsTrace(child, frontier) != null) {
				// update
				updateFrontier(pipe, prevCost, frontier);
			}

		}

		private Trace containsTraceWithTime(Trace t, Collection<Trace> list) {
			if (NEXT_VALID_PATH) {
				for (Trace trace : list) {
					if (trace.toString().equals(t.toString()))
						return trace;
				}
				return null;
			} else {
				return collectionContainsTrace(t.node, list);
			}

		}

		public void updateFrontier(Pipe pipe, int prevCost,
				LinkedList<Trace> queue) {
			Trace trace;
			Trace t = new Trace(pipe.getEnd(), pipe.getStart(), prevCost
					+ pipe.getLength());
			trace = collectionContainsTrace(pipe.getEnd(), queue);
			if (trace != null) {
				// update
				if (t.cost < trace.cost) {
					queue.remove(trace);
					int i;
					for (i = 0; i < queue.size(); i++) {
						trace = queue.get(i);
						if (t.cost <= trace.cost
								&& t.node.compareTo(trace.node) <= 0) {
							break;
						} else if (t.cost < trace.cost) {
							break;
						}
					}
					queue.add(i, t);
				}
				return;
			}
		}

		@Override
		public Trace insert(List<Pipe> vp, int prevCost, LinkedList<Trace> queue) {
			Pipe pipe;
			for (int i = 0; i < vp.size(); i++) {
				pipe = vp.get(i);
				insertPipe(pipe, prevCost, queue);
			}
			return null; // never short cut in UCS
		}

	}

	public class Pipe {
		String start;
		String end;
		int length;
		int periodsNum;
		int[][] periods;

		@Override
		public String toString() {
			return String.format("%s %s %d %d", start, end, length, periodsNum);
		}

		public Pipe(String start, String end, int length, int periodsNum,
				int[][] periods) {
			super();
			this.start = start;
			this.end = end;
			this.length = length;
			this.periodsNum = periodsNum;
			this.periods = periods;
		}

		public Pipe(String line) {
			try {
				String[] split = line.split(" ");
				this.start = split[0];
				this.end = split[1];
				this.length = Integer.valueOf(split[2]);
				this.periodsNum = Integer.valueOf(split[3]);
				this.periods = new int[periodsNum][2];
				for (int i = 0; i < periodsNum; i++) {
					periods[i][0] = Integer.valueOf(split[4 + i].split("-")[0]);
					periods[i][1] = Integer.valueOf(split[4 + i].split("-")[1]);
				}
			} catch (Exception e) {
				return;
			}
		}

		public boolean isValidTime(int time) {
			time = time % HOURS_DAY;
			for (int[] period : periods) {
				if (period[0] <= period[1]) {
					if (period[0] <= time && time <= period[1]) {
						return false;
					}
				} else { // 22-3: 22, 23, 0, 1, 2, 3
					if (period[0] <= time || time <= period[1]) {
						return false;
					}
				}
			}
			return true;
		}

		public String getStart() {
			return start;
		}

		public String getEnd() {
			return end;
		}

		public int getLength() {
			return length;
		}

	}

	private static class PipeComparator implements Comparator<Pipe> {

		@Override
		public int compare(Pipe p1, Pipe p2) {
			return p1.toString().compareTo(p2.toString());
		}

	}

}
