package problemAnalyser;

import java.util.Collection;
import java.util.List;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TypedDependency;

/**
 * Makes a quantitative entity containing an Entity and a numeric quantity. 
 * @author Jakob
 */
public class QuantitativeEntity {
	
        //determines if it is the question entity (the quantity we are trying to find)
	boolean isQuestion = false;
        
        //used around line 580 in AFSentenceAnalyzer, nowhere else
	boolean superLative = false;// just for questions: how much further, ...
	
	boolean foundRealEntity;// it is mainly used for questions to
	// find out whether the qent is matched with previous ents or not. If not matched, 
	// this flag will be false.
        
        //determines whether an entity corresponds to a number or not ("he gave some of them to Sara")
	boolean fakeEntity = false;
	
        //number associated with the quantity
        String num;
        
        //floating around in several files
	IndexedWord verbid;// almost redundant
	String pathToVerb;
	Entity entity; //we structure this class as a regular Entity with extra fields and behaviors
	boolean reversePath = false;
	int uniqueIdx;
	int preferredVmean = -1;
	int numIndex = -1;
	
	public void setPreferredVmean(int preferredVmean) {
		this.preferredVmean = preferredVmean;
	}

	public int getPreferredVmean() {
		return preferredVmean;
	}
	public static int lastUniqueIdx;
	
	AF af;
	int timeStamp;
	
         //all dependencies in the sentence
	Collection<TypedDependency> dependencies;

        /**
         * Default quantitative entity
         */
	public QuantitativeEntity() {// just for making copy
		
	}
	
        /**
         * @param td a TypedDependency this entity is part of
         * @param afs an AFSentenceAnalyzer (whatever that is) for the sentence this entity is in
         * @param EntityinGov true if the entity is the governor of the depdenency
         * @param isQuestion is it the final (question entity)
         */
	public QuantitativeEntity(TypedDependency td, AFSentenceAnalyzer afs,
			boolean EntityinGov, boolean isQuestion) {
		this.isQuestion = isQuestion;
		dependencies = afs.dependencies; //all dependencies in the sentence
		TreeGraphNode node;
                //determines if the quantity is a governor or dependent and gets
                //the right node from the TypedDepdency accordingly
		if (EntityinGov) {
			node = td.gov();
		} else {
			node = td.dep();
		}

                //create regular entity with this info
		entity = new Entity(node, afs);
		
                //superfluous modifiers (amods is adjective or adverb modifiers probably)
		if (isQuestion) {
			entity.amods.remove("many");
			entity.amods.remove("much");
		}
		
                //if it's not the question entity, the quantity is the dependent element of the dependency
		if (!isQuestion) {
			this.num = td.dep().nodeString();
		}
		afs.setRelationToVerb(this);
                //add the entities verb (what is being done to the entity)
		MathCoreNLP.addVerb(this.verbid);
		try {
			MathCoreNLP.allEntNames.add(verbid.lemma()+" "+entity.name);
		} catch (Exception e) {
			// TODO: handle exception
		}
//		MathCoreNLP.println("ent: " + entity);
	}

        /**
         * Alternative constructor
         * @param numIndex
         * @param afs 
         */
	public QuantitativeEntity(int numIndex, AFSentenceAnalyzer afs) {
		this.dependencies = afs.dependencies;
		entity = new Entity(numIndex, afs); //contain a regular entity within this quantitative entity
		this.num = afs.getLemma(numIndex); //the number associated with this entity
		this.numIndex = numIndex; //index on the dependency tree?
		afs.setRelationToVerb(this);
	}

        /**
         * @return string representation of the entity
         */
	public String toString() {
		String ret = "";
		ret += entity.toString() + "\n";
		ret += "quantity: " + num + "\n";
		ret += "relation to verb: " + pathToVerb;
		return ret;
	}

        /**
         * @return deep copy
         */
	public QuantitativeEntity getCopy() {
		QuantitativeEntity ret = new QuantitativeEntity();
		ret.num = num;
		ret.verbid = verbid;
		ret.pathToVerb = pathToVerb;
		ret.entity = entity.getCopy();
		ret.af = af;
		ret.dependencies = this.dependencies;
		ret.uniqueIdx = uniqueIdx;
		return ret;
	}

