package problemAnalyser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import verbAnalyze.SentenceAnalyzer;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TypedDependency;

public class AF {
	Entity subject; //the subject of the sentence
	private Entity iobj;// used for transfer, not just iobj

	Entity time; //extracted by parser
	Entity place; //extracted by parser
	IndexedWord verbid; //the sentence's verb
	ArrayList<String> verbRels; //dependency relations involving the verb
	boolean passive = false; //does the sentence have a passive voice?
	static HashSet<String> acceptablePlaceRels; //the set of prepositional (place) relations we can use
	static HashSet<String> acceptableEachStrs; //hardcoded modifiers like each, an, a, for things
	boolean ibojSet = false;

	private ArrayList<QuantitativeEntity> cents; //the quantitative entities in the centence
	Collection<TypedDependency> dependencies; //the dependencies from the stanford dependency parser
	SemanticGraph dependenciesTree;
	AFSentenceAnalyzer afs;
	ArrayList<EachRelation> eachrelations;

	static {
		acceptablePlaceRels = new HashSet<String>();
		acceptablePlaceRels.add("prep_in");
		acceptablePlaceRels.add("prep_at");
		acceptablePlaceRels.add("prep_on");
		acceptablePlaceRels.add("prep_into");
		acceptablePlaceRels.add("prep_out_of");
		
		acceptableEachStrs = new HashSet<String>();
		acceptableEachStrs.add("each");
		acceptableEachStrs.add("an");
		acceptableEachStrs.add("a");
	}

        /**
         * 
         * @param afs AFSentenceAnalyzer (WHAT IS THAT) for the sentence
         * @param dependenciesTree Stanford dependency tree of the sentence
         * @param verbid verb of the sentence
         */
	public AF(AFSentenceAnalyzer afs, SemanticGraph dependenciesTree,
			IndexedWord verbid) {
		this.afs = afs;
		this.dependencies = afs.dependencies;
		this.dependenciesTree = dependenciesTree;
		cents = new ArrayList<QuantitativeEntity>(); //numerical entities in sentence
		this.verbid = verbid;
		eachrelations = new ArrayList<EachRelation>();
	}

        /**
         * Some sort of setup...find these methods and document them
         */
	public void resolveAF() {
		setTime();
		setSubject();
		setPlace();
		setIobj();
		resolveDoDid();
		setVerbRels();
		
		setEachRelations();
	}

        /**
         * One of the setup functions.
         * Finds the each relations "each, an, a" and does something to them
         */
	void setEachRelations() {
		for (TypedDependency tde : dependencies) { //iterate thru the sentences dependencies
                        //if it's an each/an/a relation
			if (tde.reln().toString().equals("det")
					&& acceptableEachStrs.contains(tde.dep().nodeString()
							.toLowerCase())) {
				// SentenceAnalyzer.println("each seen for "+tde.gov().nodeString());
                                //make entity from the governor word and the AFSentenceAnalyzer
				Entity ent1 = new Entity(tde.gov(), afs);
                                //don't do anything if...COME BACK TO THIS
				if (afs.usedEachIds.contains(ent1.index)) {
					continue;
				}
				String n1 = 1 + "";
				int dis = 100000; //max distance
				Entity ent2 = null;
				String n2 = "";
                                //finds the closest entity in cents to ent1 and assigns it to ent2
				for (QuantitativeEntity cent : cents) {
					if (Math.abs(cent.entity.index - ent1.index) < dis) {
						dis = Math.abs(cent.entity.index - ent1.index);
						ent2 = cent.entity;
						n2 = cent.num;
					}
				}
				if (ent2 != null) {
					EachRelation er = new EachRelation(ent1, ent2, n1, n2);
					eachrelations.add(er);
					afs.usedEachIds.add(ent1.index);
				}
			}
		}
	}

        /**
         * 
         */
	void setVerbRels() {
		verbRels = new ArrayList<String>(); //verb relations
                //if there's no verb, get out of here
		if (verbid == null) {
			return;
		}
		for (TypedDependency tde : dependencies) {
                        //if a dependency's governor is our sentence's verb
			if (tde.gov().index() == verbid.index()) {
				String reln = tde.reln().toString();
				verbRels.add(reln); //add the relation to our sentence's list of relations
				if (!MathCoreNLP.verbRelsCounts.containsKey(reln)) {
					MathCoreNLP.verbRelsCounts.put(reln, 0);
				}
				int c = MathCoreNLP.verbRelsCounts.get(reln);
				MathCoreNLP.verbRelsCounts.put(reln, c + 1);
			}
		}
	}

