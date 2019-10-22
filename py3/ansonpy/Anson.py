# from ansonpy import AnsonException

class Anson():
    className = "io.odysz.anson.Anson";
    
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
        return self.className;
#         raise AnsonException(0, "Must override this method.");

