from dataclasses import dataclass, Field

import json
from enum import Enum
from numbers import Number
from typing import Union

from .common import Utils

java_src_path: str = ''

def class4Name(m, clssn: str) -> type:
    """
    With returned class type:
        obj = cls()  # Adjust arguments as needed
        obj.__type__=f'{m}.{clssn}'
    :param m:
    :param clssn:
    :return class type:
    """
    module = __import__(m, fromlist=[clssn])
    cls = getattr(module, clssn)
    cls.__type__=f'{m}.{clssn}'
    return cls

def getClass(_typ_: str | None):
    if _typ_ is None:
        return None
    parts = _typ_.split('.')
    module = ".".join(parts[:-1])
    # m = __import__(module if module is not None else '__main__')
    # for comp in parts[1:]:
    #     m = getattr(m, comp)
    # return m
    return class4Name(module, parts[-1])


class AnsonField:
    def __init__(self, **kwargs):
        self.elemtype = kwargs.get('elemtype', None)
        antype = kwargs.get('type', None)
        self.antype = antype if not isinstance(antype, str) else getClass(antype)

    def isAnson(self):
        return self.antype is not None and type(self.antype) == type and issubclass(self.antype, Anson)


def _fields(anson, fromval):
    """
    Get all fields from an Anson type.
    :param anson: the object of Anson type, for finding its type.
    :param fromval:
    :return:
    """
    _FIELDS = '__dataclass_fields__'
    fds = getattr(type(anson), _FIELDS)
    return {it: AnsonField(type=f.type) for it, f in fds.items()}


class DataStruct:
    def __init__(self, **kwargs):
        self.isEnm = kwargs.get('enum', False)
        self.isLst = kwargs.get('lst', False)
        self.isObj = kwargs.get('obj', False)
        self.isStr = kwargs.get('str', False)
        self.isNum = kwargs.get('num', False)
        self.isAnson = kwargs.get('anson', False)
        self.ansoname = kwargs.get('ansontype', f'{__name__}.Anson')


def value_type(v):
    if v is None: return DataStruct()
    t = type(v)
    return DataStruct(anson = issubclass(t, Anson),
                      lst = t==list,
                      obj = t==dict or t == object,
                      num = isinstance(v, Number),
                      str=t == str,
                      enum=isinstance(v, Enum),
                      ansontype=f'{t.__module__}.{t.__name__}')


def instanceof(clsname: str | type, props: dict):
    obj = getClass(clsname)() if type(clsname) == str else clsname()
    fds = _fields(obj, None)
    missingAttrs = []

    for k, v in props.items():
        if k != 'type' and k not in fds:
            missingAttrs.append(k)
        setattr(obj, k, Anson.from_value(fds[k].antype if k in fds else None, v))

    if len(missingAttrs) > 0:
        Utils.warn(f'Missing attributes in {obj.__type__}: {missingAttrs}. Anson expect a __init__() for all initialize the none default fields.')

    return obj


def parsetype(obj) -> Union[str, None]:
    return obj['__type__'] if isinstance(obj, Anson) and hasattr(obj, '__type__') \
        else f'{java_src_path}{'.' if len(java_src_path) else ''}{obj['type']}' if isinstance(obj, dict) and 'type' in obj.keys() else None


