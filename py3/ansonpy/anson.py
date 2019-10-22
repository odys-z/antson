# from ansonpy import AnsonException

class Anson():
    type = "io.odysz.anson.Anson";
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

        return "";
    
    def getClass(self):
        return self.type;
#         raise AnsonException(0, "Must override this method.");

