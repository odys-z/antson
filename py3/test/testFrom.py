# Window - Preferences - PyDev - Interpreters - Python Interpreter - Forced builtins - new... - antlr4
# https://stackoverflow.com/questions/2112715/how-do-i-fix-pydev-undefined-variable-from-import-errors
from antlr4 import *  #@UnusedWildImport

from ansonpy.JSONLexer import JSONLexer
from ansonpy.JSONParser import JSONParser

from ansonpy.anson import * #@UnusedWildImport
from unittest.case import TestCase

def parse(s):
    # lexer = JSONLexer(StdinStream())
    if (isinstance(s, FileStream)):
        ins = s
    else:
        ins = FileStream(s)
    lexer = JSONLexer(ins)
    stream = CommonTokenStream(lexer)
    parser = JSONParser(stream)
    tree = parser.json()
    printer = AnsonListener()
    walker = ParseTreeWalker()
    walker.walk(printer, tree)
    return printer.an

class test(TestCase):

    def test2AnsonMsg(self):
        # an = Anson();

        # s = "{\"type\": \"io.odysz.anson.Anson\", \"to_del\": \"some vale\", \"to_del_int\": 5}"
        f = "json/01.json"
        an = parse(FileStream(f))
        self.assertEqual("io.odysz.anson.Anson", an.type)

        s = "{\"type\": \"io.odysz.anson.AnsonMsg\", \"body\": [], \"port\": Port.session, \"to_del\": \"some vale\", \"to_del_int\": 5}"
        an = parse(s)
        self.assertEqual("io.odysz.anson.AnsonMsg", an.type)

