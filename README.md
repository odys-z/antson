# What's Antson

Antson is ANother Tool for JSON (de)serialize java object to / from json.

# Why Antson?

For short, there is no such tool / lib that can convert javascript object to or
from java or other programming language like C#.

Why? Because js object has not type. You can not define a "com.app.User" type in
js. All js objects are only a HashMap for Java. Any json data are converted into
java.lang.Map in java world traditionally. There are a lot of tools helping to do
this. For java, json is data with out type, this is every upsetting to java programmers,
at least to the author.

In many cases, only HashMap structure is not enought. Take an SOA architecture for example,
protocol packages needing a way to tell the reciever how to deserialize the package.
Servers needing to know how the request been parsed, even finding a parser - like the
portal of a SOAP server.

[Gson](https://github.com/google/gson) is a good try to go further. But the
method is still not enough. The main weakness is it's not smart enough. Every translation
of json data to Java object needing user implement business handlers, one problem a time.

Antson is trying to go further. Users only needing to define their business gramma -
the application layer protocol, then send packages back and forth with the help of Antson API,
consuming the data object like normal Java object, and only caring about it's business processing.

# Why no Stream mode?

Antson provid output stream writing API, and deserializing only json string, no input
stream mode is supported.

The reason behind this is that Antson is based on Antlr, is a LL(\*) parsing tool.

If your json data is large, try breack it into small chunks, or may be let it work
as a Karfka message consummer?
