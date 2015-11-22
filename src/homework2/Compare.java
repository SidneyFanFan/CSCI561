package homework2;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Compare {

	public static void main(String[] args) {
		String fa = "hw2/answer/traverse_log_%d.txt";
		String fb = "hw2/ma/traverse_log_%d.txt";
		Scanner sa, sb;
		for (int i = 0; i < 100; i++) {
			try {
				sa = new Scanner(new FileReader(String.format(fa, i)));
				sb = new Scanner(new FileReader(String.format(fb, i)));
				System.out.println("case:" + i);
				while (sa.hasNext()) {
					if (!sa.nextLine().equals(sb.nextLine())) {
						System.out.println("incorrect: " + i);
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
