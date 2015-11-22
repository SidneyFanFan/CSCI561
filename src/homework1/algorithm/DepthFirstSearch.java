package homework1.algorithm;

import java.util.LinkedList;
import java.util.List;

import homework1.basic.Pipe;
import homework1.basic.Problem;

public class DepthFirstSearch extends UninformedSearchAlgorithm {

	public static boolean RECURSIVE_SEARCH = false;

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
						(result.cost + problem.getStartTime()) % 24);
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
			List<Pipe> vp = problem.validPipes(nodeTrace.node, nodeTrace.cost
					+ problem.getStartTime());
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
