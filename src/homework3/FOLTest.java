package homework3;

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
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// @Test
	// public void testResolvableTure() {
	// for (String[] s : literalsT) {
	// Assert.assertTrue(new Literal(s[0]).resolvable(new Literal(s[1])));
	// }
	// }
	//
	// @Test
	// public void testResolvableFalse() {
	// for (String[] s : literalsF) {
	// Assert.assertFalse(new Literal(s[0]).resolvable(new Literal(s[1])));
	// }
	// }

	@Test
	public void testParse() {
		Literal l = new Literal("B(B,y)");
		Rule r = new Rule("R(x) ^ B(y) => H(x)");
		Assert.assertEquals("B([B, y])", l.toString());
		Assert.assertEquals("[R([x]), B([y])] => H([x])", r.toString());
	}

}