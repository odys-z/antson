class ValueNode:
    val = None

    def __int__(self, values):
        # print(values)
        self.val = values


class Envelope:
    def __init__(self):
        self.type = None
        self.fields = {}


class AST:
    def __init__(self):
        self.env = None

    def startEnvelope(self):
        self.env = Envelope()

    def type(self, etype: str):
        self.env.type = etype

    def pair(self, fname: str, val: any):
        self.env.fields[fname] = ValueNode()
        return self