        /**
         * @return the list of verb relation encodings
         */
	public ArrayList<String> getVerbRels() {
		return verbRels;
	}

        /**
         * If it says "he did X", change the verb from did to X
         */
	void resolveDoDid() {
		if (cents.get(0).isQuestion && verbid != null) { //verifies that we have the question entity and the verb is not null
			if (verbid.lemma().equals("did") || verbid.lemma().equals("do")) {
                                //go thru these dependencies
				for (TypedDependency td : dependencies) {
					if (td.reln().toString().equals("dobj")) { //direct object. "Rakesh does a thing", a thing
						if (td.gov().index() == verbid.index()) { //"does"?

							verbid = afs.getIndexedWord(td.dep().index()); //change the verb to be "a thing"
							MathCoreNLP.println("herell " + verbid.lemma());
							for (QuantitativeEntity cent : cents) {
								cent.verbid = verbid;
							}
							subject = getNPersontoIdxedWord(verbid);
							break;
						}
					}

				}
			}
		}
	}

        /**
         * Finds temporal modifiers and sets the AF's time field if it finds one
         */
	void setTime() {
		if (verbid == null) {
			return;
		}
                //Find all temporal modifiers ("I love Sara forever" --> forever)
                //gov: love, dep: forever
		for (TypedDependency tde : dependencies) {
			if (tde.reln().toString().equals("tmod")) {
                                //if the temporal modifier is the sentence's verb, set the time as a new entity
				if (tde.gov().index() == verbid.index()) {
					time = new Entity(tde.dep(), afs); //time field is "forever"
				}
			}
		}
	}

        /**
         * 
         */
	void setPlace() {
		if (verbid == null) {
			return;
		}
		SentenceAnalyzer.println("setting vid for "+verbid.index());
		int distanceToVerb = -1;
                //find a place relation from the list of acceptable place relations
                //prep_in,prep_at,prep_on,prep_into,prep_out_of
		for (TypedDependency tde : dependencies) {
			String reln = tde.reln().toString();
			if (acceptablePlaceRels.contains(reln)) {
                                //finds the shortest path to and place rel on the dependency tree and sets the place field to it
				List<SemanticGraphEdge> path = dependenciesTree
						.getShortestDirectedPathEdges(afs.getIndexedWord(cents.get(0).entity.index),
								afs.getIndexedWord(tde.dep().index()));
				SentenceAnalyzer.println("path: "+path+" for "+tde.dep().index());
				if (distanceToVerb == -1
						|| (path != null && path.size() < distanceToVerb)) {
//					if (path.size()>1){
//						continue;
//					}
					place = new Entity(tde.dep(), afs);
					if (path != null) {
						distanceToVerb = path.size();
					}
				}
			}
		}
	}

        /**
         * Sets the subject of the sentence
         */
	void setSubject() {
		if (verbid == null) {
			return;
		}
		String selectedReln = "";
                //go thru all dependencies in the sentence. find a relation that
                //matches nsubj, agent, or auxpass. agent is best, so we try to find that if possible
                //Agent: "Sara is loved by Rakesh" --> agent(loved, Rakesh)
                //nsubj (Nominal Subject): "Rakesh loves Sara" --> nsubj(loves, Rakesh)
                //"Sara is beautiful. " --> nsubj(beautiful, Sara) auxpass(beautiful, is)
		for (TypedDependency tde : dependencies) {
			String reln = tde.reln().toString();
			if (reln.equals("nsubj") || reln.equals("agent")
					|| reln.equals("auxpass")) {
				// agent is better than auxpass!!! [author put this here]
				if (selectedReln.equals("agent") && reln.equals("auxpass")) {
					continue;
				}
				selectedReln = reln;

				if (tde.gov().index() == verbid.index()) {
                                        //create an entity for the sentence's subject
					subject = new Entity(tde.dep(), afs);
					int subjIdx = tde.dep().index();
                                        //figure out if the subject is a person
					if (afs.getWordInfo(subjIdx).NE.equals("PERSON")) {
						subject.isPerson = true;
					}
                                        
                                        //look at the examples aboev, they sound passive
					if (reln.equals("auxpass") || reln.equals("agent")) {
						passive = true;
					}

					if (selectedReln.equals("nsubj")) {
						boolean shouldBreak = true;
						// if the subj is meaningful, break. (i.e. subj!=ent)
						for (int i = 0; i < cents.size(); i++) {
							if (subject.name.equals(cents.get(i).getName())) {
								shouldBreak = false;
							}
						}
						if (shouldBreak) {
							break;
						}
					}
				}

			}
		}
                //we are using a person id (subject.index) to represent this person in the equation
		if (subject != null && subject.isPerson) {
			afs.usedPersonIds.add(subject.index);
		}
                
                //if we can't find a subject, look for the subject of the verb (governor)
                //component of the first nsubj relation we find
                //save the old verbid and set it back after finding the subject of the sentence
		if (subject == null){
			for (TypedDependency tde : dependencies) {
				if (tde.reln().toString().equals("nsubj")){
					IndexedWord prevVerbid = verbid;
					verbid = afs.getIndexedWord(tde.gov().index());
					setSubject();
					verbid = prevVerbid;
					return;
				}
			}
		}
	}

