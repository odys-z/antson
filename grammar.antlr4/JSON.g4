/**
 * Original from https://raw.githubusercontent.com/antlr/grammars-v4/master/json/JSON.g4
 *
 * Taken from "The Definitive ANTLR 4 Reference" by Terence Parr
 *
 * java -jar ~/antlr4/antlr-4.7.1-complete.jar JSON.g4 -package gen.antlr.json
 */

// Derived from http://json.org
grammar JSON;

// ody:
// Used for dividing large data blocks - antlr don't work in stream mode.
// This rule can not been handlered by antlr parser, but by BlockParser.
// see https://groups.google.com/forum/#!msg/antlr-discussion/B06iG6Dht6w/S9m7IlvMslgJ
// block:
//    : json
//    : json '{' '}' block
//    ;


// ody:
// Type must presented in evelope
// json
// 	: value
// 	;
json
	: envelope (',' envelope)*
	;

envelope // top obj
	: '{' type_pair (',' pair)* '}'
	;

obj
	: '{' pair (',' pair)* '}'
	| '{' '}'
	;

// section: type extension
// ody: e.g. type : [(java.lang.)String]
// for qualifiedName, see Antlr4 java grammar:
// https://github.com/antlr/grammars-v4/blob/master/java/JavaParser.g4
type_pair
	// Type can't specified as a list because it's a type_pair also a property of object {}.
	// Object can not be an array.
	// : TYPE ':' ('[' qualifiedName ']' | qualifiedName)	// [] means it's a list
	: TYPE ':' qualifiedName
	;

qualifiedName
	// IDENTIFIER doesn't support '$', that means not enclosed classes are supported.
    : IDENTIFIER ('.' IDENTIFIER)*
    ;
// section end

// Ody: tolerate property without quote
// pair
//    : STRING ':' value
//    ;
pair
	: propname ':' value
	;

propname
	: STRING
	| IDENTIFIER
	;

array
	: '[' value (',' value)* ']'
	| '[' ']'
	;

value
	: STRING
	| NUMBER
	| obj		// all array's obj value can't parsed as Anson, taken as HashMap - TODO doc: known issue
	| envelope
	| array
	| 'true'
	| 'false'
	| 'null'
	;

STRING
	: '"' (ESC | SAFECODEPOINT)* '"'
	;


fragment ESC
	: '\\' (["\\/bfnrt] | UNICODE)
	;


fragment UNICODE
	: 'u' HEX HEX HEX HEX
	;

fragment HEX
	: [0-9a-fA-F]
	;

fragment SAFECODEPOINT
	: ~ ["\\\u0000-\u001F]
	;


NUMBER
	: '-'? INT ('.' [0-9] +)? EXP?
	;


fragment INT
	: '0' | [1-9] [0-9]*
	;

// no leading zeros

fragment EXP
	: [Ee] [+\-]? INT
	;

// \- since - means "range" inside [...]


// section: type name extension
// ody: for grammar, see
// https://github.com/antlr/grammars-v4/blob/master/java/JavaLexer.g4
TYPE
	: 'TYPE' | 'type'
	;

IDENTIFIER
	:         Letter LetterOrDigit*;

fragment LetterOrDigit
	: Letter
	| [0-9]
	;

fragment Letter
	: [a-zA-Z] // these are the "java letters" below 0x7F
//  | ~[\u0000-\u007F\uD800-\uDBFF] // covers all characters above 0x7F which are not a surrogate
//  | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
	;
// section end

WS
	: [ \t\n\r] + -> skip
	;
