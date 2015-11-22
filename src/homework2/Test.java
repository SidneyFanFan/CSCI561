package homework2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Test {

	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException {
		String output = "hw2/largecase/prunning/case_%d.txt";
		StringBuffer md5s = new StringBuffer();
		long s = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			Mancala m = new Mancala(String.format(output, i));
			m.start(i);
			FileInputStream fis = new FileInputStream(new File(String.format(
					"hw2/largecase_answer/prunningAnswer/next_state_%d.txt", i)));
//			String md5 = org.apache.commons.codec.digest.DigestUtils
//					.md5Hex(fis);
//			fis.close();
//			System.out.println(md5);
//			// md5s.append(String.format(output, i));
//			// md5s.append("\t");
//			// md5s.append("MD5: ");
//			md5s.append(md5);
//			md5s.append("\n");
		}
		System.out.println(System.currentTimeMillis() - s);

		export(md5s.toString(), "hw2/caseAnswer_prunning_state.txt");

	}

	public static void export(String result, String filePath) {
		try {
			FileWriter fw = new FileWriter(filePath);
			fw.write(result);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
