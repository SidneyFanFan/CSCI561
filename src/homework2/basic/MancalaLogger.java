package homework2.basic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MancalaLogger {

	public static final int MINIMAX_MODE = 1;
	public static final int PRUNING_MODE = 2;

	List<Trace> logList;
	List<String> log;
	BufferedWriter bw;
	String exportPath;
	int mode;
	int historySize;

	public MancalaLogger(String exportPath, int mode) {
		this.mode = mode;
		logList = new ArrayList<Trace>();
		log = new ArrayList<String>();
		historySize = 0;
		try {
			bw = new BufferedWriter(new FileWriter(exportPath, false));
			if (mode == MINIMAX_MODE) {
				bw.append("Node,Depth,Value\n");
			} else {
				bw.append("Node,Depth,Value,Alpha,Beta\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addTrace(String pit, int depth, double value, double a, double b) {
		Trace t = new Trace(pit, depth, value, a, b);
		// logList.add(t);
		if (mode == MINIMAX_MODE) {
			log.add(t.toSimpleString());
		} else {
			log.add(t.toString());
		}
		historySize++;
	}

	public void writeLog() {
		if (log.size() == 0) {
			return;
		}
		try {
			bw.append(toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.clear();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		// for (Trace trace : logList) {
		// if (mode == MINIMAX_MODE) {
		// sb.append(trace.toSimpleString());
		// } else {
		// sb.append(trace.toString());
		// }
		// sb.append("\n");
		// }
		for (String trace : log) {
			sb.append(trace);
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
			return String.format("%s,%d,%.0f,%.0f,%.0f", pit, depth, value, a,
					b);
		}

		public String toSimpleString() {
			return String.format("%s,%d,%.0f", pit, depth, value);
		}

	}

}
