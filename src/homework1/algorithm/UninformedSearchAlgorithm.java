package homework1.algorithm;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import homework1.basic.Pipe;
import homework1.basic.Problem;

public abstract class UninformedSearchAlgorithm {

	Problem problem;
	LinkedList<Trace> frontier;
	Set<Trace> explored;

	public static boolean SHORTCUT_MODE = true;

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
			// System.out.println("frontier: " + frontier.toString());
			// System.out.println("explored: " + explored.toString());
			nodeTrace = frontier.pop();
			explored.add(nodeTrace);
			if (problem.isDestination(nodeTrace.node)) {
				dst = String.format("%s %d", nodeTrace.node,
						(nodeTrace.cost + problem.getStartTime()) % 24);
				break;
			}
			vp = problem.validPipes(nodeTrace.node,
					nodeTrace.cost + problem.getStartTime());
			Trace shortcut = insert(vp, nodeTrace.cost, frontier);
			// short cut function
			if (shortcut != null && SHORTCUT_MODE) {
				dst = String.format("%s %d", shortcut.node,
						(shortcut.cost + problem.getStartTime()) % 24);
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
			return String.format("%s %s %d", node, parent, cost);
		}
	}
}
