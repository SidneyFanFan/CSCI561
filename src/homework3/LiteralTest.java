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

public class LiteralTest {

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

	@Test
	public void testParse() {
		Literal l = new Literal("B(B,y)");
		Assert.assertEquals("B([B, y])", l.toString());
	}

	@Test
	public void testParse2() {
		Rule r = new Rule("R(x) ^ B(y) => H(x)");
		Assert.assertEquals("[R([x]), B([y])] => H([x])", r.toString());
	}

	@Test
	public void testIdentify1() {
		Literal l1 = new Literal("B(B,y)");
		Literal l2 = new Literal("B(B,y)");
		Assert.assertTrue(l1.identify(l2));
	}

	@Test
	public void testIdentify2() {
		Literal l1 = new Literal("B(B,y)");
		Literal l3 = new Literal("B(x0,y0)");
		Assert.assertFalse(l1.identify(l3));
	}

	@Test
	public void testIdentify3() {
		Literal l3 = new Literal("B(x0,y0)");
		Literal l4 = new Literal("B(x1,y1)");
		Assert.assertTrue(l3.identify(l4));
	}

	@Test
	public void testIdentify4() {
		Literal l1 = new Literal("B(B,y)");
		Literal l5 = new Literal("B(x1,y)");
		Assert.assertFalse(l1.identify(l5));
	}

}
