package io.odysz.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import org.junit.jupiter.api.Test;

class FilenameUtilsTest extends FilenameUtils {
	public static void assertPathEquals(String expect, String actual) {
		try {
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

}
