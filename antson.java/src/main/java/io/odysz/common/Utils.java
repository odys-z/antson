package io.odysz.common;

import static io.odysz.common.LangExt.isNull;
import static io.odysz.common.LangExt.isblank;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.odysz.anson.Anson;

public class Utils {
	/**Used for print out all caller.
	 * Calling logi() etc. is recommended add a static final boolean flag before calling:<br>
	 * if (staticBoolean) Util.logi("messages");<br>
	 * In this way, the java compiler will optimize the code to nothing if the <i>staticBoolean</i> is false.
	 * This flag is used for find out {@link #logi(String, String...)} calling that's not controlled by the flag.
	 * */
	static boolean printCaller = false;

	public static int tabwidth = 4;
	
	/**See {@link #printCaller}
	 * @param printcall
	 */
	public static void printCaller(boolean printcall) { printCaller = printcall; }

	/**Print out log with System.out.println().<br>
	 * Note: this method will print out caller if {@link #printCaller} is true.<br>
	 * Calling logi() etc. is recommended add a static final boolean flag before calling:<br>
	 * if (staticBoolean) Util.logi("messages");<br>
	 * In this way, the java compiler will optimize the code to nothing if the <i>staticBoolean</i> is false.
	 * @param format
	 * @param args
	 */
	public static void logi(String format, Object... args) {
		try {
			if (printCaller) {
				StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
				System.out.println(String.format("\nlog by        %s.%s(%s:%s)", 
								stElements[2].getClassName(), stElements[2].getMethodName(),
								stElements[2].getFileName(), stElements[2].getLineNumber()));
				if (stElements.length > 3)
				System.out.println(String.format("              %s.%s(%s:%s)", 
								stElements[3].getClassName(), stElements[3].getMethodName(),
								stElements[3].getFileName(), stElements[3].getLineNumber()));
			}

			if (format != null)
				if (args != null && args.length > 0)
					System.out.println(String.format(format, args));
				else
					System.out.println(format);

		} catch (Exception ex) {
			StackTraceElement[] x = ex.getStackTrace();
			System.err.println(String.format("logi(): Can't print. Error: %s. called by %s.%s()",
					ex.getMessage(), x[0].getClassName(), x[0].getMethodName()));
		}
	}

	public static void logi(String[] row) {
		try {
			if (printCaller) {
				StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
				System.out.println(String.format("logger:        %s.%s(%s:%s)", 
								stElements[2].getClassName(), stElements[2].getMethodName(),
								stElements[2].getFileName(), stElements[2].getLineNumber()));
			}

			if (row != null)
				System.out.println(LangExt.toString(row));
		} catch (Exception ex) {
			System.err.println("logi(): Can't print. Error:");
			ex.printStackTrace();
		}

	}

	public static <T> void logi(List<T> list, Object... args) {
		try {
			if (printCaller) {
				StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
				System.out.println(String.format("logger:        %s.%s(%s:%s)", 
								stElements[2].getClassName(), stElements[2].getMethodName(),
								stElements[2].getFileName(), stElements[2].getLineNumber()));
			}

			if (list != null)
				for (T it : list)
					if (it == null)
						System.out.println("null");
					else if (args != null && args.length > 0)
						System.out.println(String.format(it.toString(), args));
					else
						System.out.println(it.toString());

		} catch (Exception ex) {
			System.err.println("logi(): Can't print. Error:");
			ex.printStackTrace();
		}
	}

	public static void logArr(List<String[]> list, Object... args) {
		try {
			if (printCaller) {
				StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
				System.out.println(String.format("logger:        %s.%s(%s:%s)", 
								stElements[2].getClassName(), stElements[2].getMethodName(),
								stElements[2].getFileName(), stElements[2].getLineNumber()));
			}

			if (list != null)
				for (String[] it : list)
					if (args != null && args.length > 0)
						System.out.println(String.format(LangExt.toString(it), args));
					else
						System.out.println(LangExt.toString(it));

		} catch (Exception ex) {
			System.err.println("logi(): Can't print. Error:");
			ex.printStackTrace();
		}
	}

	public static void logMap(Map<?, ?> map, String indent) {
		try {
			if (map != null) {
				System.out.println("Map size: " + map.size());
				for (Object mk : map.keySet())
					System.out.println(indent == null ? "" : indent + mk + ",\t" + map.get(mk));
			}
			else System.out.println("Map is null.");
		} catch (Exception ex) {
			StackTraceElement[] x = ex.getStackTrace();
			System.err.println(String.format("logMap(): Can't print. Error: %s. called by %s.%s()",
					ex.getMessage(), x[0].getClassName(), x[0].getMethodName()));
		}
	}

