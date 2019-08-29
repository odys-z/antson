# What's Antson

Antson is ANother Tool for JSON (de)serialize java object to / from json.

# Why Antson?

For short, there is no such tool / lib that can convert javascript object to or
from java or other programming language like C#.

Why? Because js object has no types. You can not define a "io.app.User" type in
js. All js objects are only a HashMap for Java. Any json data are converted into
java.lang.Map in java world traditionally. There are a lot of tools helping to do
this. For java, json is data with out type, this is every upsetting to java programmers,
at least to the author.

In many cases, only HashMap structure is not enought. Take an SOA architecture for example,
protocol packages needing a way to tell the reciever how to deserialize the package.
Servers needing to know how the request been parsed, even finding a parser - like find the
correct mothod for a [SOAP Envelope](https://en.wikipedia.org/wiki/SOAP#Example_message_(encapsulated_in_HTTP)).

[Gson](https://github.com/google/gson) is a good try to go further. But the
method is still not enough. The main weakness is it's not smart enough - it doesn't
handle it java fields with type information and (de)serialize back forth. Every Gson translation
of json data to Java object needing user implement business handlers, one problem a time.

Antson is trying to go further. Users only needing to define their business gramma -
the application layer protocol, then send packages back and forth with the help of Antson API,
consuming the data object like normal Java objects, and only take care of it's business processing.

If you are comfort with MS .net webservice framework, you are not supprised with this idea.

# Why no Stream Mode?

Antson provid only output stream writing API, and deserializing json string, no input
stream mode is supported.

The reason behind this is that Antson is based on Antlr, it's an LL(\*) parsing tool.

If your json data is large, try breack it into small chunks, or may be let it work
as a [Karfka](https://kafka.apache.org/intro) message consummer - might try a test in the future.

# What's it for?

Antson will be used as the transport protocol layer for semantic-\*. see his home page for it.

It's also planned publish it as an independent lib, at least with jar and DLL.

# Known Issues

## Array element's type must specified

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
	| obj		// all array's obj value can't parsed as Anson, taken as HashMap
	| envelope
	| array
	| 'true'
	| 'false'
	| 'null'
	;

The difference between envelope and obj is evelope must has a type-pair:

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

    class Outter {
        Inner[] arr; // element's can be subclasses
    }