@dataclass
class Anson(dict):
    enclosinguardtypes = set()
    
    __type__: str
    '''ansons.antson.Anson'''

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
    def toList_(lst: list, ind: int, beautify):
        return  '[' + ', '.join([e.toBlock_(ind + 0, beautify) if isinstance(e, Anson) \
               else f'"{e}"' if isinstance(e, str) and e is not None \
               else str(e) for e in lst]) + ']'

    @staticmethod
    def toDict_(dic: dict, ind: int, beautify):
        return '{\n' if len(dic) > 0 else '{' + ',\n'.join(' ' * (ind * 2 + 2) + dic[k].toBlock_(ind + 1, beautify) if isinstance(dic[k], Anson) else str(dic[k]) for k in dic) + '}' if beautify \
            else '{' + ','.join(dic[k].toBlock_(ind + 1, beautify) if isinstance(dic[k], Anson) else str(dic[k]) for k in dic) + '}'

    def toBlock(self, beautify=True) -> str:
        return self.toBlock_(0, beautify)

    def toFile(self, path: str):
        with open(path, 'w+') as jf:
            jf.write(self.toBlock(True))

    def toBlock_(self, ind: int, beautify, suggestype: type = None) -> str:
        myfds = _fields(self, None)
        # s = ' ' * (ind * 2) + '{\n' if beautify else '{'
        s = '{\n' if beautify else '{'

        has_prvious = False

        for x, k in enumerate(self.__dict__):
            if '__type__' == k:
                tp = str(self['__type__']).removeprefix(java_src_path+'.')
                if has_prvious: s += ',\n' if beautify else ', '
                s += f'{' ' * (ind * 2 + 2) if beautify else ''}"type": "{tp}"'
                has_prvious = True

            else:
                v, vt = self[k], value_type(self[k])

                if k not in myfds:
                    Utils.warn("Field {0}.{1} is not defined in Anson, which is presenting in data object. Value ignored: {2}.",
                               str(self['__type__']), k, self[k])
                    continue

                if has_prvious: s += ',\n' if beautify else ','

                s += f'{" " * (ind * 2 + 2)}"{k}": ' if beautify else f'"{k}": '

                s += 'null' if v is None or isinstance(v, Field) \
                    else f'"{v}"' if vt.isStr \
                    else v.toBlock_(ind + 1, beautify, myfds[k].antype) if vt.isAnson \
                    else f'"{v.name}"' if vt.isEnm \
                    else Anson.toList_(v, ind + 1, beautify) if vt.isLst \
                    else Anson.toDict_(v, ind + 1, beautify) if vt.isObj \
                    else str(v)
                    # else v.toBlock_(ind + 1, beautify, myfds[k].antype) if myfds[k].isAnson() \

                has_prvious = True
        return s + ('\n' if has_prvious and beautify else '') + (' ' * (ind * 2) + '}' if beautify else '}')

    @staticmethod
    def from_value(antype: str | type | None, v):
        if v is None: return None
        vt = value_type(v)

        objtype = parsetype(v)
        antype = objtype if objtype is not None else antype

        if type(antype) == str:
            try: antype = getClass(antype)
            except: pass

        # return Anson.from_obj(v, anf.antype) if vt.isAnson \
        return instanceof(antype, v) if vt.isAnson or type(antype) == type and issubclass(antype, Anson) \
            else Anson.from_dict(v, antype) if vt.isObj \
            else Anson.from_list(v, antype[0] if type(antype) == list else antype) if vt.isLst \
            else v

    @staticmethod
    def from_dict(v: dict, eletype: str) -> dict:
        if eletype is None: return v

        d = {}
        for k in v:
            d[k] = Anson.from_value(eletype, v[k])
        return d

    @staticmethod
    def from_list(v: list, eletype: str) -> list:
        # override field type with protocol package's info
        if not isinstance(v, list):
            Utils.warn(f'Expection a list instance, but got "{v}"')
            return [v]

        if len(v) > 0:
            eletype = parsetype(v[0])
            # eletype = f'{java_src_path}.{eletype}'

        return 'null' if v is None else [Anson.from_value(eletype, x) for x in v]

    @staticmethod
    def from_obj(obj: dict, ansontype: Union[str, type]) -> Union['Anson', dict, None]:

        if obj is None: return None

        vt = value_type(obj)

        if vt.isAnson:
            raise TypeError('Not goes alone from_value()?')
            # antype = getClass(ansontype) if vt.ansoname is None else getClass(vt.ansoname)
            # return instanceof(antype, obj)
        elif vt.isObj:
            _t = parsetype(obj)

            return instanceof(_t, obj) if _t is not None else instanceof(ansontype, obj) if ansontype is not None else obj

            # dicv = {} if _t is None and ansontype is None else getClass(_t)() if _t is not None else getClass(ansontype)()
            # fds = _fields(dicv, obj)
            # # if '__type__' not in fds and type(anson) != dict:
            # #     raise Exception(f'Class {type(anson)} has no field "__type__". Is it a subclass of Anson?')
            #
            # for jsonk in obj:
            #     k = '__type__' if jsonk == 'type' else jsonk
            #     if k != '__type__' and k not in fds:
            #         Utils.warn(f'Field ignored: {k}: {obj[k]}')  # TODO deserialize enum, e.g. MsgCode
            #         continue
            #
            #     dicv[k] = Anson.from_value(fds[k], obj[jsonk])
            # return dicv
        else:
            raise TypeError(f'Not here: {obj}')

    @staticmethod
    def from_json(jsonstr: str) -> 'Anson':
        obj = json.loads(jsonstr)
        v = Anson.from_envelope(obj)
        print(v, type(v))
        return v

    @staticmethod
    def from_file(fp: str) -> 'Anson':
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
