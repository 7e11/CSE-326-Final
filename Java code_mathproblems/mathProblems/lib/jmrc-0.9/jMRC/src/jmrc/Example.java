package jmrc;

import java.io.*;
/**
 * Example of MRC database queries.
 * 
 * @author Francois Mairesse, <a href=http://www.dcs.shef.ac.uk/~francois
 *         target=_top>http://www.dcs.shef.ac.uk/~francois</a>
 */
public class Example {

	/**
	 * Main method with examples.
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		if (args.length == 0) { 
			System.err.println("Usage: java jmrc.Example mrc_directory");
			System.exit(1);
		}
		// load database (need to change the file name)
		MRCDatabase db = new MRCDatabase(new File(args[0] + File.separator + "mrc2.dct"));
		
		// query concreteness value for noun 'JUSTICE'
		int happinessConc = db.getValue("happiness", MRCPoS.NOUN, MRCDatabase.FIELD_CONC);
		
		// query concreteness value for noun 'CAR'
		int carConc = db.getValue("car", MRCPoS.NOUN, MRCDatabase.FIELD_CONC);
		
		// print results
		System.out.println("Concreteness value for the word happiness: " + happinessConc);
		System.out.println("Concreteness value for the word car: " + carConc);
		System.out.println("Done.");

	}

}
