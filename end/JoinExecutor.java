package write;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import supplyedCode.Attribute;
import supplyedCode.CatalogReader;
import supplyedCode.Join;
import supplyedCode.SQLParser;
import supplyedCode.TableData;

public class JoinExecutor {
	private SQLParser parser = null;
	private CatalogReader db = null;
	private Map <String, TableData> res = null;
	private SelectInfo sinfo;
	private WhereInfo winfo;
	private boolean luo;
	
	public JoinExecutor(SQLParser parser,SelectInfo sinInfo,WhereInfo winfo, boolean luo){
		this.db = new CatalogReader("./test/Catalog.xml");
		this.parser = parser;
		res = db.getCatalog ();
		this.sinfo = sinInfo;
		this.winfo = winfo;
		this.luo = luo;
	}
	
	public DoResult DoJoin(DoResult left, DoResult right){
		
		System.out.println("Now Doing join with " + left.getTableName() + " and " + right.getTableName());
		
		DoResult result = null;
		
		/*
		 * handle right input
		 */
		ArrayList<Attribute> inAttsRight = right.getOutAtts();
		
		/*
		 * handle left input
		 */
		ArrayList<Attribute> inAttsLeft = left.getOutAtts();
		
		/*
		 * two hashset to maintain left and right att name
		 */
		HashSet<String> leftAtt = new HashSet<String>();
		HashSet<String> rightAtt = new HashSet<String>();
		for(Attribute a : left.getOutAtts()){
			leftAtt.add(a.getName());
		}
		
		for(Attribute a : right.getOutAtts()){
			rightAtt.add(a.getName());
		}
		
		/*
		 * handle output Att, exprs is for result
		 * tmpexpr is for join use
		 */
		HashMap<String, String> exprs = new HashMap<String,String>();
		HashMap<String, String> tmpexprs = new HashMap<String,String>();
		/*
		 * simply helper to get left and right into one arraylist
		 */
		ArrayList<Attribute> outAtts = new ArrayList<Attribute>(inAttsLeft);
		outAtts.addAll(inAttsRight);
		
		for(int i = 0 ; i < outAtts.size() ; i++){
			String attName = outAtts.get(i).getName(); 
			String attNameLR = convert(attName, leftAtt, rightAtt);
			exprs.put(attName,attName);
			tmpexprs.put(attName, attNameLR);
		}
		
		ArrayList<String> twoTable = new ArrayList<String>(left.getOperatedTable());
		twoTable.addAll(right.getOperatedTable());
		/*
		 * lefthash and right hash
		 */
		ArrayList<String> leftHash = new ArrayList<String>();
		ArrayList<String> rightHash = new ArrayList<String>();
		
		/*
		 * handle query
		 */
		String selection = "";
		String tmp = "";
		/*
		 * no use of t1coverTable and t2CoverTable in this join
		 */
		ArrayList<String> t1CoverTable = left.getOperatedTable();
		ArrayList<String> t2CoverTable = right.getOperatedTable();
		ArrayList<CNFNode> relatedNode = winfo.getRelatedCnfFor2Tables(t1CoverTable, t2CoverTable);
		
		
		
		/*
		 * do the real query
		 */
		for(int i = 0 ; i < relatedNode.size(); i++){
			CNFNode curNode = relatedNode.get(i);
			
			if(isStrictEqual(curNode)){
				HashSet<String> tmpIdSet = curNode.getAllidentifier();
				for(String s :  tmpIdSet){
					if(leftAtt.contains(s)){
						leftHash.add(s);
					}
					else if(rightAtt.contains(s)){
						rightHash.add(s);
					}
					else
						System.out.println(s + " can not find in left and right");
				}
			}
			
			tmp += i != relatedNode.size() - 1 ?curNode.getquery() + "&&":curNode.getquery() ;
		}
		
		selection = convert(tmp, leftAtt, rightAtt);
		
		
		/*
		 * To construct return result
		 */
		ArrayList<String> operatedTable = new ArrayList<String>(left.getOperatedTable());
		operatedTable.addAll(right.getOperatedTable());
		
		String tableName = "";
		for(int i = 0 ; i < operatedTable.size() ; i++){
			String tn = operatedTable.get(i);
			tableName += i!=operatedTable.size() - 1 ? tn + "&" : tn;
		}
			tableName +=".tbl";
		result = new DoResult(outAtts, exprs, tableName, operatedTable);
		
		String lt = left.getTableName();
		String rt = right.getTableName();
		
		/*
		 * in case there is only simple projection
		 */
		
		selection = selection == "" ? "true" : selection;
		try{
		@SuppressWarnings("unused")
		Join foo = new Join (inAttsLeft, inAttsRight, outAtts, leftHash, rightHash, selection, tmpexprs, 
                   lt, rt, tableName, "g++", "cppDir/"); 
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		
		return result;
	}
	
	private boolean isStrictEqual(CNFNode n){
		
		ArrayList<Character> otherThanEqual = new ArrayList<Character>();
		otherThanEqual.add('>');
		otherThanEqual.add('<');
		otherThanEqual.add('<');
		otherThanEqual.add('*');
		otherThanEqual.add('/');
		otherThanEqual.add('!');
		otherThanEqual.add('|');
		
		String query = n.getquery();
		for(int i = 0 ; i < query.length() ; i++){
			char curchar = query.charAt(i);
			if(otherThanEqual.contains(curchar))
				return false;
		}
		
		
		return true;
	}
	
	
	private String convert(String oriquery,HashSet<String> leftAtt, HashSet<String> rightAtt){
		String lefthandle = oriquery;
		String righthandle = "";
		for(String s : leftAtt){
			lefthandle = lefthandle.replaceAll(s, "left." + s);
		}
		
		righthandle = lefthandle;
		for(String s : rightAtt){
			righthandle = righthandle.replaceAll(s, "right." + s);
		}
		
		return righthandle;
		
	}
	
	
}
