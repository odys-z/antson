from dataclasses import dataclass, field

from src.anson.io.odysz.anson import Anson


@dataclass
class ExtraData(Anson):
    s: str# = None
    i: int# = 0
    l: list# = field(default_factory=list)
    d: dict# = field(default_factory=dict)

    def __init__(self):
        super().__init__()
        self.s = None
        self.i = 0
        self.l = field(default_factory=list)
        self.d = field(default_factory=dict)

