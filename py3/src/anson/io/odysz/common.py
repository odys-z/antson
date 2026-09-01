'''
Created on 25 Oct 2019

@author: odys-z@github.com
'''
import errno
import os
from pathlib import Path
from glob import glob
import shutil
import sys
from re import match
from typing import TextIO, Optional, TypeVar, Union, List, Tuple, Sequence, Any
from dataclasses import dataclass


def requir_pkg(pkg_name: str, require_ver: Union[str, List[str]] = None):
    '''
        Docstring for requir_pkg

        :param pkg_name: package name, e.g. 'cryptography', 'anson.py3', 'semantics.py3', ...
        :type pkg_name: str
        :param require_ver: requred version, str for minimum version,
        list for exact version or version range [min, max]
        :type require_ver: Union[str, List[str]]
    '''
    from importlib.metadata import version, PackageNotFoundError
    from packaging.version import Version

    try:
        pkg_version = version(pkg_name.replace('.', '_').replace('-', '_'))
    except PackageNotFoundError:
        print('Package not found:', pkg_name)
        sys.exit(1)

    print(f"{pkg_name}: ", pkg_version)

    if isinstance(require_ver, str):
        if Version(pkg_version) < Version(require_ver):
            print(f'Please upgrade {pkg_name} to version {require_ver} or above. Current version: {pkg_version}')
            sys.exit(1)
    elif isinstance(require_ver, list):
        if len(require_ver) == 1:
            if Version(pkg_version) != Version(require_ver[0]):
                print(f'Please install {pkg_name} version {require_ver[0]}. Current version: {pkg_version}')
                sys.exit(1)
        else:
            if Version(pkg_version) < Version(require_ver[0]) or Version(pkg_version) > Version(require_ver[1]):
                print(
                    f'Please install {pkg_name} version between {require_ver[0]} and {require_ver[1]}. Current version: {pkg_version}')
                sys.exit(1)
    print('Positive.')


def requir_executable(cmd: str, setup_hint: str, tolerate: bool = False):
    """
        Check if an executable is present on PATH, otherwise exit with user guidance.

        :tolerate: True if not to exit even the executable is missing.
    """
    if not shutil.which(cmd):
        print(f"\n[ERROR] '{cmd}' command not found.")
        print(f"-> How to fix: {setup_hint}\n")
        if not tolerate:
            sys.exit(1)


T = TypeVar('T')

passwd_allow_ext = ' @#!$%^&*()_+-=.<>,[]{}|?/:;'
'''
    allowed chars in addition to alpha numerics for password.
'''

@dataclass
class Primtypes:
    C20 = {
        "String": "string", "string": "string", "java.lang.String": "string",
        "int": "int", "Integer": "int", "java.lang.Integer": "int",
        "short": "int", "Short": "int", "java.lang.Short": "int",
        "long": "long", "Long": "long", "java.lang.Long": "long",
        "float": "float", "Float": "float", "java.lang.Float": "float",
        "double": "double", "Double": "double", "java.lang.Double": "double",
        "boolean": "bool", "Boolean": "bool", "java.lang.Boolean": "bool",
        "VarType": "LangExt::VarType", "LangExt::VarType": "LangExt::VarType", "anson::LangExt::VarType": "LangExt::VarType",
        "list": "vector",
        "map": "map"
    }


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
        try: return len(s) == 0
        except: pass
        return False
    
    @staticmethod
    def isNull(arr: Optional[Sequence[Any]] = None) -> bool:
        return arr is None or len(arr) == 0 or (len(arr) == 1 and arr[0] is None)

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
    def str(obj):
        '''
        :param obj:
        :return:
        {obj.k: obj.v, ...} if obj is dict;
        [0, 1, ...] if obj is list;
        obj.toAnson if obj is Anson;
        else str(obj)
        '''
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

    def trunc_right(s: str, bytes: int, encoding='utf-8') -> str:
        if bytes <= 0:
            return ''
        b = s.encode(encoding)
        truncated = b[-bytes:]
        # decode, ignoring any partial multi-byte char left at the start
        return truncated.decode(encoding, errors='ignore')

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

    @classmethod
    def suffix(cls, s: str, suffices: "Union[str, List[str], Tuple]") -> bool:
        if isinstance(suffices, str):
            return s.endswith((suffices))
        elif isinstance(suffices, tuple):
            return s.endswith(suffices)
        else:
            return s.endswith(tuple(suffices))


def log(out: Optional[TextIO], templt: Union [str, List[str]], *args):
    if (isinstance(templt, str)):
        try:
            print(templt if LangExt.isblank(args) else templt.format(*args), file=out)
        except Exception as e:
            print(e, file=sys.stderr)
            try: print(templt)
            except: pass
            try: print(args)
            except: pass
            try: print(e)
            except: pass
            print('If printing Anson subclasses, all their memebers must be initialized.', file=sys.stderr)
    elif isinstance(templt, (list, tuple)):
        for tmp in templt:
            log(out, tmp, *args)


