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
