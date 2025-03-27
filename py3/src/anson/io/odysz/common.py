'''
Created on 25 Oct 2019

@author: odys-z@github.com
'''
import sys
from re import match
from typing import TextIO, Optional, TypeVar

T = TypeVar('T')


class LangExt:
    '''
    classdocs
    '''

    def __init__(self, params):
        '''
        Constructor
        '''

    @staticmethod
    def isblank(s, regex=None):
        if (s == None):
            return True
        if isinstance(s, str):
            if regex == None or s == "":
                return len(s) == 0
            else:
                return match(s, regex)
        return False

    @staticmethod
    def ifnull(a: T, b: T) -> T:
        return b if a is None else a

    @classmethod
    def len(cls, obj):
        return 0 if obj is None else len(obj)

    @staticmethod
    def str(obj: dict | list):
        def quot(v) -> str:
            return f'"{v}"' if type(v) == str else f'"{v.toBlock()}"' if isinstance(v, Anson) else LangExt.str(v)
        from src.anson.io.odysz.ansons import Anson
        if type(obj) == dict:
            s = '{'
            for k, v in obj.items():
                # s += f'{"" if len(s) == 1 else ",\n"}"{k}": "{LangExt.str(v)}"'
                s += f'{"" if len(s) == 1 else ",\n"}"{k}": {quot(v)}'
            s += '}'
            return s
        elif type(obj) == list:
            s = '['
            # s += ", ".join(f'"{x}"' if type(x) == str else LangExt.str(x) for x in obj)
            s += ", ".join(quot(x) for x in obj)
            return s + ']'
        elif isinstance(obj, Anson):
            return obj.toBlock()
        else:
            return str(obj)

def log(out: Optional[TextIO], templt: str, *args):
    try:
        print(templt if LangExt.isblank(args) else templt.format(*args), file=out)
    except Exception as e:
        print(templt, args, e)


class Utils:
    def __init__(self, params):
        '''
        Constructor
        '''

    @staticmethod
    def logi(templt, *args):
        log(sys.stdout, templt, *args)

    @staticmethod
    def warn(templt, *args):
        log(sys.stderr, templt, *args)
