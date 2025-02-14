'''
A temporary solution without LL* parser.

For testing another cheap way of deserialize JSON in python, without Antlr4,
unless the stream mode is critical.

- Semantics consistence with Java is to be verified.

- No need for generating Python3 source code from JSON ?

'''
from numbers import Number

# https://colab.research.google.com/drive/1pqeZGfqdEl_kOlJQ76SCeuKTtD3NGlev
import json
from dataclasses import dataclass, field, fields, MISSING
from typing import Any, TypeVar

TAnson = TypeVar('TAnson', bound='Anson')


@dataclass
class Anson(dict):
    ''''
    Tests:
    class Anson(object):
        def __setitem__(self, key, value):
            self.__dict__[key] = value

        def __getitem__(self, item):
            return self.__dict__[item]

        def print(self):
            for k in self.__dict__:
                print(f"{k}: {self[k]}")

    from attr import dataclass
    @dataclass
    class Anson1(Anson):
        x: str = None

    a1 = Anson1()
    a1.x = 5
    print(a1)
    Anson1(x=5)
    a1.print()
    x: 5
    a1.v = "xxxx"
    a1.print()
    x: 5
    v: xxxx
    print(a1)
    Anson1(x=5)
    a1.t = "tttt"
    print(a1)
    Anson1(x=5)
    a1.print()
    x: 5
    v: xxxx
    t: tttt
    '''
    __type__: str# = type(TAnson)

    def __init__(self):
        super().__init__()
        self.__type__ = type(self)

    def __setitem__(self, key, value):
        self.__dict__[key] = value

    def __getitem__(self, key):
        return self.__dict__[key]

    @dataclass()
    class Trumpfield():
        '''
        {name, type, isAnson, antype, factory}
        '''
        name: str
        type: type
        isAnson: bool
        antype: str
        factory: any

    def fields(self) ->  dict[str, Trumpfield]:
        return {it.name: Anson.Trumpfield (
                name = it.name, type = it.type,
                isAnson = issubclass(it.type, Anson) or isinstance(it.type, Anson),
                antype = 'str' if isinstance(it.type, str) else 'num' if issubclass(it.type, Number) else 'obj',
                factory = None if it.default_factory is MISSING else it.default_factory
        ) for it in fields(type(self))}

    def toBlock(self, ind = 0) -> str:
        fields = self.fields()
        s = '{\n'
        l = len(self.__dict__) - 1
        for x, k in enumerate(self.__dict__):
            s += ' ' * (ind*2) + f'"{k:<}": '
            s += f'"{self[k]}"' if isinstance(self[k], str)\
                else self[k].toBlock(ind + 1) if fields[k].isAnson\
                else str(self[k])
            s += ',\n' if x == l else '\n'
        return s + '}'

    @staticmethod
    def from_obj(obj: dict) -> TAnson:
        anson = Anson()
        for k in obj:
            anson[k] = obj[k]
        return anson

    @staticmethod
    def from_json(jsonstr: str) -> TAnson:
        obj = json.loads(jsonstr)
        return Anson.from_obj(obj)

@dataclass
class ExtraData(Anson):
    s: str = None
    i: int = 0
    l: list = field(default_factory=list)
    d: dict = field(default_factory=dict)

    def __int__(self):
        super().__init__()

@dataclass
class MyDataClass(Anson):
    name: str
    age: int
    extra: ExtraData
    # items: list[Any] = field(default_factory=list)

    def __init__(self, name: str, age: int):
      super().__init__()
      self.extra = ExtraData()
      self.name = name
      self.age = age
 

foo = MyDataClass('Trump', 78)
print(foo.fields())

my = MyDataClass('zz', 12)
mytype = type(my)
print(my.toBlock())

your = mytype('yy', 13)
print(your.toBlock())

jsonstr = ""
his = Anson.from_json(jsonstr)
print(his.name)