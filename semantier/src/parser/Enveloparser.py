from src.gen.JSONParser import JSONParser
from src.gen.JSONVisitor import JSONVisitor
from src.parser.AST import AST, ValueNode, Envelope


class Enveloparser(JSONVisitor):
    ast = AST()

    def visitType_pair(self, ctx: JSONParser.Type_pairContext):
        child_count = ctx.getChildCount()
        if child_count == 3:
            # _key = ctx.getChild(0).getText()
            etype = ctx.getChild(2).getText()
            self.ast.type(etype)
        return self.ast.env.type

    def visitPair(self, ctx: JSONParser.PairContext):
        child_count = ctx.getChildCount()
        if child_count == 3:
            key = ctx.getChild(0).getText()
            val = ctx.getChild(2).getText()
            self.ast.pair(key, val)
        return self.ast.env.fields[key]

    def visitArray(self, ctx: JSONParser.ArrayContext):
        return [ctx.getText()]

    def visitObj(self, ctx:JSONParser.ObjContext):
        return ValueNode(ctx.getChildren())

    def visitEnvelope(self, ctx: JSONParser.EnvelopeContext):
        self.ast.startEnvelope()
        for i in range(0, ctx.getChildCount(), 2):
            n = self.visit(ctx.getChild(i))
            print(n)
        return self.ast.env