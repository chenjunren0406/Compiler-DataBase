package write;

import java.util.ArrayList;
import java.util.HashMap;

import supplyedCode.Attribute;

public class DoResult {
	private ArrayList<Attribute> outAtts;
	
	/**
	 * show "xxx.tbl"
	 */
	private String tableName;
	
	/**
	 * show "l","x","z" what kind of table this result have already down
	 */
	private ArrayList<String> optable;

	public DoResult(ArrayList<Attribute> outAtts, HashMap <String, String> exprs,String tableName, ArrayList<String>operatedTable) {
		this.outAtts = convert(outAtts, exprs);
		this.tableName = tableName;
		this.optable = operatedTable;
	}
	
	public ArrayList<Attribute> getOutAtts(){
		return outAtts;
	}
	
	public String getTableName(){
		return tableName;
	}
	
	public ArrayList<String> getOperatedTable(){
		return optable;
	}
	
	private ArrayList<Attribute> convert(ArrayList<Attribute>in,HashMap<String, String> exprs){
		
		ArrayList<Attribute> result = new ArrayList<Attribute>();
		
		for(int i = 0; i < in.size() ; i++){
			Attribute tmp = in.get(i);
			String tmpId = exprs.get(tmp.getName());
			result.add(new Attribute(tmp.getType(), tmpId));
			
		}
		
		
		return result;
	}
}
