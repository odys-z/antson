package io.odysz.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

class UtilsTest {

	@Test
	void testLoadTxt() throws IOException, URISyntaxException, ClassNotFoundException {
		assertEquals("line1\nline2", Utils.loadTxt(UtilsTest.class, "txt"));
		assertEquals("line1\nline2", Utils.loadTxt("txt"));
	}

}
