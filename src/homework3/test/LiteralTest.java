package homework3.test;

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
	public void testIdentify_vvvv() {
		Literal l1 = new Literal("B(x0,y0)");
		Literal l2 = new Literal("B(x1,y1)");
		Assert.assertTrue(l1.identify(l2));
	}

	@Test
	public void testIdentify_vvvx() {
		Literal l1 = new Literal("B(x0,y0)");
		Literal l2 = new Literal("B(x1,E)");
		Assert.assertFalse(l1.identify(l2));
	}

	@Test
	public void testIdentify_vvxv() {
		Literal l1 = new Literal("B(x0,y0)");
		Literal l2 = new Literal("B(E,y1)");
		Assert.assertFalse(l1.identify(l2));
	}

	@Test
	public void testIdentify_vvxx() {
		Literal l1 = new Literal("B(x0,y0)");
		Literal l2 = new Literal("B(A,B)");
		Assert.assertFalse(l1.identify(l2));
	}

	@Test
	public void testIdentify_xvxv() {
		Literal l1 = new Literal("B(E,y0)");
		Literal l2 = new Literal("B(E,y1)");
		Assert.assertTrue(l1.identify(l2));
	}

	@Test
	public void testIdentify_vxvx() {
		Literal l1 = new Literal("B(x0,E)");
		Literal l2 = new Literal("B(x1,E)");
		Assert.assertTrue(l1.identify(l2));
	}

	@Test
	public void testIdentify_xvvx() {
		Literal l1 = new Literal("B(E,y0)");
		Literal l2 = new Literal("B(x1,E)");
		Assert.assertFalse(l1.identify(l2));
	}

	@Test
	public void testIdentify_vxxv() {
		Literal l1 = new Literal("B(x0,E)");
		Literal l2 = new Literal("B(E,y1)");
		Assert.assertFalse(l1.identify(l2));
	}

	@Test
	public void testIdentify_xxxv() {
		Literal l1 = new Literal("B(E,F)");
		Literal l2 = new Literal("B(E,y1)");
		Assert.assertFalse(l1.identify(l2));
	}

	@Test
	public void testIdentify_xxvx() {
		Literal l1 = new Literal("B(E,F)");
		Literal l2 = new Literal("B(x1,F)");
		Assert.assertFalse(l1.identify(l2));
	}

	@Test
	public void testIdentify_xxxx() {
		Literal l1 = new Literal("B(E,F)");
		Literal l2 = new Literal("B(E,F)");
		Assert.assertTrue(l1.identify(l2));
	}

	@Test
	public void testIdentify_xxxp() {
		Literal l1 = new Literal("B(E,F)");
		Literal l2 = new Literal("B(E,G)");
		Assert.assertFalse(l1.identify(l2));
	}

}
