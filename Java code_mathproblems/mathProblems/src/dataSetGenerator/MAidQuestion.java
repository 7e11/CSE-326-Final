package dataSetGenerator;

import java.util.Scanner;

public class MAidQuestion {
	String question;
	String answerObj;
	double answerNum;
	boolean goodQ;//is the question format acceptable
	
        /**
         * Validates the format of the question
         * @return true if the question has a valid format
         */
	boolean isGoodQuestion(){
		return goodQ;
	}
	/**
         * Tests if a string is a valid question
         * @param s 
         */
        public MAidQuestion(String s) {
		Scanner sc = new Scanner(s);
		System.err.println("s: "+s);
                //test if there is an integer
		try {
			sc.nextInt();
			sc.next();
		} catch (Exception e) {
			sc.next();
		}
		
                //tests if there is one and only one question mark
		s = "";
		while (sc.hasNextLine()){
			s = s+sc.nextLine().trim()+" ";
		}
		s = s.trim();
		int lq = s.lastIndexOf('?');
		int fq = s.indexOf('?');
		if (fq != lq){
			System.out.println("bad question:\n"+s);
			return;
		}
                
		question = s.substring(0,lq+1);
		//validate the answer
                s = s.substring(lq+1).trim();
		System.out.println("ans s: "+s);
		int f_ = s.indexOf('_');
		s = s.substring(0,f_);
		sc = new Scanner(s);
		String numS = Util.dollarProcess(sc.next()); //process currency quantity
		try {
			answerNum = Double.parseDouble(numS); //gets answer as a number if possible
		} catch (Exception e) {
			e.printStackTrace();
			goodQ = false; //if we can't represent the answer as a number, badness results
			return;
		}
		
		try {
			answerObj = sc.nextLine();
		} catch (Exception e) {
			
		}
		
		if (sc.hasNext()){
			System.out.println("bad end "+question+" "+answerNum);
			goodQ = false;
		}
		goodQ = true;
	}
	
        /**
         * @return human readable question/answer string
         */
	public String toString(){
		String ret = "q: "+question+"\n"+"anso: "+answerObj+" ansn: "+answerNum;
		return ret;
	}
	/**
         * Question string for csv purposes
         * @return  csv usable string representing question/answer
         */
	public String getExcelString(){
		String ret = question+"\t"+answerNum+"\t"+answerObj;
		return ret;
	}
	
}
