package write;

import supplyedCode.AttInfo;
import supplyedCode.CatalogReader;
import supplyedCode.SQLParser;

/**
 * ge zhong helper
 * @author junrenchen
 *
 */
public class GezhongHelper {
	
	private CatalogReader reader;
	private SQLParser parser;
	
	/**
	 *  constructor
	 */
	public GezhongHelper(CatalogReader reader,SQLParser parser){
		this.reader = reader;
		this.parser = parser;
	}
		
	
}
