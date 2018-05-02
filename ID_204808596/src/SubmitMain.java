import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import ac.il.afeka.Submission.Submission;
import ac.il.afeka.fsm.Alphabet;
import ac.il.afeka.fsm.DFSM;
import ac.il.afeka.fsm.NDFSM;

public class SubmitMain implements Submission, Assignment3 {

	@Override
	public List<String> submittingStudentIds() {
		return Arrays.asList("204808596", "334018009");
	}

	@Override
	public DFSM convert(String aNDFSMencoding) throws Exception {
		return new NDFSM(aNDFSMencoding).toDFSM();
	}

	public static void main(String[] args) throws FileNotFoundException, Exception
	{
		SubmitMain submitExample = new SubmitMain();
		
		File outputFile = new File("NDFSM 2 DFSM Exercise.txt");
		PrintStream fileStream = new PrintStream(outputFile);
		String aNDFSMencoding = "0 1 2 3 4 5/0, a, 2;0, b, 3;0, " + Alphabet.EPSILON + ", q;1, a, 2;1, b, 3;1, a, 4;2, a, 2;2, b, 3;2, b, 5;3, " + Alphabet.EPSILON + ", 0;4, a, 1;4, a, 4;4, b, 5;5, a, 4/0/2 5";
		submitExample.convert(aNDFSMencoding).prettyPrint(fileStream);
		
		fileStream.close();
	}
}
