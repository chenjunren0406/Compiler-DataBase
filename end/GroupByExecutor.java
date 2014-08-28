package write;

import java.awt.PageAttributes.OriginType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import supplyedCode.AggFunc;
import supplyedCode.Attribute;
import supplyedCode.CatalogReader;
import supplyedCode.Grouping;
import supplyedCode.SQLParser;
import supplyedCode.Selection;
import supplyedCode.TableData;

public class GroupByExecutor {
	private SQLParser parser = null;
	private CatalogReader db = null;
	private Map <String, TableData> res = null;
	private SelectInfo sinfo;
	private WhereInfo winfo;
	private boolean isRealGroupBy = false;
	
	public GroupByExecutor(SQLParser parser,SelectInfo sinfo,WhereInfo winfo){
		this.db = new CatalogReader("./test/Catalog.xml");
		this.parser = parser;
		res = db.getCatalog ();
		this.sinfo = sinfo;
		this.winfo = winfo;
	}
	
	
	public String doGroupBy(DoResult s){
		
		ArrayList<CNFNode> aggf = new ArrayList<CNFNode>();
		ArrayList<String> groupByAtt = parser.getGROUPBY();
		
		for(CNFNode n : sinfo.getCnfNodes()){
			String opType = n.getOperationType();
			if(opType.equals("avg") || opType.equals("sum"))
				aggf.add(n);
		}
		
	   /*
		* inatts handle
		*/
		ArrayList<Attribute> inAtts = s.getOutAtts();
		
		
		/*
		 * outAtts handle
		 */
		ArrayList<Attribute> outAtts = new ArrayList<Attribute>();	
		
		ArrayList<CNFNode> selectExp = sinfo.getCnfNodes();
		HashMap <String, AggFunc> myAggs = new HashMap <String, AggFunc> ();
		/*
		 * in case this a simple projection
		 */
		HashMap<String, String> exprs = new HashMap<String,String>();
		for(int i = 0 ; i < selectExp.size() ; i++){
			CNFNode node = selectExp.get(i);
			outAtts.add(new Attribute(node.getOutputType(), "att"+ String.valueOf(i)));
			String curOutputType = helperOfMyAggs(node);
			String aggQuery = helperOfMyAggs2(node);
			myAggs.put("att"+ String.valueOf(i), new AggFunc(curOutputType, aggQuery));
			exprs.put("att" + String.valueOf(i), node.getquery());
		}
		
		/*
		 * groupingAtts handle
		 */
		ArrayList<String> groupingAtts = new ArrayList<String>();
		for(int i = 0; i < groupByAtt.size(); i++){
			/*
			 * get rid of "l."
			 */
			String tmp = CNFNode.getInfoFromOrId(groupByAtt.get(i), "i");
			groupingAtts.add(tmp);
		}
		
		String opTable = s.getTableName();
		
		/*
		 * this is a simply a projection
		 */
		if(isRealGroupBy == false && groupByAtt.size() == 0){
			String selection = "true";
			try {
			      Selection foo = new Selection (inAtts, outAtts, selection, exprs, opTable, "out.tbl", "g++", "cppDir/"); 
			    } catch (Exception e) {
			      throw new RuntimeException (e);
			    }
			
		}
		
		else{
			try {
			      @SuppressWarnings("unused")
				Grouping foo = new Grouping (inAtts, outAtts, groupingAtts, myAggs, opTable, "out.tbl", "g++", "cppDir/"); 
			    } catch (Exception e) {
			      throw new RuntimeException (e);
			    }
		}
		
		return "out.tbl";
		
	}
	
	/*
	 * helper to identify cnfNode type 
	 */
	private String helperOfMyAggs(CNFNode n){
		String type = n.getOperationType();
		if(!type.equals("avg") && !type.equals("sum"))
			type = "none";
		return type;
	}
	
	/*
	 * helper to get part of things out of query
	 * 
	 * input "sum(Int(1))" , outPut Int(1)
	 */
	private String helperOfMyAggs2(CNFNode n){
		String aggFuncType = helperOfMyAggs(n);
		String orginQuery = n.getquery();
		String resultQuery = null;
		
		switch (aggFuncType) {
		case "sum":
			isRealGroupBy = true;
			int position = 0;
			for(int i = 0 ; i < orginQuery.length() ; i++){
				if(orginQuery.charAt(i) == 'm'){
					position = i;
					break;
				}
			}
			resultQuery = orginQuery.substring(position+1,orginQuery.length() - 1);
			break;
		case "avg":
			isRealGroupBy = true;
			int position2 = 0;
			for(int i = 0 ; i < orginQuery.length() ; i++){
				if(orginQuery.charAt(i) == 'g'){
					position2 = i;
					break;
				}
			}
			resultQuery = orginQuery.substring(position2+1,orginQuery.length()-1);
			break;
		default:
			return orginQuery;
		}
		return resultQuery;
	}
}
