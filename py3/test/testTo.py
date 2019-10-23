# Window - Preferences - PyDev - Interpreters - Python Interpreter - Forced builtins - new... - antlr4
# https://stackoverflow.com/questions/2112715/how-do-i-fix-pydev-undefined-variable-from-import-errors
from antlr4 import *  #@UnusedWildImport

from ansonpy.JSONLexer import JSONLexer
from ansonpy.JSONListener import JSONListener
from ansonpy.JSONParser import JSONParser

from io import StringIO
import sys #@UnusedImport

from ansonpy.anson import Anson, AnsonMsg
from unittest.case import TestCase


class AnsonListener(JSONListener):
    an = None

    def enterJson(self, ctx):
        # print("Hello: %s" % ctx.envelope()[0].type_pair().TYPE())
        self.an = Anson()

def parse():
    lexer = JSONLexer(StdinStream())
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

        s = "{\"type\": \"io.odysz.anson.Anson\", \"to_del\": \"some vale\", \"to_del_int\": 5}"
        an = parse(s)
        self.assertEqual("io.odysz.anson.Anson", an.type)

