package homework1.algorithm;

import homework1.basic.Pipe;
import homework1.basic.Problem;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class UniformCostSearch extends UninformedSearchAlgorithm {

	public static boolean NEXT_VALID_PATH = false;

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
				if (t.cost <= trace.cost && t.node.compareTo(trace.node) <= 0) {
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

	public void updateFrontier(Pipe pipe, int prevCost, LinkedList<Trace> queue) {
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
