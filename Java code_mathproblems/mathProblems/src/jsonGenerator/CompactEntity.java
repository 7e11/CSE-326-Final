package jsonGenerator;

import java.util.List;

import problemAnalyser.AFSentenceAnalyzer;
import problemAnalyser.Entity;
/**
 * For just storing the data an Entity in JSON form.
 * Only includes 7 fields and none of the behaviors.
 * @author Jakob
 */
public class CompactEntity {
	String name;
	int index;
	
	List<String> nns;
	List<String> amods;
	List<String> otherRels;
	List<String> otherRelsNames;
	boolean isPerson;
        /**
         * Create entity from entity object
         * @param ent 
         */
	public CompactEntity(Entity ent) {
		if (ent==null){
			return;
		}
		name = ent.getName();
		index = ent.getIndex();
		nns = ent.getNns();
		amods = ent.getAmods();
		otherRels = ent.getOtherRels();
		otherRelsNames = ent.getOtherRelsNames();
		isPerson = ent.isPerson();
	}
        
        /**
         * Create compact entity directly from the fields
         * @param name
         * @param index
         * @param nns
         * @param amods
         * @param otherRels
         * @param otherRelsNames
         * @param isPerson 
         */
	public CompactEntity(String name, int index, List<String> nns, List<String> amods, List<String> otherRels, List<String> otherRelsNames, boolean isPerson) {
		this.name = name;
		this.index = index;
		this.nns = nns;
		this.amods = amods;
		this.otherRels = otherRels;
		this.otherRelsNames = otherRelsNames;
		this.isPerson = isPerson;
	}
	
        /**
         * String representation for JSON
         **/
	public String toString(){
		String str = "";
		if (amods.size()>0){
			str += "amods: ";
			for (String s:amods){
				str += s+" ";
			}
		}
		if (nns.size()>0){
			str += "nns: ";
			for (String s:nns){
				str += s+" ";
			}
		}
		str += "name: "+ name+" idx_"+index;
		return str;
	}
}
