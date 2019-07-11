// Original from https://raw.githubusercontent.com/antlr/grammars-v4/master/json/JSON.g4

/** Taken from "The Definitive ANTLR 4 Reference" by Terence Parr */

// Derived from http://json.org
grammar JSON;

json
   : value
   ;

obj
// ody  : '{' pair (',' pair)* '}'
   : '{' type_pair (',' pair)* '}'
   | '{' '}'
   ;

// section: type extension
// ody: e.g. type : [(java.lang.)String]
// for qualifiedName, see Antlr4 java grammar:
// https://github.com/antlr/grammars-v4/blob/master/java/JavaParser.g4
type_pair
   : TYPE ':' ('[' qualifiedName ']' | qualifiedName)
   ;

qualifiedName
    : IDENTIFIER ('.' IDENTIFIER)*
    ;
// section end

pair
   : STRING ':' value
   ;

array
   : '[' value (',' value)* ']'
   | '[' ']'
   ;

value
   : STRING
   | NUMBER
   | obj
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


// ody: for grammar, see
// section: type name extension
// https://github.com/antlr/grammars-v4/blob/master/java/JavaLexer.g4
IDENTIFIER:         Letter LetterOrDigit*;

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
