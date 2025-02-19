
class AnsonException:
    type = "io.odysz.ansons.x.AnsonException"
    excode = 0
    err = ""

    def __init__(self, excode: int, template: str, *param: object):
        super().__init__()
        self.excode = excode
        self.err = template if param is None else template.format(param)
