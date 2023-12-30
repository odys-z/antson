Anprism Plugin
==============

Antson for Anprism plugin is a tool for parsing json object into a few language's
data structure.

Primary Data Types
------------------

::

    json value         Java                C++ 2014                Typescript
	STRING        java.lang.String         string                   string
	NUMBER        int, double              int, double              number
	obj           HashMap                  map                      object
	envelope      io.oz.anson.Anson        io.oz.anson.Anson        Anson
	array         ArrayList<?>             vector<>                 array *
	'true'        true                     true                     true
	'false'       false                    false                    false
	'null'        null                     NULL                     undefined

..

    * different element types are not allowed

Basic development process with Antson plugin
--------------------------------------------

1. Create sample data used for binding with Anclient typescript, @anclient/anreact
2. Requests & response packages, code snipts (or base class) at serverside in Java, c14/typescript/java at client are generated
3. Load @anclient/anreact application UI components
4. Implement serverside semantier's functions
