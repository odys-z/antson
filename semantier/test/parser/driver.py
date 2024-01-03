from antlr4 import *

from src.gen.JSONLexer import JSONLexer
from src.gen.JSONParser import JSONParser
from src.parser.Enveloparser import Enveloparser


def main():
    input_stream = FileStream('./test.json')
    lexer = JSONLexer(input_stream)
    stream = CommonTokenStream(lexer)
    parser = JSONParser(stream)
    tree = parser.envelope()
    if parser.getNumberOfSyntaxErrors() > 0:
        print("syntax errors")
    else:
        parser = Enveloparser()
        x = parser.visit(tree)
        print(x.type, x.fields)


if __name__ == '__main__':
    main()
