package homework3;

import homework3.FOL.Literal;
import homework3.FOL.Rule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

public class BackwardChainingSystem {

	Set<String> constants;
	List<Literal> queries;
	List<Rule> rules;
	List<Literal> facts;

	private static int RULE_COUNT = 0;

	/** Rule Indexing */
	Map<String, List<Rule>> ruleMap;

	/** Fact Indexing */
	Map<String, List<Literal>> factMap;

	public BackwardChainingSystem(String configPath) {
		queries = new ArrayList<Literal>();
		rules = new ArrayList<Rule>();
		facts = new ArrayList<Literal>();
		constants = new HashSet<String>();
		init(configPath);
		// indexing
		ruleMap = new HashMap<String, List<Rule>>();
		factMap = new HashMap<String, List<Literal>>();
		buildIndexing();
	}

	private void buildIndexing() {
		String p;
		List<Rule> ruleList;
		for (Rule r : rules) {
			p = r.getProduction().getPredicate();
			ruleList = ruleMap.get(p);
			if (ruleList == null) {
				ruleList = new ArrayList<Rule>();
			}
			ruleList.add(r);
			ruleMap.put(p, ruleList);
		}
		List<Literal> factList;
		for (Literal f : facts) {
			p = f.getPredicate();
			factList = factMap.get(p);
			if (factList == null) {
				factList = new ArrayList<Literal>();
			}
			factList.add(f);
			factMap.put(p, factList);
		}
	}

	public void start(String exportPath) {
		StringBuffer sb = new StringBuffer();
		List<Literal> trace = new ArrayList<Literal>();

		PrintStream sysout = System.out;
		for (int i = 0; i < queries.size(); i++) {
			long s = System.currentTimeMillis();
			// try {
			// System.setOut(new PrintStream(new File(String.format(
			// "hw3/test/generated/%d_log.txt", i))));
			// } catch (FileNotFoundException e) {
			// e.printStackTrace();
			// }
			Literal query = queries.get(i);
			boolean truth = false;
			// dfs
			System.out.println("Query:" + query);
			trace.add(query);
			truth = backwardChaining1(query, trace,
					generateInitUnification(query));
			trace.remove(query);
			sysout.printf("%dth: %s = %s\t %d\n", i + 1, query, truth,
					System.currentTimeMillis() - s);
			sb.append(String.valueOf(truth).toUpperCase());
			sb.append("\n");
			// break;
		}
		System.setOut(sysout);

		export(sb.toString(), exportPath);
	}

	private Map<String, Set<String>> generateInitUnification(Literal query) {
		Map<String, Set<String>> map = new HashMap<String, Set<String>>();
		for (String v : query.getVariables()) {
			if (Literal.isVairable(v))
				map.put(v, new HashSet<String>(constants));
		}
		return map;
	}

