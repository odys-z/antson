'''
Created on 25 Oct 2019

@author: odys-z@github.com
'''
import os
import sys
from re import match
from typing import TextIO, Optional, TypeVar, Union


T = TypeVar('T')

passwd_allow_ext = ' @#!$%^&*()_+-='
'''
    allowed chars in addition to alpha numerics for password.
'''

class LangExt:
    '''
    Language helper
    '''

    def __init__(self, params):
        '''
        Constructor
        '''

    @staticmethod
    def isblank(s, regex=None):
        """
        ::
        
            self.assertTrue(LangExt.isblank(None))
            self.assertTrue(LangExt.isblank(''))
            self.assertTrue(LangExt.isblank(' '))
            self.assertTrue(LangExt.isblank('00', r'0+'))
            self.assertTrue(LangExt.isblank('00', r'0'))
            self.assertFalse(LangExt.isblank(' ', r'0'))
        :param s:
        :param regex:
        :return: is it taken as blank string
        """
        if (s == None):
            return True
        if isinstance(s, str):
            if regex == None:
                return len(s.strip()) == 0
            else:
                return match(regex, s) is not None
        return False

    @staticmethod
    def ifnull(a: T, b: T) -> T:
        return b if a is None else a

    @staticmethod
    def ifblank(a: str, b: str) -> str:
        return b if len(a) == 0 else a

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

    @staticmethod
    def musteqs(a: str, b: str, msg = None):
        if a != b:
            from .anson import AnsonException
            raise AnsonException(0, f'{a} != {b}' if msg == None else msg)

    @staticmethod
    def only_wordextlen(likely: str, ext='', minlen = 0, maxlen = -1):
        if ext is None:
            ext = ''
        if maxlen >= 0 and len(likely) > maxlen:
            from .anson import AnsonException
            raise AnsonException(0, f'len {likely[0: 10]} > {maxlen}')

        if minlen > 0 and len(likely) < minlen:
            from .anson import AnsonException
            raise AnsonException(0, f'len {likely[0:10]} < {minlen}')

        for c in likely:
            if not c.isalnum() and c not in ext:
                from .anson import AnsonException
                raise AnsonException(0, f'Not allowed char: {c}')
        return True


    @staticmethod
    def only_wordtlen(likely: str, minlen=0, maxlen=-1):
        return LangExt.only_wordextlen(likely, minlen=minlen, maxlen=maxlen)

    @staticmethod
    def only_id_len(likely: str, ext='', minlen=0, maxlen=-1):
        '''
        Verify the *likely* string is only with chars of alphanumberic or anyof '`~!@#$%^&*_-+=:;,./'.
        :param likely: 
        :param ext: 
        :param minlen: 
        :param maxlen: 
        :return: verified
        '''
        return LangExt.only_wordextlen(likely,
                ext='`~!@#$%^&*_-+=:;,./' if ext is None else ext + '`~!@#$%^&*_-+=:;,./',
                minlen=minlen, maxlen=maxlen)

    @staticmethod
    def only_passwdlen(likely: str, minlen=0, maxlen=-1):
        '''
        String likely mus only an alphanumeric word and with length in between [minlen, maxlen].
        :param likely:
        :param minlen:
        :param maxlen:
        :return: likely
        '''
        return LangExt.only_wordextlen(likely, ext=passwd_allow_ext, minlen=minlen, maxlen=maxlen)


def log(out: Optional[TextIO], templt: str, *args):
    try:
        print(templt if LangExt.isblank(args) else templt.format(*args), file=out)
    except Exception as e:
        try: print(templt)
        except: pass
        try: print(args)
        except: pass
        try: print(e)
        except: pass
        print('If printing Anson subclasses, all their memebers must be initialized.', file=sys.stderr)


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

        cnt = 0
        # updated_content = re.sub(pattern, repl, content)
        for i, line in enumerate(lines):
            updated = set()
            for k, v in patterns.items():
                if re.search(k, line):
                    lines[i] = re.sub(k, v, line)
                    updated.add(k)
                    print('Updated line:', lines[i])
                    cnt += 1

                if len(updated) == len(patterns):
                    break

        with open(file, 'w') as f:
            f.writelines(lines)

        print(f'[{cnt / len(patterns)}] lines updated. Patterns updating finsiedh.', file)

        return None

    @classmethod
    def writeline_nl(cls, file: str, lines: list[str]):
        with open(file, 'w+') as f:
            for l in lines:
                f.write(l)
                f.write('\n')
