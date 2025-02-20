import importlib
from dataclasses import dataclass, fields, MISSING, Field

import json
from numbers import Number
from typing import TypeVar, List, Dict, get_origin, get_args, ForwardRef, Type, Any, Union

from .common import Utils

TAnson = TypeVar('TAnson', bound='Anson')

java_src_path: str = ''

@dataclass
class Anson(dict):
    enclosinguardtypes = set()
    
    __type__: str  # = 'ansons.antson.Anson'

    def __init__(self):
        super().__init__()
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
        fieldtype: type
        origintype: type
        isAnson: bool
        elemtype: type
        antype: str
        factory: any

    @staticmethod
    def fields(instance) -> dict[str, Trumpfield]:
        _FIELDS = '__dataclass_fields__' # see dataclasses.fields()
        fds = getattr(type(instance), _FIELDS)

        def figureNormalType(f: Field) -> tuple:
            try: isAnson = issubclass(f.type, Anson) or isinstance(f.type, Anson)
            except: isAnson = False
            return f, get_origin(f.type), get_args(f.type), isAnson

        def figure_list(f: Field, guardTypes: set[Anson]):
            if not isinstance(f.type, list):
                raise Exception("Not here")

            ot = list

            try:
                et = f.type[0].__bound__ if len(f.type[0]) > 0 and isinstance(f.type[0], TypeVar) else get_args(f.type)
                et = et.__evaluate(globals(), locals(), recursive_guard=guardTypes) if isinstance(et, ForwardRef) else et
                et = (et)
            except: et = ()
            return f, ot, et, False

        def figure_dict(f: Field, envType: set[Anson]) -> tuple:
            pass

        def toTrump(fn: Field):
            """
            Simply & brutally figuring types. We only care about Anson types, the exceptional.
            :param fn:
            :return: TrumpField
            """
            f = fds[fn]
            if isinstance(f.type, List):
                f, ot, et, isAnson = figure_list(f, Anson.enclosinguardtypes)
            elif isinstance(f.type, Dict):
                f, ot, et, isAnson = figure_dict(f, Anson.enclosinguardtypes)
            else:
                f, ot, et, isAnson = figureNormalType(f)
            # ot, et = get_origin(f.type), get_args(f.type)
            #
            # try: isAnson = issubclass(f.type, Anson) or isinstance(f.type, Anson)
            # except: isAnson = False

            return Anson.Trumpfield(
                f.name, f.type,
                ot, isAnson,
                None if et is None or len(et) == 0 else et[0],
                'str' if f.type == str
                      else 'lst' if ot == list
                      else 'dic' if ot == dict
                      else 'num' if ot is None and issubclass(f.type, Number)
                      else f.type if isAnson
                      else 'obj',
                None if f.default_factory is MISSING else f.default_factory)

        return {it: toTrump(it) for it in fds}

    @staticmethod
    def toList_(lst: list, elemtype: type, ind: int):
        if elemtype is None or not issubclass(elemtype, Anson): return str(lst)
        return '[\n' + ','.join([Anson.toBlock_(e, ind + 1) for e in lst]) + ']'

    @staticmethod
    def toDict_(dic: dict, elemtype: type, ind: int):
        if elemtype is None or not issubclass(elemtype, Anson): return json.dumps(dic)
        return '{\n' + ',\n'.join(' ' * (ind * 2 + 2) + Anson.toBlock_(dic[k], ind + 1) for k in dic) + ']'

    def toBlock(self) -> str:
        return self.toBlock_(0)

    def toFile(self, path: str):
        with open(path, 'w+') as jf:
            jf.write(self.toBlock())

    def toBlock_(self, ind: int) -> str:
        myfds = self.fields(self)
        s = ' ' * (ind * 2) + '{\n'
        lx = len(self.__dict__) - 1
        for x, k in enumerate(self.__dict__):
            if '__type__' == k:
                if ind == 0:
                    tp = str(self['__type__']).removeprefix(java_src_path+'.')
                    s += f'  "type": "{tp}"'
                else: continue # later can figure out type by field's type
            else:
                if k not in myfds:
                    Utils.warn("Field {0} not defined in Anson presents in data object, value ignored: {1}.", k, self[k])
                    continue
                s += f'{" " * (ind * 2 + 2)}"{k}": '
                v = self[k]
                s += 'null' if v is None or isinstance(v, Field) \
                    else f'"{v}"' if isinstance(v, str) \
                    else v.toBlock_(ind + 1) if myfds[k].isAnson \
                    else Anson.toList_(v, myfds[k].elemtype, ind + 1) if myfds[k].antype == 'lst' \
                    else Anson.toDict_(v, myfds[k].elemtype, ind + 1) if myfds[k].antype == 'obj' \
                    else str(v)
            s += ',\n' if x != lx else '\n'
        return s + ' ' * (ind * 2) + '}'

    @staticmethod
    def from_dict(v: dict, eletype: type) -> dict:
        if eletype is None: return v

        d = {}
        for k in v:
            d[k] = Anson.from_obj(v[k], eletype)
        return d

    @staticmethod
    def from_list(v: list, eletype: type) -> list:
        if eletype is None: return v
        return [Anson.from_obj(x, eletype) for x in v]

    @staticmethod
    def from_obj(obj: dict, typename: Union[str, type]) -> TAnson:
        def getClass(_typ_: str):
            parts = _typ_.split('.')
            # if len(java_src_path) > 0:
            #     parts.insert(0, java_src_path)
            module = ".".join(parts[:-1])
            m = __import__(module if module is not None else '__main__')
            for comp in parts[1:]:
                m = getattr(m, comp)
            return m

        anson = getClass(typename)() if isinstance(typename, str) else typename()

        fds = Anson.fields(anson)
        if '__type__' not in fds:
            raise Exception(f'Class {type(anson)} has no field "__type__". Is it a subclass of Anson?')

        for jsonk in obj:
            k = '__type__' if jsonk == 'type' else jsonk
            if k != '__type__' and k not in fds:
                Utils.warn(f'Field ignored: {k}: {obj[k]}')
                continue

            # else [Anson.from_obj(x, 'str' if thefields[k].elemtype is None else thefields[k].elemtype) for x in obj[jsonk]] if thefields[k].antype == 'obj' else \
            anson[k] = Anson.from_obj(obj[jsonk], fds[k].antype) if fds[k].isAnson \
                    else Anson.from_dict(obj[jsonk], fds[k].elemtype) if fds[k].antype == 'obj'\
                    else Anson.from_list(obj[jsonk], fds[k].elemtype) if fds[k].antype == 'lst' \
                    else obj[jsonk]

        return anson

    @staticmethod
    def from_json(jsonstr: str) -> TAnson:
        obj = json.loads(jsonstr)
        v = Anson.from_envelope(obj)
        print(v, type(v))
        return v

    @staticmethod
    def from_file(fp: str) -> TAnson:
        with open(fp, 'r') as file:
            obj = json.load(file)
            return Anson.from_envelope(obj)

    @classmethod
    def java_src(cls, src_root: str = ''):
        """
        :param src_root: e. g. 'src'
        """
        global java_src_path
        java_src_path = src_root

    @classmethod
    def from_envelope(cls, obj: dict):
        return Anson.from_obj(obj,
                '.'.join([java_src_path, obj['type']]) if len(java_src_path) > 0 else obj['type'])
