import inspect
from enum import Enum
from ansonpy import JSONListener

################################# From Json ##############################
class AnsonListener(JSONListener):
    an = None

    def enterJson(self, ctx):
        # print("Hello: %s" % ctx.envelope()[0].type_pair().TYPE())
        self.an = Anson()

################################## To Json ###############################

def writeVal(outstream, v):
    if (isinstance(v, str)):
        outstream.write("\"")
        outstream.write(v)
        outstream.write("\"")
    else:
        outstream.write(str(v))


class Anson():
    to_del = "some vale"
    to_del_int = 5
    
    def toBlock(self, outstream, opts):
        quotK = opts == None or opts.length == 0 or opts[0] == None or opts[0].quotKey();
        if (quotK == True):
            outstream.write("{\"type\": \"");
            outstream.write(self.getClass());
            outstream.write('\"');
        else :
            outstream.write("{type: ");
            outstream.write(self.getClass());
        
        for (n, v) in self.getFields():
            outstream.write(", ");
            if (quotK == True):
                outstream.write("\"%s\": " % n);
            else :
                outstream.write("%s: " % n);
            writeVal(outstream, v);

        outstream.write("}")
        return "";
    
    def getClass(self):
        return "io.odysz.anson.Anson"

    def getFields(self):
        env_dict = []
        for (name, att) in inspect.getmembers(self, lambda attr: not callable(attr) ):
            if (not name.startswith("__")):
                env_dict.append((name, att))
        return env_dict

class MsgCode(Enum):
    ok = "ok"
    exGeneral = "exGeneral"
    exSemantics = "exSemantics"
    exTransc = "exTransac"

class Port(Enum):
    session = "login.serv"
    r = "r.serv"

class AnsonMsg(Anson):
    # code = MsgCode.ok
    port = Port.session
    body = []

    def getClass(self):
        return "io.odysz.anson.AnsonMsg" # self.type;

class AnsonBody(Anson):
    pass

class AnsonReq(AnsonBody):
    a = None

class AnsonResp(AnsonBody):
    a = None