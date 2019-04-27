package problemAnalyser;

import java.util.ArrayList;

public class WordInfo {
	String name;
	String lemma;
	String pos;
	String NE;
	
        /**
         * 
         * @param name the name of the word
         * @param lemma headword, e.g. run is lemma of runs, running, runner...
         * @param pos position?
         * @param NE might be PERSON, might be something else
         */
	public WordInfo(String name, String lemma, String pos, String NE){
		this.name = name;
		this.pos = pos;
		this.lemma = lemma;
		this.NE = NE;
	}
        /**
         * @return string representation of info
         */
	public String toString(){
		return (name+" "+pos+" "+lemma+" "+NE);
	}
}
