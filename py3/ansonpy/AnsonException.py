
class AnsonException(object):
    excode = 0;
    err = "";
    
    def __init__(self, excode, err):
        self.excode = excode;
        self.err = err;
