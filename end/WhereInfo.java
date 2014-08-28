package write;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import javax.security.sasl.RealmCallback;

import supplyedCode.Expression;
import supplyedCode.SQLParser;

/**
 *	get all information about query
 * @author junrenchen
 *
 */
public class WhereInfo {
	
	
	private ArrayList<Expression> cnfExpressions;
	
	/*
	 * will be ready for use after constructor
	 */
	private ArrayList<CNFNode> cnfNodes;
	
	
	private SQLParser parser;
	/**
	 * constructor
	 * @param where
	 */
	public WhereInfo(Expression where,SQLParser parser){
		this.parser = parser;
		getCNF(where);
		convertCnfNodes();	
	}
	
	/**
	 * get the CNFNode from where clause
	 */
	
	public ArrayList<CNFNode> getCnfNodes(){
		return cnfNodes;
	}
	
	/**
	 * get all related identifier about of table
	 * 
	 * input "l"
	 * @return
	 */
	public ArrayList<String> getAllRelatedIdentifier(String table){
		
		ArrayList<String> allidentifier = new ArrayList<String>();
		HashSet<String> tmpid = new HashSet<String>();
		
		for(CNFNode n : cnfNodes){
			
			if(n.getOperationTablesShort().contains(table)){
				HashSet<String> tmp = n.getAllidentifier();
				tmpid.addAll(tmp);
			}
		}
		
		for(String s:tmpid){
			if(s.startsWith(table + String.valueOf(CNFNode.divider)))
			allidentifier.add(s);
		}
		
		return allidentifier;
	}
	/**
	 * input a or multipule tables, then output related cnfNodes
	 * @param table is like "i","o"
	 * @return
	 */
	public ArrayList<CNFNode> getRelatedCnfNodes(ArrayList<String> tables){
		ArrayList<CNFNode> related = new ArrayList<CNFNode>();
		for(CNFNode n: cnfNodes){
			if(n.getOperationTablesShort().size() == tables.size()){
				for(String table : tables){
					if(n.getOperationTablesShort().contains(table) == false)
						break;
					related.add(n);
				}
			}
		}
		return related;
	}
	
	/**
	 * this function is for join use
	 * 
	 * @param t1 "x","y","z"  shortname of table
	 * @param t2  "a","b","c"
	 * @return a group of cnfnodes that has involved with t1 and also involved with t2,
	 *  if it only involve in one side, it will not count.
	 *  
	 *  x.ooo = b.ooo     ok
	 *  x.ooo = y.ooo     no!!!
	 *  
	 *  And this t1 and t2 must contains all tables it related, and it also must be splited in two table
	 *  
	 */
	public ArrayList<CNFNode> getRelatedCnfFor2Tables(ArrayList<String> t1,ArrayList<String> t2){
		ArrayList<CNFNode> result = new ArrayList<CNFNode>();
		/*
		 * make a union
		 */
		ArrayList<String> alltable = new ArrayList<String>(t1);
		alltable.addAll(t2);
		
		for(CNFNode n: cnfNodes){
			ArrayList<String> tmpTable = new ArrayList<String>(n.getOperationTablesShort());
			boolean t1IncludeAll = isSubClass(tmpTable, t1);
			boolean t2IncludeAll = isSubClass(tmpTable, t2);
			boolean allIncludeAll = isSubClass(tmpTable, alltable);
			
			if(t1IncludeAll == false && t2IncludeAll == false && allIncludeAll == true)
				result.add(n);
		}
		
		return result;
	}
	
	/*
	 * helper for preview function
	 * just see, whether son is a subclass of dad or not
	 */
	private boolean isSubClass(ArrayList<String>son, ArrayList<String>dad){
		for(String s : son){
			if(!dad.contains(s))
				return false;
		}
		
		return true;
	}

	/**
	 * split every and get each caluse out
	 */
	private void getCNF(Expression e){
		
		cnfExpressions = new ArrayList<Expression>();
		
		getCNFExp(e, cnfExpressions);
		
	}
	
	
	
	/**
	 * convert each split clause into a cnf node
	 */
	private void convertCnfNodes(){
		cnfNodes = new ArrayList<CNFNode>();
		
		for(Expression e : cnfExpressions)
			cnfNodes.add(new CNFNode(e,parser));
	}
	
	
	/**
	 * helpful function of getcnf
	 */
	private void getCNFExp(Expression e, ArrayList<Expression> result){
		
		if(!e.getType().equals("and")){
			result.add(e);
		}
		else{
			getCNFExp(e.getSubexpression("left"), result);
			getCNFExp(e.getSubexpression("right"), result);
		}
		
	}
	
	
}
