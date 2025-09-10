from semanticshare.io.odysz.semantic.jprotocol import AnsonBody, AnsonMsg



class EchoReq(AnsonBody):
    class A:
        echo = "echo"
        inet = "inet"

    def __init__(self, parent: AnsonMsg = None):
        super().__init__(parent)
