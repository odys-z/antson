'''
Created on 25 Oct 2019

@author: odys-z@github.com
'''
import sys
from re import match


class LangExt():
    '''
    classdocs
    '''


    def __init__(self, params):
        '''
        Constructor
        '''

    @staticmethod
    def isblank(s, regex = None):
        if (s == None):
            return True
        if (isinstance(s, str)):
            if (regex == None or s == ""):
                return True
            else:
                return match(s, regex)
        return False

class Utils():
    def __init__(self, params):
        '''
        Constructor
        '''

    @staticmethod
    def logi (self):
        pass
    
    @staticmethod
    def warn (templt, args = None):
        print(templt, file=sys.stderr)
        pass
    
        