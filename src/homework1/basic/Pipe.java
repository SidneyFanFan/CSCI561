package homework1.basic;

import java.util.Comparator;

public class Pipe {
	String start;
	String end;
	int length;
	int periodsNum;
	int[][] periods;

	public static final int HOURS_DAY = 24;

	@Override
	public String toString() {
		return String.format("%s %s %d %d", start, end, length, periodsNum);
	}

	/**
	 * Constructor
	 */
	public Pipe(String start, String end, int length, int periodsNum,
			int[][] periods) {
		super();
		this.start = start;
		this.end = end;
		this.length = length;
		this.periodsNum = periodsNum;
		this.periods = periods;
	}

	/**
	 * @param line
	 *            - String of parameters in form
	 *            "start end length #off-periods period1 .... periodn"
	 * @return Edge if parameters are valid, otherwise null
	 */
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

	public static final Comparator<Pipe> PIPE_COMPARATOR = new PipeComparator();

	private static class PipeComparator implements
			Comparator<Pipe> {

		@Override
		public int compare(Pipe p1, Pipe p2) {
			return p1.toString().compareTo(p2.toString());
		}

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

	public int getPeriodsNum() {
		return periodsNum;
	}

	public int[][] getPeriods() {
		return periods;
	}

	public static int getHoursDay() {
		return HOURS_DAY;
	}

}