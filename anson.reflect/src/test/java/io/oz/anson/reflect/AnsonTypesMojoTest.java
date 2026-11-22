package io.oz.anson.reflect;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

import io.odysz.anson.Anson;
import io.odysz.anson.AnsT2;
import io.odysz.reflect.AnAnnotation;
import io.odysz.reflect.AnsonAst;
import io.odysz.reflect.AnsonTypes;

/**
 * Run the goal on io.oz.anson.reflect.ttypes, without Maven, with antson's test classes
 * as the only dependency.
 */
public class AnsonTypesMojoTest {

	@Test
	public void testExecute() throws Exception {
		Path out = Files.createTempDirectory("anson-types");
		Path classes = out.resolve("classes");
		Path src = out.resolve("src-main-resources");

		// target/classes of a module, with only the test types
		Path ttypes = TypeScannerTest.ttypes();
		Path pkg = classes.resolve("io/oz/anson/reflect/ttypes");
		Files.createDirectories(pkg);
		try (var files = Files.list(ttypes)) {
			for (Path f : (Iterable<Path>) files::iterator)
				Files.copy(f, pkg.resolve(f.getFileName()));
		}

		Model model = new Model();
		model.setGroupId("io.github.odys-z");
		model.setArtifactId("anson-test-types");
		model.setVersion("0.0.1");
		MavenProject project = new MavenProject(model);

		// antson's test types, as the test-jar dependency, and antson
		project.setArtifacts(Set.of(
			artifact("antson", "test-jar", codeSource(AnsT2.class)),
			artifact("antson", "jar", codeSource(Anson.class))));

		AnsonTypesMojo mojo = new AnsonTypesMojo();
		mojo.project = project;
		mojo.classes = classes.toFile();
		mojo.fileName = "anson-types.json";
		mojo.srcDir = src.toFile();
		mojo.execute();

		String json = Files.readString(classes.resolve("anson-types.json"), StandardCharsets.UTF_8);
		assertEquals(json, Files.readString(src.resolve("anson-types.json"), StandardCharsets.UTF_8));

		AnsonTypes types = (AnsonTypes) Anson.fromJson(json);
		assertEquals("io.github.odys-z:anson-test-types:0.0.1", types.module);
		assertEquals(8, types.asts.size());
		assertEquals("record", types.asts.get("io.oz.anson.reflect.ttypes.TArrays.Rec").kind);

		// fields and annotations, read back from json
		AnsonAst t = types.asts.get("io.oz.anson.reflect.ttypes.TArrays");
		assertEquals("list<list<list<io.odysz.anson.AnsT2", t.fields.get("cube").dataAnclass);
		assertEquals(io.odysz.reflect.AnsonField.class, t.fields.get("cube").getClass());
		assertEquals(List.of("a", "b"), List.copyOf(types.asts.get("io.oz.anson.reflect.ttypes.TArrays.Kind").enums.keySet()));
		AnAnnotation a = t.fields.get("parent").annotations.get(0);
		assertEquals("io.odysz.anson.AnsonField", a.annotation);
		assertEquals("true", a.element("ignoreTo").value);

		// an annotation element named "type", Anson's envelope key
		AnAnnotation p = t.fields.get("pkgMap").annotations.get(0);
		assertEquals("io.odysz.anson.AnsT3", p.element("type").value);
		assertEquals("java.lang.Class", p.element("type").valType);
		assertEquals(List.of("1", "2"), p.element("nums").array);

		assertEquals("io.github.odys-z:antson:1.2.0", types.dependencies.get("io.odysz.anson.AnsT2"));
		assertEquals("io.github.odys-z:antson:1.2.0", types.dependencies.get("io.odysz.anson.AnsT4Enum.MsgCode"));
		assertEquals("io.github.odys-z:antson:1.2.0", types.dependencies.get("io.odysz.anson.Anson"));
		assertEquals("io.github.odys-z:antson:1.2.0", types.dependencies.get("io.odysz.anson.AnsonField"));
		assertNull(types.dependencies.get("java.util.HashMap"), "JDK types are not in dependencies");

		// unchanged, not rewritten
		long modified = Files.getLastModifiedTime(src.resolve("anson-types.json")).toMillis();
		Thread.sleep(20);
		mojo.execute();
		assertEquals(modified, Files.getLastModifiedTime(src.resolve("anson-types.json")).toMillis());
	}

	static File codeSource(Class<?> c) throws Exception {
		return new File(c.getProtectionDomain().getCodeSource().getLocation().toURI());
	}

	static Artifact artifact(String artifactId, String type, File file) {
		Artifact a = new DefaultArtifact("io.github.odys-z", artifactId, "1.2.0", "compile", type,
				"test-jar".equals(type) ? "tests" : null, new DefaultArtifactHandler(type));
		a.setFile(file);
		return a;
	}
}
