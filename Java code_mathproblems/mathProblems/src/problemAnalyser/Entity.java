package problemAnalyser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TypedDependency;

public class Entity {
	String name;
	int index;
	static String[] processedRelNames = new 
			String[]{"num", "amod", "nn"};
	List<String> nns;
	List<String> amods;
	AFSentenceAnalyzer afs;
	List<String> otherRels;
	List<String> otherRelsNames;
	WordInfo wi;
	boolean isPerson;
	//identifies whether the entity is a person or not, mainly used for subj
	
	//for makeCopy
	public Entity() {}
	
        /**
         * @return true if the entity is a person
         */
	public boolean isPerson() {
		return wi.NE.equals("PERSON");
	}

        /**
         * For entities with a named quantity
         * @param tdnode a parse tree representing the sentence the entity is in
         * @param afs AFSentenceAnalyzer for the sentence the entity is in
         */
	public Entity(TreeGraphNode tdnode, AFSentenceAnalyzer afs) {
		this(tdnode.index(),afs); //call the constructor below
		index = tdnode.index(); //predefined index
		this.name = afs.getLemma(index); //sets the name for us
	}
        
	/**
         * when there is no name for the counted entity! e.g., I have 3.
         * @param numIndex
         * @param afs AFSentenceAnalyzer for the sentence this entity is in?
         */
        public Entity(int numIndex, AFSentenceAnalyzer afs) {
		this.index = numIndex;
		this.afs = afs;
		nns = new ArrayList<String>();
		amods = new ArrayList<String>();
		otherRels = new ArrayList<String>();
		otherRelsNames = new ArrayList<String>();
		addRelateds(afs.dependencies, "amod", amods);
		amods.remove("more");
		addRelateds(afs.dependencies, "nn", nns);
		addRelateds(afs.dependencies, "", otherRels);
		wi = afs.getWordInfo(index);
		this.isPerson = wi.NE.equals("PERSON");
	}
	
        /**
         * list of words from dependencies based on a specific relation
         * @param dependencies list of dependencies, which contain a governor word, a dependent word, and the relation between the two
         * @param relName type of relation?
         * @param relateds 
         */
	void addRelateds(Collection<TypedDependency> dependencies, String relName, List<String> relateds){
		for (TypedDependency tde:dependencies){
			String tdeRelName = tde.reln().toString(); //relation type from the TypedDependency
			boolean shouldIgnore = false;
                        //we don't need to do anything with rel names nn, amod, num
			if (relName.equals("")){
				for (String s:processedRelNames){
					if (s.equals(tdeRelName)){
						shouldIgnore = true;
						break;
					}
				}
				if (shouldIgnore){
					continue;
				}
			}
                        //if it's not one of those three, we travel down here
                        //enter this if statement if the dependency's relation type is the one specified by parameter relName
                        //(or any if relName is "", and also the governor word's index must be equal to the Entity's index field
			if ((tdeRelName.equals(relName) || relName.equals("") ) && Util.getNumber(tde.gov()) == index){
                                //ignore words whose lemma is "the" or "poss", otherwise add them to the list of relateds
				String o = afs.getLemma(tde.dep().index());
				String oRelName = tde.reln().getShortName();
				if (o.equals("the") || oRelName.equals("poss")){
					continue;
				}
				relateds.add(o);
				if (relName.equals("")){
					otherRelsNames.add(oRelName);
				}
			}
		}
	}
	
	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

	public static String[] getProcessedRelNames() {
		return processedRelNames;
	}

	public List<String> getNns() {
		return nns;
	}

	public List<String> getAmods() {
		return amods;
	}

	public AFSentenceAnalyzer getAfs() {
		return afs;
	}

	public List<String> getOtherRels() {
		return otherRels;
	}

	public List<String> getOtherRelsNames() {
		return otherRelsNames;
	}

	public WordInfo getWi() {
		return wi;
	}

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
//		if (otherRels.size()>0){
//			str += "others: ";
//			for (int i=0; i<otherRels.size(); i++){
//				str += otherRels.get(i)+" ("+otherRelsNames.get(i)+") ";
//			}
//		}
		str += "name: "+ name+" idx_"+index;
		return str;
	}
	
        /**
         * Copies some features from another entity into this in certain conditions
         * @param en some other entity to copy amods and nns and wordinfo from
         * @param isQuestion is the question entity
         */
	public void absorbFeatures(Entity en, boolean isQuestion){
		this.name = en.name;
                //if no amods or nns in this entity, or its' the question entity (final entity)
		if ((this.amods.size()==0 && this.nns.size()==0)
				|| isQuestion){
                        //copy into our (empty) list of amods and nns those of the Entity en
			Util.getCopy(this.amods, en.amods);
			Util.getCopy(this.nns, en.nns);
		}
                //set our word info to the en's
		this.wi = en.wi;
	}
	
        /**
         * @param ent1 an entity
         * @return if ent1 is equal to this entity
         */
	public boolean match(Entity ent1){
		if (!Util.SEqual(ent1.name, name)){
			return false;
		}
		if (!Util.listEqual(amods, ent1.amods) ||
				!Util.listEqual(amods, ent1.amods)){
			return false;
		}
		return true;
	}
	
        /**
         * @return deep copy of an entity
         */
	public Entity getCopy(){
		Entity ret = new Entity();
		ret.name = name;
		ret.index = index;
		ret.nns = Util.getCopy(nns);
		ret.amods = Util.getCopy(amods);
		ret.otherRels = Util.getCopy(otherRels);
		ret.otherRelsNames = Util.getCopy(otherRelsNames);
		return ret;
	}
}
