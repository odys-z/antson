package io.odysz.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Scanner;

import org.eclipse.jetty.util_ody.RolloverFileOutputStream;
import org.junit.jupiter.api.Test;

class UtilsTest {

	@Test
	void testLoadTxt() throws IOException, URISyntaxException, ClassNotFoundException {
		assertEquals("line1\nline2", Utils.loadTxt(UtilsTest.class, "txt"));
		assertEquals("line1\nline2", Utils.loadTxt("txt"));
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
		
		assertEquals("1\n3\n", out.toString());

		Utils.touchDir("temp");
        RolloverFileOutputStream os = new RolloverFileOutputStream("temp/yyyy_mm_dd.log", true);
		String fn = os.getDatedFilename();
		File f = new File(fn);
		if (f.exists()) {
			f.delete();
			os.close();
			os = new RolloverFileOutputStream("temp/yyyy_mm_dd.log", true);
		}

        PrintStream logStream = new PrintStream(os);
        Utils.logOut(logStream);
        Utils.logErr(logStream);

		String line = new Date().toString() + ": RolloverFileOutputStream printing...";

		Utils.logi(line);
        Utils.logOut(null);
		Utils.logi("Printing system.out ...");

		os.flush();
		os.close();
		
		Scanner freader = new Scanner(f);
	      while (freader.hasNextLine()) {
	        String data = freader.nextLine();
	        assertEquals(line, data);
	      }
	   freader.close();
	}
}
