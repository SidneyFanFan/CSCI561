import homework3.BackwardChainingSystem;

public class inference {

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Input format: -i <filename>");
			return;
		}
		// CONSTRUCTIONS
		BackwardChainingSystem bcs;
		// READ INPUT FILE
		bcs = new BackwardChainingSystem(args[1]);
		// INFERENCE DFS
		bcs.start("output.txt");

	}

}
