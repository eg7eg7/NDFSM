
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import ac.il.afeka.Submission.Submission;
import ac.il.afeka.fsm.Alphabet;
import ac.il.afeka.fsm.DFSM;
import ac.il.afeka.fsm.NDFSM;

public class SubmitMain implements Submission, Assignment3 {
	private static int DFSMCount=0;
		
	public SubmitMain()
	{
		DFSMCount++;
	}
	
	@Override
	public List<String> submittingStudentIds() {
		return Arrays.asList("204808596", "334018009");
	}

	@Override
	public DFSM convert(String aNDFSMencoding) throws Exception {
		NDFSM ndfsm = new NDFSM(aNDFSMencoding);
		DFSM dfsm_converted_from_ndfsm = ndfsm.toDFSM();
		String filePath = "DFSM Converted from NDFSM " + DFSMCount + ".txt";
		PrintStream fileStream = new PrintStream(filePath);
		dfsm_converted_from_ndfsm.prettyPrint(fileStream);
		fileStream.println("\nAfter DFSM minimization :\n");
		dfsm_converted_from_ndfsm.minimize().prettyPrint(fileStream);
		fileStream.close();
		System.out.println("NDFSM encoding :\n" + aNDFSMencoding
				+ "\nConverted NDFSM to DFSM\n\ncheck File to view conversion (" + filePath + ")");
		// returns non-minimized DFSM
		return dfsm_converted_from_ndfsm;
	}

	public static void main(String[] args) throws FileNotFoundException, Exception {
		SubmitMain submitExample = new SubmitMain();
		String aNDFSMencoding = "0 1 2 3 4 5/a b/0, a, 2;0, b, 3;0,, 1;1, a, 2;1, b, 3;1, a, 4;2, a, 2;2, b, 3;2, b, 5;3,, 0;4, a, 1;4, a, 4;4, b, 5;5, a, 4/0/2 5";
		submitExample.convert(aNDFSMencoding);
		
		
		SubmitMain submitExample2 = new SubmitMain();
		DFSM Dfsm2;
		String aNDFSMencoding2 = "0 1 2/a b/0, a, 0;0, a, 1;0, b, 2;1, a, 0;1, b, 1;2, b, 0;2, b, 1/0/2";
		Dfsm2 = submitExample2.convert(aNDFSMencoding2);
		
		SubmitMain submitExample3 = new SubmitMain();
		DFSM Dfsm3;
		String aNDFSMencoding3 = "1 2 3 4/a b c/1, a, 2;1, c, 4;2,, 1;2, b, 3;3, a, 2;4, c, 3;4,, 3/1/3";
		Dfsm3 = submitExample3.convert(aNDFSMencoding3);
		
		

	}
}
