# Generated from JSON.parser by ANTLR 4.13.1
from antlr4 import *
if "." in __name__:
    from .JSONParser import JSONParser
else:
    from JSONParser import JSONParser

# This class defines a complete generic visitor for a parse tree produced by JSONParser.

class JSONVisitor(ParseTreeVisitor):

    # Visit a parse tree produced by JSONParser#json.
    def visitJson(self, ctx:JSONParser.JsonContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by JSONParser#envelope.
    def visitEnvelope(self, ctx:JSONParser.EnvelopeContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by JSONParser#obj.
    def visitObj(self, ctx:JSONParser.ObjContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by JSONParser#type_pair.
    def visitType_pair(self, ctx:JSONParser.Type_pairContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by JSONParser#qualifiedName.
    def visitQualifiedName(self, ctx:JSONParser.QualifiedNameContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by JSONParser#pair.
    def visitPair(self, ctx:JSONParser.PairContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by JSONParser#propname.
    def visitPropname(self, ctx:JSONParser.PropnameContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by JSONParser#array.
    def visitArray(self, ctx:JSONParser.ArrayContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by JSONParser#value.
    def visitValue(self, ctx:JSONParser.ValueContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by JSONParser#type.
    def visitType(self, ctx:JSONParser.TypeContext):
        return self.visitChildren(ctx)



del JSONParser