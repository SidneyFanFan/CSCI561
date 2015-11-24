package homework3.FOL;

import java.util.ArrayList;
import java.util.List;

public class Rule {
	final List<Literal> condition;
	final Literal production;
	private String str;

	public Rule(String str) {
		str = str.replace(" ", "");
		int impliesPos = str.indexOf("=>");
		condition = new ArrayList<Literal>();
		String[] conditionStr = str.substring(0, impliesPos).split("\\^");
		for (String cstr : conditionStr) {
			condition.add(new Literal(cstr));
		}
		production = new Literal(str.substring(impliesPos + 2, str.length()));
		this.str = str;
	}

	@Override
	public String toString() {
		String s = String.format("%s => %s", condition.toString(), production);
		return s;
	}

	public List<Literal> getCondition() {
		return condition;
	}

	public Literal getProduction() {
		return production;
	}

	public Rule clone() {
		return new Rule(str);
	}

}
