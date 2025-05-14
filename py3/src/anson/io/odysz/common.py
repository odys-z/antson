'''
Created on 25 Oct 2019

@author: odys-z@github.com
'''
import os
import sys
from re import match
from typing import TextIO, Optional, TypeVar, Union

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
    def str(obj: Union[dict, list]):
        def quot(v) -> str:
            return f'"{v}"' if type(v) == str else f'"{v.toBlock()}"' if isinstance(v, Anson) else LangExt.str(v)
        from .anson import Anson
        if type(obj) == dict:
            s = '{'
            for k, v in obj.items():
                # s += f'{"" if len(s) == 1 else ",\n"}"{k}": "{LangExt.str(v)}"'
                SEP = ",\n"
                s += f'{"" if len(s) == 1 else SEP}"{k}": {quot(v)}'
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

    @staticmethod
    def get_os():
        """
        :return: Windows | Linux | macOS
        """
        if os.name == 'nt':
            return 'Windows'
        elif os.name == 'posix':
            if sys.platform.startswith('linux') or sys.platform.startswith('freebsd'):
              return 'Linux'
            elif sys.platform.startswith('darwin'):
                return 'macOS'
        return 'Unknown'

    @staticmethod
    def iswindows():
        return Utils.get_os() == 'Windows'

    @staticmethod
    def update_patterns(file, patterns: dict):
        """
        Update the version in a text file.

        Example
        -------
        ::

            Utils.update_patterns(version_file,
                {'@set jar_ver=[0-9\\.]+': f'@set jar_ver={jar_ver}',
                 '@REM set version=[0-9\\.]+': f'@set version={version}',
                 '@set html_ver=[0-9\\.]+': f'@set html_ver={html_ver}'})

        Args:
            file (str): Path to the JAR file.
            patterns (dict): Regular expression pattern, key, to replace with value.
        """
        import re
        print('Updating JAR version...', file)

        with open(file, 'r') as f:
            lines = f.readlines()

        # updated_content = re.sub(pattern, repl, content)
        for i, line in enumerate(lines):
            updated = set()
            for k, v in patterns.items():
                if re.search(k, line):
                    lines[i] = re.sub(k, v, line)
                    updated.add(k)
                    print('Updated line:', lines[i])

                if len(updated) == len(patterns):
                    break

        with open(file, 'w') as f:
            f.writelines(lines)

        print('JAR version updated successfully.', file)

        return None

    @classmethod
    def writeline_nl(cls, file: str, lines: list[str]):
        with open(file, 'w+') as f:
            for l in lines:
                f.write(l)
                f.write('\n')
