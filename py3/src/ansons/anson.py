import importlib
from dataclasses import dataclass, fields, MISSING, Field

import json
from numbers import Number
from typing import TypeVar, List, Dict, get_origin

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
    __type__: str  # = 'ansons.anson.Anson'

    def __init__(self):
        super().__init__()
        # self.__type__ = type(self) #'ansons.anson.Anson'
        t = type(self)
        self.__type__ = f'{t.__module__}.{t.__name__}'

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
        origintype: type
        isAnson: bool
        antype: str
        factory: any

    @staticmethod
    def fields(instance) -> dict[str, Trumpfield]:
        # for it in fields(type(self)):
        #     ot = get_origin(it.type)
        #     print(it.name, ot)
        #     # print(it.type == str, isinstance(it.type, type) and issubclass(it.type, Number), ot == list, ot == dict)
        #     print(it.type)
        #     print(it)

        # fields(type(self))
        _FIELDS = '__dataclass_fields__' # see dataclasses.fields()
        fds = getattr(type(instance), _FIELDS)
        def toTrump(fn: Field):
            f = fds[fn]
            ot = get_origin(f.type)
            isAnson = issubclass(f.type, Anson) or isinstance(f.type, Anson)
            return Anson.Trumpfield(
                f.name, f.type,
                ot,
                isAnson,
                'str' if f.type == str
                      else 'lst' if ot == list
                      else 'dic' if ot == dict
                      else 'num' if ot is None and issubclass(f.type, Number)
                      else f'{f.type.__module__}.{f.type.__name__}' if isAnson
                      else 'obj',
                None if f.default_factory is MISSING else f.default_factory)

        return {it: toTrump(it) for it in fds}

        '''
        return {it[0].name: Anson.Trumpfield(
            name=it[0].name, type=it[0].type,
            isAnson=issubclass(it[0].type, Anson) or isinstance(it[0].type, Anson),
            antype='str' if it[0].type == str
            else 'lst' if it[1] == list
            else 'dic' if it[1] == dict
            else 'num' if it[1] is None and issubclass(it.type, Number) else 'obj',
            factory=None if it[0].default_factory is MISSING else it[0].default_factory
        )  # for it in list(map(lambda f: (f, get_origin(f.type)), fields(type(self))))}
            for it in list(map(toTrump, fields(type(self))))}
        '''

    def toBlock(self, ind=0) -> str:
        myfields = self.fields(self)
        s = '{\n'
        lx = len(self.__dict__) - 1
        for x, k in enumerate(self.__dict__):
            s += ' ' * (ind * 2 + 2) + f'"{k:<}": '
            v = self[k]
            s += 'null' if v is None \
                else f'"{v}"' if isinstance(v, str) \
                else self[k].toBlock(ind + 1) if myfields[k].isAnson \
                else str(self[k])
            s += ',\n' if x != lx else '\n'
        return s + '}'

    @staticmethod
    def from_obj(obj: dict, typename: str = None) -> TAnson:
        def getClass(_typ_: str):
            parts = _typ_.split('.')
            module = ".".join(parts[:-1])
            m = __import__(module if module is not None else '__main__')
            for comp in parts[1:]:
                m = getattr(m, comp)
            return m

        # module3 = __import__('__main__')
        # module2 = importlib.import_module('__main__')
        # class_ = getattr(module2, 'MyDataClass')
        # anson = class_()

        anson = getClass(typename if typename is not None else obj['__type__'])()

        thefields = Anson.fields(anson)

        for k in obj:
            anson[k] = Anson.from_obj(obj[k], thefields[k].antype) if thefields[k].isAnson else obj[k]
        return anson

    @staticmethod
    def from_json(jsonstr: str) -> TAnson:
        obj = json.loads(jsonstr)
        v = Anson.from_obj(obj)
        print(v, type(v))
        return v
