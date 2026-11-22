package io.oz.anson.reflect.ttypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.odysz.anson.Ans2dArr;
import io.odysz.anson.AnsT1;
import io.odysz.anson.AnsT2;
import io.odysz.anson.AnsT3;
import io.odysz.anson.AnsT3son;
import io.odysz.anson.AnsT4Enum;
import io.odysz.anson.AnsTList;
import io.odysz.anson.AnsTMap;
import io.odysz.anson.AnsTSet;
import io.odysz.anson.AnsTStrsList;
import io.odysz.anson.Anson;
import io.odysz.anson.AnsonException;
import io.odysz.anson.AnsonField;

/**
 * Test type for TypeScanner, depending on antson's test types.
 */
public class TArrays extends Anson {

	/** 3d array of a test type */
	public AnsT2[][][] cube;

	/** primitive 3d array, no uses */
	public int[][][] ints;

	/** nested enum in nested generics */
	public HashMap<String, ArrayList<AnsT4Enum.MsgCode>> codes;

	public List<Ans2dArr> arrs;

	public Map<String, AnsTList[]> lists;

	protected AnsT4Enum.T4_Port port;

	/** private, not exported, not in uses */
	@SuppressWarnings("unused")
	private AnsTStrsList hidden;

	/** RUNTIME annotation, of which only the values specified are in the class file */
	@AnsonField(ignoreTo = true, ref = AnsonField.enclosing)
	public AnsT1 parent;

	/** package private, exported; CLASS retention annotation with enum, class and array values */
	@TAnno(value = "pkg", kinds = {Kind.a, Kind.b}, type = AnsT3.class, nums = {1, 2})
	HashMap<String, List<AnsTSet>> pkgMap;

	public static final String CONST = "c";

	protected transient int counter;

	public TArrays() {
		// an anonymous class, TArrays$1, not exported
		Runnable r = new Runnable() { public void run() { } };
		r.run();
	}

	public TArrays(AnsTMap m, String... args) throws AnsonException { }

	/** type parameter's bound */
	public <T extends AnsT3> T find(Class<T> c) { return null; }

	public static class Nested {
		public AnsTSet set;
	}

	public interface IVisitor {
		void visit(AnsT3son son);
	}

	public enum Kind { a, b }

	public record Rec(AnsT1 t1, List<AnsT2> t2s) { }

	/** protected nested, exported */
	protected static class Prot {
		public AnsT1 t1;
	}

	/** package private nested, exported */
	static class Pkg {
		AnsT3son son;
	}

	/** private nested, not exported, nor the public nested in it */
	private static class Hidden {
		public static class InHidden { public AnsT1 t1; }
	}
}

/** package private, not exported */
class PkgPrivate {
	public AnsT1 t1;
}
