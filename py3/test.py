from antlr4 import *  #@UnusedWildImport
from ansonpy.JSONLexer import JSONLexer
from ansonpy.JSONListener import JSONListener
from ansonpy.JSONParser import JSONParser
import sys #@UnusedImport

class JSONPrintListener(JSONListener):
    def enterJson(self, ctx):
        print("Hello: %s" % ctx.envelope()[0].type_pair().TYPE())

def main():
    lexer = JSONLexer(StdinStream())
    stream = CommonTokenStream(lexer)
    parser = JSONParser(stream)
    tree = parser.json()
    printer = JSONPrintListener()
    walker = ParseTreeWalker()
    walker.walk(printer, tree)

if __name__ == '__main__':
    main()
