package homework3;

public class Inference {

	public static void main(String[] args) {
		args = new String[] { "-i", "hw3/input_0.txt" };
//		args = new String[] { "-i", "hw3/test/test_1.txt" };
		if (args.length != 2) {
			System.out.println("Input format: -i <filename>");
			return;
		}
		// CONSTRUCTIONS
		BackwardChainingSystem bcs;
		// READ INPUT FILE
		bcs = new BackwardChainingSystem(args[1]);
		// INFERENCE DFS
		bcs.start("hw3/output.txt");
	}

}
