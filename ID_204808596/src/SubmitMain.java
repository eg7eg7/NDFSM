
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import ac.il.afeka.Submission.Submission;
import ac.il.afeka.fsm.DFSM;
import ac.il.afeka.fsm.NDFSM;

public class SubmitMain implements Submission, Assignment3 {

	@Override
	public List<String> submittingStudentIds() {
		return Arrays.asList("204808596", "334018009");
	}

	@Override
	public DFSM convert(String aNDFSMencoding) throws Exception {
		NDFSM ndfsm = new NDFSM(aNDFSMencoding);
		DFSM dfsm_converted_from_ndfsm = ndfsm.toDFSM();
		String filePath = "DFSM Converted from NDFSM.txt";
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

	}
}
