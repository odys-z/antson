package io.oz.anson.reflect;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.odysz.reflect.AnAnnotation;
import io.odysz.reflect.AnsonAst;
import io.odysz.reflect.AnsonTypes;

import io.oz.anson.reflect.ttypes.TArrays;

/**
 * Scan io.oz.anson.reflect.ttypes, test types depending on antson's test types.
 */
public class TypeScannerTest {
	static final String tt = "io.oz.anson.reflect.ttypes.";
	static final String at = "io.odysz.anson.";

	static Path ttypes() throws Exception {
		return Path.of(TArrays.class.getProtectionDomain().getCodeSource().getLocation().toURI())
				   .resolve("io/oz/anson/reflect/ttypes");
	}

	static AnsonTypes scan() throws Exception {
		return new TypeScanner().scan(ttypes()).types("io.github.odys-z:anson-reflect-test:0");
	}

	@Test
	public void testExported() throws Exception {
		AnsonTypes types = scan();

		assertEquals(List.of(
				tt + "TAnno",
				tt + "TArrays",
				tt + "TArrays.IVisitor",
				tt + "TArrays.Kind",
				tt + "TArrays.Nested",
				tt + "TArrays.Pkg",
				tt + "TArrays.Prot",
				tt + "TArrays.Rec"),
			List.copyOf(types.asts.keySet()),
			"Public types and non-private nested types, sorted. " +
			"Not exported: TArrays$1, Hidden, Hidden.InHidden, PkgPrivate.");

		assertEquals("class", types.asts.get(tt + "TArrays").kind);
		assertEquals("annotation", types.asts.get(tt + "TAnno").kind);
		assertEquals("interface", types.asts.get(tt + "TArrays.IVisitor").kind);
		assertEquals("enum", types.asts.get(tt + "TArrays.Kind").kind);
		assertEquals("record", types.asts.get(tt + "TArrays.Rec").kind);
		assertEquals("class", types.asts.get(tt + "TArrays.Nested").kind);

		assertEquals(List.of("public"), types.asts.get(tt + "TArrays").modifiers);
		assertEquals(List.of("protected", "static"), types.asts.get(tt + "TArrays.Prot").modifiers);
		assertEquals(List.of("static"), types.asts.get(tt + "TArrays.Pkg").modifiers);
		assertEquals(List.of("public", "static"), types.asts.get(tt + "TArrays.IVisitor").modifiers);
		assertEquals(List.of("public", "static", "final"), types.asts.get(tt + "TArrays.Rec").modifiers);
	}

	@Test
	public void testUses() throws Exception {
		AnsonTypes types = scan();

		List<String> uses = types.asts.get(tt + "TArrays").uses;

		assertTrue(uses.contains(at + "Anson"), "super class");
		assertTrue(uses.contains(at + "AnsT2"), "AnsT2[][][] cube");
		assertTrue(uses.contains("java.util.HashMap"), "HashMap<String, ArrayList<AnsT4Enum.MsgCode>>");
		assertTrue(uses.contains("java.util.ArrayList"), "HashMap<String, ArrayList<AnsT4Enum.MsgCode>>");
		assertTrue(uses.contains(at + "AnsT4Enum.MsgCode"), "nested enum in generics, named with '.'");
		assertTrue(uses.contains("java.util.List"), "List<Ans2dArr>");
		assertTrue(uses.contains(at + "Ans2dArr"), "List<Ans2dArr>");
		assertTrue(uses.contains("java.util.Map"), "Map<String, AnsTList[]>");
		assertTrue(uses.contains(at + "AnsTList"), "Map<String, AnsTList[]>");
		assertTrue(uses.contains(at + "AnsT4Enum.T4_Port"), "protected field");
		assertTrue(uses.contains(at + "AnsTSet"), "package private field, HashMap<String, List<AnsTSet>>");
		assertTrue(uses.contains(at + "AnsTMap"), "ctor parameter");
		assertTrue(uses.contains(at + "AnsonException"), "thrown exception");
		assertTrue(uses.contains(at + "AnsT3"), "<T extends AnsT3>");
		assertTrue(uses.contains("java.lang.Class"), "Class<T>");

		assertFalse(uses.contains(at + "AnsTStrsList"), "private field");
		assertFalse(uses.contains(tt + "TArrays"), "itself");
		assertFalse(uses.stream().anyMatch(u -> u.contains("$")), "no binary names");
		assertFalse(uses.stream().anyMatch(u -> u.contains("/")), "no internal names");

		assertEquals(List.of(at + "AnsTSet", "java.lang.Object"), types.asts.get(tt + "TArrays.Nested").uses);
		assertTrue(types.asts.get(tt + "TArrays.Pkg").uses.contains(at + "AnsT3son"), "package private field");
		assertTrue(types.asts.get(tt + "TArrays.IVisitor").uses.contains(at + "AnsT3son"));
		assertTrue(types.asts.get(tt + "TArrays.Rec").uses.containsAll(
				List.of(at + "AnsT1", at + "AnsT2", "java.util.List", "java.lang.Record")));

		assertEquals(uses.stream().sorted().toList(), uses, "uses are sorted");
	}

