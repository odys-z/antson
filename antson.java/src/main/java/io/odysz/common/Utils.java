package io.odysz.common;

import static io.odysz.common.LangExt.isNull;
import static io.odysz.common.LangExt.isblank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.odysz.anson.Anson;

public class Utils {
	/**
	 * Used for print out all caller.
	 * 
	 * Calling logi() etc. is recommended add a static final boolean flag before calling:<br>
	 * if (staticBoolean) Util.logi("messages");<br>
	 * In this way, the java compiler will optimize the code to nothing if the <i>staticBoolean</i> is false.
	 * This flag is used for find out {@link #logi(String, String...)} calling that's not controlled by the flag.
	 */
	static boolean printCaller = false;

	public static int tabwidth = 4;
	
	public static boolean printag = false;

	private static PrintStream os;
	private static PrintStream os() { return os == null ? System.out : os; }
	private static PrintStream es;
	private static PrintStream es() { return es == null ? System.err : es; }

	public static void touchDir(String dir) {
		File f = new File(dir);
		if (f.isDirectory())
			return;
		else if (!f.exists())
			// create dir
			f.mkdirs();
		else
			// must be a file
			Utils.warn("FATAL ExtFile can't create a folder, a same named file exists: ", dir);
	}
	
	/**
	 * @since 0.9.86
	 * @param logStream e.g. System.out stream
	 */
	public static void logOut(PrintStream logStream) {
		os = logStream;
	}

	/**
	 * @since 0.9.86
	 * @param logStream e.g. System.err stream
	 */
	public static void logErr(PrintStream logStream) {
		es = logStream;
	}

