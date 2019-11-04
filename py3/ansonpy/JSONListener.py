# Generated from JSON.g4 by ANTLR 4.7.2
from antlr4 import * # @UnusedWildImport
if __name__ is not None and "." in __name__:
    from .JSONParser import JSONParser
# else:
#     from JSONParser import JSONParser

# This class defines a complete listener for a parse tree produced by JSONParser.
class JSONListener(ParseTreeListener):

    # Enter a parse tree produced by JSONParser#json.
    def enterJson(self, ctx:JSONParser.JsonContext):
        pass

    # Exit a parse tree produced by JSONParser#json.
    def exitJson(self, ctx:JSONParser.JsonContext):
        pass


    # Enter a parse tree produced by JSONParser#envelope.
    def enterEnvelope(self, ctx:JSONParser.EnvelopeContext):
        pass

    # Exit a parse tree produced by JSONParser#envelope.
    def exitEnvelope(self, ctx:JSONParser.EnvelopeContext):
        pass


    # Enter a parse tree produced by JSONParser#obj.
    def enterObj(self, ctx:JSONParser.ObjContext):
        pass

    # Exit a parse tree produced by JSONParser#obj.
    def exitObj(self, ctx:JSONParser.ObjContext):
        pass


    # Enter a parse tree produced by JSONParser#type_pair.
    def enterType_pair(self, ctx:JSONParser.Type_pairContext):
        pass

    # Exit a parse tree produced by JSONParser#type_pair.
    def exitType_pair(self, ctx:JSONParser.Type_pairContext):
        pass


    # Enter a parse tree produced by JSONParser#qualifiedName.
    def enterQualifiedName(self, ctx:JSONParser.QualifiedNameContext):
        pass

    # Exit a parse tree produced by JSONParser#qualifiedName.
    def exitQualifiedName(self, ctx:JSONParser.QualifiedNameContext):
        pass


    # Enter a parse tree produced by JSONParser#pair.
    def enterPair(self, ctx:JSONParser.PairContext):
        pass

    # Exit a parse tree produced by JSONParser#pair.
    def exitPair(self, ctx:JSONParser.PairContext):
        pass


    # Enter a parse tree produced by JSONParser#propname.
    def enterPropname(self, ctx:JSONParser.PropnameContext):
        pass

    # Exit a parse tree produced by JSONParser#propname.
    def exitPropname(self, ctx:JSONParser.PropnameContext):
        pass


    # Enter a parse tree produced by JSONParser#array.
    def enterArray(self, ctx:JSONParser.ArrayContext):
        pass

    # Exit a parse tree produced by JSONParser#array.
    def exitArray(self, ctx:JSONParser.ArrayContext):
        pass


    # Enter a parse tree produced by JSONParser#value.
    def enterValue(self, ctx:JSONParser.ValueContext):
        pass

    # Exit a parse tree produced by JSONParser#value.
    def exitValue(self, ctx:JSONParser.ValueContext):
        pass


