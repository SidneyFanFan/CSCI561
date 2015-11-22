package homework1;

import homework1.algorithm.BreadFirstSearch;
import homework1.algorithm.DepthFirstSearch;
import homework1.algorithm.UniformCostSearch;
import homework1.algorithm.UninformedSearchAlgorithm;
import homework1.basic.Pipe;
import homework1.basic.Problem;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;

public class waterFlow {

	public static int CASE_NUM;
	public static Problem[] PROBLEMS;

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Please input with format -i <inputFile>");
			return;
		}
		UninformedSearchAlgorithm.SHORTCUT_MODE = false;
		DepthFirstSearch.RECURSIVE_SEARCH = true;
		UniformCostSearch.NEXT_VALID_PATH = false;

		args[1] = "hw1/sampleInput.txt";
		waterFlow m = new waterFlow();
		m.init(args);
		String result = m.startTask();
		m.export(result, "output.txt");
	}

	public void init(String[] args) {
		try {
			System.out.println("Initialization...");
			BufferedReader br = new BufferedReader(new FileReader(args[1]));
			CASE_NUM = Integer.valueOf(br.readLine());
			StringBuffer sb = new StringBuffer();
			while (br.ready()) {
				sb.append(br.readLine());
				sb.append("\n");
			}
			String[] split = sb.toString().split("\n\n");
			System.out.println("Reading finished...");
			PROBLEMS = new Problem[CASE_NUM];
			for (int i = 0; i < split.length; i++) {
				if (i % 100 == 0)
					System.out.println(i);
				PROBLEMS[i] = new Problem(split[i]);
				// exportCSVGraph(
				// String.format("src/homework1/csv/case%d.csv", i),
				// PROBLEMS[i]);
			}
			br.close();
			System.out.println("Initialization finished...");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void exportCSVGraph(String path, Problem problem) {
		String result = "source,target,value\n";
		for (Pipe p : problem.getPipes()) {
			result += String.format("%s,%s,%d,\n", p.getStart(), p.getEnd(),
					p.getLength());
		}
		export(result, path);
	}

	// void exportJsonGraph(String path, Problem problem) {
	// String result = "{\"nodes\":[%s],\"links\":[%s]}";
	// String nodesFormat = "{\"name\":\"%s\", \"group\":1,\"value\":1}";
	// String linkFormat = "{\"source\":%s, \"target\":%s,\"value\":%d}";
	// String nodes = "";
	// Map<String, Integer> map = new HashMap<String, Integer>();
	// int nodeCount = 0;
	// nodes += String.format(nodesFormat, problem.getSource());
	// map.put(problem.getSource(), nodeCount);
	// for (String d : problem.getDestinations()) {
	// nodeCount++;
	// map.put(d, nodeCount);
	// nodes += String.format(",\n" + nodesFormat, d);
	// }
	// for (String d : problem.getMiddleNodes()) {
	// nodeCount++;
	// map.put(d, nodeCount);
	// nodes += String.format(",\n" + nodesFormat, d);
	// }
	// String links = "";
	// for (Pipe p : problem.getPipes()) {
	// links += String.format(linkFormat + ",\n", map.get(p.getStart()),
	// map.get(p.getEnd()), p.getLength());
	// }
	// int p = links.lastIndexOf(",");
	// links = links.substring(0, p);
	// export(String.format(result, nodes, links), path);
	// }

	public String startTask() {
		String result = "";
		UninformedSearchAlgorithm usa = null;
		int count = 1;

		for (Problem problem : PROBLEMS) {
			System.out.printf("Start task %s...\n>Algorithm: %s\tSource: %s\n",
					count, problem.getTask(), problem.getSource());
			if (count == 952) {
				System.out.println(problem.toString());
			}
			switch (problem.getTask()) {
			case "BFS":
				usa = new BreadFirstSearch(problem);
				for(Pipe p: problem.getPipes()){
					System.out.println(p.toString());
				}
				break;
			case "DFS":
				usa = new DepthFirstSearch(problem);
				break;
			case "UCS":
				usa = new UniformCostSearch(problem);
				break;
			}
			if (usa == null) {
				System.out.printf("Task %d error: no algorithm %s\n", count,
						problem.getTask());
				count++;
			} else {
				result += String.format("%s\n", usa.search());
				System.out.printf("Task %d finished...\n", count);
				count++;
			}
		}
		System.out.println(result);
		return result;
	}

	public void export(String result, String filePath) {
		try {
			FileWriter fw = new FileWriter(filePath);
			fw.write(result);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
