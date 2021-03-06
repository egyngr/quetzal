package com.ibm.research.rdf.store.sparql11.sqltemplate;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.research.rdf.store.Context;
import com.ibm.research.rdf.store.Store;
import com.ibm.research.rdf.store.sparql11.model.Variable;
import com.ibm.research.rdf.store.sparql11.planner.PlanNode;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;

public abstract class HttpSQLTemplate extends JoinNonSchemaTablesSQLTemplate {

	public HttpSQLTemplate(List<String> templateName, Store store, Context ctx,
			STPlanWrapper wrapper, PlanNode planNode) {
		super(templateName, store, ctx, wrapper, planNode);
		this.planNode = planNode;
		wrapper.mapPlanNode(planNode);
	}

	public List<String> getProjectedVariablesFromPredecessor() {
		if (pred == null) {
			return Collections.emptyList();
		}
		List<String> projected = new LinkedList<String>();
		Set<Variable> vars = pred.getAvailableVariables();
		String leftSQLCte = wrapper.getPlanNodeCTE(pred, false);
		Set<Variable> joinVars = getJoinVariables();
		
		for(Variable v : vars) {
			if (joinVars.contains(v)) {
				continue;
			}
			String vPredNameLeft = wrapper.getPlanNodeVarMapping(pred,v.getName());
			projected.add(leftSQLCte + "." + vPredNameLeft);
			if (!wrapper.iriBoundVariablesInQuery.contains(v)) {
				projected.add(leftSQLCte + "." + vPredNameLeft + "_TYP");
			}
	
		}
		return projected;
	}

	protected List<Variable> getAllLiteralVars(Set<Variable> vars) {
		List<Variable> l = new LinkedList<Variable>();
		for (Variable v : vars) {
			if (!wrapper.iriBoundVariablesInQuery.contains(v)) {
				l.add(v);
			}
		}
		return l;
	}

	@Override
	Map<String, SQLMapping> populateMappings() {	
		// wrapper.getQuery().getPrologue().getPrefixes();
		
		Map<String, SQLMapping> mappings = HashMapFactory.make();

		mappings.put("sql_id", new SQLMapping("sql_id",wrapper.getPlanNodeId(planNode), null));
		
		List<String> targetList = getTargetMapping();
		SQLMapping tMapping = new SQLMapping("target", targetList, null);
		mappings.put("target", tMapping);
		
		Set<Variable> req = planNode.getRequiredVariables();
		Set<Variable> vars = planNode.getProducedVariables();
		List<String> cols = new LinkedList<String>();
		Set<String> firstProjectCols = HashSetFactory.make();
		Set<String> secondProjectCols = HashSetFactory.make();
		
		List<String> allColumns = new LinkedList<String>();

		for (Variable v : vars) {
			firstProjectCols.add((req.contains(v)? "pred": "xml") + "." + v.getName());
			allColumns.add(v.getName());
			allColumns.add(v.getName()+ "_TYP");
			secondProjectCols.add(wrapper.getPlanNodeCTE(planNode, false) + "_TMP." + v.getName());
			cols.add(v.getName());
		}
		
		List<Variable> literalVars = getAllLiteralVars(vars);
		Set<String> dtCols = HashSetFactory.make();
		List<String> dtConstraints = new LinkedList<String>();

		for (Variable v : literalVars) {
			String vt = v.getName() + "_TYP";
			firstProjectCols.add((req.contains(v)? "pred": "xml") + "." + vt);
			secondProjectCols.add(wrapper.getPlanNodeCTE(planNode, false) + "_TMP." + vt);
			dtCols.add(v.getName());
			dtConstraints.add("((" + vt + " IS NOT NULL AND " + vt + " = " + "DATATYPE_NAME) OR (" + vt + " IS NULL AND DATATYPE_NAME='SIMPLE_LITERAL_ID'))");
		}
					
		secondProjectCols.addAll(getProjectedVariablesFromPredecessor());
		mappings.put("firstProjectCols", new SQLMapping("firstProjectCols", firstProjectCols, null));
		mappings.put("secondProjectCols", new SQLMapping("secondProjectCols", secondProjectCols, null));
			
		if (pred != null) {
			String leftSQLCte = wrapper.getPlanNodeCTE(pred, false);
			String rightSQLCte = wrapper.getPlanNodeCTE(planNode, false) + "_TMP"; 
			List<String> joinConstraints = getJoinConstraintMapping(leftSQLCte, rightSQLCte);
			SQLMapping joinMapping = new SQLMapping("join_constraint", joinConstraints, null);
			mappings.put("join_constraint", joinMapping);
		}

		mappings.put("cols", new SQLMapping("cols", cols, null));
		mappings.put("dtCols", new SQLMapping("dtCols", dtCols, null));
		mappings.put("dtConstraints", new SQLMapping("dtConstraints", dtConstraints, null));
		mappings.put("dtTable", new SQLMapping("dtTable", this.store.getDataTypeTable(), null));

		return mappings;
	}
}