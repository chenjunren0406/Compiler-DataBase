package write;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;

import org.antlr.gunit.swingui.parsers.StGUnitParser.output_return;
import org.omg.CORBA.PUBLIC_MEMBER;

import sematic.SematicCheck;
import supplyedCode.Expression;
import supplyedCode.SQLParser;

/**
 * CNF node will contains one where clause 
 * @author junrenchen
 *
 */
public class CNFNode {
	
	public static char divider = '_';
	
	private Expression e;
	
	/**
	 * Int Float Str Boolean
	 */
	private String outPutType = "";
	
	/**
	 * query for execute
	 */
	private String query = "";
	
	/**
	 * tables of this CNFNode need (short)
	 */
	private HashSet<String> tablesShort;
	
	/**
	 * keep record of all identifier related to this cnfNode
	 */
	private HashSet<String> identifier;
	
	
	private SQLParser parser;
	/**
	 * constuctor
	 * @param e
	 */
	public CNFNode(Expression e,SQLParser parser){
		this.e = e;
		tablesShort = new HashSet<String>();
		identifier = new HashSet<String>();
		this.parser = parser;
		query = queryString(query, e);
	}
	
	public String getquery(){
		return "( "+ query +" )";
	}
	
	public String getOperationType(){
		return e.getType();
	}
	public HashSet<String> getOperationTablesShort(){
		
		return tablesShort;
	}
	
	public HashSet<String> getAllidentifier(){
		return identifier;
	}
	
	public String getOutputType(){
		return outPutType;
	}
	/**
	 * t: input "l.l_xxx"; output "l"
	 * i: input "l.l_xxx"; output "l_l_xxx"
	 */
	public static String getInfoFromOrId (String query, String what){
		int start = 0;
		for(int i = 0 ; i < query.length() ; i++){
			if(query.charAt(i) == '.'){
				start = i+1;
				break;
			}
		}
		return what.equals("t") ? query.substring(0,start-1): query.substring(0, start - 1) +String.valueOf(divider)+query.substring(start);
	}
	
	public static String getInfoFromNewId(String newid, String what){
		int start = 0;
		for(int i = 0 ; i < newid.length() ; i++){
			if(newid.charAt(i) == divider ){
				start = i+1;
				break;
			}
		}
		return what.equals("t") ? newid.substring(0,start-1): newid.substring(start);
	}
	
	private String queryString(String result, Expression ex){	
		String type = null;
		
		switch (ex.getType()) {
		case "plus":
			type = " + ";
			break;

		case "minus":
			type = " - ";
			break;
			
		case "times":
			type = " * ";
			break;
			
		case "divided by":
			type = " / ";
			break;
			
		case "equals":
			type = " == ";
			outPutType = "Boolean";
			break;
			
		case "greater than":
			type = " > ";
			outPutType = "Boolean";
			break;
			
		case "less than":
			type = " < ";
			outPutType = "Boolean";
			break;
		
		case "not":
			return "!(" + queryString(result, ex.getSubexpression()) + ")";
			
		case "sum":
			return "sum ( " + queryString(result, ex.getSubexpression()) + " )";
		case "avg":
			return "avg ( " + queryString(result, ex.getSubexpression()) + " )";
		
		case "or":
			type = " || ";
			break;
		default:
			
			return subQueryString(ex);
		}
		return result + "(" + queryString(result, ex.getSubexpression("left")) + ")" +type+ "(" + queryString(result, ex.getSubexpression("right")) + ")";
			
	}
	
	// Do something like this
	//String selection = "o_orderdate > Str (\"1996-12-19\") && o_custkey < Int (100)";
	
	private String subQueryString(Expression ex){
		String valueOfExpression = ex.print();
		switch (ex.getType()) {
		case "literal string":
			if(!outPutType.equals("Boolean"))
				outPutType = "Str";
			return "Str " + "(" + valueOfExpression +")";
		case "literal float":
			if(!outPutType.equals("Boolean")&&!outPutType.equals("Str"))
				outPutType = "Float";
			return "Float (" + valueOfExpression + ")" ;
		case "literal int":
			if(outPutType.equals(""))
				outPutType = "Int";
			return "Int (" +valueOfExpression + ")";
		case "identifier":
			String curid = ex.print();
			outPutType = OutputType(curid, outPutType);
			tablesShort.add(getInfoFromOrId(curid, "t"));
			identifier.add(getInfoFromOrId(curid, "l"));
			return getInfoFromOrId(curid, "l");
		default:
			return null;
		}
	}
	
	/*
	 * helper for identifier to identify their dataType
	 */
	private String OutputType(String identifier,String curType){
		
		SematicCheck tmps = new SematicCheck(parser);
		
		String idType = tmps.AttributesCheck(identifier);
		
		switch (idType) {
		case "Int":
			if(!curType.equals(""))
				idType = curType;
			break;
		case "Float":
			if(curType.equals("Str"))
				idType = curType;
			break;
			
		case "Str":
			
			break;
		default:
			break;
		}
		
		return idType;
	}
	
}
