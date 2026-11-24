[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.odys-z/semantics.java)](https://central.sonatype.com/artifact/io.github.odys-z/semantics.java)
[![License](https://img.shields.io/badge/license-MIT-blue)](../LICENSE)

# semantics.java

The parent pom of the odys-z java projects: antson, semantic.transact, semantic.DA,
semantic.jserv, anclient.jserv, doctree.jserv, html-service, and others.

```
io.github.odys-z:semantics.java:0.0.3   (packaging pom)
```

## Why

Plugins without a version in a pom take the defaults of whichever Maven runs the
build. Maven 3.9.11 and 3.9.12 differ in those defaults, so a build that works on one
machine can fail offline on another (e.g. maven-jar-plugin 3.4.1 missing from
m2-offline). This pom pins the plugin versions once for all projects.

## What it manages

| | |
|---|---|
| Lifecycle plugins | clean, resources, compiler, surefire, jar, install, deploy |
| Release plugins | source, javadoc, gpg, central-publishing, flatten |
| Others | shade, assembly, dependency, plugin-plugin (maven-plugin packaging) |
| Properties | UTF-8 encodings, Java 17 (`maven.compiler.source` / `target`) |
| Test dependencies | JUnit, by importing `junit-bom` (`junit.version`) |
| Metadata | url, licenses, developers, scm, snapshot repository |

The versions are properties, see `<properties>` in [pom.xml](pom.xml).

Apart from JUnit it does **not** manage dependencies: the projects depend on each
other at different versions, so those versions stay in each project's pom.

## Use it in a project

```xml
<parent>
  <groupId>io.github.odys-z</groupId>
  <artifactId>semantics.java</artifactId>
  <version>0.0.3</version>
  <relativePath/>     <!-- ../semantics.java for antson.java and anson.reflect -->
</parent>
```

Add the flatten plugin, so the pom deployed to Central is self-contained. Central
validates the deployed pom without following `<parent>`, and rejects it when the
licenses, developers, scm or a dependency version come from the parent:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>flatten-maven-plugin</artifactId>
    </plugin>
  </plugins>
</build>
```

Then remove the `<version>` of the plugins listed above, and the metadata the
parent already has. Declare JUnit without a version:

```xml
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter-engine</artifactId>
  <scope>test</scope>
</dependency>
```
 Keep each project's own plugin configuration (manifest, shade
setup, executions, ...).

A project needing another version overrides the property, e.g.

```xml
<properties>
  <surefire.version>2.22.1</surefire.version>
</properties>
```