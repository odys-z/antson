package io.oz.anson.reflect;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import io.odysz.anson.JsonOpt;
import io.odysz.reflect.AnsonTypes;

/**
 * Goal types: list the module's types, their non-private fields, and the types they use, as io.odysz.reflect.AnsonAst, into anson-types.json.
 *
 * <p>Runs after compile, in phase process-classes, and writes</p>
 * <ol>
 * <li>${project.build.outputDirectory}/anson-types.json, packed into the jar;</li>
 * <li>a copy in {@link #srcDir}, default src/main/resources, to be committed and read
 *     before compiling, e.g. by tasks.py. The copy is written only if changed.</li>
 * </ol>
 *
 * Usage, in a project's pom.xml:
 * <pre>
 * &lt;plugin&gt;
 *   &lt;groupId&gt;io.github.odys-z&lt;/groupId&gt;
 *   &lt;artifactId&gt;anson.reflect&lt;/artifactId&gt;
 *   &lt;version&gt;0.0.1&lt;/version&gt;
 *   &lt;executions&gt;
 *     &lt;execution&gt;&lt;goals&gt;&lt;goal&gt;types&lt;/goal&gt;&lt;/goals&gt;&lt;/execution&gt;
 *   &lt;/executions&gt;
 * &lt;/plugin&gt;
 * </pre>
 *
 * @since 0.0.1
 */
@Mojo(name = "types",
	  defaultPhase = LifecyclePhase.PROCESS_CLASSES,
	  requiresDependencyResolution = ResolutionScope.COMPILE,
	  threadSafe = true)
public class AnsonTypesMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	MavenProject project;

	/** The compiled classes to be scanned. */
	@Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true, required = true)
	File classes;

	@Parameter(property = "anson.types.file", defaultValue = "anson-types.json")
	String fileName;

	/** Where the committed copy is written. Empty: no copy. */
	@Parameter(property = "anson.types.srcDir", defaultValue = "${project.basedir}/src/main/resources")
	File srcDir;

	@Parameter(property = "anson.types.skip", defaultValue = "false")
	boolean skip;

	@Override
	public void execute() throws MojoExecutionException {
		if (skip) {
			getLog().info("anson-types: skipped");
			return;
		}

		try {
			TypeScanner scanner = new TypeScanner().scan(classes.toPath());
			AnsonTypes types = scanner.types(String.format("%s:%s:%s",
					project.getGroupId(), project.getArtifactId(), project.getVersion()));

			resolveDependencies(scanner.externals(), types);

			String json = types.toBlock(JsonOpt.beautify());

			Path out = classes.toPath().resolve(fileName);
			Files.createDirectories(out.getParent());
			Files.writeString(out, json, StandardCharsets.UTF_8);

			if (srcDir != null && !srcDir.getPath().isEmpty()) {
				Path copy = srcDir.toPath().resolve(fileName);
				if (!Files.exists(copy) || !json.equals(Files.readString(copy, StandardCharsets.UTF_8))) {
					Files.createDirectories(copy.getParent());
					Files.writeString(copy, json, StandardCharsets.UTF_8);
					getLog().info("anson-types: updated " + copy);
				}
			}

			getLog().info(String.format("anson-types: %d types, %d external types in dependencies -> %s",
					types.asts.size(), types.dependencies.size(), out));
		} catch (Exception e) {
			throw new MojoExecutionException("Failed to list types in " + classes, e);
		}
	}

	/**
	 * Find the artifacts of the used types, in the resolved compile dependencies.
	 *
	 * @param externals {internal name: qualified name}
	 * @param types the result, of which the dependencies is filled
	 * @throws IOException
	 */
	void resolveDependencies(Map<String, String> externals, AnsonTypes types) throws IOException {
		for (Artifact a : project.getArtifacts()) {
			File f = a.getFile();
			if (f == null) continue;

			String gav = String.format("%s:%s:%s", a.getGroupId(), a.getArtifactId(), a.getVersion());

			for (String internal : classNames(f))
				if (externals.containsKey(internal))
					types.dependencies.putIfAbsent(externals.get(internal), gav);
		}

		// sorted
		types.dependencies = new LinkedHashMap<String, String>(new TreeMap<String, String>(types.dependencies));
	}

	/**
	 * @param jarOrDir
	 * @return internal names of the class files, e.g. io/odysz/anson/Anson
	 * @throws IOException
	 */
	static Iterable<String> classNames(File jarOrDir) throws IOException {
		ArrayList<String> names = new ArrayList<String>();

		if (jarOrDir.isDirectory()) {
			Path root = jarOrDir.toPath();
			try (Stream<Path> files = Files.walk(root)) {
				files.filter(p -> p.toString().endsWith(".class"))
					 .forEach(p -> names.add(classname(root.relativize(p).toString().replace(File.separatorChar, '/'))));
			}
		}
		else if (jarOrDir.getName().endsWith(".jar")) {
			try (ZipFile zip = new ZipFile(jarOrDir)) {
				Enumeration<? extends ZipEntry> en = zip.entries();
				while (en.hasMoreElements()) {
					String n = en.nextElement().getName();
					if (n.endsWith(".class") && !n.startsWith("META-INF/"))
						names.add(classname(n));
				}
			}
		}
		return names;
	}

	static String classname(String path) {
		return path.substring(0, path.length() - ".class".length());
	}
}
