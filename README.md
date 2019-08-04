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
