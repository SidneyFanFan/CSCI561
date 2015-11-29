package homework3;

public class Inference {

	public static void main(String[] args) {
		// args = new String[] { "-i", "hw3/input_0.txt" };
		// args = new String[] { "-i", "hw3/test/test_3.txt" };
		// args = new String[] { "-i",
		// "/Users/apple/Documents/workspace/inferencetestsample/samples/testInput.txt"
		// };
		args = new String[] { "-i", "hw3/test/siqi/input_4.txt" };
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

		// for (int i = 22; i <= 32; i++) {
		// String fileName = String.format("input_%d.txt", i);
		// args = new String[] { "-i", "hw3/test/naiqing/" + fileName };
		// if (args.length != 2) {
		// System.out.println("Input format: -i <filename>");
		// return;
		// }
		// // CONSTRUCTIONS
		// BackwardChainingSystem bcs;
		// // READ INPUT FILE
		// bcs = new BackwardChainingSystem(args[1]);
		// // INFERENCE DFS
		// bcs.start("hw3/test/naiqing/" + String.format("output_%d.txt", i));
		// }

	}

}
