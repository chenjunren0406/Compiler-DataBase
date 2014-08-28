package write;

import java.util.ArrayList;
import java.util.HashMap;

import supplyedCode.Attribute;
import supplyedCode.GetKRecords;
import supplyedCode.Selection;

public class test {
	public static void main(String[] args){
		 ArrayList <Attribute> inAtts = new ArrayList <Attribute> ();
		    inAtts.add (new Attribute ("Int", "l_l_orderkey"));
		    inAtts.add (new Attribute ("Int", "l_l_partkey"));
		    inAtts.add (new Attribute ("Int", "l_l_suppkey"));
		    inAtts.add (new Attribute ("Int", "l_l_linenumber"));
		    inAtts.add (new Attribute ("Int", "l_l_quantity"));
		    inAtts.add (new Attribute ("Float", "l_l_extendedprice"));
		    inAtts.add (new Attribute ("Float", "l_l_discount"));
		    inAtts.add (new Attribute ("Float", "l_l_tax"));
		    inAtts.add (new Attribute ("Str", "l_l_returnflag"));
		    inAtts.add (new Attribute ("Str", "l_l_linestatus"));
		    inAtts.add (new Attribute ("Str", "l_l_shipdate"));
		    inAtts.add (new Attribute ("Str", "l_l_commitdate"));
		    inAtts.add (new Attribute ("Str", "l_l_receiptdate"));
		    inAtts.add (new Attribute ("Str", "l_l_shipinstruct"));
		    inAtts.add (new Attribute ("Str", "l_l_shipmode"));
		    inAtts.add (new Attribute ("Str", "l_l_comment"));
		    
		    ArrayList <Attribute> outAtts = new ArrayList <Attribute> ();
		    outAtts.add (new Attribute ("Str", "att1"));
		    
		    String selection = "l_l_shipdate==Str(\"1994-05-12\")&&l_l_commitdate==Str(\"1994-05-22\")&&l_l_receiptdate==Str(\"1994-06-10\")";	
		    HashMap <String, String> exprs = new HashMap <String, String> ();
		    exprs.put ("att1", "l_l_comment");
		    
		    
		    // run the selection operation
		    try {
		      Selection foo = new Selection (inAtts, outAtts, selection, exprs, "lineitem.tbl", "out.tbl", "g++", "cppDir/"); 
		    } catch (Exception e) {
		      throw new RuntimeException (e);
		    }
		    
		    GetKRecords result = new GetKRecords ("out.tbl", 30);
		    result.print ();
		    
	}
}
