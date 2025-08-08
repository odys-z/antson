package io.odysz.common;

import static org.junit.jupiter.api.Assertions.*;

import static io.odysz.common.LangExt.isblank;
import static io.odysz.common.Utils.logi;

import java.io.File;
import org.junit.jupiter.api.Test;

class FilenameUtilsTest extends FilenameUtils {
	public static void assertPathEquals(String expect, String actual) {
		try {
			if (isblank(expect) && isblank(actual)) return;
			assertEquals(expect, actual);
		} catch (AssertionError e) {
			assertEquals(expect.replaceAll("/", "\\\\"), actual);
		}
	}
	
	@Test
	void testConcat() {
		assertPathEquals("foo/bar", concat("foo", "bar"));
		assertPathEquals("/foo/bar", concat("/foo", "bar"));
		assertPathEquals("/foo/bar", concat("/foo/", "bar"));
		assertPathEquals("/bar", concat("/foo", "/bar"));
		assertPathEquals("C:/bar", concat("/foo", "C:/bar"));
		assertPathEquals("C:bar", concat("/foo", "C:bar"));
		assertPathEquals("/foo/bar", concat("/foo/a/", "../bar"));
		assertPathEquals("../bar", concat("/foo/", "../../bar"));
		assertPathEquals("/bar", concat("/foo/", "/bar"));
		assertPathEquals("/bar", concat("/foo/..", "/bar"));
		assertPathEquals("/foo/bar/c.txt", concat("/foo", "bar/c.txt"));
		assertPathEquals("/foo/c.txt/bar", concat("/foo/c.txt", "bar"));

		assertPathEquals("src/test/res/lines.txt", concat("src/test/java/", "../res/lines.txt"));
		assertPathEquals("/foo/../bar", concat("/foo/..", "bar"));
		assertPathEquals("src/test/java/../res/lines.txt", concat("src/test/java/..", "res/lines.txt"));
		assertTrue(new File("src/test/java/../res/lines.txt").exists());
	}
	
	@Test
	void testNormalize() {
		// since 0.9.118
		assertPathEquals("src/test/res/lines.txt", normalize("src/test/java/../res/lines.txt"));
		assertPathEquals("src/res/lines.txt", normalize("src/test/java/../../res/lines.txt"));
		assertPathEquals("res/lines.txt", normalize("src/test/java/../../../res/lines.txt"));
		assertPathEquals("../res/lines.txt", normalize("src/test/java/../../../../res/lines.txt"));
		
		assertPathEquals("../foo", normalize("../foo"));
		assertPathEquals("~/../bar", normalize("~/../bar"));
		assertPathEquals("C:/../bar", normalize("C:\\..\\bar"));
		assertPathEquals("~/../bar", normalize("~/../bar"));

		// same as before 0.9.118
		assertPathEquals("/foo/", normalize("/foo//"));
		assertPathEquals("/foo/", normalize("/foo//"));
		assertPathEquals("/foo/", normalize("/foo/./"));
		assertPathEquals("/bar", normalize("/foo/../bar"));
		assertPathEquals("/baz", normalize("/foo/../bar/../baz"));
		assertPathEquals("/foo/bar", normalize("/foo//./bar"));
		assertPathEquals("/../", normalize("/../"));
		assertPathEquals("../foo", normalize("../foo"));
		assertPathEquals("../foo/", normalize("../foo/"));
		assertPathEquals("../foo/bar", normalize("../foo/bar"));
		assertPathEquals("//server/bar", normalize("//server/foo/../bar"));
		assertPathEquals("C:/bar", normalize("C:\\foo\\..\\bar"));
		assertPathEquals("~/bar", normalize("~/foo/../bar"));

		/*
		 /foo//               -->   /foo/
		 /foo/./              -->   /foo/
		 /foo/../bar          -->   /bar
		 /foo/../bar/         -->   /bar/
		 /foo/../bar/../baz   -->   /baz
		 //foo//./bar         -->   /foo/bar
		 /../                 -->   null
		 ../foo               -->   null
		 foo/bar/..           -->   foo/
		 foo/../../bar        -->   null
		 foo/../bar           -->   bar
		 //server/foo/../bar  -->   //server/bar
		 //server/../bar      -->   null
		 C:\foo\..\bar        -->   C:\bar
		 C:\..\bar            -->   null
		 ~/foo/../bar/        -->   ~/bar/
		 ~/../bar             -->   null
		 */
	}

	@Test
	void testRelative2Abs() {
		String dot = new File(".").getAbsolutePath();

		String test = FilenameUtils.concat(dot, "testRelative2Abs");
		logi(test);
		assertPathEquals(test, rel2abs("testRelative2Abs"));

		String test2 = FilenameUtils.concat(dot, "test", "2test.json");
		logi(test2);
		assertPathEquals(test2, rel2abs("test", "2test.json"));
		
		
		String src_test = new File("src/test").getAbsolutePath();
		logi("[%s]", src_test);

		test = FilenameUtils.concat(src_test, "../../testRelative2Abs");
		logi(test);
		assertPathEquals(test, rel2abs("src/test", "../../testRelative2Abs"));
		assertPathEquals(test, rel2abs("src/test", "..", "..", "testRelative2Abs"));

		src_test = new File("src").getAbsolutePath();
		logi("[%s]", src_test);
		test2 = FilenameUtils.concat(src_test, "2test.json");
		logi(test2);
		assertPathEquals(test2, rel2abs("src/test", "../2test.json"));
		assertPathEquals(test2, rel2abs("src/test", "..", "2test.json"));
	}
}
