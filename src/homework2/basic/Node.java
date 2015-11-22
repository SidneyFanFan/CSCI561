package homework2.basic;

import java.util.ArrayList;
import java.util.List;

public class Node<S extends MancalaState> {
	public static final int MAX_NODE = 1;
	public static final int MIN_NODE = 2;

	public int nodeType;
	S state;
	double val;
	Node<S> parent;
//	List<Node<S>> children;
	int depth;
	Node<S> bestMove;
	double a; // alpha
	double b; // beta

	public Node(int nodeType, S state, Node<S> parent, int depth) {
		this(nodeType, state, parent, new ArrayList<Node<S>>(), depth);
	}

	private Node(int nodeType, S state, Node<S> parent, List<Node<S>> childs,
			int depth) {
		super();
		this.nodeType = nodeType;
		this.state = state;
		this.val = nodeType == MAX_NODE ? Double.NEGATIVE_INFINITY
				: Double.POSITIVE_INFINITY;
		this.parent = parent;
//		this.children = childs;
		this.depth = depth;
		if (parent == null) {
			a = Double.NEGATIVE_INFINITY;
			b = Double.POSITIVE_INFINITY;
		} else {
			a = parent.a;
			b = parent.b;
		}
	}

	@Override
	public String toString() {
		String typeStr = nodeType == MAX_NODE ? "MAX" : "MIN";
		return String.format("%s depth=%d val=%.0f a=%.0f b=%.0f\t%s", typeStr,
				depth, val, a, b, state.toStringFlat());
	}

	public void printTree() {
		printNode(0, this);
	}

	private void printNode(int numTab, Node<S> root) {
		for (int i = 0; i < numTab; i++) {
			System.out.print("\t");
		}
		System.out.println(root.toString());
//		if (root.children != null) {
//			for (Node<S> node : root.children) {
//				printNode(numTab + 1, node);
//			}
//		}
	}

	public S getState() {
		return state;
	}

	public void setState(S state) {
		this.state = state;
	}

	public double getVal() {
		return val;
	}

	public void setVal(double val) {
		this.val = val;
	}

	public Node<S> getParent() {
		return parent;
	}

	public void setParent(Node<S> parent) {
		this.parent = parent;
	}

//	public void setChildren(List<Node<S>> childs) {
//		this.children = childs;
//	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

//	public List<Node<S>> getChildren() {
//		return children;
//	}
//
//	public void oppoNodeType(List<Node<S>> children) {
//		this.children = children;
//	}

	public static int changeType(int t) {
		if (t == MAX_NODE)
			return MIN_NODE;
		else
			return MAX_NODE;
	}

	public boolean hasAddtionalMove() {
		if (parent == null) {
			return false;
		} else if (state.gameOver()) {
			return false;
		} else {
			return parent.nodeType == nodeType;
		}
	}

	public boolean isAddtionalMove() {
		if (parent == null) {
			return false;
		} else {
			return parent.depth == depth;
		}
	}

//	public void addChild(Node<S> node) {
//		if (children == null) {
//			children = new ArrayList<Node<S>>();
//		}
//		children.add(node);
//	}

	public Node<S> getBestMove() {
		return bestMove;
	}

	public void setBestMove(Node<S> bestMove) {
		this.bestMove = bestMove;
	}

	public double getA() {
		return a;
	}

	public void setA(double a) {
		this.a = a;
	}

	public double getB() {
		return b;
	}

	public void setB(double b) {
		this.b = b;
	}

}
