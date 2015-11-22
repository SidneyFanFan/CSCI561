package homework1.algorithm;

import java.util.LinkedList;
import java.util.List;

import homework1.basic.Pipe;
import homework1.basic.Problem;

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
		return shortcut;
	}

}