	@Test
	public void testFields() throws Exception {
		AnsonAst t = scan().asts.get(tt + "TArrays");

		assertEquals(List.of("cube", "ints", "codes", "arrs", "lists", "port", "parent", "pkgMap", "CONST", "counter"),
				List.copyOf(t.fields.keySet()), "non-private fields, in declaring order");

		// AST syntax, not closed
		assertEquals("list<list<list<" + at + "AnsT2", t.fields.get("cube").dataAnclass);
		assertEquals("list<list<" + at + "AnsT2", t.fields.get("cube").valType, "the direct element type");
		assertEquals("list<list<list<int", t.fields.get("ints").dataAnclass);
		assertEquals("map<string, list<" + at + "AnsT4Enum.MsgCode", t.fields.get("codes").dataAnclass);
		assertEquals("list<" + at + "AnsT4Enum.MsgCode", t.fields.get("codes").valType);
		assertEquals("list<" + at + "Ans2dArr", t.fields.get("arrs").dataAnclass);
		assertEquals(at + "Ans2dArr", t.fields.get("arrs").valType);
		assertEquals("map<string, list<" + at + "AnsTList", t.fields.get("lists").dataAnclass);
		assertEquals(at + "AnsT4Enum.T4_Port", t.fields.get("port").dataAnclass);
		assertNull(t.fields.get("port").valType);
		assertEquals("string", t.fields.get("CONST").dataAnclass);
		assertEquals("int", t.fields.get("counter").dataAnclass);

		assertEquals("cube", t.fields.get("cube").fieldname);

		assertEquals(List.of("public"), t.fields.get("cube").modifiers);
		assertEquals(List.of("protected"), t.fields.get("port").modifiers);
		assertEquals(List.of(), t.fields.get("pkgMap").modifiers, "package private");
		assertEquals(List.of("public", "static", "final"), t.fields.get("CONST").modifiers);
		assertEquals(List.of("protected", "transient"), t.fields.get("counter").modifiers);

		assertEquals(List.of(), t.fields.get("cube").annotations);

		// @AnsonField(ignoreTo = true, ref = AnsonField.enclosing)
		AnAnnotation a = t.fields.get("parent").annotations.get(0);
		assertEquals(at + "AnsonField", a.annotation);
		assertTrue(a.runtime, "RUNTIME");
		assertEquals(List.of("ignoreTo", "ref"), a.elements.stream().map(v -> v.name).toList(), "only values specified");
		assertEquals("true", a.element("ignoreTo").value);
		assertEquals("boolean", a.element("ignoreTo").valType);
		assertEquals("1", a.element("ref").value);
		assertEquals("int", a.element("ref").valType);
		assertNull(a.element("ignoreFrom"), "default, not specified");

		// @TAnno(value = "pkg", kinds = {Kind.a, Kind.b}, type = AnsT3.class, nums = {1, 2})
		AnAnnotation p = t.fields.get("pkgMap").annotations.get(0);
		assertEquals(tt + "TAnno", p.annotation);
		assertFalse(p.runtime, "CLASS retention");
		assertEquals("pkg", p.element("value").value);
		assertEquals("java.lang.String", p.element("value").valType);

		assertEquals(List.of(tt + "TArrays.Kind.a", tt + "TArrays.Kind.b"), p.element("kinds").array, "enum constants");
		assertEquals(tt + "TArrays.Kind[]", p.element("kinds").valType);
		assertNull(p.element("kinds").value);

		assertEquals(at + "AnsT3", p.element("type").value, "class literal, of an element named 'type'");
		assertEquals("java.lang.Class", p.element("type").valType);

		assertEquals(List.of("1", "2"), p.element("nums").array, "primitive array");
		assertEquals("int[]", p.element("nums").valType);

		assertEquals(at + "AnsT3son", scan().asts.get(tt + "TArrays.Pkg").fields.get("son").dataAnclass);
	}

	@Test
	public void testAst() throws Exception {
		AnsonTypes types = scan();
		AnsonAst t = types.asts.get(tt + "TArrays");

		assertEquals(tt + "TArrays", t.dataAnclass);
		assertEquals(at + "Anson", t.baseAnclass);
		assertTrue(t.isJsonable, "extends Anson");
		assertFalse(t.isEnum);
		assertNull(t.enums);

		AnsonAst k = types.asts.get(tt + "TArrays.Kind");
		assertTrue(k.isEnum);
		assertFalse(k.isJsonable);
		assertEquals("java.lang.Enum", k.baseAnclass);
		assertEquals(List.of("a", "b"), List.copyOf(k.enums.keySet()), "constants, in declaring order");
		assertEquals(1, k.enums.get("b"));
		assertEquals(List.of(), List.copyOf(k.fields.keySet()), "constants are not fields");

		AnsonAst rec = types.asts.get(tt + "TArrays.Rec");
		assertEquals("java.lang.Record", rec.baseAnclass);
		assertEquals(List.of(), List.copyOf(rec.fields.keySet()), "record components are private fields");

		assertEquals(List.of(), types.asts.get(tt + "TArrays.IVisitor").interfaces);
		assertEquals("java.lang.Object", types.asts.get(tt + "TArrays.Nested").baseAnclass);
		assertFalse(types.asts.get(tt + "TArrays.Nested").isJsonable);
	}

	@Test
	public void testExternals() throws Exception {
		TypeScanner scanner = new TypeScanner().scan(ttypes());
		var ext = scanner.externals();

		assertEquals(at + "AnsT4Enum.MsgCode", ext.get("io/odysz/anson/AnsT4Enum$MsgCode"));
		assertEquals(at + "AnsT2", ext.get("io/odysz/anson/AnsT2"));
		assertEquals(at + "AnsonField", ext.get("io/odysz/anson/AnsonField"), "fields' annotation");
		assertNull(ext.get("io/oz/anson/reflect/ttypes/TArrays$Kind"), "own types are not external");
		assertNull(ext.get("io/odysz/anson/AnsTStrsList"), "only used by a private field");
	}
}
