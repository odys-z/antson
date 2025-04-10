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

# def get_env_vars(an):
#     env_dict = {}
# #     for name in dir(an):
# #         attr = getattr(an, name)
# # 
# # #         if isinstance(attr, str):
# # #             env_dict[name] = attr
# #         if inspect.ismemberdescriptor(attr):
# #             env_dict[name] = "%s %s" % (attr, attr.__class__)
# #     for (name, att) in inspect.getmembers(an, lambda attr: not callable(attr) and not attr.name.startswith("__")):
#     for (name, att) in inspect.getmembers(an, lambda attr: not callable(attr) ):
#         if (not name.startswith("__")):
#             env_dict[name] = "%s %s = %s" % (name, att.__class__, att)
#         
# 
#     return env_dict

class test(TestCase):

    def testFromAnsonMsg(self):
        an = Anson();
        print(type(an));
        print(dir(an));
        
        s = StringIO()
        an.toBlock(s, None)
        # print("------- json -------\n", s.getvalue())
        self.assertEqual("{\"type\": \"io.odysz.ansons.Anson\", \"to_del\": \"some vale\", \"to_del_int\": 5}",
                         s.getvalue(), "deserializing AnsonMsg failed.")

        msg = AnsonMsg();
        s = StringIO()
        msg.toBlock(s, None)
        # print("------- json -------\n", s.getvalue())

        self.assertEqual("{\"type\": \"io.odysz.ansons.AnsonMsg\", \"body\": [], \"port\": Port.session, \"to_del\": \"some vale\", \"to_del_int\": 5}",
                         s.getvalue(), "deserializing AnsonMsg failed.")
