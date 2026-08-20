from dataclasses import dataclass
from anson.io.odysz.anson import Anson

@dataclass
class SemanticException (Exception):
    type = 'io.odysz.semantic.x.SemanticException'
    msg: str

    def __init__(self, ex: str = ''):
        super().__init__()
        msg = ex