        /**
         * Sets indirect object
         * We want to find an iobj or dobj dependency with the smallest(?) distance path in the dependency tree
         * Set iobj field to this Entity if it is a person
         */
	public void setIobj() {
		int distanceToVerb = -1;
		boolean isPersonFound = false;
		for (TypedDependency tde : dependencies) {
			String reln = tde.reln().toString();
			if (reln.equals("iobj") || reln.equals("dobj")) {
				if (tde.gov().index() == verbid.index()) {
					List<SemanticGraphEdge> path = dependenciesTree
							.getShortestDirectedPathEdges(verbid,
									afs.getIndexedWord(tde.dep().index()));
					Entity ent = new Entity(tde.dep(), afs);
					if (distanceToVerb == -1
							|| (path != null && path.size() < distanceToVerb)
							|| (ent.isPerson && !isPersonFound)) {

						if (isPersonFound && !ent.isPerson) {
							continue;
						}
						if (ent.isPerson) {
							isPersonFound = true;
						}
						iobj = ent;
						int iobjIdx = tde.dep().index();
						// MathCoreNLP.println("it is "+subjIdx+" "+afs.getWordInfo(subjIdx));

					}
				}
			}
		}

		// search for nsubjpass to be the iobj! e.g. A ship is filled with...
		if (iobj == null) {
			SentenceAnalyzer.println("setting iobj with passive");
			for (TypedDependency tde : dependencies) {
				String reln = tde.reln().toString();
				if (reln.equals("nsubjpass")) {
					if (verbid != null && tde.gov().index() != verbid.index()) {
						continue;
					}
					iobj = new Entity(tde.dep(), afs);
					SentenceAnalyzer.println("iobj set to: " + iobj.name);
					return;
				}

			}
		}

		// find the nearest person to verb
		if (iobj == null) {
			Entity ent = getNPersontoIdxedWord(verbid);
			if (ent != null
					&& (subject == null || !ent.name.equals(subject.name))
					&& !afs.usedPersonIds.contains(ent.index)) {
				iobj = ent;
			}
		} else if (!iobj.isPerson) {
			Entity ent = getNPersontoIdxedWord(afs.getIndexedWord(iobj.index));
			if (ent != null
					&& (subject == null || !ent.name.equals(subject.name))
					&& !afs.usedPersonIds.contains(ent.index)) {
				iobj = ent;
			}
		}

	}

        /**
         * Simple getter for iobj
         * @return the sentence's indirect object
         */
	public Entity getIobj() {
		if (!ibojSet) {
			setIobj();
			ibojSet = true;
		}
		return iobj;
	}

