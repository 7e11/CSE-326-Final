package dataSetGenerator;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.PDFTextStripperByArea;

public class ParseQuestions {

	public static void main(String[] args) throws FileNotFoundException {
		// File f = new File(
		// "D:/cources/research_UW/math_words/data/math-aids/word_add_1digit_2addend.pdf");
		// readPDF(f);
		// parseMathAidsFile(f);
		parseMathAids();
	}

        /**
         * Gets a PDF and puts the text contents in a string.
         * @param f PDF file
         * @return string contents
         */
	static String readPDF(File f) {
		PDDocument document;

		try {
			document = PDDocument.load(f);
			document.getClass();
			if (document.isEncrypted()) {
				try {
					document.decrypt("");
				} catch (InvalidPasswordException e) {
					System.err
							.println("Error: Document is encrypted with a password.");
					System.exit(1);
				}
			}

			PDFTextStripperByArea stripper = new PDFTextStripperByArea();
			stripper.setSortByPosition(true);
			PDFTextStripper stripper2 = new PDFTextStripper();
			String st = stripper2.getText(document);

			System.out.println(st);
			return st;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

        /**
         * Converts PDF question files to Excel? Why?
         * @throws FileNotFoundException 
         */
	static void parseMathAids() throws FileNotFoundException {
		String folderName = "other/irrelevant_math-aids/";
		File folder = new File(
				folderName);
		File[] files = folder.listFiles();
		PrintStream op = new PrintStream(new File(folderName + "mathDS2.xls"));
		op.println("Question\tNum\tObject");
                //Reads the PDF files in some folder and creates an excel file
		for (File f : files) {
			if (!f.getName().endsWith(".pdf")){
				continue;
			}
			try {
				ArrayList<MAidQuestion> mqs = parseMathAidsFile(f);
				for (MAidQuestion mq : mqs) {
					op.println(mq.getExcelString());
				}
			} catch (Exception e) {
				System.err.println(f.getName());
				e.printStackTrace();
			}

		}
		op.close();
		System.err.println("END");

	}

        /**
         * @param f a pdf file
         * @return a set of valid questions in the file
         */
	static ArrayList<MAidQuestion> parseMathAidsFile(File f) {
		String docs = readPDF(f); //pdf contents as string
		Scanner sc = new Scanner(docs);
		String line = sc.nextLine();
                //go to after the 2nd line beginning with 1) ???
		while (!line.startsWith("1 )") && !line.startsWith("1)")) {
			line = sc.nextLine();
		}
		line = sc.nextLine();
		while (!line.startsWith("1 )") && !line.startsWith("1)")) {
			line = sc.nextLine();
		}
		int qnum = 1; //question number
		ArrayList<MAidQuestion> maidQuestions = new ArrayList<MAidQuestion>();

		while (sc.hasNext()) {
			String q = "";
			qnum++;
                        //gets the question into a string, even if it is on
                        //multiple lines, ending with #)
			while (!line.startsWith(qnum + " )") &&
					!line.startsWith(qnum + ")")) {
				q = q + line + " ";
				if (!sc.hasNext()) {
					break;
				}
				line = sc.nextLine();
			}
                        //makes sure string is valid question
			MAidQuestion mq = new MAidQuestion(q);
			if (mq.goodQ) {
				maidQuestions.add(mq);
			}
		}

		for (MAidQuestion mq : maidQuestions) {
			System.out.println(mq);
		}
		return maidQuestions;
	}

}