	private void init(String configPath) {
		Scanner sc = null;
		try {
			sc = new Scanner(new File(configPath));
			if (sc.hasNext()) {
				String str;
				int qnum = sc.nextInt();
				sc.nextLine();// skip
				for (int i = 0; i < qnum; i++) {
					str = sc.nextLine();
					queries.add(new Literal(str));
				}
				int kbnum = sc.nextInt();
				sc.nextLine();// skip
				for (int i = 0; i < kbnum; i++) {
					str = sc.nextLine();
					if (str.contains("=>")) {
						rules.add(new Rule(str));
					} else {
						facts.add(new Literal(str));
					}
				}
			} else {
				sc.close();
				throw new IllegalArgumentException("Empty input");
			}
			// Get all constant
			for (Literal fact : facts) {
				for (String c : fact.getVariables()) {
					if (!Literal.isVairable(c))
						constants.add(c);
				}
			}
			for (Rule rule : rules) {
				for (Literal condition : rule.getCondition()) {
					for (String c : condition.getVariables())
						if (!Literal.isVairable(c))
							constants.add(c);
				}
				for (String c : rule.getProduction().getVariables())
					if (!Literal.isVairable(c))
						constants.add(c);
			}

			System.out.println("Initialization finished:");
			System.out.println("KB:");
			for (Literal fact : facts) {
				System.out.println(fact);
			}
			for (Rule rule : rules) {
				System.out.println(rule);
			}
			System.out.println("Query:");
			for (Literal query : queries) {
				System.out.println(query);
			}
			System.out.println("Constants: " + constants);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	boolean backwardChaining1(Literal query, List<Literal> trace,
			Map<String, Set<String>> unification) {
		System.out.printf("bc: query=%s\ttrace=%s\tnunification=%s\n", query,
				trace, unification);
		// check if unification is workable
		// if (!isValidUnification(unification)) {
		// return false;
		// }
		// compare with fact
		boolean hasFactMatched = false;
		Map<String, Set<String>> factUniSet = new HashMap<String, Set<String>>();
		Map<String, Set<String>> ruleUniSet = new HashMap<String, Set<String>>();

		/* using indexing */
		List<Literal> factList = factMap.get(query.getPredicate());
		if (factList != null) {
			for (Literal fact : factList) {
				Map<String, String> factUnification = query.matchFact(fact);
				if (factUnification != null) {
					System.out.printf("Match fact: %s\n", fact);
					mergeUnification(factUniSet, factUnification);
					hasFactMatched = true; // fact and consistent
				}
			}
		}

		if (hasFactMatched) {
			joinUnificationSet(factUniSet, unification);
			System.out.printf("return fact for [%s] uni= %s\n", query,
					factUniSet);
			// if (factUniSet.equals(unification))
			// return true;
		}

		// OPTIMIZATION 1
		// remove possible constants from fact already achieve

		// compare with rules
		boolean resolvedByRule = false;

		/* using indexing */
		List<Rule> ruleList = ruleMap.get(query.getPredicate());
		if (ruleList != null) {
			for (Rule rule : ruleList) {
				rule = standardizeRule(rule);
				boolean allConditionSatisfied = true;
				Map<String, String> productUnification = query.matchRule(rule);
				System.out.printf("Applying rule: %s\t%s\n", rule,
						productUnification);
				if (productUnification == null) {
					continue; // actually we are checking negation
				}
				Map<String, Set<String>> jointConditionUnification = generateInitUnification(rule); // TODO
				joinUnificationMapToSet(jointConditionUnification,
						productUnification);
				// mapSetDifference(jointConditionUnification, factUniSet);
				List<Literal> conditions = rule.getCondition();
				for (Literal con : conditions) {
					Literal c = con.clone();
					Literal.substitute(c, productUnification);
					if (loopDetected(trace, c)) {
						System.out.println("Loop detected: " + c);
						allConditionSatisfied = false;
					} else {
						Map<String, Set<String>> conditionUnification = generateInitUnification(c);
						// mapSetDifference(conditionUnification, factUniSet);
						trace.add(c);
						allConditionSatisfied &= backwardChaining1(c, trace,
								conditionUnification);
						trace.remove(c);
						joinUnificationSet(jointConditionUnification,
								conditionUnification);
						System.out.printf(
								"After checking condition [%s]: %s\n", c,
								jointConditionUnification);
					}
					if (!allConditionSatisfied)
						break;
				}
				if (allConditionSatisfied
						&& unifyCondition(jointConditionUnification, conditions)) {
					// all conditions are satisfied
					// and there is no conflict
					selectUnificationSet(ruleUniSet, jointConditionUnification,
							query);
					System.out.println(rule + "\t" + jointConditionUnification);
					resolvedByRule |= true;
				}
			}
		}

		if (resolvedByRule) {
			joinUnificationSet(ruleUniSet, unification);
			System.out.printf("return rule for [%s] uni= %s\n", query,
					ruleUniSet);
		}
		// (Fact OR Rule) AND Unification
		unification.clear();
		mergeUnificationSet(unification, factUniSet);
		mergeUnificationSet(unification, ruleUniSet);

		System.out.printf("return: query=%s\t\tunification=[%s] : %s\n", query,
				unification, hasFactMatched || resolvedByRule);
		return hasFactMatched || resolvedByRule;
	}

	public void startFOL(String exportPath) {
		StringBuffer sb = new StringBuffer();
		List<Literal> trace = new ArrayList<Literal>();
		PrintStream sysout = System.out;
		for (int i = 0; i < queries.size(); i++) {
			long s = System.currentTimeMillis();
			Literal query = queries.get(i);
			boolean truth = false;
			// dfs
			System.out.println("Query:" + query);
			trace.add(query);
			truth = FOL_BC_ASK(query);
			trace.remove(query);
			sysout.printf("%dth: %s = %s\t %d\n", i + 1, query, truth,
					System.currentTimeMillis() - s);
			sb.append(String.valueOf(truth).toUpperCase());
			sb.append("\n");
		}
		System.setOut(sysout);
		export(sb.toString(), exportPath);
	}

	public boolean FOL_BC_ASK(Literal query) {
		return FOL_BC_OR(ruleMap, factMap, query,
				new HashMap<String, String>(),
				new HashSet<Map<String, String>>(), new Stack<Literal>());
	}

	static boolean FOL_BC_OR(Map<String, List<Rule>> ruleMap,
			Map<String, List<Literal>> factMap, Literal goal,
			Map<String, String> theta, Set<Map<String, String>> yields,
			Stack<Literal> track) {
		System.out.printf("FOL_BC_OR: goal=%s, theta=%s, track=%s\n", goal,
				theta, track);
		if (loopDetected(track, goal)) {
			System.out.println("Loop detected");
			return false;
		} else {
			track.push(goal);
		}
		boolean matched = false;
		// for each fact
		List<Literal> factList = factMap.get(goal.getPredicate());
		if (factList != null) {
			for (Literal fact : factList) {
				Map<String, String> factUnification = goal.matchFact(fact);
				if (factUnification != null) {
					for (Entry<String, String> en : theta.entrySet()) {
						if (factUnification.containsKey(en.getKey())) {
							if (!factUnification.get(en.getKey()).equals(
									en.getValue())) {
								continue;
							}
						} else {
							factUnification.put(en.getKey(), en.getValue());
						}
					}
					System.out.printf("Match fact: %s\n", fact);
					yields.add(factUnification);
					matched |= true;

				}
			}
		}
		// for each rule (lhs => rhs) in FETCH-RULES-FOR-GOAL(KB, goal) do
		List<Rule> ruleList = ruleMap.get(goal.getPredicate());
		if (ruleList != null) {
			for (Rule rule : ruleList) {
				rule = standardizeRule(rule);
				System.out.printf("Checking rule: %s\n", rule);
				Set<Map<String, String>> yields_and = new HashSet<Map<String, String>>();
				if (!FOL_BC_AND(ruleMap, factMap, rule.getCondition(),
						UNIFY(rule.getProduction(), goal, theta), yields_and,
						track)) {
					matched |= false;
				} else {
					for (Map<String, String> theta2 : yields_and) {
						yields.add(theta2);
					}
					matched |= true;
				}
			}
		}
		track.pop();
		return matched;
	}

	static boolean FOL_BC_AND(Map<String, List<Rule>> ruleMap,
			Map<String, List<Literal>> factMap, List<Literal> goals,
			Map<String, String> theta, Set<Map<String, String>> yields,
			Stack<Literal> track) {
		System.out.printf("FOL_BC_AND: goal=%s, theta=%s, track=%s\n", goals,
				theta, track);
		if (theta == null)
			return false;
		else if (goals.isEmpty()) {
			yields.add(theta);
			return true;
		} else {
			Literal first = goals.get(0).clone();
			Literal.substitute(first, theta);
			Set<Map<String, String>> yields_or = new HashSet<Map<String, String>>();
			if (!FOL_BC_OR(ruleMap, factMap, first, theta, yields_or, track)) {
				
				return false;
			} else {
				boolean hasThetaSatisfied = false;
				for (Map<String, String> theta2 : yields_or) {
					HashSet<Map<String, String>> yields_and = new HashSet<Map<String, String>>();
					if (!FOL_BC_AND(ruleMap, factMap,
							goals.subList(1, goals.size()), theta2, yields_and,
							track)) {
						hasThetaSatisfied |= false;
					} else {
						for (Map<String, String> AND_subs : yields_and) {
							yields.add(AND_subs);
						}
						hasThetaSatisfied |= true;
					}
				}
				return hasThetaSatisfied;
			}
		}
	}

	public static Map<String, String> UNIFY(Literal rhs, Literal goal,
			Map<String, String> theta) {
		// substitute variables in rhs to goal
		// specifically,
		// for case (x,x)/(y,z), add (y=z) to substitution
		// for case (x,x)/(C,z), add (z=C,x=C,x=z) to substitution
		// for case (x,x)/(y,C), add (y=C,x=C,x=y) to substitution
		// for case (y,z)/(x,x), do as usual
		// for case (C,x)/(y,z), add (y=C) to substitution
		if (rhs.isNegation() == goal.isNegation()
				&& rhs.getPredicate().equals(goal.getPredicate())
				&& rhs.getVariables().length == goal.getVariables().length) {
			Map<String, String> product_substitution = new HashMap<String, String>();
			String[] rhs_variables = rhs.getVariables();
			String[] goal_variables = goal.getVariables();
			for (int i = 0; i < rhs_variables.length; i++) {
				String x1 = rhs_variables[i];
				String x2 = goal_variables[i];
				boolean v1 = Literal.isVairable(x1);
				boolean v2 = Literal.isVairable(x2);
				if (!v1 && !v2) {
					if (!x1.equals(x2)) {
						return null;
					} else {
						// both are the same constant
						continue;
					}
				} else if (v1 && !v2) {
					if (!addUnification(product_substitution, x1, x2))
						return null;
				} else if (!v1 && v2) {
					if (!addUnification(product_substitution, x2, x1))
						return null;
				} else { // v1 and v2
					if (!addUnification(product_substitution, x1, x2))
						return null;
				}
			}
			// check chaining
			for (Entry<String, String> en : product_substitution.entrySet()) {
				String key = en.getKey();
				String value = en.getValue();
				if (Literal.isVairable(key) && Literal.isVairable(value)) {
					while (value != null) {
						if (!Literal.isVairable(value)) {
							en.setValue(value);
							break;
						}
						value = product_substitution.get(value);
					}
				}
			}
			// checking theta TODO
			for (Entry<String, String> en : theta.entrySet()) {
				if (product_substitution.containsKey(en.getKey())) {
					if (product_substitution.get(en.getKey()).equals(
							en.getValue())) {
						continue;
					} else {
						return null; // conflict
					}
				} else {
					product_substitution.put(en.getKey(), en.getValue());
				}
			}
			return product_substitution;
		}
		return null;
	}

	public static boolean addUnification(Map<String, String> unification,
			String x1, String x2) {
		if (unification == null)
			return false;
		if (unification.containsKey(x1)) {
			String y = unification.get(x1);
			if (Literal.isVairable(y)) {
				if (Literal.isVairable(x2)) {
					if (y.equals(x2)) {
						return true;
					} else {
						if (!addUnification(unification, x2, y))
							return false;
					}
				} else {
					// for case (x,x)/(y,C), already has x=y
					// add (y=C) to substitution
					unification.put(x1, x2);
					if (!addUnification(unification, y, x2))
						return false;
					// (x,x,x)/(y,C,D) or (x,x,x)/(y,z,D)
				}

			} else {
				if (Literal.isVairable(x2)) {
					if (!addUnification(unification, x2, y))
						return false;
				} else {
					if (y.equals(x2)) {
						return true;
					} else {
						return false;
					}
				}
			}
		} else {
			unification.put(x1, x2);
		}
		return true;
	}

	private boolean isValidUnification(Map<String, Set<String>> unification) {
		for (Entry<String, Set<String>> en : unification.entrySet()) {
			if (en.getValue().isEmpty()) {
				return false;
			}

		}
		return true;
	}

	// private void mapSetDifference(Map<String, Set<String>> x,
	// Map<String, Set<String>> y) {
	// for (Entry<String, Set<String>> en : x.entrySet()) {
	// if (y.containsKey(en.getKey())) {
	// en.getValue().removeAll(y.get(en.getKey()));
	// }
	// }
	// }

	private Map<String, Set<String>> generateInitUnification(Rule rule) {
		Map<String, Set<String>> map = new HashMap<String, Set<String>>();
		for (Literal l : rule.getCondition()) {
			for (String v : l.getVariables()) {
				if (Literal.isVairable(v))
					map.put(v, new HashSet<String>(constants));
			}
		}
		for (String v : rule.getProduction().getVariables()) {
			if (Literal.isVairable(v))
				map.put(v, new HashSet<String>(constants));
		}
		return map;
	}

	private static boolean loopDetected(List<Literal> trace, Literal c) {
		for (Literal literal : trace) {
			if (literal.identify(c)) {
				return true;
			}
		}
		return false;
	}

	static Rule standardizeRule(Rule rule) {
		Rule standardRule = rule.clone();
		for (Literal c : standardRule.getCondition()) {
			for (int j = 0; j < c.getVariables().length; j++) {
				if (Literal.isVairable(c.getVariables()[j]))
					c.getVariables()[j] += String.valueOf(RULE_COUNT);
			}
		}
		for (int j = 0; j < standardRule.getProduction().getVariables().length; j++) {
			if (Literal
					.isVairable(standardRule.getProduction().getVariables()[j]))
				standardRule.getProduction().getVariables()[j] += String
						.valueOf(RULE_COUNT);
		}
		RULE_COUNT++;
		return standardRule;
	}

	private void selectUnificationSet(Map<String, Set<String>> map,
			Map<String, Set<String>> unification, Literal query) {
		for (String v : query.getVariables()) {
			if (!Literal.isVairable(v))
				continue;
			Set<String> uniSet = unification.get(v);
			if (uniSet == null) {
				// the choice of v is anything
				continue;
			}
			try {
				if (map.containsKey(v)) {
					map.get(v).addAll(uniSet);
				} else {
					map.put(v, uniSet);
				}
			} catch (NullPointerException e) {
				System.err.println(map);
				System.err.println(unification);
				System.err.println(query);
				System.exit(0);
			}
		}
	}

	private boolean unifyCondition(Map<String, Set<String>> unification,
			List<Literal> conditions) {
		for (Literal literal : conditions) {
			for (String v : literal.getVariables()) {
				if (Literal.isVairable(v)) {
					Set<String> uniSet = unification.get(v);
					if (uniSet == null) {
						return false;
					}
					if (uniSet.isEmpty()) {
						return false;
					}
				} else
					continue;
			}
		}
		return true;
	}

	private void mergeUnification(Map<String, Set<String>> setUni,
			Map<String, String> uni) {
		for (Entry<String, String> en : uni.entrySet()) {
			if (!setUni.containsKey(en.getKey())) {
				Set<String> s = new HashSet<String>();
				s.add(en.getValue());
				setUni.put(en.getKey(), s);
			} else {
				setUni.get(en.getKey()).add(en.getValue());
			}
		}
	}

	private void joinUnificationSet(Map<String, Set<String>> ToSetUni,
			Map<String, Set<String>> AppendSetUni) {
		for (Entry<String, Set<String>> en : AppendSetUni.entrySet()) {
			if (!ToSetUni.containsKey(en.getKey())) {
				ToSetUni.put(en.getKey(), en.getValue());
			} else {
				ToSetUni.put(en.getKey(),
						joinSet(ToSetUni.get(en.getKey()), en.getValue()));
			}
		}
	}

	private void joinUnificationMapToSet(Map<String, Set<String>> setMap,
			Map<String, String> stringMap) {
		for (Entry<String, Set<String>> en : setMap.entrySet()) {
			if (stringMap.containsKey(en.getKey())) {
				en.getValue().clear();
				en.getValue().add(stringMap.get(en.getKey()));
			}
		}
	}

	private Set<String> joinSet(Set<String> set1, Set<String> set2) {
		Set<String> set = new HashSet<String>();
		for (String s1 : set1) {
			if (set2.contains(s1))
				set.add(s1);
		}
		return set;
	}

	private void mergeUnificationSet(Map<String, Set<String>> ToSetUni,
			Map<String, Set<String>> AppendSetUni) {
		for (Entry<String, Set<String>> en : AppendSetUni.entrySet()) {
			if (!ToSetUni.containsKey(en.getKey())) {
				ToSetUni.put(en.getKey(), en.getValue());
			} else {
				ToSetUni.get(en.getKey()).addAll(en.getValue());
			}
		}
	}

	private void export(String result, String filePath) {
		try {
			FileWriter fw = new FileWriter(filePath);
			fw.write(result);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
