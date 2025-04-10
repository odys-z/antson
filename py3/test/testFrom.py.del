# Window - Preferences - PyDev - Interpreters - Python Interpreter - Forced builtins - new... - antlr4
# https://stackoverflow.com/questions/2112715/how-do-i-fix-pydev-undefined-variable-from-import-errors
from antlr4 import *  #@UnusedWildImport

from src.ansonpy_del.JSONLexer import JSONLexer
from src.ansonpy_del.JSONParser import JSONParser

from src.ansonpy_del.anson import * #@UnusedWildImport
from unittest.case import TestCase

def parse(s):
    # lexer = JSONLexer(StdinStream())
    if (isinstance(s, FileStream)):
        ins = s
    else:
        ins = InputStream(s)
    lexer = JSONLexer(ins)
    stream = CommonTokenStream(lexer)
    parser = JSONParser(stream)
    tree = parser.json()
    printer = AnsonListener()
    walker = ParseTreeWalker()
    walker.walk(printer, tree)
    return printer.parsedEnvelope()

class test(TestCase):

    def test2AnsonMsg(self):
        # an = Anson();

        # s = "{\"type\": \"io.odysz.ansons.Anson\", \"to_del\": \"some vale\", \"to_del_int\": 5}"
        f = "json/01.json"
        an = parse(FileStream(f))
        self.assertEqual("io.odysz.ansons.Anson", an.type)

        s = "{\"type\": \"io.odysz.ansons.AnsonMsg\", \"body\": [], \"port\": Port.session, \"to_del\": \"some vale\", \"to_del_int\": 5}"
        an = parse(s)
        self.assertEqual("io.odysz.ansons.AnsonMsg", an.type)