class Utils:
    def __init__(self, params):
        '''
        Constructor
        '''
        pass

    @staticmethod
    def logi(templt: Union[str, List[str]], *args):
        log(sys.stdout, templt, *args)

    @classmethod
    def log_arr(cls, lines):
        for tmp in lines:
            log(sys.stdout, tmp)
            print(file=sys.stdout)

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
    def update_patterns(file, patterns: dict, replaced_vals: dict=None):
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
        print('Updating Patterns ...', file)

        with open(file, 'r', encoding='utf-8') as f:
            lines = f.readlines()

        cnt = 0
        # updated_content = re.sub(pattern, repl, content)
        for i, line in enumerate(lines):
            updated = set()
            for k, v in patterns.items():
                # if re.search(k, line):
                matched = re.search(k, line)
                if matched:
                    lines[i] = re.sub(k, v, line)
                    updated.add(k)
                    print('Updated line:', lines[i])
                    cnt += 1

                    if replaced_vals is not None and k in replaced_vals and replaced_vals[k] >= 0:
                        replaced_vals[k] = matched.group(replaced_vals[k])

                if len(updated) == len(patterns):
                    break

        with open(file, 'w', encoding='utf-8') as f:
            f.writelines(lines)

        print(f'[{cnt / len(patterns)}] lines updated. Patterns updating finsied.', file)

        return replaced_vals

    @classmethod
    def writeline_nl(cls, file: str, lines: list[str]):
        with open(file, 'w+', encoding='utf-8') as f:
            for l in lines:
                f.write(l)
                f.write('\n')

    @classmethod
    def rm_any(cls, res: Union[str, Path]):
        try:
            if os.path.isfile(res):
                os.remove(res)
            else:
                shutil.rmtree(res, ignore_errors=False)
            print(f"Successfully removed {res}")
        except FileNotFoundError:
            pass
        except PermissionError:
            print(f"Permission denied: Unable to remove {res}")
        except OSError as e:
            if e.errno != errno.ENOENT:  # Ignore "No such file or directory" errors
                pass
            else:
                print(f"Path {res} does not exist")

    @classmethod
    def copy_anyway(cls, src: Union[str, Path, List[Path]], dest: Union[Path, str], log: bool = False) -> Union[
        Path, List[Path]]:
        if isinstance(src, (list, tuple)):
            dest_dir = Path(dest)
            dest_dir.mkdir(parents=True, exist_ok=True)
            return [
                cls.copy_anyway(s, dest_dir, log)
                if any(ch in str(s) for ch in "*?[")
                else cls.copy_anyway(s, dest_dir / Path(s).name, log)
                for s in src
            ]

        src_str = str(src)

        # Wildcard: expand (recursively) and recurse per match, copying into dest as a directory
        if any(ch in src_str for ch in "*?["):
            matches = [Path(p) for p in glob(src_str, recursive=True) if Path(p).is_file()]
            if not matches:
                raise FileNotFoundError(f"no files matched pattern: {src_str}")

            dest_dir = Path(dest)
            dest_dir.mkdir(parents=True, exist_ok=True)
            return [cls.copy_anyway(match, dest_dir / match.name, log) for match in matches]

        src = Path(src)
        if not src.is_file():
            raise FileNotFoundError(f"source path not found: {src}")

        dest = Path(dest)
        dest.parent.mkdir(parents=True, exist_ok=True)
        if log:
            print(src.absolute().as_posix(), ":=>", dest.absolute().as_posix())
        shutil.copy2(src, dest)
        return dest

    @classmethod
    def move_anyway(cls, src: Union[str, Path, List[Path]], dest: Union[Path, str], overwrite: bool = True,
                     log: bool = False) -> Union[Path, List[Path]]:
        '''
        Credits to Claude.
        :param src:
        :param dest:
        :param overwrite:
        :param log:
        :return:
        '''
        if isinstance(src, (list, tuple)):
            dest_dir = Path(dest)
            dest_dir.mkdir(parents=True, exist_ok=True)
            return [
                cls.move_anyway(s, dest_dir, overwrite=overwrite, log=log)
                if any(ch in str(s) for ch in "*?[")
                else cls.move_anyway(s, dest_dir / Path(s).name, overwrite=overwrite, log=log)
                for s in src
            ]

        src_str = str(src)

        # wildcard support: expand (recursively) and recurse per match
        if any(ch in src_str for ch in "*?["):
            matches = [Path(p) for p in glob(src_str, recursive=True) if Path(p).is_file()]
            if not matches:
                raise FileNotFoundError(f"no files matched pattern: {src_str}")

            dest_dir = Path(dest)
            dest_dir.mkdir(parents=True, exist_ok=True)
            return [cls.move_anyway(match, dest_dir / match.name, overwrite=overwrite, log=log) for match in matches]

        src = Path(src)
        dest = Path(dest)
        if not src.is_file():
            raise FileNotFoundError(f"source path not found: {src}")

        # if dest is an existing directory, the real target is dest/src.name
        final_dest = dest / src.name if dest.is_dir() else dest

        if final_dest.exists():
            if not overwrite:
                raise FileExistsError(f"destination already exists: {final_dest}")
            # shutil.move raises shutil.Error instead of overwriting when the
            # target already exists inside a directory dest -- remove it first
            if final_dest.is_dir():
                shutil.rmtree(final_dest)
            else:
                final_dest.unlink()

        final_dest.parent.mkdir(parents=True, exist_ok=True)

        if log:
            print(src.absolute().as_posix(), ":=>", final_dest.absolute().as_posix())

        shutil.move(str(src), str(final_dest))
        return final_dest
