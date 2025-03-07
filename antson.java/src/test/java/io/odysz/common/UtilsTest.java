package io.odysz.common;

import static io.odysz.common.Utils.*;
import static io.odysz.common.LangExt.eq;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Date;
import org.eclipse.jetty.util_ody.RolloverFileOutputStream;
import org.junit.jupiter.api.Test;

class UtilsTest {

	@Test
	void testLoadTxt() throws IOException, URISyntaxException, ClassNotFoundException {
		assertEquals("line1\nline2", Utils.loadTxt(UtilsTest.class, "txt"));
		assertEquals("line1\nline2", Utils.loadTxt("txt"));

		assertEquals("drop table if exists test;\ncreate table if not exists test (id: varchar(2));",
			Utils.loadTxt("test.sqlite.ddl"));
	}

	@Test
	void testRepeat() {
		assertEquals("", Utils.repeat("", 0));
		assertEquals("", Utils.repeat(".", 0));
		assertEquals(".", Utils.repeat(".", 1));
		assertEquals("..", Utils.repeat(".", 2));
		assertEquals("+ + ", Utils.repeat("+ ", 2));
		assertEquals("+ + + ", Utils.repeat("+ ", 3));
		assertEquals("++++++", Utils.repeat("+", 6));
	}
	
	@Test
	void testOut2file() throws IOException {
        
        OutputStream out = new OutputStream() {
            private StringBuilder string = new StringBuilder();

            @Override
            public void write(int b) throws IOException {
                this.string.append((char) b );
            }

            public String toString() {
                return this.string.toString();
            }
        };
        
		Utils.logOut(new PrintStream(out));
		Utils.logi("1");
		Utils.logOut(null);
		Utils.logi("OK: System.out printing...");
		Utils.logOut(new PrintStream(out));
		Utils.logi("3");
		
		assertTrue(eq("1\n3\n", out.toString()) || eq("1\r\n3\r\n", out.toString()));

		Utils.touchDir("temp");
        RolloverFileOutputStream os = new RolloverFileOutputStream("temp/yyyy_mm_dd.log", true);
		String fn = os.getDatedFilename();
//		os.close();

		// File f = new File(fn);

//		if (f.exists()) {
//			if (!f.delete());
//				fail("Cannot delete file " + fn);
//		}
//		os = new RolloverFileOutputStream("temp/yyyy_mm_dd.log", true);

        PrintStream logStream = new PrintStream(os);
        Utils.logOut(logStream);
        Utils.logErr(logStream);

		String line = new Date().toString() + ": RolloverFileOutputStream printing...";

		Utils.logi(line);
        Utils.logOut(null);
		Utils.logi("Printing system.out ...");

		os.flush();
		os.close();
		
		new IAssertTest().lineEq(fn, -1, line);

//		Scanner freader = new Scanner(f);
//	    while (freader.hasNextLine()) {
//	        String data = freader.nextLine();
//	        assertEquals(line, data);
//	        //<Thu Sep 19 11:43:23 CST 2024: RolloverFileOutputStream printing...> but was: 
//	        //<Thu Sep 19 11:41:49 CST 2024: RolloverFileOutputStream printing...
//	      }
//	   freader.close();
	   
	   Utils.logOut(null);
	   Utils.logErr(null);
	}
	
	@Test
	void testWait() throws InterruptedException {
		boolean[] lights = new boolean[] {false, false, false};
		
		new Thread(() -> {
			for (int i = 0; i < lights.length; i++)
				lights[i] = true;
		}).start();

		awaitAll(lights, -1);

		for (boolean light : lights)
			assertTrue(light);
		
		waiting(lights, 1);

		new Thread(() -> {
			lights[1] = true;
		}).start();

		awaitAll(lights);
		assertTrue(lights[1]);
	}
}
