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

	public Map<String, String> unify(Literal goal) {
		if (this.negation == goal.negation
				&& this.predicate.equals(goal.predicate)
				&& this.variables.length == goal.variables.length) {
			Map<String, String> unification = new HashMap<String, String>();
			for (int i = 0; i < variables.length; i++) {
				String x1 = this.variables[i];
				String x2 = goal.variables[i];
				boolean v1 = isVairable(x1);
				boolean v2 = isVairable(x2);
				if (!v1 && !v2) {
					if (!x1.equals(x2)) {
						return null;
					} else {
						// both are the same constant
						continue;
					}
				} else if (v1 && !v2) {
					addKVInUnification(unification, x1, x2);
					if (unification == null)
						return null;
				} else if (!v1 && v2) {
					addKVInUnification(unification, x2, x1);
					if (unification == null)
						return null;
				} else { // v1 and v2

				}
			}
		}
		// return null means cannot be unified
		return null;
	}

	private void addKVInUnification(Map<String, String> unification, String x1,
			String x2) {
		if (unification.containsKey(x1)) {
			String x1_value = unification.get(x1);
			if (isVairable(x1_value)) {
				// (x1,x1)/(x1_value,x2=John)
				// x1/x1_value is already there
				// now add {x1/x2=John} and {x1_value/x2=John}
				unification.put(x1, x2);
				addKVInUnification(unification, x1_value, x2);
			} else {
				// x1_value is constant
				// x2 is also constant
				// conflict return null
				unification = null;
			}
		} else {
			unification.put(x1, x2);
		}
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
				} else if (v2) {
					if (unification.containsKey(x2)) {
						if (unification.get(x2).equals(x1)) {
							continue;
						} else {
							return null;
						}
					} else {
						unification.put(x2, x1);
					}
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

	public static Literal substitute(Literal l, Map<String, String> unification) {
		Literal subst = l.clone();
		String[] vs = subst.variables;
		for (int i = 0; i < vs.length; i++) {
			if (unification.containsKey(vs[i])) {
				vs[i] = unification.get(vs[i]);
			}
		}
		return subst;
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

	public boolean identify(Literal c) {
		if ((this.negation == c.negation) && this.predicate.equals(c.predicate)
				&& this.variables.length == c.variables.length) {
			boolean v1, v2;
			boolean isIdentified = true;
			for (int i = 0; i < variables.length; i++) {
				v1 = isVairable(variables[i]);
				v2 = isVairable(c.variables[i]);
				if (v1 && v2) {
					isIdentified &= true;
				} else if (!v1 && !v2) {
					isIdentified &= variables[i].equals(c.variables[i]);
				} else {
					return false;
				}
			}
			return isIdentified;
		} else
			return false;
	}
}