        /**
         * Returns person who is closest in the dependency parse tree to some word
         * @param first root word in tree
         * @return person closest in the dependency tree to the word "first"
         */
	Entity getNPersontoIdxedWord(IndexedWord first) {
		int distanceToVerb = -1;
		Entity ret = null;
		for (TypedDependency tde : dependencies) { //iterate thru sentence's dependencies
                        //find prople from both the governor and dependent nodes and add them to a list
			List<TreeGraphNode> personNodes = new ArrayList<TreeGraphNode>();
			if (afs.getWordInfo(tde.gov().index()).NE.equals("PERSON")) {
				personNodes.add(tde.gov());
			}
			if (afs.getWordInfo(tde.dep().index()).NE.equals("PERSON")) {
				personNodes.add(tde.dep());
			}
                        //for each of the people (at most 2?)
                        //make ret the person Entity with the shortest path in the trees
			for (TreeGraphNode tgn : personNodes) {
				List<SemanticGraphEdge> path = dependenciesTree
						.getShortestDirectedPathEdges(first,
								afs.getIndexedWord(tgn.index()));
				if (distanceToVerb == -1
						|| (path != null && path.size() < distanceToVerb)) {
					ret = new Entity(tgn, afs);
					if (path != null) {

						distanceToVerb = path.size();
					}
				}
			}
		}
		return ret;
	}

        /**
         * @return string representation of AF
         */
	public String toString() {
		String str = "";
		str += ("verb: " + ((verbid != null) ? verbid.lemma() : "null")) + "\n";
		str += ("subject: " + ((subject != null) ? subject.toString() : "null"))
				+ "\n";
		str += ("time: " + (time != null ? time.toString() : "null")) + "\n";
		str += ("place: " + (place != null ? place.toString() : "null")) + "\n";
		str += ("counted entities: ") + "\n";
		for (QuantitativeEntity cent : cents) {
			str += "\n" + (cent.toString()) + "\n";
		}
		if (eachrelations.size() > 0) {
			str += "erels: \n";
			for (EachRelation er : eachrelations) {
				str += er;
			}
		}

		return str;
	}

        /**
         * add an entity to the list and set its af field to this AF
         * @param cent 
         */
	public void addCent(QuantitativeEntity cent) {
		cents.add(cent);
		cent.af = this;
	}

	public ArrayList<QuantitativeEntity> getCents() {
		return cents;
	}

	public Entity getSubject() {
		return subject;
	}

	public Entity getTime() {
		return time;
	}

	public Entity getPlace() {
		return place;
	}

	public IndexedWord getVerbid() {
		return verbid;
	}

	public Collection<TypedDependency> getDependencies() {
		return dependencies;
	}

	public SemanticGraph getDependenciesTree() {
		return dependenciesTree;
	}

	public AFSentenceAnalyzer getAfs() {
		return afs;
	}

	public boolean isPassive() {
		return passive;
	}
        
        /**
         * Determines if the sentence's subject is "meaningful"
         * @return true if the subject is a person or meets some weird condition below
         */
	public boolean isSubjMeaningFul() {
		if (subject == null) {
			return false;
		}
		if (subject.getName().equals("be")){
			return false;
		}
		if (subject.isPerson) {
			return true;
		}
		if (afs.dependencies.toString().contains("expl")) {
			return false;
		}
                //subject is the number in the question entity
		if (subject.getName()!=null && 
				subject.getName().equals(cents.get(0).getNum())){
			SentenceAnalyzer.println("our case");
			return false;
		}
//		if (subject.name.equals(cents.get(0).entity.name)){
//			return false;
//		}
                //find entities whose name equals the subject's name, unless the entity's num is null, 1, or 2, return false
		for (int i = 0; i < cents.size(); i++) {
			if (subject.name.equals(cents.get(i).getName())) {
				String num = cents.get(i).getNum();
				if (num!=null && !num.equals("1") && !num.equals("2")){
					return false;
				}
			}
		}
		return true;
	}

	// public boolean isIobjMeaningFul() {
	// if (Math.abs(QuestionAnalyzer.getVerbMean(this.cents.get(0))) != 2) {
	// return true;
	// }
	// if (iobj == null) {
	// return false;
	// }
	// if (iobj.isPerson) {
	// return true;
	// }
	//
	// for (int i = 0; i < cents.size(); i++) {
	// if (iobj.name.equals(cents.get(i).getName())) {
	// return false;
	// }
	// }
	// return true;
	// }

	public List<EachRelation> getEachRelations() {
		return eachrelations;
	}

	public void filterEachRelations(HashSet<String> entNames) {
		// ArrayList<EachRelation> newEachRels = new ArrayList<EachRelation>();
		// for (EachRelation er:eachrelations){
		// if (entNames.contains(er.ent1.name)){
		// newEachRels.add(er);
		// }
		// }
		// eachrelations = newEachRels;
	}
}
