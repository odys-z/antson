package io.oz.anson.reflect;

import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import io.odysz.reflect.AnAnnotation;
import io.odysz.reflect.AnValue;
import io.odysz.reflect.AnsonAst;
import io.odysz.reflect.AnsonField;
import io.odysz.reflect.AnsonTypes;

/**
 * Find a module's types, their fields, and the types used in their API, from compiled classes,
 * as {@link AnsonAst}s, the same structure of the ASTs used by semantier_gen and anson.cmake.
 *
 * <p>Exported: public top-level types, and nested types which are not private, nor in a private one.
 * Anonymous, local and synthetic classes are not exported. Fields are exported unless private.
 * Enum constants are exported as {@link AnsonAst#enums}, not fields.</p>
 *
 * <p>Classes are read with ASM, without loading, so no static initializers run and
 * the module's dependencies are not needed.</p>
 *
 * <p>ASTs, fields and annotations are created in the visiting events. Names which need the nesting
 * of other classes, e.g. io/oz/x/Doc$Kind &rarr; io.oz.x.Doc.Kind, are assigned after all classes
 * are scanned, see {@link Cls#resolves}.</p>
 *
 * <p>Field types are in AST syntax, see {@link #astType(TypeSig)}.</p>
 *
 * Usage:
 * <pre>
 * AnsonTypes types = new TypeScanner()
 *     .scan(Path.of("target/classes"))
 *     .types("io.github.odys-z:x:0.1");
 * </pre>
 *
 * E.g. for
 * <pre>
 * package io.oz.x;
 *
 * import java.util.HashMap;
 *
 * import io.odysz.anson.Anson;
 * import io.odysz.anson.AnsonField;
 *
 * public class Doc extends Anson {
 *     {@literal @}AnsonField(ignoreTo = true)
 *     public Doc parent;
 *
 *     protected HashMap&lt;String, Kind[]&gt; kinds;
 *
 *     private String secret;
 *
 *     public enum Kind { a, b }
 * }
 * </pre>
 *
 * the anson-types.json, written by {@link AnsonTypesMojo} after resolving the dependencies, is
 * <pre>
 * { "type": "io.odysz.reflect.AnsonTypes",
 *   "module": "io.github.odys-z:x:0.1",
 *   "asts": {"io.oz.x.Doc": { "type": "io.odysz.reflect.AnsonAst",
 *           "isVar": false,
 *           "static_val": null,
 *           "interfaces": [],
 *           "kind": "class",
 *           "isEnum": false,
 *           "isJsonable": true,
 *           "dataAnclass": "io.oz.x.Doc",
 *           "modifiers": ["public"],
 *           "isList": false,
 *           "isMap": false,
 *           "enums": null,
 *           "isPortEnum": false,
 *           "ctorsemantics": [],
 *           "isInt": false,
 *           "istring": false,
 *           "uses": ["io.odysz.anson.Anson", "io.oz.x.Doc.Kind", "java.lang.String", "java.util.HashMap"],
 *           "fields": {"parent": { "type": "io.odysz.reflect.AnsonField",
 *                   "fieldname": "parent",
 *                   "valType": null,
 *                   "annotations": [{ "type": "io.odysz.reflect.AnAnnotation",
 *                       "annotation": "io.odysz.anson.AnsonField",
 *                       "elements": [{ "type": "io.odysz.reflect.AnValue",
 *                           "valType": "boolean",
 *                           "array": null,
 *                           "name": "ignoreTo",
 *                           "annotations": null,
 *                           "value": "true"}],
 *                       "runtime": true}],
 *                   "dataAnclass": "io.oz.x.Doc",
 *                   "modifiers": ["public"]},
 *               "kinds": { "type": "io.odysz.reflect.AnsonField",
 *                   "fieldname": "kinds",
 *                   "valType": "list&lt;io.oz.x.Doc.Kind",
 *                   "annotations": [],
 *                   "dataAnclass": "map&lt;string, list&lt;io.oz.x.Doc.Kind",
 *                   "modifiers": ["protected"]}},
 *           "baseAnclass": "io.odysz.anson.Anson",
 *           "isDouble": false},
 *       "io.oz.x.Doc.Kind": { "type": "io.odysz.reflect.AnsonAst",
 *           "isVar": false,
 *           "static_val": null,
 *           "interfaces": [],
 *           "kind": "enum",
 *           "isEnum": true,
 *           "isJsonable": false,
 *           "dataAnclass": "io.oz.x.Doc.Kind",
 *           "modifiers": ["public", "static", "final"],
 *           "isList": false,
 *           "isMap": false,
 *           "enums": {"a": 0,
 *               "b": 1},
 *           "isPortEnum": false,
 *           "ctorsemantics": [],
 *           "isInt": false,
 *           "istring": false,
 *           "uses": ["java.lang.Enum", "java.lang.String"],
 *           "fields": {},
 *           "baseAnclass": "java.lang.Enum",
 *           "isDouble": false}},
 *   "dependencies": {"io.odysz.anson.Anson": "io.github.odys-z:antson:1.2.0",
 *       "io.odysz.anson.AnsonField": "io.github.odys-z:antson:1.2.0"}
 * }
 * </pre>
 *
 * @since 0.0.1
 */