	/**See {@link #printCaller}
	 * @param printing
	 */
	public static void printCaller(boolean printing) { printCaller = printing; }

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
				os().println(String.format("\nlog by        %s.%s(%s:%s)", 
								stElements[2].getClassName(), stElements[2].getMethodName(),
								stElements[2].getFileName(), stElements[2].getLineNumber()));
				if (stElements.length > 3)
				os().println(String.format("              %s.%s(%s:%s)", 
								stElements[3].getClassName(), stElements[3].getMethodName(),
								stElements[3].getFileName(), stElements[3].getLineNumber()));
			}

			if (printag)
				es().print(String.format("[%s.%s] ",
					new Throwable().getStackTrace()[1].getClassName(),
					new Throwable().getStackTrace()[1].getMethodName()));

			if (format != null)
				if (args != null && args.length > 0)
					os().println(String.format(format, args));
				else
					os().println(format);

		} catch (Exception ex) {
			StackTraceElement[] x = ex.getStackTrace();
			es().println(String.format("logi(): Can't print. Error: %s. called by %s.%s()",
					ex.getMessage(), x[0].getClassName(), x[0].getMethodName()));
		}
	}

	public static void logi(Object[] row) {
		try {
			if (printCaller) {
				StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
				os().println(String.format("logger:        %s.%s(%s:%s)", 
								stElements[2].getClassName(), stElements[2].getMethodName(),
								stElements[2].getFileName(), stElements[2].getLineNumber()));
			}

			if (row != null) {
				if (printag)
					es().print(String.format("[%s.%s] ",
						new Throwable().getStackTrace()[1].getClassName(),
						new Throwable().getStackTrace()[1].getMethodName()));

				os().println(LangExt.toString(row));
			}
		} catch (Exception ex) {
			es().println("logi(): Can't print. Error:");
			ex.printStackTrace();
		}

	}

	public static <T> void logi(List<T> list, Object... args) {
		try {
			if (printCaller) {
				StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
				os().println(String.format("logger:        %s.%s(%s:%s)", 
								stElements[2].getClassName(), stElements[2].getMethodName(),
								stElements[2].getFileName(), stElements[2].getLineNumber()));
			}

			if (list != null) {
				if (printag)
					es().print(String.format("[%s.%s] ",
						new Throwable().getStackTrace()[1].getClassName(),
						new Throwable().getStackTrace()[1].getMethodName()));

				for (T it : list)
					if (it == null)
						os().println("null");
					else if (args != null && args.length > 0)
						os().println(String.format(it.toString(), args));
					else
						os().println(it.toString());
			}

		} catch (Exception ex) {
			es().println("logi(): Can't print. Error:");
			ex.printStackTrace();
		}
	}

	public static void logArr(List<String[]> list, Object... args) {
		try {
			if (printCaller) {
				StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
				os().println(String.format("logger:        %s.%s(%s:%s)", 
								stElements[2].getClassName(), stElements[2].getMethodName(),
								stElements[2].getFileName(), stElements[2].getLineNumber()));
			}

			if (list != null) {
				if (printag)
					es().print(String.format("[%s.%s] ",
						new Throwable().getStackTrace()[1].getClassName(),
						new Throwable().getStackTrace()[1].getMethodName()));

				for (String[] it : list)
					if (args != null && args.length > 0)
						os().println(String.format(LangExt.toString(it), args));
					else
						os().println(LangExt.toString(it));
			}

		} catch (Exception ex) {
			es().println("logi(): Can't print. Error:");
			ex.printStackTrace();
		}
	}
	public static void logi(Map<?, ?> map, String... indent) {
		logMap(map, indent);
	}

	public static void logMap(Map<?, ?> map, String... indent) {
		try {
			if (map != null) {
				if (printCaller) {
					StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
					os().println(String.format("logger:        %s.%s(%s:%s)", 
									stElements[2].getClassName(), stElements[2].getMethodName(),
									stElements[2].getFileName(), stElements[2].getLineNumber()));
				}

				if (map != null) {
					if (!isNull(indent))
						for (String ind : indent)
							os().print(ind);

					if (printag)
						es().print(String.format("[%s.%s] ",
							new Throwable().getStackTrace()[1].getClassName(),
							new Throwable().getStackTrace()[1].getMethodName()));

					final boolean[] comma = new boolean[] {false};
					os().print("{");
					map.forEach((k, v) -> {
						if (!comma[0]) comma[0] = true;
						else os().print(", ");

						os().print(k);
						os().print(": ");
						if (v instanceof Map)
							logMap((Map<?, ?>)v);
						else if (v instanceof List)
							logi((List<?>)v);
						else if (v != null && v.getClass().isArray())
							logi((Object[])v);
						else
							os().print(v);
					});
					os().println("}");
				}
			}
			else os().println("Map is null.");
		} catch (Exception ex) {
			StackTraceElement[] x = ex.getStackTrace();
			es().println(String.format("logMap(): Can't print. Error: %s. called by %s.%s()",
					ex.getMessage(), x[0].getClassName(), x[0].getMethodName()));
		}
	}

	public static void logkeys(Map<String, ?> map) {
		try {
			if (map != null) {
				if (printag)
					es().print(String.format("[%s.%s] ",
						new Throwable().getStackTrace()[1].getClassName(),
						new Throwable().getStackTrace()[1].getMethodName()));

				for (String mk : map.keySet())
					os().print(mk + ", ");
			}

			os().println();
		} catch (Exception ex) {
			StackTraceElement[] x = ex.getStackTrace();
			es().println(String.format("logkeys(): Can't print. Error: %s. called by %s.%s()",
					ex.getMessage(), x[0].getClassName(), x[0].getMethodName()));
		}
	}
	
	public static void logAnson(Anson ans) {
		try {
			if (printCaller) {
				StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
				os().println(String.format("logger:        %s.%s(%s:%s)", 
								stElements[2].getClassName(), stElements[2].getMethodName(),
								stElements[2].getFileName(), stElements[2].getLineNumber()));
			}

			if (printag)
				es().print(String.format("[%s.%s] ",
					new Throwable().getStackTrace()[1].getClassName(),
					new Throwable().getStackTrace()[1].getMethodName()));

			if (ans != null)
				os().println(ans.toString());
		} catch (Exception ex) {
			es().println("logAnson(): Can't print. Error:");
			ex.printStackTrace();
		}
	}

	public static void warn(String format, Object... args) {
		try {
			if (printCaller) {
				StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
				es().println(String.format("\nlog by        %s.%s(%s:%s)", 
								stElements[2].getClassName(), stElements[2].getMethodName(),
								stElements[2].getFileName(), stElements[2].getLineNumber()));
				if (stElements.length > 3)
				es().println(String.format("              %s.%s(%s:%s)", 
								stElements[3].getClassName(), stElements[3].getMethodName(),
								stElements[3].getFileName(), stElements[3].getLineNumber()));
			}
			
			if (printag)
				es().print(String.format("[%s.%s] ",
					new Throwable().getStackTrace()[1].getClassName(),
					new Throwable().getStackTrace()[1].getMethodName()));

			if (format != null)
				if (args != null && args.length > 0)
					es().println(String.format(format, args));
				else
					es().println(format);

		} catch (Exception ex) {
			StackTraceElement[] x = ex.getStackTrace();
			es().println(String.format("logi(): Can't print. Error: %s. called by %s.%s()",
					ex.getMessage(), x[0].getClassName(), x[0].getMethodName()));
		}
	}

	public static void warn(ArrayList<Object> list, Object... args) {
		try {
			if (printCaller) {
				StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
				os().println(String.format("logger:        %s.%s(%s:%s)", 
								stElements[2].getClassName(), stElements[2].getMethodName(),
								stElements[2].getFileName(), stElements[2].getLineNumber()));
			}

			if (printag)
				es().print(String.format("[%s.%s] ",
					new Throwable().getStackTrace()[1].getClassName(),
					new Throwable().getStackTrace()[1].getMethodName()));


			if (list != null)
				for (Object it : list)
					if (args != null && args.length > 0)
						es().println(String.format(it.toString(), args));
					else
						es().println(it);

		} catch (Exception ex) {
			es().println("logi(): Can't print. Error:");
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
			URI uri = clzz.getResource(filename).toURI();
			try {
				// https://stackoverflow.com/a/25033217
				Map<String, String> env = new HashMap<>(); 
				env.put("create", "true");
				@SuppressWarnings("unused")
				FileSystem zipfs = FileSystems.newFileSystem(uri, env);
			} catch (Exception e){}

			return Files.readAllLines(
				Paths.get(uri), Charset.defaultCharset())
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
			repeat(level == 0 ? "=" :
				   level == 1 ? "-" :
				   level == 2 ? "_" :
				   "+", len - 1));
	}

	/**
	 * Returns a string whose value is the concatenation of {@code s}
	 * repeated {@code count} times.
	 * <p>
	 * If this string is empty or count is zero then the empty
	 * string is returned.
	 * </p>
	 * <p>Please note that this method uses system's default coder.</p>
	 *
	 * @param   count number of times to repeat
	 *
	 * @return  A string composed of this string repeated
	 *          {@code count} times or the empty string if this
	 *          string is empty or count is zero
	 *
	 * @throws  IllegalArgumentException if the {@code count} is
	 *          negative.
	 *
	 * see JDK 11 String.repeat
	 * 
	 * @since 0.9.68
	 */
	public static String repeat(String s, int count) {
		if (count < 0) {
			throw new IllegalArgumentException("count is negative: " + count);
		}
		if (count == 1) {
			return s;
		}
		final int len = s.length();
		if (len == 0 || count == 0) {
			return "";
		}
		if (Integer.MAX_VALUE / count < len) {
			throw new OutOfMemoryError("Required length exceeds implementation limit");
		}
		if (len == 1) {
			final byte[] single = new byte[count];
			Arrays.fill(single, (byte)s.charAt(0));
			return new String(single);
		}
		final int limit = len * count;
		final byte[] multiple = new byte[limit];
		System.arraycopy(s.getBytes(), 0, multiple, 0, len);
		int copied = len;
		for (; copied < limit - copied; copied <<= 1) {
			System.arraycopy(multiple, 0, multiple, copied, copied);
		}
		System.arraycopy(multiple, 0, multiple, copied, limit - copied);
		return new String(multiple);
	}

	/**
	 * @param tag always create as {@code new Object(){}}
	 */
	public static void warnT(Object tag, String format, Object ... args) {
		tag(es(), tag, format, args);
	}
	
	private static void tag(PrintStream p, Object tag, String format, Object[] args) {
		try {
			if (printCaller) {
				StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
				p.println(String.format("\nlog by        %s.%s(%s:%s)", 
						stElements[2].getClassName(), stElements[2].getMethodName(),
						stElements[2].getFileName(), stElements[2].getLineNumber()));
				if (stElements.length > 3)
				p.println(String.format("              %s.%s(%s:%s)", 
						stElements[3].getClassName(), stElements[3].getMethodName(),
						stElements[3].getFileName(), stElements[3].getLineNumber()));
			}
			
			Method m = tag.getClass().getEnclosingMethod();
			p.print(String.format("[%s#%s()] ",
					tag.getClass().getEnclosingClass() == null
						? "??"
						: tag.getClass().getEnclosingClass().getName(),
					m == null ? "static?" : m.getName()));
		
			if (format != null)
				if (args != null && args.length > 0)
					p.println(String.format(format, args));
				else
					p.println(format);

		} catch (Exception ex) {
			if (ex instanceof NullPointerException
				&& tag != null && tag.getClass().getEnclosingClass() == null)
				es().println("The 'tag' object doesn't have any enclosing instance. Is it initializaed like: 'new Object() {}'?");
			StackTraceElement[] x = ex.getStackTrace();
			p.println(String.format("warn(): Can't print. Error: %s. called by %s.%s()",
					ex.getMessage(), x[0].getClassName(), x[0].getMethodName()));
		}
	}
	
	public static void logT(Object tag, String format, Object ... args) {
		tag(os(), tag, format, args);
	}
	
	/**
	 * Pause console, waiting for any key input.
	 * @param msg
	 */
	public static void pause(String msg) {
		Utils.logi(msg);
		try {
			BufferedReader reader = new BufferedReader(
	            new InputStreamReader(System.in));
			reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Wait until all lights turn into green (true), a helper for tests.
	 * @example <pre>
	 * boolean[] lights = new boolean[] {true, false, false};
	 * // running background threads, setting light if succeed.
	 * joinby(lights, X, Y);
	 * joinby(lights, X, Z);
	 * awaitAll(lights);
	 * </pre>
	 * @param green lights
	 * @param x100ms default 100 times, -1 for infinitive waiting
	 * @throws InterruptedException time limit reached while some lights are red.
	 */
	public static void awaitAll(boolean[] greenlights, int... x100ms) throws InterruptedException {
		int wait = 0;
		int times = isNull(x100ms) ? 100 : x100ms[0];
		while (times < 0 || wait++ < times) {
			boolean green = true;
			for (boolean g : greenlights) {
				if (!g) Thread.sleep(100);
				green &= g;
			}
			if (green)
				return;
		}
		
		for (boolean g : greenlights)
			if (!g) throw new InterruptedException("Green light");
	}
	
	/**
	 * Wait on n-th light only.
	 * @see #awaitAll(boolean[], int...)
	 * @param signals
	 * @param n_th
	 */
	public static void waiting(boolean[] signals, int n_th) {
		for (int i = 0; i < signals.length; i++)
			if (i != n_th)
				signals[i] = true;
			else signals[i] = false;
	}
	
	/**
	 * Turn lights to red,
	 * @see #awaitAll(boolean[], int...)
	 * @param signals
	 */
	public static void turnred(boolean[] signals) {
		for (int i = 0; i < signals.length; i++)
			signals[i] = false;
	}
	
}
