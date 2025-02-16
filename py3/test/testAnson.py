'''
A temporary solution without LL* parser.

For testing another cheap way of deserialize JSON in python, without Antlr4,
unless the stream mode is critical.

- Semantics consistence with Java is to be verified.

- No need for generating Python3 source code from JSON ?

'''
from dataclasses import dataclass, field
from typing import Any

from ansons.anson import Anson
from testier.extra import ExtraData


# https://colab.research.google.com/drive/1pqeZGfqdEl_kOlJQ76SCeuKTtD3NGlev


@dataclass
class MyDataClass(Anson):
    name: str
    age: int
    extra: ExtraData
    items: list[Any]  # = field(default_factory=list)

    def __init__(self, name: str, age: int):
        super().__init__()
        self.extra = ExtraData()
        self.name = name
        self.age = age
        self.items = ['']  # field(default_factory=list)


foo = MyDataClass('Trump', 78)
foo.extra.l = ['']
print(f'{foo.extra.__module__}.{foo.__class__.__name__}')

print(foo.fields())

my = MyDataClass('zz', 12)
mytype = type(my)
print(my.toBlock())

your = mytype('yy', 13)
print(your.toBlock())

jsonstr = '{"name": "zzz", "extra": {"s": "sss", "i": 1, "l": 2, "d": {"u": "uuu"}}}'
his = Anson.from_json(jsonstr)
print(his.name)
