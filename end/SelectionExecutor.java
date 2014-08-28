package write;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import sematic.SematicCheck;
import supplyedCode.AttInfo;
import supplyedCode.Attribute;
import supplyedCode.CatalogReader;
import supplyedCode.Expression;
import supplyedCode.SQLParser;
import supplyedCode.Selection;
import supplyedCode.TableData;

/**
 * happen on one table
 * @author junrenchen
 *
 */
public class SelectionExecutor {
	
	private SQLParser parser = null;
	private CatalogReader db = null;
	private Map <String, TableData> res = null;
	private SelectInfo sinfo;
	private WhereInfo winfo;
	private String table;
	public SelectionExecutor(SQLParser parser,SelectInfo sinInfo, WhereInfo winfo,String table){
		this.db = new CatalogReader("./test/Catalog.xml");
		this.parser = parser;
		res = db.getCatalog ();
		this.sinfo = sinInfo;
		this.winfo = winfo;
		this.table = table;
	}
	
	/**
	 * input table need to be like "l", "o","s"
	 * @param table
	 * @param info
	 * 
	 * return "xxx.tbl"
	 */
	public DoResult doSelection(){
		
		/*
		 * input atts
		 */
		String fullNameOfTable = parser.getFROM().get(table);
		
		System.out.println("Now Doing with Selection with " + fullNameOfTable + ".tbl" );
		
		TableData td = res.get(fullNameOfTable);
		
		ArrayList<Attribute> inAtts = new ArrayList<Attribute>();
		
		/*
		 *  make these things with sequence
		 */
		for(int i = 0 ; i < td.getAttributes().size() ; i++){
			inAtts.add(new Attribute("x", "x"));
		}
		
		Iterator it = td.getAttributes().entrySet().iterator();
		while(it.hasNext()){
			Entry entry = (Entry) it.next();
			AttInfo tmp = (AttInfo) entry.getValue();
			String attName = (String) entry.getKey();
			inAtts.set(tmp.getAttSequenceNumber(),(new Attribute(tmp.getDataType(),table+ String.valueOf(CNFNode.divider) +attName)));
		}
		
		/*
		 * output atts
		 */
		ArrayList<Attribute> outAtts = new ArrayList<Attribute>();
		HashMap <String, String> exprs = new HashMap <String, String> ();
		HashSet<String> mergeUse = new HashSet<String>();
		
		ArrayList<String> tmp1 = winfo.getAllRelatedIdentifier(table);
		ArrayList<String> tmp2 = sinfo.getIdentifierRealted(table);
		mergeUse.addAll(tmp1);
		mergeUse.addAll(tmp2);
		
		ArrayList<String> outid = new ArrayList<String>(mergeUse);
		
		/*
		 * Then this is will be use to later cross join, because there is no use, so we only choose one atts 
		 * 
		 * inatts will be the same of outatts
		 */
		if(outid.size() == 0){
				Attribute a = inAtts.get(0);
				String type = a.getType();
				String newId = a.getName();
				outAtts.add(new Attribute(type, "att" + String.valueOf(0)));
				exprs.put("att" + String.valueOf(0), newId);
		}
		
		/*
		 * hash to map output to identifier
		 * 
		 * 
		 * handle  expre and outatts
		 */
		
		else{
			for(int i = 0; i < outid.size() ; i++){
				String curid = CNFNode.getInfoFromNewId(outid.get(i),"i");
				String type = td.getAttInfo(curid).getDataType();
				outAtts.add(new Attribute(type, "att" + String.valueOf(i)));
				exprs.put("att" + String.valueOf(i),outid.get(i));
			}
		}
		/*
		 * query to execute
		 */
		String query = "";
		
		ArrayList<String> tmptables = new ArrayList<String>();
		tmptables.add(table);
		
		/*
		 * get these cnfNode, only for this table 
		 */
		ArrayList<CNFNode> nodes = winfo.getRelatedCnfNodes(tmptables);
		
		for(int i = 0 ; i < nodes.size() ; i++){
			query += i != nodes.size() - 1 ?nodes.get(i).getquery() + " && " : nodes.get(i).getquery();
		}
		
		/*
		 * in case there is only simple projection
		 */
		
		query = query == "" ? "true" : query;
		try {
		      @SuppressWarnings("unused")
			Selection foo = new Selection (inAtts, outAtts, query, exprs, fullNameOfTable+".tbl",  table+".tbl", "g++", "cppDir/"); 
		    } catch (Exception e) {
		      throw new RuntimeException (e);
		    }
		ArrayList<String> handledTable = new ArrayList<String>();
		handledTable.add(table);
		return new DoResult(outAtts, exprs, table +".tbl",handledTable);
	}
	
	
}
