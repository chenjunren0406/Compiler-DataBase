package supplyedCode;
import java.io.*;

import org.antlr.gunit.swingui.parsers.StGUnitParser.output_return;
import org.antlr.runtime.*;
import org.omg.CORBA.PUBLIC_MEMBER;

import sematic.SematicCheck;
import write.CNFNode;
import write.GroupByExecutor;
import write.QueryExecutor;
import write.SelectionExecutor;
import write.SelectInfo;
import write.DoResult;
import write.WhereInfo;

import java.util.*;
  
class Interpreter {
  
  @SuppressWarnings("static-access")
public static void main (String [] args) throws Exception {
      
      CatalogReader foo = new CatalogReader ("./test/Catalog.xml");
      Map <String, TableData> res = foo.getCatalog ();
      System.out.println (foo.printCatalog (res));
      
      InputStreamReader converter = new InputStreamReader(System.in);
      BufferedReader in = new BufferedReader(converter);
      
      System.out.format ("\nSQL>");
      String soFar = in.readLine () + " ";
     
      // loop forever, or until someone asks to quit
      while (true) {
        
        // keep on reading from standard in until we hit a ";"
        while (soFar.indexOf (';') == -1) {
          soFar += (in.readLine () + " ");
        }
        
        // split the string
        String toParse = soFar.substring (0, soFar.indexOf (';') + 1);
        soFar = soFar.substring (soFar.indexOf (';') + 1, soFar.length ());
        toParse = toParse.toLowerCase ();
        
        // parse it
        ANTLRStringStream parserIn = new ANTLRStringStream (toParse);
        SQLLexer lexer = new SQLLexer (parserIn);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLParser parser = new SQLParser (tokens);
        
        // if we got a quit
        if (parser.parse () == false) {
          break; 
        }
        
        // print the results
        System.out.println ("RESULT OF PARSING");
        System.out.println ("Expressions in SELECT:");
        
        ArrayList <Expression> mySelect = parser.getSELECT ();
        for (Expression e : mySelect)
          System.out.println ("\t" + e.print ());
        
        System.out.println ("Tables in FROM:");
        /*
         * key "l" ;value "lineitem"
         */
        Map <String, String> myFrom = parser.getFROM ();
        System.out.println ("\t" + myFrom);
        
        System.out.println ("WHERE clause:");
        Expression where = parser.getWHERE ();
        if (where != null)
          System.out.println ("\t" + where.print());
        
        System.out.println ("GROUPING atts:");
        for (String att : parser.getGROUPBY ()) {
          System.out.println ("\t" + att);
        }
        
        //TODO need to be change
        
        /*
         *  all are table name in short
         */
        
        SelectInfo sinfo = new SelectInfo(mySelect,parser);
        WhereInfo winfo = new WhereInfo(where,parser);
        
        QueryExecutor qe = new QueryExecutor(sinfo, winfo, parser);
        
        long startTime = System.currentTimeMillis();
        
        qe.doSelection();
        qe.doJoin();
        qe.doGroupBy();
        
        long endTime = System.currentTimeMillis();

        qe.print();
        
        System.out.println(" \nThe run took " + (endTime - startTime) + " milliseconds");
        
        System.out.format ("\nSQL>");
              
      } 
  }
}