public class TypeScanner {

	/** A class file read. */
	static class Cls {
		String name;
		int access;
		String superName;
		String[] interfaces;
		AnsonAst ast;
		/** internal names */
		Set<String> uses = new TreeSet<String>();
		/** internal names of the fields' annotation types */
		Set<String> annotationTypes = new TreeSet<String>();
		/** enum constants, in declaring order */
		List<String> enums = new ArrayList<String>();
		/** Assignments of resolved names, run after all classes are scanned. */
		List<Runnable> resolves = new ArrayList<Runnable>();
	}

	/** An InnerClasses entry, the declared nesting and modifiers. */
	record Inner(String outer, String simple, int access) {}

	/**
	 * A parsed type signature (or descriptor), of which the names are internal.
	 * A class type, a base type, a type variable, or an array of {@link #component}.
	 */
	static class TypeSig extends SignatureVisitor {
		/** internal name, a base type's name, e.g. int, or null for a type variable / unbounded wildcard */
		String name;
		boolean base;
		TypeSig component;
		List<TypeSig> args = new ArrayList<TypeSig>();

		TypeSig() { super(ASM9); }

		static TypeSig of(String signature) {
			TypeSig t = new TypeSig();
			new SignatureReader(signature).acceptType(t);
			return t;
		}

		@Override public void visitBaseType(char descriptor) {
			name = Type.getType(String.valueOf(descriptor)).getClassName();
			base = true;
		}

		@Override public void visitTypeVariable(String n) { name = null; }

		@Override public SignatureVisitor visitArrayType() {
			component = new TypeSig();
			return component;
		}

		@Override public void visitClassType(String n) { name = n; }

		/** Outer&lt;T&gt;.Inner -> Outer$Inner, of which the type arguments are Inner's */
		@Override public void visitInnerClassType(String n) {
			name = name + "$" + n;
			args.clear();
		}

		/** unbounded wildcard, ? */
		@Override public void visitTypeArgument() { args.add(new TypeSig()); }

		@Override public SignatureVisitor visitTypeArgument(char wildcard) {
			TypeSig a = new TypeSig();
			args.add(a);
			return a;
		}
	}

	static final Set<String> lists = Set.of("java.util.List", "java.util.ArrayList", "java.util.LinkedList",
			"java.util.Collection", "java.util.Set", "java.util.HashSet", "java.util.LinkedHashSet",
			"java.util.TreeSet", "java.util.Vector", "java.lang.Iterable");

	static final Set<String> maps = Set.of("java.util.Map", "java.util.HashMap", "java.util.LinkedHashMap",
			"java.util.TreeMap", "java.util.Hashtable", "java.util.concurrent.ConcurrentHashMap");

	static final Map<String, String> boxes = Map.of("java.lang.Integer", "int", "java.lang.Long", "long",
			"java.lang.Short", "short", "java.lang.Byte", "byte", "java.lang.Character", "char",
			"java.lang.Float", "float", "java.lang.Double", "double", "java.lang.Boolean", "boolean");

	/** {internal name: class}, the module's own classes */
	final Map<String, Cls> classes = new TreeMap<String, Cls>();

