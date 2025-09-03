package io.odysz.common;

import static org.junit.jupiter.api.Assertions.*;

import static io.odysz.common.LangExt.eq;
import static io.odysz.common.Regex.*;

import org.junit.jupiter.api.Test;

import io.odysz.anson.Anson;

class RegexTest {
	static final boolean verbose = true;

	@Test
	void testIsHttps() {
		assertTrue(isHttps("https://odys-z.github.io"));
		assertTrue(isHttp("http://odys-z.github.io"));
		assertFalse(isHttps("http://odys-z.github.io"));
	}

	@Test
	void testUrls() {
		// https://www.rfc-editor.org/rfc/rfc3986#appendix-B
		// Regex reg = new Regex("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?");

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
			
			if (verbose) {
				 Utils.logi(uri);
				 Utils.logix(Regex.reg3986.findGroups(uri));
			}
			Object[] domport = getHostPort(uri);
			assertTrue(eq((String)url[1], domport[0].toString()), uri);
			assertEquals(url[2], domport[1], uri);
			assertEquals(url[3], isHttps(uri), uri);
		}
	}
	
	static UrlValidator urlValidator = new UrlValidator();

	@Test
	void testValidJserv() {
		Object[][] urls = new Object[][] {
			//           [0] ok? [1] case                                                      [2] https? [3] port-range      [4] root-path                        [5] ipv6
			// 0
			new Object[] {true,  "https://odys-z.github.io:443/notes/index.html?v=1&w=2#rave", true,  new int[] {80, 443},    new String[] {"notes", "index.html"}, false},
			new Object[] {false, "https://odys-z.github.io/notes/index.html#rave?v=1&w=2",     true,  new int[] {1024, -1},   new String[] {"notes", "index.html"}, false},
			new Object[] {true,  "//odys-z.github.io/notes/index.html#rave?v=1&w=2",           false, new int[] {80, 1024},   new String[] {"notes", "index.html"}, false},
			new Object[] {true,  "//odys-z.github.io/notes/index.html#rave?v=1&w=2",           false, new int[] {80, 1024},   new String[] {"notes", "index.html"}, false},
			// 4
			new Object[] {true,  "//odys-z.github.io/notes/",                                  false, new int[] {80, 1024},   new String[] {"notes"}, false},
			new Object[] {true,  "//odys-z.github.io/notes%20/",                               false, new int[] {80, 1024},   new String[] {"notes%20"}, false},
			new Object[] {false, "//odys-z.github.io/notes /",                                 false, new int[] {80, 1024},   new String[] {"notes "}, false},
			new Object[] {true,  "//odys-z.github.io/notes%20",                                false, new int[] {80, 1024},   new String[] {"notes%20"}, false},
			// 8
			new Object[] {true,  "//odys-z.github.io/",                                        false, new int[] {80, 1024},   null, false},
			new Object[] {true,  "//odys-z.github.io",                                         false, new int[] {80, 1024},   null, false},
			new Object[] {false, "//odys-z.github.io%20",                                      false, new int[] {80, 1024},   null, false},
			new Object[] {false, "//odys-z.github.io%20/notes%20",                             false, new int[] {80, 1024},   new String[] {"notes%20"}, false},
			// 12
			new Object[] {true,  "odys-z.github.io/notes/index.html#rave?v=1&w=2",             false, new int[] {80, 1024},   new String[] {"notes", "index.html"}, false},
			new Object[] {false, "odys-z.github.io/notes/index.html#rave?v=1&w=2",             false, new int[] {81, 1024},   new String[] {"notes", "index.html"}, false},
			new Object[] {true,  "https://odys-z.github.io/notes/index.html",                  true,  new int[] {443,1024},   new String[] {"notes", "index.html"}, false},
			new Object[] {false, "https://odys-z.github.io/notes/index.html",                  true,  new int[] {1024, -1},   new String[] {"notes", "index.html"}, false},
			// 16
			new Object[] {false, "https://127.0.0.1/jserv-album",                              true,  new int[] {1024, -1},   new String[] {"jserv-album"}, false},
			new Object[] {true,  "https://127.0.0.1:8964/jserv-album",                         true,  new int[] {1024, -1},   new String[] {"jserv-album"}, false},
			new Object[] {false, "//127.0.0.1/jserv-album",                                    true,  new int[] {1024, -1},   new String[] {"jserv-album"}, false},
			new Object[] {true,  "127.0.0.1:8964/jserv-album",                                 false, new int[] {1024, -1},   new String[] {"jserv-album"}, false},
			// 20
			new Object[] {false, "https://::1/jserv-album",                                    true,  new int[] {1024, -1},   new String[] {"jserv-album"}, false},
			new Object[] {true,  "https://[::3]:8964/jserv-album",                             true,  new int[] {1024, -1},   new String[] {"jserv-album"}, true},
			new Object[] {false, "//2604:9cc0:14:b140:5706:4ab0:6cb8:d348/jserv-album",        true,  new int[] {80, -1},     new String[] {"jserv-album"}, false},
			new Object[] {true,  "https://[2604:9cc0:14:b140:5706:4ab0:6cb8:d348]/jserv-album",true,  new int[] {443, -1},    new String[] {"jserv-album"}, true},
			// 24
			new Object[] {true,  "[2604:9cc0:14:b140:5706:4ab0:6cb8:d348]:8964/jserv-album",   false, new int[] {1024, -1},   new String[] {"jserv-album"}, true},
		};
		
		for (Object[] url : urls)
			assertEquals((boolean)url[5], isIPv6((String)url[1]), (String)url[1]);
	
		int ix = 0;
		for (Object[] url : urls) {
			String uri = (String)url[1];
			
			Object[] jservparts = getHttpParts(uri);
			if (verbose) {
				Utils.logi("[%s] %s: %s", ix++, uri, LangExt.join(", ", jservparts));
			}
			assertEquals(url[0],
					urlValidator.isValid(asJserv(uri)) &&
					jservparts[1] == url[2] &&
					validUrlPort((int)jservparts[3], (int[])url[3]) &&
					validPaths((String[])url[4], (String[]) jservparts[4]),
					uri);
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
