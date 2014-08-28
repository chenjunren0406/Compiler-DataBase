package sematic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import supplyedCode.*;

public class SematicCheck {
	
	private SQLParser parser = null;
	private CatalogReader db = null;
	private Map <String, TableData> res = null;
	private Map<String,String> myFrom;
	
	public SematicCheck(SQLParser parser){
		this.db = new CatalogReader("./test/Catalog.xml");
		this.parser = parser;
		res = db.getCatalog ();
		myFrom = parser.getFROM();
	}
	
	public boolean MapExistCheck(){
		
		Iterator<String> inputTable = myFrom.values().iterator();
		
		while(inputTable.hasNext()){
			String tmp = inputTable.next();
			
			if(res.get(tmp) == null){
				System.out.println("In From Clause: Table " + "'" + tmp + "'" + " is not exist");
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * check the single expression
	 * @param e
	 * @return type
	 */
	public String SingleMatchCheck(Expression e){
		
		int type = WhichType(e);
		/*
		 * check if it is a value Types
		 */
		
		if(type == 1){
			if(e.getType().equals("identifier")){
				String value = e.getValue();
				
				String IdType = AttributesCheck(value);
				
				if(IdType == null)
					return IdType;
				
				switch (IdType) {
				case "Int":
					return "literal int";
				case "Float":
					return "literal float";
				case "Str":
					return "literal string";
				default:
					break;
				}
			}
			
			else 
				return e.getType();
		}
		
		/*
		 *  check if it is unary Types
		 */
		else if(type == 2){
			switch (e.getType()) {
			case "not":
				if(isPredict(e.getSubexpression()))
					return "and";
				else {
					System.out.println(e.getType() + " is not a predict");
					return null;
				}
				
			case "sum":
				String sub = SingleMatchCheck(e.getSubexpression());
				if(sub == null)
					return null;
				else if(sub.equals("literal int") || sub.equals("literal float"))
					return sub;
				else {
					System.out.println("Sum() only allow numeric input");
					return null;
				}
				
			case "avg":
			case "unary minus":
				String subtype = SingleMatchCheck(e.getSubexpression());
				if(subtype.equals("literal string")){
					System.out.println(e.getSubexpression().getType() + " can not deal with " + e.getType());
					return null;
				}
				else 
					return subtype;
			default:
				break;
			}
		}
		
		/*
		 * check if it is binary types
		 */
		else if(type == 3){
			
			Expression left = e.getSubexpression("left");
			Expression right = e.getSubexpression("right");
			
			String leftType = SingleMatchCheck(left);
			String rightType = SingleMatchCheck(right);
			
			/*
			 * if there is one null, then whole query is null 
			 */
			if(leftType == null || rightType == null)
				return null;
			
//			if(leftType.equals("literal string") && !rightType.equals("literal string")){
//				System.out.println(left.print() + " which type is "+ leftType +" not match with " + 
//									right.print() + "which type is " + rightType);
//				return null;
//			}
//			
//			if(!leftType.equals("literal string") && rightType.equals("literal string")){
//				System.out.println(left.print() + " which type is "+ leftType +" not match with " + 
//						right.print() + " which type is " + rightType);
//				return null;
//			}
			
			switch (e.getType()) {
			case "plus":
			
				if(leftType.equals(rightType))
					return leftType;
				else if(leftType.equals("literal string") || rightType.equals("literal string"))
					return "literal string";
				else
					return leftType.equals("literal int") ? rightType : leftType;
			
			case "greater than":
			case "less than":
			case "equals":
				if(!(leftType.equals("literal string") ^ rightType.equals("literal string")))
					return "and";
				else{
					System.out.println(left.print() + " is not match with " + right.print());
					return null;
				}
				
			case "minus":	
			case "times":
			case "divided by":	
				if(leftType.equals("literal string") || rightType.equals("literal string")){
					System.out.println(e.getType() + " can not deal with " + left.print() + " and " + right.print());
					return null;
				}
				else
					return leftType.equals("literal int") ? rightType : leftType;
			
			case "or":
			case "and":
				if(isPredict(left) && isPredict(right))
					return "and";
				else {
					System.out.println("predict problem: " +left.print() + " is " + leftType + 
							" and " + right.print() + " is " + rightType);
					return null;
				}
			default:
				
				return null;
			}

		}
		 
		return null;
	}
	
	/**
	 * To see what kinds of type of this attribute
	 * 
	 * @return type of attribute
	 */
	public String AttributesCheck(String identifier){
		String type = null;
		String table = "";
		String tableRealName = null;
		String attribute = "";
		boolean douHaoChuXian = false;
		
		for(int i = 0 ; i < identifier.length() ; i++){
			if(identifier.charAt(i) == '.'){
				douHaoChuXian = true;
				continue;
			}
			
			if(douHaoChuXian == false)
				table += String.valueOf(identifier.charAt(i));
			else 
				attribute += String.valueOf(identifier.charAt(i));
		}
			
			
			tableRealName = myFrom.get(table);
			
			if(tableRealName == null){
				System.out.println("'"+ table +"'"+ " has no reference defined before");
				return null;
			}
			AttInfo tmp = res.get(tableRealName).getAttInfo(attribute);
			
			if(tmp == null){
				System.out.println(attribute +" is not exist in " + tableRealName);
				return null;
			}
			type = tmp.getDataType();
			

		
		return type;
	}
	
	public boolean checkGroupBy(){
		
		ArrayList<String> groupBy = parser.getGROUPBY();
		ArrayList<Expression> select = parser.getSELECT();
		
		ArrayList<String> mustAppearInGroupBy = new ArrayList<String>();
		
		boolean hasSeperateIdentifier =  false;
		boolean hasAggregation = false;
		/*
		 * if there is no group
		 */
		if(groupBy.size() == 0 ){
			for(Expression e : select){
				if(e.getType().equals("identifier"))
					hasSeperateIdentifier = true;
				else if(e.getType().equals("sum") || e.getType().equals("avg"))
					hasAggregation = true;
			}
			if(hasSeperateIdentifier && hasAggregation ){
				System.out.println("all identifier has to appeare in aggregation");
				return false;
			}
		}
		
		/*
		 * there is group by
		 */
		else{
			for (Expression e : select) {
				if(e.getType().equals("identifier"))
					mustAppearInGroupBy.add(e.getValue());
			}
			
			for(String s : mustAppearInGroupBy){
				if(!groupBy.contains(s)){
					System.out.println(s + " need to be GROUP BY");
					return false;
				}
			}
		}
		
		
		return true;
	}
	
	/**
	 * There are three gen kinds of type
	 * 1 = value
	 * 2 = unary
	 * 3 = binary
	 * 0 = unknow
	 * @param e
	 * @return
	 */
	private int WhichType(Expression e){
		/*
		 * check if it is value types
		 */
		for(int i = 0 ; i < Expression.valueTypes.length ;i++){
			if(e.getType().equals(Expression.valueTypes[i]))
				return 1;
		}
		
		/*
		 *  check if it is unary Types
		 */
		for(int i = 0 ; i < Expression.unaryTypes.length ; i++){
			if(e.getType().equals(Expression.unaryTypes[i]))
				return 2;
		}
		
		/*
		 * check if it is binary types
		 */
		for(int i = 0 ; i < Expression.binaryTypes.length ; i++){
			if(e.getType().equals(Expression.binaryTypes[i]))
				return 3;
		}
		
		return 0;
	}
	
	private boolean isPredict(Expression e){
		
		if(e.getType().equals("equals"))
			return true;
		if(e.getType().equals("greater than"))
			return true;
		if(e.getType().equals("less than"))
			return true;
		if(e.getType().equals("or"))
			return true;
		if(e.getType().equals("and"))
			return true;
		if(e.getType().equals("not"))
			return true;
	
		return false;
	}
	
	public void check(){
		
		System.out.println();
		System.out.println("Sematic check begin....");
		System.out.println();
		
		/*
		 * check from
		 */
		if(!MapExistCheck()){
			System.out.println("from problem");
			return;
		}
		
		System.out.println("from check....Done!");
		System.out.println();
		/*
		 * check group by
		 */
		if(!checkGroupBy()){
			System.out.println("group by problem");
			return;
		}
		
		System.out.println("group by check....Done!");
		System.out.println();
		
		/*
		 * check where
		 */
		if(parser.getWHERE() != null && SingleMatchCheck(parser.getWHERE()) == null){
			System.out.println("where problem");
			return;
		}
		
		System.out.println("where caluse check....Done!");
		System.out.println();
		/*
		 * check select
		 */
		ArrayList<Expression> select = parser.getSELECT();
		
		for (Expression e : select) {
			if(SingleMatchCheck(e) == null){
				System.out.println("select problem: " + e.print() + " is mismatch");
				
				return;
			}
				
		}
		
		System.out.println("select check....Done!");
		System.out.println();
		System.out.println("The whole query is totally right");
	}
}
