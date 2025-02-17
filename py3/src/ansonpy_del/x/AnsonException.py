
class AnsonException(BaseException):
    type = "io.odysz.ansons.x.AnsonException"
    excode = 0
    err = ""
    
    def __init__(self, excode, err):
        self.excode = excode
        self.err = err
