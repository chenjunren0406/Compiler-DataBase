package write;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import supplyedCode.CatalogReader;
import supplyedCode.GetKRecords;
import supplyedCode.SQLParser;

public class QueryExecutor {
	
	private SelectInfo sinfo = null;
	private WhereInfo  winfo = null;
	private SQLParser parser = null;
	private ArrayList<String> optable = null;
	private DoResult finalResult = null;
	private ArrayList<String> luo = null;
	
	//private CatalogReader reader = new CatalogReader("");
	/*
	 * use to store each of result after selection
	 */
	private HashMap<String, DoResult> mapResult = null; 
	
	public QueryExecutor(SelectInfo sinfo, WhereInfo winfo, SQLParser parser){
		this.sinfo = sinfo;
		this.winfo = winfo;
		this.parser = parser;
		optable = new ArrayList<String>(parser.getFROM().keySet());
		mapResult = new HashMap<String,DoResult>();
		this.luo = new ArrayList<String>();
		optimizer();
		System.out.println("\nExcuting query.....\n");
	}
	
	public void doSelection(){
		System.out.println("\nBegin to do selection.....\n");
		DoResult curResult = null;
		
		for(String table : optable){
			SelectionExecutor curSE = new SelectionExecutor(parser, sinfo, winfo, table);
			curResult = curSE.doSelection();
			mapResult.put(table, curResult);
		}
	}
	
	public void doJoin(){
		System.out.println("\nBegin to do Join.....\n");
		DoResult curResult = null;
		if(optable.size() < 2){
			finalResult = mapResult.get(optable.get(0));
			return;
		}
		else{
			String start = optable.get(0) ;
			curResult = mapResult.get(start);
			
			/*
			 * conditional join
			 */
			for(int i = 1 ; i < optable.size() ; i++){
				String nexttable = optable.get(i);
				DoResult nextResult = mapResult.get(nexttable);
				JoinExecutor je = new JoinExecutor(parser, sinfo, winfo,false);
				curResult = je.DoJoin(curResult, nextResult);
			}
			
			/*
			 * luo join
			 */
			for(String s : luo){
				DoResult nextResult = mapResult.get(s);
				JoinExecutor je = new JoinExecutor(parser, sinfo, winfo,true);
				curResult = je.DoJoin(curResult, nextResult);
			}
			finalResult = curResult;
		}
		
	}
	
	public void doGroupBy(){
		System.out.println("\nBegin to do group by.....\n");
		GroupByExecutor gb = new GroupByExecutor(parser, sinfo, winfo);
		gb.doGroupBy(finalResult);
	}
	
	public void print(){
		GetKRecords result = new GetKRecords("out.tbl", 30);
		result.print();
	}
	

	private void optimizer(){
		
		ArrayList<CNFNode> allCnfNodes = winfo.getCnfNodes();
		ArrayList<String> exSeq = new ArrayList<String>();
		for(CNFNode n:allCnfNodes){
			if(n.getOperationTablesShort().size() >= 2){
				ArrayList<String> relatedTable = new ArrayList<String>(n.getOperationTablesShort());
				for(String s: relatedTable){
					if(!exSeq.contains(s))
						exSeq.add(s);
				}
			}
		}
		
		/*
		 * in case some of tables never used in where clause, so we put the natural join at the end of query
		 */
		for(String s : optable){
			if(!exSeq.contains(s))
				luo.add(s);
		}
		
	}
}
