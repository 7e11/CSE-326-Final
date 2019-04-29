package dataSetGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class GenerateTrainTest {
	public static void main(String[] args) throws FileNotFoundException {
//		divideTrainTest();
		generatePure();
	}
        
        /**
         * Gets the question file and makes training and test files
         * @throws FileNotFoundException 
         */
	static void divideTrainTest() throws FileNotFoundException{
		String root = "other/irrelevant_math-aids/"; //not sure this path is right?
		Scanner sc = new Scanner(new File(root + "mathDS2.xls"));
		PrintStream train = new PrintStream(new File(root+"train.xls")); //train questions
		PrintStream test = new PrintStream(new File(root+"test.xls")); //test questions
		HashSet<Integer> testNums = getTestPos(75, 75);
		System.out.println(testNums.size());
//		PrintStream train = new PrintStream(new File(""))
		int i=0;
		while (sc.hasNextLine()){
			String line = sc.nextLine(); //read line from the file
			if (testNums.contains(i)){ //if in list of test positions, write it to test file
				test.println(line);
			}
			else{ //otherwise write it to train file
				train.println(line);
			}
			i++;
		}
		train.close();
		test.close();
		sc.close();
	}
        /**
         * Gets positions of test examples within the dataset
         * @param N number of samples
         * @param testNum number to put in test set
         * @return 
         */
	static HashSet<Integer> getTestPos(int N, int testNum){
                //number of samples of any kind
		ArrayList<Integer> all = new ArrayList<Integer>();
		for (int i=0; i<N; i++){
			all.add(i);
		}
                
                //create test set of size testNum by selecting random positions
		HashSet<Integer> test = new HashSet<Integer>();
		for (int i=0; i<testNum; i++){
			int rand = (int) (Math.random()*all.size());
			int tnum = all.remove(rand);
			test.add(tnum);
		}
		return test;
	}
	/**
         * Filters non addition/subtraction problems out of data set?
         * @throws FileNotFoundException 
         */
	public static void generatePure() throws FileNotFoundException{
		Scanner sc = new Scanner(new File("DfN/a.txt")); //questions
		Scanner sc2 = new Scanner(new File("DfN/aa.txt")); //answers
		Scanner scnot = new Scanner(new File("DfN/not +-.txt")); //what is this?
		HashSet<String> notS = new HashSet<String>();
		while (scnot.hasNext()){
			notS.add(scnot.nextLine());
		}
                //some kind of filtering mechanism?
		PrintStream op = new PrintStream(new File("pure_a.txt"));
		PrintStream op2 = new PrintStream(new File("pure_aa.txt"));
		while (sc.hasNext()){
			String l = sc.nextLine();
			String a = sc2.nextLine();
			if (!notS.contains(l)){
				op.println(l);
				op2.println(a);
			}
		}
		
	}
}