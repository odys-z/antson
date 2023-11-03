Anprism Plugin
==============

Antson for Anprism plugin is a tool for parsing json object into a few language's
data structure.

Primary Data Types
------------------

::

    json value         Java                C++ 2013                Typescript
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

1. create sample data used for binding with Anclient typescript, @anclient/anreact
2. requests & response package, code snipts (or base class) at serverside are generated 
3. load @anclient/anreact application UI components
4. implement serverside semantier's functions 
