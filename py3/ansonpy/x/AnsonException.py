
class AnsonException(object):
    type = "io.odysz.anson.x.AnsonException"
    excode = 0
    err = ""
    
    def __init__(self, excode, err):
        self.excode = excode
        self.err = err
