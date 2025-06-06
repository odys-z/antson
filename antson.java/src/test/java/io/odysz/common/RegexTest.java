package io.odysz.common;

import static org.junit.jupiter.api.Assertions.*;

import static io.odysz.common.LangExt.eq;
import static io.odysz.common.Regex.*;

import org.junit.jupiter.api.Test;

import io.odysz.anson.Anson;

class RegexTest {

	@Test
	void testIsHttps() {
		assertTrue(isHttps("https://odys-z.github.io"));
		assertTrue(isHttp("http://odys-z.github.io"));
		assertFalse(isHttps("http://odys-z.github.io"));
	}

	@Test
	void testUrls() {
		// https://www.rfc-editor.org/rfc/rfc3986#appendix-B
		@SuppressWarnings("unused")
		Regex reg = new Regex("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?");

		Object[][] urls = new Object[][] {
			new Object[] {"https://odys-z.github.io/notes/index.html#rave?v=1&w=2", "odys-z.github.io", null, true},
			new Object[] {"//odys-z.github.io/notes/index.html#rave?v=1&w=2",       "odys-z.github.io", null, false},
			new Object[] {"odys-z.github.io/notes/index.html#rave?v=1&w=2",         "odys-z.github.io", null, false},

			new Object[] {"https://odys-z.github.io:8964", "odys-z.github.io", 8964, true},
			new Object[] {"//odys-z.github.io:8964",       "odys-z.github.io", 8964, false},
			new Object[] {"odys-z.github.io:8964",         "odys-z.github.io", 8964, false},

			new Object[] {"http://odys-z.github.io", "odys-z.github.io", null, false},
			new Object[] {"//odys-z.github.io",      "odys-z.github.io", null, false},
			new Object[] {"odys-z.github.io",        "odys-z.github.io", null, false},

			new Object[] {"http://127.0.0.1/notes/index.html", "127.0.0.1", null, false},
			new Object[] {"//127.0.0.1/notes/index.html",      "127.0.0.1", null, false},
			new Object[] {"127.0.0.1/notes/index.html",        "127.0.0.1", null, false},

			new Object[] {"http://127.0.0.1:8964/index.html?a=1&b=2", "127.0.0.1", 8964, false},
			new Object[] {"//127.0.0.1:8964/index.html?a=1&b=2",      "127.0.0.1", 8964, false},
			new Object[] {"127.0.0.1:8964/index.html?a=1&b=2",        "127.0.0.1", 8964, false},

			new Object[] {"https://127.0.0.1:8964", "127.0.0.1", 8964, true},
			new Object[] {"//127.0.0.1:8964",       "127.0.0.1", 8964, false},
			new Object[] {"127.0.0.1:8964",         "127.0.0.1", 8964, false},

			new Object[] {"https://127.0.0.1", "127.0.0.1", null, true},
			new Object[] {"//127.0.0.1",       "127.0.0.1", null, false},
			new Object[] {"127.0.0.1",         "127.0.0.1", null, false}
		};
		
		for (Object[] url : urls) {
			String uri = (String)url[0];
			// Utils.logi(uri);
			// Utils.logix(reg.findGroups(uri));
			Object[] domport = getHostPort(uri);
			assertTrue(eq((String)url[1], domport[0].toString()), uri);
			assertEquals(url[2], domport[1], uri);
			assertEquals(url[3], isHttps(uri), uri);
		}
	}
	
	@Test
	void testIsEnvelope() {
		assertTrue(Anson.startEnvelope("'{\"type\": \"com.examples.test\"}"));
		assertTrue(Anson.startEnvelope("'{ \"type\": \"com.examples.test\"}"));
		assertTrue(Anson.startEnvelope("'{\t\"type\": \"com.examples.test\"}"));
		assertTrue(Anson.startEnvelope("'{\n\"type\": \"com.examples.test\"}"));

		assertTrue(Anson.startEnvelope("{\"type\": \"com.examples.test\"}"));
		assertTrue(Anson.startEnvelope("{\n\"type\": \"com.examples.test\"}"));

		assertFalse(Anson.startEnvelope("{type: \"com.examples.test\"}"));
		assertFalse(Anson.startEnvelope("'{type: \"com.examples.test\"}"));
		assertFalse(Anson.startEnvelope("{type, \"com.examples.test\"}"));

		assertTrue(Anson.startEnvelope("{'type': \"com.examples.test\"}"));
	}
	
	@Test
	void testRmVolume() {
		String reluri = FilenameUtils.concat("move", "0001 1.pdf");
		String upload = FilenameUtils.concat("upload", reluri);
		String vol_1 = FilenameUtils.concat("$VOLUME_1", reluri);
		String vol_2 = FilenameUtils.concat("$VOLUME_2", upload);

		assertEquals(reluri, FilenameUtils.removePrefixVolume(reluri));
		assertEquals(upload, FilenameUtils.removePrefixVolume(upload));
		assertEquals(reluri, FilenameUtils.removePrefixVolume(reluri, "upload"));
		assertEquals(reluri, FilenameUtils.removePrefixVolume(upload, "upload"));

		assertEquals(reluri, FilenameUtils.removePrefixVolume(upload, "upload"));
		
		assertEquals(reluri, FilenameUtils.removePrefixVolume(vol_1));
		assertEquals(upload, FilenameUtils.removePrefixVolume(vol_2));
	}
}
