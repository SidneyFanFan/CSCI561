package homework3.FOL;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Literal {
	boolean negation;
	String predicate;
	String[] variables; // not sure if there is P(Q(x))
	String oriStr;

	public Literal(String str) {
		oriStr = str;
		int lp = str.indexOf('(');
		if (str.charAt(0) == '~') {
			negation = true;
			predicate = str.substring(1, lp);
		} else {
			negation = false;
			predicate = str.substring(0, lp);
		}
		variables = str.substring(lp + 1, str.length() - 1).split(",");
	}

	public Literal(boolean negation, String predicate, String[] variables,
			String oriStr) {
		super();
		this.negation = negation;
		this.predicate = predicate;
		this.variables = variables;
		this.oriStr = oriStr;
	}

	@Override
	public String toString() {
		String s = negation ? "~" : "";
		s = s.concat(String.format("%s(%s)", predicate,
				Arrays.toString(variables)));
		return s;
	}

	public boolean isNegation() {
		return negation;
	}

	public void setNegation(boolean negation) {
		this.negation = negation;
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public String[] getVariables() {
		return variables;
	}

	public void setVariables(String[] variables) {
		this.variables = variables;
	}

	public Map<String, String> resolvable(Literal l) {
		if (this.negation == l.negation && this.predicate.equals(l.predicate)
				&& this.variables.length == l.variables.length) {
			Map<String, String> unification = new HashMap<String, String>();
			for (int i = 0; i < variables.length; i++) {
				String x1 = this.variables[i];
				String x2 = l.variables[i];
				boolean v1 = isVairable(x1);
				boolean v2 = isVairable(x2);
				if (!v1 && !v2) {
					if (!x1.equals(x2))
						return null;
				} else if (!v1 && v2) {
					unification.put(x2, x1);
				}
			}
			return unification;
		}
		return null;
	}

	public Map<String, String> matchFact(Literal fact) {
		if (this.negation == fact.negation
				&& this.predicate.equals(fact.predicate)
				&& this.variables.length == fact.variables.length) {
			Map<String, String> unification = new HashMap<String, String>();
			for (int i = 0; i < variables.length; i++) {
				String x1 = this.variables[i];
				String x2 = fact.variables[i];
				boolean v1 = isVairable(x1);
				boolean v2 = isVairable(x2);
				if (!v1 && !v2) {
					if (!x1.equals(x2))
						return null;
				} else if (v1 && !v2) {
					unification.put(x1, x2);
				}
			}
			return unification;
		}
		return null;
	}

	public Map<String, String> matchRule(Rule rule) {
		Literal p = rule.production;
		Map<String, String> unification = new HashMap<String, String>();
		if (!(this.negation ^ p.negation) && this.predicate.equals(p.predicate)
				&& this.variables.length == p.variables.length) {
			for (int i = 0; i < variables.length; i++) {
				String x1 = this.variables[i];
				String x2 = p.variables[i];
				boolean v1 = isVairable(x1);
				boolean v2 = isVairable(x2);
				if (!v1 && !v2) {
					if (!x1.equals(x2))
						return null;
				} else if (!v1 && v2) {
					unification.put(x2, x1);
				} else if (v1 && v2) {
					unification.put(x2, x1);
				}
			}
			// substitution
			// for (Literal con : rule.condition) {
			// conditions.add(new Literal(con.negation, con.predicate,
			// substitute(con.variables, unification), con.oriStr));
			// }
			return unification;
		}
		return null;
	}

	public static void substitute(Literal l, Map<String, String> unification) {
		String[] vs = l.variables;
		for (int i = 0; i < vs.length; i++) {
			if (unification.containsKey(vs[i])) {
				vs[i] = unification.get(vs[i]);
			}
		}
	}

	public static boolean isVairable(String x) {
		char c = x.charAt(0);
		return 'a' <= c && c <= 'z';
	}

	public Literal negate() {
		return new Literal(!negation, predicate, variables.clone(), oriStr);
	}

	public Literal clone() {
		return new Literal(negation, predicate, variables.clone(), oriStr);
	}

	public boolean isFact() {
		for (String x : variables) {
			if (isVairable(x))
				return false;
		}
		return true;
	}

}
