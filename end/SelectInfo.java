package write;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import antlr.Parser;
import supplyedCode.Expression;
import supplyedCode.SQLParser;

/**
 * 
 * @author junrenchen
 *
 */
public class SelectInfo {
	
	private ArrayList<Expression> select;
	
	private HashSet<String> id;
	
	private ArrayList<CNFNode> cnfnodes = new ArrayList<CNFNode>();
	
	private SQLParser parser;
	
	public SelectInfo(ArrayList<Expression> select,SQLParser parser){
		this.select = select;
		this.parser = parser;
		allidentifier();	
	}
	/**
	 * get cnfNode
	 * @return 
	 */
	public ArrayList<CNFNode> getCnfNodes(){
		return cnfnodes;
	}
	
	private HashSet<String> allidentifier(){
		id = new HashSet<String>();
		helperOfGettingidentifier();
		return id;
	}
	
	/*
	 * helper function of above one 
	 */
	private void helperOfGettingidentifier(){
		for(Expression e : select){
			CNFNode tmp = new CNFNode(e,parser);
			cnfnodes.add(tmp);
			Iterator<String> it = tmp.getAllidentifier().iterator();
			while(it.hasNext()){
				String curidentifer = it.next();
				id.add(curidentifer);
			}
		}
	}
	
	
	/**
	 *  input a table("o"), out put all identifier related to this table
	 */
	
	public ArrayList<String> getIdentifierRealted(String table){
		ArrayList<String> specificIdentifiers = new ArrayList<String>();
		for(String s : id){
			if(s.startsWith(table + String.valueOf(CNFNode.divider)))
				specificIdentifiers.add(s);
		}
		
		return specificIdentifiers;
	}
	
	
	public void print(){
		for(CNFNode c: cnfnodes)
			System.out.println(c.getquery());
	}
	
	
}