	/**
	 * {internal name: InnerClasses entry}, of both the module's and the referenced types,
	 * e.g. java/util/Map$Entry, for naming nested types.
	 */
	final Map<String, Inner> inners = new HashMap<String, Inner>();

	/** {@link Cls#resolves} are run */
	boolean resolved;

	/**
	 * Scan all class files in the folder, e.g. target/classes.
	 * Can be called for multiple folders.
	 *
	 * @param classesDir
	 * @return this
	 * @throws IOException
	 */
	public TypeScanner scan(Path classesDir) throws IOException {
		if (!Files.isDirectory(classesDir))
			return this;

		try (Stream<Path> files = Files.walk(classesDir)) {
			for (Path p : (Iterable<Path>) files::iterator) {
				String fn = p.getFileName().toString();
				if (!fn.endsWith(".class") || fn.equals("module-info.class") || fn.equals("package-info.class"))
					continue;
				try (InputStream in = Files.newInputStream(p)) {
					scan(in);
				}
			}
		}
		return this;
	}

	/**
	 * Scan one class file.
	 * @param classfile
	 * @return this
	 * @throws IOException
	 */
	public TypeScanner scan(InputStream classfile) throws IOException {
		if (resolved)
			throw new IllegalStateException("Can't scan more classes after types() or externals().");
		new ClassReader(classfile).accept(new Collector(),
				ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
		return this;
	}

	/**
	 * @param module groupId:artifactId:version
	 * @return the exported types' ASTs, sorted by name. {@link AnsonTypes#dependencies}
	 * is not resolved here, see {@link #externals()}.
	 */
	public AnsonTypes types(String module) {
		resolve();

		AnsonTypes types = new AnsonTypes(module);
		types.asts = new LinkedHashMap<String, AnsonAst>();

		TreeMap<String, AnsonAst> sorted = new TreeMap<String, AnsonAst>();
		for (Cls c : classes.values())
			if (exported(c.name))
				sorted.put(c.ast.dataAnclass, c.ast);

		types.asts.putAll(sorted);
		types.dependencies = new LinkedHashMap<String, String>();
		return types;
	}

	/**
	 * Run all classes' {@link Cls#resolves}, and assign the class level properties
	 * which need the whole module.
	 */
	void resolve() {
		if (resolved) return;

		for (Cls c : classes.values()) {
			for (Runnable r : c.resolves) r.run();

			AnsonAst a = c.ast;
			a.dataAnclass = qname(c.name);
			a.baseAnclass = c.superName == null ? null : qname(c.superName);
			a.interfaces = new ArrayList<String>();
			if (c.interfaces != null)
				for (String i : c.interfaces) a.interfaces.add(qname(i));

			a.kind = kind(c.access);
			a.modifiers = modifiers(declaredAccess(c.name), true);
			a.isEnum = (c.access & ACC_ENUM) != 0;
			a.isJsonable = jsonable(c.name);

			TreeSet<String> uses = new TreeSet<String>();
			for (String u : c.uses)
				if (!u.equals(c.name)) uses.add(qname(u));
			a.uses = new ArrayList<String>(uses);

			if (c.enums.size() > 0) {
				a.enums = new LinkedHashMap<String, Integer>();
				for (int i = 0; i < c.enums.size(); i++)
					a.enums.put(c.enums.get(i), i);
			}
		}
		resolved = true;
	}

	/**
	 * @return {internal name: qualified name} of the types used, not of this module,
	 * including fields' annotation types.
	 */
	public Map<String, String> externals() {
		resolve();

		TreeMap<String, String> ext = new TreeMap<String, String>();
		Consumer<String> add = u -> { if (!classes.containsKey(u)) ext.put(u, qname(u)); };

		for (Cls c : classes.values())
			if (exported(c.name)) {
				c.uses.forEach(add);
				c.annotationTypes.forEach(add);
			}
		return ext;
	}

	/**
	 * Exported: public top-level types, and nested types which are not private, nor nested in a private one.
	 * Synthetic, anonymous and local classes are not exported.
	 *
	 * <p>A nested class file's own access flags can't tell private or protected, so the declared
	 * modifiers are taken from the InnerClasses attribute.</p>
	 *
	 * @param name internal name
	 * @return true if exported
	 */
	boolean exported(String name) {
		Cls c = classes.get(name);
		if (c == null || (c.access & ACC_SYNTHETIC) != 0)
			return false;

		Inner in = inners.get(name);
		if (in == null)
			return (c.access & ACC_PUBLIC) != 0;
		if (in.outer() == null || in.simple() == null)
			return false; // anonymous or local
		return (in.access() & ACC_PRIVATE) == 0 && exported(in.outer());
	}

	/**
	 * Extends io.odysz.anson.Anson or implements io.odysz.anson.IJsonable, through the super types
	 * in this module. A super type in another module is not looked into.
	 *
	 * @param name internal name
	 * @return true if it's an Anson type
	 */
	boolean jsonable(String name) {
		if ("io/odysz/anson/Anson".equals(name) || "io/odysz/anson/IJsonable".equals(name))
			return true;
		Cls c = classes.get(name);
		if (c == null)
			return false;
		if (c.interfaces != null)
			for (String i : c.interfaces)
				if (jsonable(i)) return true;
		return c.superName != null && jsonable(c.superName);
	}

	/** The declared modifiers, from InnerClasses for a nested type. */
	int declaredAccess(String name) {
		Inner in = inners.get(name);
		return in != null ? in.access() : classes.get(name).access;
	}

	/**
	 * io/odysz/anson/AnsT4Enum$MsgCode -> io.odysz.anson.AnsT4Enum.MsgCode
	 *
	 * <p>Nesting is taken from the InnerClasses entries, as '$' is legal in Java names.</p>
	 * @param internal
	 * @return qualified name
	 */
	String qname(String internal) {
		Inner in = inners.get(internal);
		if (in != null && in.outer() != null && in.simple() != null)
			return qname(in.outer()) + "." + in.simple();
		return internal.replace('/', '.');
	}

	/**
	 * @param t erased type
	 * @return e.g. int, io.odysz.anson.AnsT2[][][], io.odysz.anson.AnsT4Enum.MsgCode
	 */
	String typename(Type t) {
		if (t.getSort() == Type.ARRAY)
			return typename(t.getElementType()) + "[]".repeat(t.getDimensions());
		if (t.getSort() == Type.OBJECT)
			return qname(t.getInternalName());
		return t.getClassName(); // primitives
	}

	/**
	 * The type in AST syntax: int, boolean, ..., string, VarType (Object, type variables, wildcards),
	 * list&lt;E (lists, sets and arrays), map&lt;K, V, or a qualified name.
	 * Not closed, e.g. map&lt;string, list&lt;io.oz.x.Doc.Kind
	 *
	 * @param t
	 * @return the type
	 */
	String astType(TypeSig t) {
		if (t.component != null)
			return "list<" + astType(t.component);
		if (t.name == null)
			return "VarType";
		if (t.base)
			return t.name;

		String qn = qname(t.name);
		if ("java.lang.String".equals(qn)) return "string";
		if ("java.lang.Object".equals(qn)) return "VarType";
		if (boxes.containsKey(qn)) return boxes.get(qn);
		if (lists.contains(qn))
			return "list<" + (t.args.size() > 0 ? astType(t.args.get(0)) : "VarType");
		if (maps.contains(qn))
			return "map<" + (t.args.size() > 1
					? astType(t.args.get(0)) + ", " + astType(t.args.get(1))
					: "string, VarType");
		return qn;
	}

	/**
	 * @param t
	 * @return element type of a list (array), value type of a map, in AST syntax; otherwise null.
	 */
	String valType(TypeSig t) {
		if (t.component != null)
			return astType(t.component);
		if (t.name == null || t.base)
			return null;
		String qn = qname(t.name);
		if (lists.contains(qn))
			return t.args.size() > 0 ? astType(t.args.get(0)) : "VarType";
		if (maps.contains(qn))
			return t.args.size() > 1 ? astType(t.args.get(1)) : "VarType";
		return null;
	}

	/**
	 * @param v String, or boxed primitives
	 * @return e.g. java.lang.String, boolean, int
	 */
	static String valType(Object v) {
		if (v instanceof String) return "java.lang.String";
		if (v instanceof Boolean) return "boolean";
		if (v instanceof Integer) return "int";
		if (v instanceof Long) return "long";
		if (v instanceof Short) return "short";
		if (v instanceof Byte) return "byte";
		if (v instanceof Character) return "char";
		if (v instanceof Float) return "float";
		if (v instanceof Double) return "double";
		return v == null ? "" : v.getClass().getName();
	}

	static String kind(int access) {
		if ((access & ACC_ANNOTATION) != 0) return "annotation";
		if ((access & ACC_INTERFACE) != 0) return "interface";
		if ((access & ACC_ENUM) != 0) return "enum";
		if ((access & ACC_RECORD) != 0) return "record";
		return "class";
	}

	/**
	 * @param access
	 * @param type for a type, interfaces and annotations are not listed abstract
	 * @return e.g. ["protected", "static", "final"]; package private: []
	 */
	static ArrayList<String> modifiers(int access, boolean type) {
		ArrayList<String> m = new ArrayList<String>();
		if ((access & ACC_PUBLIC) != 0) m.add("public");
		if ((access & ACC_PROTECTED) != 0) m.add("protected");
		if ((access & ACC_PRIVATE) != 0) m.add("private");
		if ((access & ACC_STATIC) != 0) m.add("static");
		if ((access & ACC_ABSTRACT) != 0 && !(type && (access & ACC_INTERFACE) != 0)) m.add("abstract");
		if ((access & ACC_FINAL) != 0) m.add("final");
		if (!type && (access & ACC_TRANSIENT) != 0) m.add("transient");
		if (!type && (access & ACC_VOLATILE) != 0) m.add("volatile");
		return m;
	}

	/** not private, and not compiler generated */
	static boolean api(int access) {
		return (access & (ACC_PRIVATE | ACC_SYNTHETIC | ACC_BRIDGE)) == 0;
	}

	/**
	 * Collect a class' AST, nesting, fields and the types in its API.
	 */
	class Collector extends ClassVisitor {
		Cls cls;

		Collector() { super(ASM9); }

		@Override
		public void visit(int version, int access, String name, String signature,
				String superName, String[] interfaces) {
			cls = new Cls();
			cls.name = name;
			cls.access = access;
			cls.superName = superName;
			cls.interfaces = interfaces;
			cls.ast = new AnsonAst(null, null);
			cls.ast.fields = new LinkedHashMap<String, AnsonField>();
			classes.put(name, cls);

			if (signature != null)
				useSignature(signature);
			else {
				if (superName != null) cls.uses.add(superName);
				if (interfaces != null) for (String i : interfaces) cls.uses.add(i);
			}
		}

		@Override
		public void visitInnerClass(String name, String outerName, String innerName, int access) {
			inners.putIfAbsent(name, new Inner(outerName, innerName, access));
		}

		@Override
		public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
			if (!api(access))
				return null;

			if ((access & ACC_ENUM) != 0) {
				cls.enums.add(name);
				return null;
			}

			useType(Type.getType(descriptor));
			if (signature != null) useSignature(signature);

			AnsonField f = new AnsonField(name, modifiers(access, false));
			cls.ast.fields.put(name, f);

			TypeSig t = TypeSig.of(signature != null ? signature : descriptor);
			cls.resolves.add(() -> {
				f.dataAnclass = astType(t);
				f.valType = valType(t);
			});

			return new FieldVisitor(ASM9) {
				@Override
				public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
					cls.annotationTypes.add(Type.getType(desc).getInternalName());
					AnAnnotation a = new AnAnnotation(null, visible);
					cls.resolves.add(() -> a.annotation = typename(Type.getType(desc)));
					f.annotations.add(a);
					return new Elements(cls, a);
				}
			};
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
			if (api(access) && !"<clinit>".equals(name)) {
				for (Type t : Type.getArgumentTypes(descriptor)) useType(t);
				useType(Type.getReturnType(descriptor));
				if (signature != null) useSignature(signature);
				if (exceptions != null) for (String e : exceptions) cls.uses.add(e);
			}
			return null;
		}

