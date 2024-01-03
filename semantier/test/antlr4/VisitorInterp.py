from gen.ExprParser import ExprParser
from gen.ExprVisitor import ExprVisitor


class VisitorInterp(ExprVisitor):
    def visitAtom(self, ctx: ExprParser.AtomContext):
        return int(ctx.getText())

    def visitExpr(self, ctx: ExprParser.ExprContext):
        childcnt = ctx.getChildCount()
        if childcnt == 3:
            if ctx.getChild(0).getText() == "(":
                return self.visit(ctx.getChild(1))
            op = ctx.getChild(1).getText()
            v1 = self.visit(ctx.getChild(0))
            v2 = self.visit(ctx.getChild(2))
            print(f"visit 3 op expr -- {v1} {op} {v2}")
            if op == "+":
                return v1 + v2
            if op == "-":
                return v1 - v2
            if op == "*":
                return v1 * v2
            if op == "/":
                return v1 / v2
            if op == "**":
                return v1 ** v2
            return 0
        if childcnt == 2:
            opc = ctx.getChild(0).getText()
            if opc == "+":
                v = self.visit(ctx.getChild(1))
                print(f"visit 2 op expr -- {opc}{v}")
                return self.visit(ctx.getChild(1))
            if opc == "-":
                # return - self.visit(ctx.getChild(1))
                v = self.visit(ctx.getChild(1))
                print(f"visit 2 op expr -- {opc}{v}")
                return -v
            return 0
        if childcnt == 1:
            v = self.visit(ctx.getChild(0))
            print(f"visit 1 op expr -- {v}")
            return self.visit(ctx.getChild(0))
        return 0

    def visitStart_(self, ctx: ExprParser.Start_Context):
        for i in range(0, ctx.getChildCount(), 2):
            print(self.visit(ctx.getChild(i)))
        return 0
