
[![Ukraine](https://cdn3.emoji.gg/emojis/6101-ukraine.gif)](https://emoji.gg/emoji/6101-ukraine)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.odys-z/antson/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.odys-z/antson/)
[![PyPI version](https://img.shields.io/pypi/v/anson.py3.svg)](https://pypi.org/project/anson.py3)
[![License](https://img.shields.io/badge/license-MIT-blue)](./LICENSE)

# What's Antson

Antson is **AN**other **T**ool for j**SON** (de)serializing objects in typed
language such as Java or Python3 to / from json.

A java object of type

```
    class A {
        String name;

        public A (String name) {
            this.name = name;
        }
    }

    A obj = new A ("Antson");
```

is sereialized into json

```
    { type: 'A',
      name: 'Antson'
    }
```

Then the json object can be deserialized to an Object in client side, any one of Java, C# or Typescript.

# What it is for?

Antson will be used as the transport protocol layer for semantic-\*. see his
[home page](https://odys-z.github.io) for details.

Currently there are only two different language runtime lib, c# & java. There is
a typescript client, [@anclient/semantier](https://github.com/odys-z/Anclient/tree/master/js/semantier),
implementing the protocol layer between
json data service, usually by java, and the the js front end. Although it's plausible,
users are not recommended to use Antson from scratch for parsing the raw json data by
themselves. But if you are interested in other usages, the test cases located in
antson.java/src/test subfolder provided a bunch of examples for (de)serializing json
objects to and from java types.

The runtimes are antson.java & antson.csharp sub folders. The c# version come with
an example in [Anclient/example.cs](https://github.com/odys-z/Anclient#repository-structure).

Unfortunately, only antson.java can work stably currently. See [API documents](https://odys-z.github.io/javadoc/antson/).

A c# version is pushed in Nuget and verified that it is efficient, but it is pretty old
and you should not waste time on it.

# Why Antson?

For short, there is no such tool / lib that can convert javascript objects to or
from java or other programming languages like C#, on the fly.

Why? Because js objects has no structure types with support of any compiler. Users
can not define a "io.app.User" type in js. All js objects are only a HashMap in typed
languages e.g. Java. Any json data is converted into java.lang.Map in java world
traditionally. Although there are a lot of tools helping to do this, a js object is
data without type which means no type checking and the data is nothing carrying any
semantic rules. This is every upsetting to java programmers, at least to the author.

In many cases, only a hash table structure is not enough. Take a SOA architecture for
example, protocol packages needing a way to tell the receivers how to deserialize
the package. Servers needing to know how the requests been parsed, even for finding
the parser - like finding the correct method for a
[SOAP Envelope](https://en.wikipedia.org/wiki/SOAP#Example_message_(encapsulated_in_HTTP)).

If all envelopes can not be parsed before dispatched, the dispatcher must at
least first try to docode "port" name, then let the receivers handle the rest
of the content - because it's not understandable by dispatcher.

One way to do this is use semantics defined by structure, like this:

~~~
    {port: p-name, data : {method-requesting-type-data}}
~~~

It's no longer simply a map now, it's structured data. If this structure is getting
more complex, data users will have to explain the structure, otherwise it won't been
deserialized properly. Then the (de)serializer will become complicated. The down
side of this is your architecture will turn into a mess because you can't separate
the protocol layer from the application's business handling as the message parsing
depends on business logic.

For java or c# data types, this semantics is supported at language level. The server
and client should talk on the same data type and exchange object at ease. To achieve
this, typed data should been deserialized transparently, and exchanged between the
client and server. If users are comfortable with MS .net webservice framework, they
shouldn't be suprised with this idea.

[Gson](https://github.com/google/gson) is a good try to go further. But the method
is still not enough. The main weakness is it's not smart enough - it doesn't handle
java fields with type information and (de)serialize back forth. Every Gson translation
of json data to Java object needing users implementing business handlers, handling
one problem a time.

Antson is trying to go further. Users only need to define their business gramma -
the application business data defined in strictly typed language like java or c#,
then send json packages back and forth with the help of Antson API, consuming the
data objects like normal structured objects, and only take care of it's business
processing.

(Since 0.9.4, Antson also implemented a c# version, and we are still comparing it
with [Newtonsoft Json.NET](https://www.newtonsoft.com/json). )

# Why no input stream mode?

Antson provid only output stream writing API, for serializing into json string.
No input (deserializing) stream mode is supported.

The reason behind this is that Antson is based on [Antlr4](https://github.com/antlr/antlr4),
which is an LL(\*) parsing tool that can not work in stream mode.

If your json data is large, try breack it into small chunks, or may be let Anston
working as a [Karfka](https://kafka.apache.org/intro) message consummer - might
try a test in the future.

(Since Apr. 2022, a block chain schema based on java client on Android and Tomcat
proved (without workbench results) the "chuck" mode can work efficiently.
See [Anclient example for Android](https://github.com/odys-z/Anclient/tree/master/examples/example.android))

# Anson Envelope

-- *Anson, not Antson is a type in typed language as the base class of serializable types.*

The json gramma is taken and modified from [Antlr4's Gramma Page](https://github.com/antlr/grammars-v4/blob/master/json/JSON.g4),
which is kept consistency with [JSON Gramma](https://www.json.org/).

The problem of this official version is that an object value's type present in
an array can not been figured out automatically.

So Antson uses a slightly modified version of JSON specification, adding a new
type of value (envelope) to value's declaration, e.g. array is:

~~~
    array
	: '[' value (',' value)* ']'
	| '[' ']'
	;

    value
	: STRING
	| NUMBER
	| obj		// all array's obj value can't be parsed as HashMap
	| envelope	// the java equivalent is io.odysz.anson.Anson
	| array
	| 'true'
	| 'false'
	| 'null'
	;
~~~

The difference between envelope and obj is an envelope must have a type-pair:

~~~
    envelope // also top node
	: '{' type_pair (',' pair)* '}'
	;

    obj
	: '{' pair (',' pair)* '}'
	| '{' '}'
	;
~~~

In short, if an element in an array should be parsed as an Anson object in java,
it must include a 'type_pair' as the first pair:

~~~
    type_pair
	: TYPE ':' qualifiedName
	;
~~~

e.g., the json string

~~~
    { type: "Outter",
      arr: [{type: "Inner", prop: "v..."}, ...]
    }
~~~

can be parsed into java type of

~~~
    class Outter extends Anson {
        Inner[] arr; // elements are subclasses of Anson
    }
~~~

Note that all envelopes in java are instances of io.odysz.anson.Anson.

See the test case [AnsonTest#testFromJson_asonArr()](https://github.com/odys-z/antson/blob/master/antson.java/src/test/java/io/odysz/anson/AnsonTest.java).

# Known Issues

## 1. Needing provide an annotation if a type in List or Map is complicate

If the value's type in a list or map is more complicate than string, you must
provide the information.

In the current version, it's already proved at least java version needing this
information because java is [type erasure](https://docs.oracle.com/javase/tutorial/java/generics/erasure.html)
for generic type parameters.

Here is the test case:

~~~
    public class AnsonResultset {
        @AnsonField(valType="[Ljava.lang.Object;")
        private HashMap<String, Object[]> colnames;
    }
~~~

For usable valType string, see [Class.forName() API](https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#forName%28java.lang.String%29).

For test case, see [AnsonTest#testFromJson_rs()](https://github.com/odys-z/antson/blob/master/antson.java/src/test/java/io/odysz/anson/AnsonTest.java).

## 2. Only 2D array are supported without annotation

Non primitive elements in array or list must specify type with annotation AnsonField#valType.

If the elements are also arrays of array, the 3D array (list) won't be deserialized correctly. There would be error log like this:

```
- Trying convert array to annotated type failed.
- type: [Ljava.util.ArrayList;
- json: [["0-0-0",""],["0-1-0"]]
- error: array element type mismatch
```

For 3D and more dimension array, element type can be annotated like:

~~~
    @AnsonField (valType="java.util.ArrayList/[Ljava.lang.Object;")
    protected ArrayList<ArrayList<Object[]>> name_value_pairs;
~~~

This defined a 2D table of name-value pair, where if row is the main order, then
the rows are element of type "ArrayList<Object[]>", where the cell is "Object[]".

Row type and cell type are separated with "/".

Also, another error prone annotation is the array of list:

~~~
    @AnsonField (valType="java.util.ArrayList/[Ljava.lang.Object;")
    protected ArrayList<Object[]>[] name_value_pairs;
~~~

The problem here is with the first section, "[Ljava.util.ArrayList;", shouldn't been
specified here for it's the type of field which is already declared as a java type.
The component type of array is "java.util.ArrayList" (of "java.util.ArrayList/
[Ljava.lang.Object;").

## 3. Referencing loops

Antson try to deep serializing Anson objects. If two or more objects referencing
each other, the java serializing processing will end up with stack over flow error.

To avoid this, one of the referencing field must specified with annotation to break
the endless serializing looping:

~~~
    @AnsonField(ignore='true')
~~~

This will successfully serialize java object into json. But the problem is it can
not deserialize the reference correctly.

To re-establish this relation is not implemented except the "parent" relationship.
Use the "ref=AnsonField.enclosing" annotation to deserialize the relation automatically:

~~~
    @AnsonField(ref=AnsonField.enclosing)
~~~

This will make Antson deserialize an object with a guess - just set it to the enclosing
object.

The common tree structure is handled smoothly with this. For a demo, see junit
test case:
[AnsonTest#test2Json_PC()](https://github.com/odys-z/antson/blob/master/antson.java/src/test/java/io/odysz/anson/AnsonTest.java).

More freature requirement is open to comments and discussion.

## 4. Inner class (java) can only be declared as static

This is because it's not easy to figure out the parent object to create the inner class instance.

For how inner class examples, see test case
[AnsonTest#test_innerClass()](https://github.com/odys-z/antson/blob/master/antson.java/src/test/java/io/odysz/anson/AnsonTest.java).

If you have any idea, please let the author know.

#### [API documents](https://odys-z.github.io/javadoc/antson/)
