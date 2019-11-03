# Window - Preferences - PyDev - Interpreters - Python Interpreter - Forced builtins - new... - antlr4
# https://stackoverflow.com/questions/2112715/how-do-i-fix-pydev-undefined-variable-from-import-errors
from antlr4 import * #@UnusedWildImport

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

def tryTypes():
    s = list()
    l = list()
    l.append('s')
    l.append('abc')
    l.append(1)
    s.append(l)

    t = list()
    t.append('t')
    t.append('xyz')
    t.append(t)
    s.append(t)

    print(s)
    
    m = {}
    m[1] = "1"
    print(m)

if __name__ == '__main__':
    main()
#     tryTypes()