	// the match function used in the question
	// equalVerbsCount check for
	// "Tom found 15 seashells and Fred found 43 seashells on the beach. When they cleaned them, they discovered that 29 were cracked. How many seashells did they find together ?"
	//determines if quantity is important?
        public boolean questionMatch(QuantitativeEntity qcent, boolean featuresImportant
			) {
		Entity qent = qcent.entity;
		// TODO: conversions: batch & muffin
		if (!Util.SEqual(qent.name, entity.name)) {
			return false;
			//TODO: if needed, add equalsVerbCount...
//		} else if (equalVerbsCount && this.verbid != null
//				&& CoreNLP.verbMean.get(this.verbid.lemma()) != 0
//				&& !Util.SEqual(qcent.verbid.lemma(), this.verbid.lemma())) {
//			MathCoreNLP.println("new false");
//			return false;
		} else if (!featuresImportant) {
			return true;
		}

		// try {
		// if (qcent.af.subject.wi.NE.equals("PERSON")){
		// if (!Util.SEqual(qcent.af.subject.name, af.subject.name)){
		// return false;
		// }
		// }
		// } catch (Exception e) {
		// }
		// TODO for TIME and PLACE
		// if (!Util.SEqual(af.time, qcent.af.time)
		// || !Util.SEqual(af.place, qcent.af.place)){
		// return false;
		// }
		// TODO
		
		List<String> nnamod1 = Util.getUnion(qent.amods, qent.nns);
		List<String> nnamod2 = Util.getUnion(entity.amods, entity.nns);
		
//		if (!Util.listSubset2(qent.amods, entity.amods)
//				|| !Util.listSubset2(qent.nns, entity.nns)) {
//			return false;
//		}
		
		if (!Util.listSubset2(nnamod1, nnamod2)) {
			return false;
		}
		return true;
	}

        /**
         * @param cent another entity
         * @return true if they're both relevant and have the same lemma, subject, time, and place, i.e. are the same entity
         */
	public boolean match(QuantitativeEntity cent) {
		Entity ent1 = cent.entity;
		if (!entity.match(ent1)) {
			return false;
		}
		if (!Util.SEqual(af.verbid.lemma(), cent.af.verbid.lemma())
				|| !Util.SEqual(af.subject, cent.af.subject)
				|| !Util.SEqual(af.time, cent.af.time)
				|| !Util.SEqual(af.place, cent.af.place)) {
			return false;
		}

		return true;
	}

        /**
         * @return subject of entity's sentence
         */
	public String getSubject() {
		if (af.subject == null) {
			return null;
		}
		return af.subject.name;
	}

	public void setVerbid(IndexedWord verbid) {
		this.verbid = verbid;
	}

	public String getName() {
		return entity.name;
	}

	public boolean isQuestion() {
		return isQuestion;
	}

	public boolean isFoundRealEntity() {
		return foundRealEntity;
	}

	public boolean isFakeEntity() {
		return fakeEntity;
	}

	public String getNum() {
		return num;
	}

	public IndexedWord getVerbid() {
		return verbid;
	}

	public String getPathToVerb() {
		return pathToVerb;
	}

	public Entity getEntity() {
		return entity;
	}

	public AF getAf() {
		return af;
	}
	
	public int getTimeStamp() {
		return timeStamp;
	}

	public Collection<TypedDependency> getDependencies() {
		return dependencies;
	}
	
	public void setUniqeIdx(){
//		System.out.println("l u i: "+lastUniqueIdx+this.getName());
		if (verbid!=null){
			this.uniqueIdx = lastUniqueIdx++;
		}
		
	}
	public int getUniqeIdx(){
		return this.uniqueIdx;
	}
	
	public boolean isSuperLative() {
		return superLative;
	}

	public void setSuperLative(boolean superLative) {
		this.superLative = superLative;
	}
}
