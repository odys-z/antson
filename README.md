# What's Antson

Antson is ANother Tool for JSON (de)serialize java object to / from json.

# Why Antson?

For short, there is no such tool / lib that can convert javascript object to or
from java or other programming language like C#.

Why? Because js object has no structure types with support of compiler. You can
not define a "io.app.User" type in js. All js objects are only a HashMap for Java.
Any json data are converted into java.lang.Map in java world traditionally. There
are a lot of tools helping to do this. For java, js object is data without type,
this is every upsetting to java programmers, at least to the author.

In many cases, only HashMap structure is not enought. Take a SOA architecture for
example, protocol packages needing a way to tell the reciever how to deserialize
the package. Server needing to know how the request been parsed, even for finding
the parser - like finding the correct mothod for a
[SOAP Envelope](https://en.wikipedia.org/wiki/SOAP#Example_message_(encapsulated_in_HTTP)).

If all envelopes can not parsed before dispatched, the dispatcher must at least
first try to docode "port" name, then let the reciever handle the rest of the
content - because it's not understandable by dispatcher.

One way to do this is use semantics defined by structure, like this:

~~~
    {port: p-name, data : {method-requesting-type-data}}
~~~

It's no longer simply a map now, it's structured data. If this structure getting
more complex, data user will have to explain the structure, otherwise it won't been
deserialized properly. Then the (de)serializer will getting complicate. The down
side of this is your architecture will turn into mess because you can't separate
the protocol layer from the application's business handling - your massage parsing
depends on business logics.

For java or c# data type, the structure is supported at language level. The server
and client should talk on the same data type and exchange object at ease. To achieve
this, typed data should been deserialized transparently, and exchanged between the
client and server. If you are comfort with MS .net webservice framework, you are
not supprised with this idea.

[Gson](https://github.com/google/gson) is a good try to go further. But the method
is still not enough. The main weakness is it's not smart enough - it doesn't handle
java fields with type information and (de)serialize back forth. Every Gson translation
of json data to Java object needing user implementing business handlers, one problem
a time.

Antson is trying to go further. Users only needing to define their business gramma -
the application business data defined in strictly typed language like java or c#,
then send json packages back and forth with the help of Antson API, consuming the
data objects like normal structured objects, and only take care of it's business
processing.

# Why no input stream mode?

Antson provid only output stream writing API, for serializing into json string.
No input stream mode is supported.

The reason behind this is that Antson is based on [Antlr4](https://github.com/antlr/antlr4),
which is an LL(\*) parsing tool that can not work in stream mode.

If your json data is large, try breack it into small chunks, or may be let Anston
working as a [Karfka](https://kafka.apache.org/intro) message consummer - might
try a test in the future.

# What's it for?

Antson will be used as the transport protocol layer for semantic-\*. see his
[home page](https://odys-z.github.io) for details.

Before Antson, one of the important way to extend semantic-jserv logics, the servlet
way, is limited for it's unconvenient of message (de)serializing - message's structure
must been handled with Gson extension by user implementation. With Antson, users
don't need to override handler for different structure anymore. Following some
basic rules like extends io.odysz.Anson or break reference loop with annotation,
json v.s java type translation will be automatically handled and being transparent
to upper tiers.

It's also planned to publish Antson as an independent lib, at least as a jar and DLL.

# Known Issues

## 1. Array element's type must specified

The json gramma is taken and modified from [Antlr4's Gramma Page](https://github.com/antlr/grammars-v4/blob/master/json/JSON.g4),
which is kept consistency with [JSON Gramma](https://www.json.org/).

The problem of this official version is that an object value's type present in
an array can not been figured out automatically.

So Antson uses a slightly modified version of JSON specificateion, adding a new
type of value (envelope) to value's declaration, like this:

~~~
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
~~~

The difference between envelope and obj is an evelope must have a type-pair:

~~~
    envelope // also top node
	: '{' type_pair (',' pair)* '}'
	;

    obj
	: '{' pair (',' pair)* '}'
	| '{' '}'
	;
~~~

In short, if an element in an array should been parsed as an Anson object in java,
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
        Inner[] arr; // element's can be subclasses of Anson
    }
~~~

Note that all envelopes in java are instances of io.odysz.anson.Anson.

See the test case [AnsonTest#testFromJson_asonArr()](https://github.com/odys-z/antson/blob/master/antson.java/src/test/java/io/odysz/anson/AnsonTest.java).

## 2. You need provide annotation if your type in List or Map is complicate

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

## 3. Only 2D array are supported without annotation

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
    @AnsonField (valType="java.util.ArrayList/java.util.ArrayList/[Ljava.lang.Object;")
    protected ArrayList<Object[]>[] name_value_pairs;
~~~

The problem here is with the first section, "[Ljava.util.ArrayList;", shouldn't been
specified here for it's the type of field which is already declared as a java type.
The component type of array is "java.util.ArrayList" (of "java.util.ArrayList/
[Ljava.lang.Object;").

## 4. Referencing loops

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

## 5. Inner class (java) can only been declared as static

This is because it's not easy to figure out the parent object to create the inner class instance.

For how inner class examples, see test case
[AnsonTest#test_innerClass()](https://github.com/odys-z/antson/blob/master/antson.java/src/test/java/io/odysz/anson/AnsonTest.java).

## 6. Java enum can't been constructed at runtime - needing register a factory

This is what java spec defined.

If a field is intend to be defined as an interface implemented with a java Enum,
it can't been deserialized as expected.

~~~
    Enum en implements IPort {
        ...
    }

    class Sample {
        // This field can't been deserialize
        IPort enPort;
        // use this instead
        en port;
    }
~~~

Field enPort can't be parsed correctly because no way in java to create an Enum
instance then cast to IPort.

Currently Antson need user register a call back to the parser (JSONAsonListener)
for enum type "en" to create the instance, with call to JSONAnsonListener#registFactory().

~~~
    public enum Port implements IPort {
        heartbeat("ping.serv11"), session("login.serv11"), dataset("ds.serv11");

        static {
            JSONAnsonListener.registFactory(IPort.class, (s) -> {
                    return Port.valueOf(s);
            });
        }
    }
~~~

See test case: [AnsonTest#test2Json4Enum](https://github.com/odys-z/antson/blob/master/antson.java/src/test/java/io/odysz/anson/AnsonTest.java) and the testing type AnsT4Enum.Port.

If you have any idea, please let the him know.
