package dataSetGenerator;

import java.util.ArrayList;

import edu.stanford.nlp.ie.NumberNormalizer;
/**
 * Utilities
 * @author Jakob
 */
public class Util {
        /**
         * Trims $ of money quantities
         * @param s quantity
         * @return quantity without dollar sign
         */
	public static String dollarProcess(String s){
		if (s.startsWith("$")){
			return s.substring(1);
		}
		return s;
	}
	
}