	public static void logkeys(Map<String, ?> map) {
		try {
			if (map != null)
				for (String mk : map.keySet())
					System.out.print(mk + ", ");
			System.out.println();
		} catch (Exception ex) {
			StackTraceElement[] x = ex.getStackTrace();
			System.err.println(String.format("logkeys(): Can't print. Error: %s. called by %s.%s()",
					ex.getMessage(), x[0].getClassName(), x[0].getMethodName()));
		}
	}
	
	public static void logAnson(Anson ans) {
		try {
			if (printCaller) {
				StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
				System.out.println(String.format("logger:        %s.%s(%s:%s)", 
								stElements[2].getClassName(), stElements[2].getMethodName(),
								stElements[2].getFileName(), stElements[2].getLineNumber()));
			}

			if (ans != null)
				System.out.println(ans.toString());
		} catch (Exception ex) {
			System.err.println("logAnson(): Can't print. Error:");
			ex.printStackTrace();
		}
	}

	public static void warn(String format, Object... args) {
		try {
			if (printCaller) {
				StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
				System.out.println(String.format("\nlog by        %s.%s(%s:%s)", 
								stElements[2].getClassName(), stElements[2].getMethodName(),
								stElements[2].getFileName(), stElements[2].getLineNumber()));
				if (stElements.length > 3)
				System.out.println(String.format("              %s.%s(%s:%s)", 
								stElements[3].getClassName(), stElements[3].getMethodName(),
								stElements[3].getFileName(), stElements[3].getLineNumber()));
			}

			if (format != null)
				if (args != null && args.length > 0)
					System.err.println(String.format(format, args));
				else
					System.err.println(format);

		} catch (Exception ex) {
			StackTraceElement[] x = ex.getStackTrace();
			System.err.println(String.format("logi(): Can't print. Error: %s. called by %s.%s()",
					ex.getMessage(), x[0].getClassName(), x[0].getMethodName()));
		}
	}

	public static void warn(ArrayList<Object> list, Object... args) {
		try {
			if (printCaller) {
				StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
				System.out.println(String.format("logger:        %s.%s(%s:%s)", 
								stElements[2].getClassName(), stElements[2].getMethodName(),
								stElements[2].getFileName(), stElements[2].getLineNumber()));
			}

			if (list != null)
				for (Object it : list)
					if (args != null && args.length > 0)
						System.err.println(String.format(it.toString(), args));
					else
						System.err.println(it);

		} catch (Exception ex) {
			System.err.println("logi(): Can't print. Error:");
			ex.printStackTrace();
		}
	}

	/**
	 * Load text in the file located within the package path.
	 * @since 0.9.26
	 * @param clzz
	 * @param filename
	 * @return text
	 */
	public static String loadTxt(Class<?> clzz, String filename) {
		try {
			return Files.readAllLines(
				Paths.get(clzz.getResource(filename).toURI()), Charset.defaultCharset())
				.stream().collect(Collectors.joining("\n"));
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static FileInputStream input(Class<?> clzz, String filename) throws URISyntaxException, FileNotFoundException {
		Path pth = Paths.get(clzz.getResource(filename).toURI());
		return new FileInputStream(pth.toAbsolutePath().toString());
	}

	/**
	 * Load text in the file located within the calling class' package path.
	 * @since 0.9.26
	 * @param filename
	 * @return text
	 */
	public static String loadTxt(String filename) {
		try {
			StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
			return loadTxt(Class.forName(stElements[2].getClassName()), filename);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Print system out with 
	 * <pre>
	 * if section &ge 0:
	 * [section.] fromat
	 * =================
	 * 
	 * else
	 * fromat
	 * ======</pre>
	 * 
	 * Print "====" if section = 0, or "----" if 1, or "____" if 2, or "++++" if &gt; 3
	 * 
	 * <p><a href='https://stackoverflow.com/a/62548488/7362888'>
	 * This method requires jdk 11</a></p>
	 * 
	 * @since 0.9.67
	 */
	public static void logrst(String text, int ... subsects) {
		logrst(new String[] {text}, subsects);
	}

	/**
	 * @see #logrst(String, int...)
	 * @param texts
	 * @param subsects
	 */
	public static void logrst(String[] texts, int ... subsects) {
		int len = 0;
		int level = isNull(subsects) ? 0 : subsects.length - 1;

		for (int t : subsects)
			len += String.valueOf(t).length() + 1;

		for (String t : texts) {
			if (isblank(t)) continue;
			len += t.length() + 1;
			long count = t.chars().filter(ch -> ch == '\t').count();
			len += count * (tabwidth - 1);
		}

		logi("\n%s %s\n%s\n",
			IntStream.of((int[])subsects).mapToObj(n -> String.format("%d", n)).collect(Collectors.joining(".")),
			Stream.of(texts).collect(Collectors.joining(" ")),
			( level == 0 ? "=" :
			  level == 1 ? "-" :
			  level == 2 ? "_" :
			  "+").repeat(len - 1));

	}
}
