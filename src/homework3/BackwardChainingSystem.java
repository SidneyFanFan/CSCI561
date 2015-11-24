package homework3;

import homework3.FOL.Literal;
import homework3.FOL.Rule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class BackwardChainingSystem {

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
		for (Literal query : queries) {
			boolean truth = false;
			// dfs
			System.out.println("Query:" + query);
			trace.add(query);
			truth = backwardChaining1(query, trace,
					new HashMap<String, Set<String>>());
			trace.remove(query);
			System.out.println(query + "=" + truth);
			sb.append(String.valueOf(truth).toUpperCase());
			sb.append("\n");
		}
		export(sb.toString(), exportPath);
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
			// standardize of variables
			// for (int i = 0; i < rules.size(); i++) {
			// Rule rule = rules.get(i);
			// for (Literal c : rule.getCondition()) {
			// for (int j = 0; j < c.getVariables().length; j++) {
			// c.getVariables()[j] += String.valueOf(i);
			// }
			// }
			// for (int j = 0; j < rule.getProduction().getVariables().length;
			// j++) {
			// rule.getProduction().getVariables()[j] += String.valueOf(i);
			// }
			// }
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	boolean backwardChaining1(Literal query, List<Literal> trace,
			Map<String, Set<String>> unification) {
		System.out.printf("bc: query=%s\ttrace=%s\n", query, trace);
		// compare with fact
		boolean hasFactMatched = false;
		Map<String, Set<String>> factUniSet = new HashMap<String, Set<String>>();
		Map<String, Set<String>> ruleUniSet = new HashMap<String, Set<String>>();

		/* using indexing */
		List<Literal> factList = factMap.get(query.getPredicate());
		if (factList != null) {
			for (Literal fact : factList) {
				System.out.printf("Applying fact: %s\n", fact);
				Map<String, String> factUnification = query.matchFact(fact);
				if (factUnification != null) {
					mergeUnification(factUniSet, factUnification);
					hasFactMatched = true; // fact and consistent
				}
			}
		}

		// compare with rules
		boolean resolvedByRule = false;

		/* using indexing */
		List<Rule> ruleList = ruleMap.get(query.getPredicate());
		if (ruleList != null) {
			for (Rule rule : ruleList) {
				rule = standardizeRule(rule);
				System.out.printf("Applying rule: %s\n", rule);
				boolean allConditionSatisfied = true;
				Map<String, String> productUnification = query.matchRule(rule);
				if (productUnification == null) {
					continue;
				}
				Map<String, Set<String>> jointConditionUnification = new HashMap<String, Set<String>>();
				mergeUnification(jointConditionUnification, productUnification);
				List<Literal> conditions = rule.getCondition();
				for (Literal con : conditions) {
					Literal c = con.clone();
					Literal.substitute(c, productUnification);
					if (loopDetected(trace, c)) {
						allConditionSatisfied = false;
					} else {
						Map<String, Set<String>> conditionUnification = new HashMap<String, Set<String>>();
						trace.add(c);
						allConditionSatisfied &= backwardChaining1(c, trace,
								conditionUnification);
						trace.remove(c);
						joinUnificationSet(jointConditionUnification,
								conditionUnification);
					}
					if (!allConditionSatisfied)
						break;
				}
				System.out.println(rule + "\t" + jointConditionUnification);
				if (allConditionSatisfied
						&& unifyCondition(jointConditionUnification, conditions)) {
					// all conditions are satisfied
					// and there is no conflict
					selectUnificationSet(ruleUniSet, jointConditionUnification,
							query);
					resolvedByRule |= true;
				}
			}
		}

		if (hasFactMatched) {
			mergeUnificationSet(unification, factUniSet);
			System.out.printf("return: fact uni= %s\n", factUniSet);
		}
		if (resolvedByRule) {
			mergeUnificationSet(unification, ruleUniSet);
			System.out.printf("return: rule uni= %s\n", ruleUniSet);
		}

		System.out.printf("return: query=%s\t\tunification=[%s] : %s\n", query,
				unification, hasFactMatched || resolvedByRule);
		return hasFactMatched || resolvedByRule;
	}

	private boolean loopDetected(List<Literal> trace, Literal c) {
		for (Literal literal : trace) {
			if (literal.identify(c)) {
				return true;
			}
		}
		return false;
	}

	private Rule standardizeRule(Rule rule) {
		Rule standardRule = rule.clone();
		for (Literal c : standardRule.getCondition()) {
			for (int j = 0; j < c.getVariables().length; j++) {
				c.getVariables()[j] += String.valueOf(RULE_COUNT);
			}
		}
		for (int j = 0; j < standardRule.getProduction().getVariables().length; j++) {
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
			if (map.containsKey(v)) {
				map.get(v).addAll(uniSet);
			} else {
				map.put(v, uniSet);
			}
		}
	}

	private boolean unifyCondition(Map<String, Set<String>> unification,
			List<Literal> conditions) {
		for (Literal literal : conditions) {
			for (String v : literal.getVariables()) {
				Set<String> uniSet = unification.get(v);
				if (uniSet == null) {
					return false;
				}
				if (uniSet.isEmpty()) {
					return false;
				}

			}
		}
		return true;
	}

	private boolean backwardChaining(Literal query, StringBuffer trace,
			Map<String, Set<String>> siblingUnification) {
		// System.out.printf("bc: %s\t[%s] [%s]\n", query, trace,
		// siblingUnification);
		trace.append(query.toString());
		trace.append(",");
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
					// check consistency
					if (unificationConsistent(siblingUnification,
							factUnification)) {
						mergeUnification(factUniSet, factUnification);
						hasFactMatched = true; // fact and consistent
					}
					// else check rules
				}
			}
		}

		/* no indexing using iteration */
		// for (Literal fact : facts) {
		// Map<String, String> factUnification = query.matchFact(fact);
		// if (factUnification != null) {
		// // check consistency
		// if (unificationConsistent(siblingUnification, factUnification)) {
		// mergeUnification(factUniSet, factUnification);
		// hasFactMatched = true; // fact and consistent
		// }
		// // else check rules
		// }
		// }
		// compare with rules
		boolean resolvedByRule = false;

		/* using indexing */
		List<Rule> ruleList = ruleMap.get(query.getPredicate());
		if (ruleList != null) {
			for (Rule rule : ruleList) {
				boolean resolvedWithOneRule = false;
				Map<String, String> productUnification = query.matchRule(rule);
				Map<String, Set<String>> possibleConditionUnification = new HashMap<String, Set<String>>();
				if (productUnification != null) {
					if (!unificationConsistent(siblingUnification,
							productUnification)) {
						continue; // this rule is inconsistent
					}
					resolvedWithOneRule = true;
					List<Literal> conditions = rule.getCondition();
					for (Literal con : conditions) { // B(x,y) ^ C(x,y) => A(x)
						// all variables in cnf should be consistent
						Literal c = con.clone();
						Literal.substitute(c, productUnification);
						if (trace.toString().contains(c.toString())) {
							// TODO may revisit
							// loop detected
							resolvedWithOneRule = false;
							break;
						} else {
							resolvedWithOneRule &= backwardChaining(c,
									new StringBuffer(trace.toString()),
									possibleConditionUnification);

						}
					}
					if (resolvedWithOneRule) {
						// TODO may false on condition
						mergeUnification(ruleUniSet, productUnification);
					}
					resolvedByRule |= resolvedWithOneRule;
				}
			}
		}

		/* no indexing using iteration */
		// for (Rule rule : rules) {
		// boolean resolvedWithOneRule = true;
		// Map<String, String> productUnification = query.matchRule(rule);
		// Map<String, Set<String>> possibleConditionUnification = new
		// HashMap<String, Set<String>>();
		// if (productUnification != null) {
		// if (!unificationConsistent(siblingUnification,
		// productUnification)) {
		// continue; // this rule is inconsistent
		// } else {
		// mergeUnification(ruleUniSet, productUnification);
		// }
		// List<Literal> conditions = rule.getCondition();
		// for (Literal con : conditions) { // B(x,y) ^ C(x,y) => A(x)
		// // all variables in cnf should be consistent
		// Literal c = con.clone();
		// Literal.substitute(c, productUnification);
		// if (trace.toString().contains(c.toString())) {
		// // loop detected
		// resolvedWithOneRule = false;
		// break;
		// } else {
		// resolvedWithOneRule &= backwardChaining(c,
		// new StringBuffer(trace.toString()),
		// possibleConditionUnification);
		//
		// }
		// }
		// resolvedByRule |= resolvedWithOneRule;
		// }
		// }
		if (hasFactMatched) {
			mergeUnificationSet(siblingUnification, factUniSet);
		}
		if (resolvedByRule) {
			mergeUnificationSet(siblingUnification, ruleUniSet);
		}
		return hasFactMatched || resolvedByRule;
	}

	private boolean unificationConsistent(Map<String, Set<String>> setUni,
			Map<String, String> uni) {
		for (Entry<String, String> en : uni.entrySet()) {
			if (setUni.containsKey(en.getKey())) {
				if (!setUni.get(en.getKey()).contains(en.getValue())) {
					return false; // inconsistency
				}
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
