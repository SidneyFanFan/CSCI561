package homework2.basic;

import java.util.Arrays;

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
		String ret = String.format("%s\n%s\n%d\n%d\n", Arrays.toString(A),
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
		MancalaState oppoState = new MancalaState(A.clone(), B.clone(), AScore,
				BScore, 3 - who);
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
