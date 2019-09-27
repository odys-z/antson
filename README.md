# What's Antson

Antson is ANother Tool for JSON (de)serialize java object to / from json.

# Why Antson?

For short, there is no such tool / lib that can convert javascript object to or
from java or other programming language like C#.

Why? Because js object has no types. You can not define a "io.app.User" type in
js. All js objects are only a HashMap for Java. Any json data are converted into
java.lang.Map in java world traditionally. There are a lot of tools helping to do
this. For java, json is data without type, this is every upsetting to java programmers,
at least to the author.

In many cases, only HashMap structure is not enought. Take a SOA architecture for example,
protocol packages needing a way to tell the reciever how to deserialize the package.
Servers needing to know how the request been parsed, even finding a parser - like finding the
correct mothod for a [SOAP Envelope](https://en.wikipedia.org/wiki/SOAP#Example_message_(encapsulated_in_HTTP)).

If all envelopes can not parsed before dispatched, the dispatcher must atleast first try to docode "port" name,
then let the reciever handle the rest of the content - because it's not understandable by dispatcher.
If you are comfort with MS .net webservice framework, you are not supprised with this idea.

On way to do this is use semantics defined by structure, like this:

    {port: p-name, data : {method-quest-type-data}}

It's not simply a map now, it's structured data. If this structure getting more complex,
user will have to explain the semantics. Then (de)serializer will getting complicate.
The down side of this is your architecture will turn into mess because you can't separate the
protocol layer from the application's business handling.

[Gson](https://github.com/google/gson) is a good try to go further. But the
method is still not enough. The main weakness is it's not smart enough - it doesn't
handle java fields with type information and (de)serialize back forth. Every Gson translation
of json data to Java object needing user implementing business handlers, one problem a time.

Antson is trying to go further. Users only needing to define their business gramma -
the application business data defined in strictly typed language like java or c#,
then send packages back and forth with the help of Antson API, consuming the data
objects like normal structured objects, and only take care of it's business processing.

# Why no stream mode?

Antson provid only output stream writing API, for serializing into json string. No input
stream mode is supported.

The reason behind this is that Antson is based on Antlr, it's an LL(\*) parsing tool.

If your json data is large, try breack it into small chunks, or may be let anston working
as a [Karfka](https://kafka.apache.org/intro) message consummer - might try a test in the future.

# What's it for?

Antson will be used as the transport protocol layer for semantic-\*. see his
[home page](https://odys-z.github.io) for details.

It's also planned publishing it as an independent lib, at least as a jar and DLL.

# Known Issues

## 1. Array element's type must specified

The json gramma is taken and modified from [Antlr4's Gramma Page](https://github.com/antlr/grammars-v4/blob/master/json/JSON.g4), which is kept consistency with [JSON Gramma](https://www.json.org/)

The problem of this official version is that an object value's type present in an array can not been figured out automatically.

So I'v added a new type of value (envelope) to value's declaration, like this:

    array
	: '[' value (',' value)* ']'
	| '[' ']'
	;

    value
	: STRING
	| NUMBER
	| obj		// all array's obj value can't parsed as HashMap
	| envelope	// the java equivalent is io.odysz.anson.Anson
	| array
	| 'true'
	| 'false'
	| 'null'
	;

The difference between envelope and obj is an evelope must have a type-pair:

    envelope // also top node
	: '{' type_pair (',' pair)* '}'
	;

    obj
	: '{' pair (',' pair)* '}'
	| '{' '}'
	;

In short, if an element in an array should been parsed as an Anson object in java, it must include a 'type_pair':

    type_pair
	: TYPE ':' qualifiedName
	;

e.g., the json string

    { type: "Outter",
      arr: [{type: "Inner", prop: "v..."}, ...]
    }

can be parsed into java type of

    class Outter extends Anson {
        Inner[] arr; // element's can be subclasses of Anson
    }

Note that all envelopes in java are instances of io.odysz.anson.Anson.

See the test case [AnsonTest#testFromJson_asonArr()](https://github.com/odys-z/antson/blob/master/antson.java/src/test/java/io/odysz/anson/AnsonTest.java).

## 2. You need provide annotation if your type in List or Map is complicate

If the value's type is more complicate than string, you must provide the information.

In the current version, it's already proved at least java version needing this information
because java is [type erasure](https://docs.oracle.com/javase/tutorial/java/generics/erasure.html)
for generic type parameters.

Here is the test case:

~~~
    public class AnsonResultset {
        @AnsonField(valType="[Ljava.lang.Object;")
        private HashMap<String, Object[]> colnames;
    }
~~~

For usable valType string, see [Class.forName() API](https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#forName(java.lang.String).

For test case, see [AnsonTest#testFromJson_rs()](https://github.com/odys-z/antson/blob/master/antson.java/src/test/java/io/odysz/anson/AnsonTest.java).

## 3. Referencing loops

Antson try to deep serializing Anson objects. If two or more objects referencing
each other, the java serializing processing will endup with stack over flow error.

To avoid this, one of the referencing field must specified with annotation to break
the looping:

~~~
    @AnsonField(ignore='true')
~~~

This will successfully serialize java object into json. But the problem is it can
not deserialize the reference correctly.

To re-establish this relation is not implemented except the "enclosing" relationship.
Use the "enclosing" annotation to deserialize the relation automatically:

~~~
    @AnsonField(enclosing)
~~~

This will make Antson deserialize an object with a guess - just set it to the enclosing
object.

See test case: [AnsonTest#test2Json_PC()](https://github.com/odys-z/antson/blob/master/antson.java/src/test/java/io/odysz/anson/AnsonTest.java).
