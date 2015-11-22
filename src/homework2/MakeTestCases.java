package homework2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class MakeTestCases {

	public static void main(String[] args) {
		String output = "hw2/prunning/case_%d.txt";
		for (int i = 0; i < 100; i++) {
			String c = getACase(3);
			export(c, String.format(output, i));
		}
	}

	private static int[] randomArray(int n) {
		int[] arr = new int[n];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = (int) (Math.random() * 10) + 1;
		}
		return arr;
	}

	private static String getACase(int task) {
		int player = (int) (Math.random() * 2) + 1;
		while (player != 1 && player != 2)
			player = (int) (Math.random() * 2) + 1;
		int depth = task == 2 ? (int) (Math.random() * 5) + 1 : (int) (Math
				.random() * 10) + 1;
		int n = (int) (Math.random() * 9) + 2;
		int[] A = randomArray(n);
		int[] B = randomArray(n);
		int As = 0;
		int Bs = 0;
		String c = String.format("%d\n%d\n%d\n%s\n%s\n%d\n%d\n", task, player,
				depth, Arrays.toString(A), Arrays.toString(B), As, Bs);
		c = c.replaceAll("\\[|\\]|,", "");
		return c;
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
