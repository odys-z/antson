'''
A temporary solution without LL* parser.

For testing another cheap way of deserialize JSON in python, without Antlr4,
unless the stream mode is critical.

- Semantics consistence with Java is to be verified.

- No need for generating Python3 source code from JSON ?

'''
from dataclasses import dataclass
from typing import Any

from src.anson.io.odysz.ansons import Anson
from testier.extra import ExtraData


# https://colab.research.google.com/drive/1pqeZGfqdEl_kOlJQ76SCeuKTtD3NGlev


@dataclass
class MyDataClass(Anson):
    name: str
    age: int
    extra: ExtraData
    items: list[Any]  # = field(default_factory=list)

    def __init__(self, name: str = '', age: int = '0'):
        super().__init__()
        self.extra = ExtraData()
        self.name = name
        self.age = age
        self.items = ['']  # field(default_factory=list)


foo = MyDataClass('Trump', 78)
foo.extra.l = ['']
print(f'{foo.extra.__module__}.{foo.__class__.__name__}')

print(Anson.fields(foo))

my = MyDataClass('zz', 12)
mytype = type(my)
print(my.toBlock())

your = mytype('yy', 13)
print(your.toBlock())

jsonstr = '{"type": "__main__.MyDataClass", "name": "Trump", "age": 78, "extra": {"s": "sss", "i": 1, "l": 2, "d": {"u": "uuu"}}}'
his = Anson.from_json(jsonstr)
print(his.name)
print(his)

jsonstr = '{\
  "type": "__main__.MyDataClass",\
  "extra": {\
    "s": null,\
    "i": 0,\
    "l": ["a", 2],\
    "d": {}\
  },\
  "name": "zz",\
  "age": 12,\
  "items": ['']\
}'
her = Anson.from_json(jsonstr)
print(her.name, type(her))
print(her.toBlock())