package homework1.basic;

import java.util.LinkedList;
import java.util.List;

public class Problem {

	/** Algorithm that you are supposed to use for this case */
	String task;

	/** Name of the source node */
	String source;

	/** Names of the destination nodes */
	String[] destinations;

	/** Names of the middle nodes */
	String[] middleNodes;

	/** Represents the number of pipes */
	int pipeNum;

	/** Represents start-end nodes, lengths and off-times of pipes */
	Pipe[] pipes;

	/** The time when water will start flowing */
	int startTime;

	String originString;

	public Problem(String string) {
		this(string.split("\n"));
		this.originString = string;
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
		vp.sort(Pipe.PIPE_COMPARATOR);
		vp.toArray(pipes);
		this.startTime = Integer.valueOf(lines[5 + pipeNum]) % 24;
	}

	@Override
	public String toString() {
		return originString;
	}

	public boolean GOAL_TEST(String node) {
		return isDestination(node);
	}

	public List<Pipe> validPipes(String node, int time) {
		time = time % 24;
		List<Pipe> vp = new LinkedList<Pipe>();
		for (Pipe pipe : pipes) {
			if (pipe.start.equals(node) && pipe.isValidTime(time)) {
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

	public String[] getDestinations() {
		return destinations;
	}

	public String[] getMiddleNodes() {
		return middleNodes;
	}

	public int getPipeNum() {
		return pipeNum;
	}

	public Pipe[] getPipes() {
		return pipes;
	}

	public int getStartTime() {
		return startTime;
	}

}