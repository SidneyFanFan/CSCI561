package homework3;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import homework3.FOL.Literal;
import homework3.FOL.Rule;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FOLTest {

	public static List<String[]> literalsT;
	public static List<String[]> literalsF;
	public static BackwardChainingSystem bcs;
	public static String[] configPaths;
	public static int CONFIG_IDX;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		literalsT = new ArrayList<String[]>();
		literalsT.add(new String[] { "L(x)", "L(y)" }); // T
		literalsT.add(new String[] { "L(x,y)", "L(x,y)" }); // T
		literalsT.add(new String[] { "L(x,y)", "L(A,y)" }); // T
		literalsT.add(new String[] { "L(x,y)", "L(x,B)" }); // T
		literalsT.add(new String[] { "L(x,y)", "L(A,B)" }); // T
		literalsT.add(new String[] { "L(A,y)", "L(x,B)" }); // T

		literalsF = new ArrayList<String[]>();
		literalsF.add(new String[] { "L(A,y)", "L(C,y)" }); // F
		literalsF.add(new String[] { "L(x,y)", "~L(x,y)" }); // F
		literalsF.add(new String[] { "L(x)", "L(x,y)" }); // F

		File dir = new File("hw3/test/");
		if (dir.isDirectory()) {
			File[] fs = dir.listFiles();
			configPaths = new String[fs.length];
			for (int i = 0; i < fs.length; i++) {
				configPaths[i] = fs[i].getAbsolutePath();
			}
		} else {
			configPaths = new String[] { "Error" };
		}
		CONFIG_IDX = 1;
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		if (CONFIG_IDX <= configPaths.length) {
			bcs = new BackwardChainingSystem(configPaths[CONFIG_IDX]);
		}
	}

	@After
	public void tearDown() throws Exception {
		CONFIG_IDX++;
	}

	@Test
	public void testInput() {
		bcs.start(String.format("hw3/test/output_%d.txt", CONFIG_IDX));
	}

	@Test
	public void testParse() {
		Literal l = new Literal("B(B,y)");
		Rule r = new Rule("R(x) ^ B(y) => H(x)");
		Assert.assertEquals("B([B, y])", l.toString());
		Assert.assertEquals("[R([x]), B([y])] => H([x])", r.toString());
	}

	@Test
	public void testIdentify() {
		Literal l1 = new Literal("B(B,y)");
		Literal l2 = new Literal("B(B,y)");
		Literal l3 = new Literal("B(x0,y0)");
		Literal l4 = new Literal("B(x1,y1)");
		Literal l5 = new Literal("B(x1,y)");
		Assert.assertTrue(l1.identify(l2));
		Assert.assertTrue(l1.identify(l3));
		Assert.assertTrue(l3.identify(l4));
		Assert.assertTrue(l1.identify(l5));

	}

}