		/** Object types, and arrays' element types, e.g. AnsT2[][][] -> AnsT2. */
		void useType(Type t) {
			if (t.getSort() == Type.ARRAY)
				t = t.getElementType();
			if (t.getSort() == Type.OBJECT)
				cls.uses.add(t.getInternalName());
		}

		/** Generic signatures, e.g. HashMap&lt;String, ArrayList&lt;AnsT4Enum.MsgCode&gt;&gt; */
		void useSignature(String signature) {
			new SignatureReader(signature).accept(new SignatureVisitor(ASM9) {
				/** class types being visited, nested by type arguments */
				final ArrayDeque<String> types = new ArrayDeque<String>();

				@Override
				public void visitClassType(String name) {
					types.push(name);
					cls.uses.add(name);
				}

				/** Outer&lt;T&gt;.Inner -> Outer$Inner */
				@Override
				public void visitInnerClassType(String name) {
					String inner = types.pop() + "$" + name;
					types.push(inner);
					cls.uses.add(inner);
				}

				@Override
				public void visitEnd() {
					types.pop();
				}
			});
		}
	}

	/**
	 * Visits an annotation's elements into {@link AnAnnotation#elements}.
	 * Names of classes and enums are assigned later, see {@link Cls#resolves}.
	 */
	class Elements extends AnnotationVisitor {
		final Cls cls;
		final AnAnnotation an;

		Elements(Cls cls, AnAnnotation an) {
			super(ASM9);
			this.cls = cls;
			this.an = an;
		}

		@Override
		public void visit(String name, Object value) {
			if (value instanceof Type t) { // class literal
				AnValue v = new AnValue(name, "java.lang.Class", null);
				cls.resolves.add(() -> v.value = typename(t));
				an.elements.add(v);
			}
			else if (value != null && value.getClass().isArray()) { // primitive arrays, e.g. int[]
				AnValue arr = AnValue.array(name);
				for (int i = 0; i < Array.getLength(value); i++)
					arr.add(valType(Array.get(value, i)), String.valueOf(Array.get(value, i)));
				an.elements.add(arr);
			}
			else
				an.elements.add(new AnValue(name, valType(value), String.valueOf(value)));
		}

		@Override
		public void visitEnum(String name, String descriptor, String value) {
			AnValue v = new AnValue(name, null, null);
			cls.resolves.add(() -> {
				v.valType = typename(Type.getType(descriptor));
				v.value = v.valType + "." + value;
			});
			an.elements.add(v);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String name, String descriptor) {
			AnAnnotation nested = new AnAnnotation(null, true);
			AnValue v = AnValue.annotation(name, nested);
			cls.resolves.add(() -> v.valType = nested.annotation = typename(Type.getType(descriptor)));
			an.elements.add(v);
			return new Elements(cls, nested);
		}

		@Override
		public AnnotationVisitor visitArray(String name) {
			AnValue arr = AnValue.array(name);
			an.elements.add(arr);
			return new ArrayElements(cls, arr);
		}
	}

	/** Visits an array element's values, of which the names are null. */
	class ArrayElements extends AnnotationVisitor {
		final Cls cls;
		final AnValue arr;

		ArrayElements(Cls cls, AnValue arr) {
			super(ASM9);
			this.cls = cls;
			this.arr = arr;
		}

		@Override
		public void visit(String name, Object value) {
			if (value instanceof Type t) {
				int ix = arr.array.size();
				arr.add("java.lang.Class", null);
				cls.resolves.add(() -> arr.array.set(ix, typename(t)));
			}
			else
				arr.add(valType(value), String.valueOf(value));
		}

		@Override
		public void visitEnum(String name, String descriptor, String value) {
			int ix = arr.array.size();
			arr.add("?", null);
			cls.resolves.add(() -> {
				String enm = typename(Type.getType(descriptor));
				arr.valType = enm + "[]";
				arr.array.set(ix, enm + "." + value);
			});
		}

		@Override
		public AnnotationVisitor visitAnnotation(String name, String descriptor) {
			AnAnnotation nested = new AnAnnotation(null, true);
			if (arr.annotations == null) {
				arr.array = null;
				arr.annotations = new ArrayList<AnAnnotation>();
			}
			arr.annotations.add(nested);
			cls.resolves.add(() -> {
				nested.annotation = typename(Type.getType(descriptor));
				arr.valType = nested.annotation + "[]";
			});
			return new Elements(cls, nested);
		}
	}
}
