import sys
from antlr4 import *
from gen.ExprLexer import ExprLexer
from gen.ExprParser import ExprParser
from VisitorInterp import VisitorInterp


def main(argv):
    print(argv[1])
    input_stream = FileStream(argv[1])
    lexer = ExprLexer(input_stream)
    stream = CommonTokenStream(lexer)
    parser = ExprParser(stream)
    tree = parser.start_()
    if parser.getNumberOfSyntaxErrors() > 0:
        print("syntax errors")
    else:
        vinterp = VisitorInterp()
        vinterp.visit(tree)


if __name__ == '__main__':
    main(sys.argv)
