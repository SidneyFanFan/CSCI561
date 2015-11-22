package homework2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Competition {

	public static void main(String[] args) {
		args = new String[] { "-i", "hw2/next_input.txt" };
		int player = 3;
		int winner = -1;
		int depth = 10;
		winner = readStateTrasformToInput(2, player, depth);
		while (winner == -1) {
			long start = System.currentTimeMillis();
			System.out.printf("===PLAYER %d===\n", player);
			Mancala m = new Mancala(args[1]);
			m.start(1);
			long elapsedTime = System.currentTimeMillis() - start;
			System.out.printf("Time: %d ms\n", elapsedTime);
			player = 3 - player;
			depth = 20 - depth;
			winner = readStateTrasformToInput(3, player, depth);
		}
		System.out.printf("===WINNER: %d===", winner);
	}

	private static int readStateTrasformToInput(int task, int player, int cutOff) {
		int winner = -1;
		Scanner sc = null;
		StringBuffer sb = new StringBuffer();
		sb.append(task);
		sb.append("\n");
		sb.append(player);
		sb.append("\n");
		sb.append(cutOff);
		sb.append("\n");
		try {
			sc = new Scanner(new File("hw2/next_state.txt"));
			if (sc.hasNext()) {
				String A = sc.nextLine();
				String B = sc.nextLine();
				int As = sc.nextInt();
				int Bs = sc.nextInt();
				// judge
				if (win(A) || win(B)) {
					if (As > Bs) {
						winner = 2;
					} else if (As < Bs) {
						winner = 1;
					} else {
						winner = 0;
					}
				}
				sb.append(A);
				sb.append("\n");
				sb.append(B);
				sb.append("\n");
				sb.append(As);
				sb.append("\n");
				sb.append(Bs);
				sb.append("\n");
			} else {
				sc.close();
				throw new IllegalArgumentException("Empty input");
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		// System.out.println("Initialization finished:");
		// System.out.println(sb.toString());
		export(sb.toString(), "hw2/next_input.txt");
		return winner;
	}

	private static boolean win(String pits) {
		String[] split = pits.split(" ");
		for (String p : split) {
			if (!p.equals("0")) {
				return false;
			}
		}
		return true;
	}

	public static void export(String result, String filePath) {
		try {
			FileWriter fw = new FileWriter(filePath);
			fw.write(result);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
