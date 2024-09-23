package io.odysz.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Interface for separating main sources' depending on JUnit Assertion.
 * <h5>Use Case:</h5>
 * Semantic.DA's utils, Docheck, for test only, is depending on JUnit
 * test package, but is used in Semantic.jserv.
 * 
 * <p>Implementation example:<pre>
 * import static io.odysz.common.LangExt.isNull;
 * import static org.junit.jupiter.api.Assertions.assertEquals;
 * import io.odysz.semantic.syn.IAssert;
 * public class AssertImpl implements IAssert {
 *   public <T> void equals(T a, T b, String... msg) throws Error {
 *     assertEquals(a, b, isNull(msg) ? null : msg[0]);
 *   }
 *   public void equals(int a, int b, String... msg) throws Error {
 *     assertEquals(a, b, isNull(msg) ? null : msg[0]);
 *   }
 *   public void fail(String e) throws Error {
 *     fail(e);
 *   }
 *   public void equals(long a, long b, String... msg) {
 *     ssertEquals(a, b, isNull(msg) ? null : msg[0]);
 *   }
 * }</pre>
 * </p>
 * 
 * @since 0.9.86
 * @author Ody Z
 */
public interface IAssert {
	<T> void equals(T a, T b, String ...msg) throws Error;

	void equali(int a, int b, String ...msg) throws Error;

	void equall(long l, long m, String... msg);

	void fail(String format) throws Error;
	
	/**
	 * Assert n-th line of file fn equals to str.
	 * <h6>About Performance</h6>
	 * The default implementation read a line in the file then close the file.
	 * This is not suitable for multiple lines assertions.
	 * 
	 * @since 0.9.86
	 * @param fn
	 * @param n_th n-th, start from 0, -1 for last line
	 * @param str regex to be matched
	 * @throws FileNotFoundException
	 */
	default void lineEq(String fn, int n_th, String str)
			throws FileNotFoundException {
		File f = new File(fn);
		Scanner freader = new Scanner(f);

		if (n_th < 0) {
			ArrayList<String> linebuf = new ArrayList<String> (-n_th);

			while (freader.hasNextLine()) {
				String ln = freader.nextLine();
				linebuf.add(ln);
				if (linebuf.size() > -n_th)
					linebuf.remove(0);
			}
			equals(str, linebuf.get(0));
		}

		else {
			while (n_th >= 0 && freader.hasNextLine()) {
				String data = freader.nextLine();
				if (n_th == 0) {
					equals(str, data);
					break;
				}
				else n_th--;
			}
		}
	   freader.close();
	}

}
