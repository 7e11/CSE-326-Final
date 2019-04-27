package dataSetGenerator;

import java.io.File;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;

import equationExtraction.World;

public class Test {
	static ArrayList<HashSet<String>> ops;
	
	public static void main(String[] args) throws FileNotFoundException {
//		readVerbsH();
		testEquations();
//		refineAns();
//		refineEq();
//		whatsdifference();
//		testSimpeMethod();
//		readTest();
	}
        /**
         * Pulls out the answers for each question in test file
         * @throws FileNotFoundException 
         */
	static void readTest() throws FileNotFoundException{
		Scanner sc = new Scanner(new File("b.txt"));
		while (sc.hasNext()){
			String txt = sc.nextLine();
			int i = txt.indexOf('?');
			txt = txt.substring(i+2);
//			System.out.println(txt);
			Scanner sc2 = new Scanner(txt);
			double d =  sc2.nextDouble();
			System.out.println(d);
		}
	}
        /**
         * Maybe just a utility function that prints verbs.
         * Can probably be ignored.
         * @throws FileNotFoundException 
         */
	static void readVerbsH() throws FileNotFoundException{
                //should be verbs3? that file lists verbs and their categories
		Scanner sc = new Scanner(new File("verbs.txt"));
                //maps tokenizers to lines in the verbs list
		HashMap<String, String> verbs = new HashMap<String, String>();
		while (sc.hasNext()){
			String nextLine = sc.nextLine();
			StringTokenizer st = new StringTokenizer(nextLine);
			verbs.put(st.nextToken(), nextLine) ;
		}
                //where is verbsn?
		Scanner scnew = new Scanner(new File("verbsn.txt"));
                //looks for verbs from verbs.txt in verbsn.txt, whatever those are
		while (scnew.hasNext()){
			String nextLine = scnew.nextLine();
//			System.out.println(nextLine);
			Scanner sc2 = new Scanner(nextLine);
			String verb = sc2.next();
			if (verbs.containsKey(verb)){
				System.out.println(verbs.get(verb));
			}
			else{
				System.out.println(verb+" not found");
			}
		}
	}
        
        /**
         * Another method that doesn't really do anything, just prints stuff
         * @throws FileNotFoundException 
         */
	static void testSimpeMethod() throws FileNotFoundException{
		ops = new ArrayList<HashSet<String>>();
		Scanner sc = new Scanner(new File("base.txt")); //what is this?
		HashSet<String> h = new HashSet<String>();
		ops.add(h);
                //for each line in base.txt
		while (sc.hasNext()){
			String l = sc.nextLine();
                        //if a line is only new lines, we stop adding lines
                        //to the current hashmap and make a new one
			if (l.trim().length()==0){
				h = new HashSet<String>();
				ops.add(h);
			}
                        //add a line to the hashmap
			else{
				h.add(l);
			}
		}
                //now we print this out
		for (HashSet<String> hh:ops){
			System.out.println(hh);
		}
	}
        /**
         * 
         * @param q a question
         * @return a list of all numbers in the question
         */
	public static int BaseGuess(String q){
		StringTokenizer st = new StringTokenizer(q);
		ArrayList<Double> nums = new ArrayList<Double>();
		
		while (st.hasMoreTokens()){
			String tok = st.nextToken();
			double d;
			try {
				d = Double.parseDouble(tok);
				nums.add(d);
			} catch (Exception e) {
			}
		}
		return 0;
	}
	
        /**
         * Solves and validates the generated equations
         * @throws FileNotFoundException 
         */
	static void testEquations() throws FileNotFoundException{
		Scanner sc = new Scanner(new File("DfN/DS2/ans.txt")); //answers
		Scanner eqsc = new Scanner(new File("DfN/DS2/eq.txt")); //equations
		int i=1;
		while (sc.hasNext()){
			double res = sc.nextDouble();
			String eq = eqsc.nextLine();
			double sres = World.solveEquation(eq, "X"); //solves the generated equation (algebra?)
                        //compares solved equation with ground truth
			if (Math.abs(res-sres)>.00001){
				System.out.println(i+" "+sres);
				System.out.println(eq);
			}
			i++;
		}
		System.out.println(i);
	}
	
        /**
         * Finds lines in the two files that are the same and prints them
         * @throws FileNotFoundException 
         */
	static void whatsdifference() throws FileNotFoundException{
		Scanner sc1 = new Scanner(new File("a.txt"));
		Scanner sc2 = new Scanner(new File("DfN/refined/DS1/q.txt"));
		String l2 = sc2.nextLine();
		int i=1;
		while (sc1.hasNext()){
			String l = sc1.nextLine();
			
			if (l.equals(l2)){
				l2 = sc2.next();
				continue;
			}
			else{
				System.out.println(i);
				System.out.println(l);
			}
			i++;
		}
	}
	
	
	/**
         * Grabs a double from each answer and prints it?
         * @throws FileNotFoundException 
         */
	static void refineAns() throws FileNotFoundException{
		Scanner sc = new Scanner(new File("DfN/DS2/ans.txt"));
		while (sc.hasNext()){
			String s = sc.nextLine();
			System.out.println(problemAnalyser.Util.getDouble(s));
		}
	}
	
	static void refineEq() throws FileNotFoundException{
		Scanner sc = new Scanner(new File("DfN/DS2/eq.txt"));
                //read each equation
		while (sc.hasNext()){
			String l = sc.nextLine();
			Scanner sc2 = new Scanner(l);
			
                        //tokenize the equation
			ArrayList<String> tokens = new ArrayList<String>();
			while (sc2.hasNext()){
				String tok = sc2.next();
//				System.out.println(tok);
                                //if a token is a number, convert it to a double, otherwise just add it as a string
				if (isNumber(tok)){
					tokens.add(problemAnalyser.Util.getDouble(tok)+"");
				}
				else{
					tokens.add(tok);
				}
			}
			
//			boolean isDouble = false;
                        //lastNum is the previous num in the tokens
                        //WHAT DOES S DO?
			double lastNum = 0;
			String s = "";
			for (String tok:tokens){ //iterate thru the tokens
				if (!isNumber(tok)){
					if (lastNum!=0){
						s += lastNum +" ";
						lastNum = 0;
					}
					s += tok+" ";
//					System.out.println("here "+tok);
				}
                                //if a token is a number, add it to lastNum
				else{
					lastNum += Double.parseDouble(tok);
				}
			}
			if (lastNum!=0){
				s += lastNum;
			}
			
			System.out.println(s.trim());
		}
	}
	
        /**
         * @param s a prospective number
         * @return true if s is a number
         */
	static boolean isNumber(String s){
		char c = s.charAt(0);
		if (c<='9' && c>='0'){
			return true;
		}
		return false;
	}
}
