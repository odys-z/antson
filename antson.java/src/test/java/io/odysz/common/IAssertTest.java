package io.odysz.common;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

class IAssertTest implements IAssert {
	static IAssert azert;

	static {
		azert = new IAssertTest();
	}

	@Test
	void testAzertFile() throws FileNotFoundException {
		azert.lineEq("src/test/res/lines.txt", 0, "8961");
		azert.lineEq("src/test/res/lines.txt", 1, "8962");
		azert.lineEq("src/test/res/lines.txt", 2, "8963");
		azert.lineEq("src/test/res/lines.txt", 3, "8964");

		azert.lineEq("src/test/res/lines.txt", -1, "8964");
		azert.lineEq("src/test/res/lines.txt", -2, "8963");
		azert.lineEq("src/test/res/lines.txt", -3, "8962");
		azert.lineEq("src/test/res/lines.txt", -4, "8961");
	}

	@Override
	public <T> void equals(T a, T b, String... msg) throws Error { }

	@Override
	public void equali(int a, int b, String... msg) throws Error { }

	@Override
	public void equall(long l, long m, String... msg) { }

	@Override
	public void fail(String format) throws Error { }
}
