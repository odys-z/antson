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
import json
from packaging.version import Version


def requir_pkg(pkg_name: str, require_ver: Optional[Union[str, List[str]]] = None, tolerate: bool = False):
    '''
        :param pkg_name: package name, e.g. 'cryptography', 'anson.py3', 'semantics.py3', ...
        :type pkg_name: str
        :param require_ver: requred version, str for minimum version,
        list for exact version or version range [min, max]
        :type require_ver: Union[str, List[str]]
        @since 0.6.4
    '''
    if not check_package(pkg_name, require_ver):
        if not tolerate:
            sys.exit(1)
        else:
            print(f'*** WARNING *** Please install {pkg_name} {require_ver}.')


def check_verstr(pkg_version: str, require_ver: Optional[Union[str, List[str]]]) -> bool:
    if not require_ver or require_ver == []:
        return True

    if isinstance(require_ver, str):
        if Version(pkg_version) < Version(require_ver):
            print(f'Please upgrade to version {require_ver} or above. Current version: {pkg_version}')
            return False
    elif isinstance(require_ver, list):
        if len(require_ver) == 1:
            if Version(pkg_version) != Version(require_ver[0]):
                print(f'Please install version {require_ver[0]}. Current version: {pkg_version}')
                return False
        else:
            if Version(pkg_version) < Version(require_ver[0]) or Version(pkg_version) > Version(require_ver[1]):
                print(f'Please install version between {require_ver[0]} and {require_ver[1]}. Current version: {pkg_version}')
                return False
    return True


def check_package(pkg_name: str, require_ver: Optional[Union[str, List[str]]] = None) -> bool:
    '''
        Check if a package can be imported.

        :param pkg_name: package name, e.g. 'cryptography', 'anson.py3', 'semantics.py3', ...
        :param require_ver: required version, str for minimum version,
                            list for exact version or version range [min, max]
        :return: True if the package can be imported and matches version, False otherwise.
        @since 0.6.4
    '''
    from importlib.metadata import version, PackageNotFoundError

    try:
        pkg_version = version(pkg_name.replace('.', '_').replace('-', '_'))
    except PackageNotFoundError:
        print('Package not found:', pkg_name)
        return False

    print(f"{pkg_name}: {pkg_version}")

    # FIX: Check the return value of check_verstr
    if not check_verstr(pkg_version, require_ver):
        return False

    print(f'{pkg_name} {require_ver}: Positive.')
    return True


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


def requir_npm_package_resolve(start_path: Union[str, Path] = "", pkg_name: str = "",
                                  require_ver: Optional[Union[str, List[str]]] = None,
                                  tolerate: bool = False) -> None:
    '''
        Trace real module resolution for an npm package, the same way Node's require()
        and webpack's default resolver do: starting from `start_path`, check
        <dir>/node_modules/<pkg_name>/package.json, then walk up to each parent
        directory and check again, until the filesystem root is reached.
 
        This answers "which version will actually get imported" -- not "which version
        does package-lock.json say should be installed here". A package version installed
        by npm can not satisfy the required version - Webpack won't check version.
 
        :param start_path: directory to start resolving from -- typically the directory
                            containing the file that does the `import`/`require`, or the
                            project root. Defaults to cwd.
        :param pkg_name: npm package name, scoped names supported, e.g. '@anclient/anreact'
        :param require_ver: required version, str for minimum version,
                            list for exact version or version range [min, max]
        :param tolerate: if True, report problems but don't sys.exit(1)
        :return: None. Exits; non-zero (unless tolerate) if the version that would
                 actually be resolved does not satisfy require_ver, or if the package
                 isn't found anywhere along the resolution path.
    '''
 
    start = Path(start_path) if start_path else Path.cwd()
    start = start.resolve()
    current = start if start.is_dir() else start.parent
 
    # Walk up the directory tree exactly as Node's resolver does, collecting
    # every node_modules/<pkg_name> found along the way, closest first.
    candidates: List[tuple] = []  # (node_modules_dir, package_json_path, version)
    seen = set()
    while True:
        if current in seen:
            break
        seen.add(current)
 
        pkg_json = current / 'node_modules' / pkg_name / 'package.json'
        if pkg_json.exists():
            version = None
            try:
                with open(pkg_json, 'r', encoding='utf-8') as f:
                    version = json.load(f).get('version')
            except (json.JSONDecodeError, OSError) as e:
                print(f'*** Warning *** Could not read {pkg_json}: {e}')
            candidates.append((current / 'node_modules', pkg_json, version))
 
        parent = current.parent
        if parent == current:
            break
        current = parent
 
    if not candidates:
        print(f'{pkg_name}: not found in any node_modules from {start} up to filesystem root')
        if not tolerate:
            sys.exit(1)
        return
 
    print(f'Resolution trace for {pkg_name} starting at {start}:')
    for i, (nm_dir, pkg_json, version) in enumerate(candidates):
        tag = '-> RESOLVED (this is what import/require actually gets)' if i == 0 else '   (shadowed, never reached)'
        print(f'  [{i}] {nm_dir}  version={version}  {tag}')
 
    resolved_dir, _pkg_json, resolved_version = candidates[0]
    resolved_version = resolved_version or ''
 
    print(f"{pkg_name}: {resolved_version} (resolved from {resolved_dir})")
 
    if require_ver is None or require_ver == []:
        return
 
    if check_verstr(resolved_version, require_ver):
        print(f'{pkg_name} {require_ver}: Positive.')
    else:
        print(f'*** Error *** {pkg_name} {require_ver}: resolved version is '
              f'{resolved_version!r} from {resolved_dir}, which does not satisfy the requirement.')
        # If a satisfying version exists deeper in the tree, it's being shadowed --
        # worth calling out explicitly since that's an easy thing to miss.
        for nm_dir, pkg_json, version in candidates[1:]:
            if version and check_verstr(version, require_ver):
                print(f'    Note: a satisfying version ({version}) exists at {nm_dir}, '
                      f'but it is shadowed by the resolved copy above and will not be used.')
                break
        if not tolerate:
            sys.exit(1)


def requir_npm_package_lock(lock_file_path: Union[str, Path] = "", pkg_name: str = "",
                            require_ver: Optional[Union[str, List[str]]] = None,
                            tolerate: bool = False) -> None:
    '''
        Check if an npm package is present in package-lock.json and satisfies version requirements.

        :param lock_file_path: Path to package-lock.json or project directory
        :param pkg_name: npm package name, e.g. 'express', 'lodash'
        :param require_ver: required version, str for minimum version,
                            list for exact version or version range [min, max]
        :return: True if package exists and satisfies version, False otherwise.
    '''

    path_obj = Path(lock_file_path) if lock_file_path else Path.cwd()
    if path_obj.is_dir() or path_obj.name != 'package-lock.json':
        lock_file_path = path_obj / 'package-lock.json'
    else:
        lock_file_path = path_obj

    if not lock_file_path.exists():
        print(f'File package-lock.json is not found: {lock_file_path}')
        if not tolerate:
            sys.exit(1)
        return

    if require_ver is None or require_ver == []:
        return

    with open(lock_file_path, 'r', encoding='utf-8') as f:
        lock_data = json.load(f)

    installed_version: Optional[str] = None

    # 1. Check modern 'packages' structure (npm v7+)
    packages = lock_data.get('packages', {})
    target_key = f"node_modules/{pkg_name}"
    if target_key in packages:
        installed_version = packages[target_key].get('version')

    # 2. Fallback to legacy top-level 'dependencies' structure if not found
    if not installed_version:
        dependencies = lock_data.get('dependencies', {})
        if pkg_name in dependencies:
            installed_version = dependencies[pkg_name].get('version')

    if not installed_version:
        if not tolerate:
            sys.exit(1)
        else:
            print('Package not found in package-lock.json:', pkg_name)

    installed_version: str = installed_version if installed_version else '' # already checked, suppress warning

    print(f"{pkg_name}: {installed_version}")

    if check_verstr(installed_version, require_ver):
        print(f'{pkg_name} {require_ver}: Positive.')
    else:
        if not tolerate:
            sys.exit(1)
        else:
            print('*** Error ***', f'{pkg_name} {require_ver}: Positive.')


def _read_cmake_cache(cache_file: Path) -> dict:
    '''
        Parse CMakeCache.txt entries of the form NAME:TYPE=VALUE into {NAME: VALUE}.
    '''
    entries = {}
    with open(cache_file, 'r', encoding='utf-8', errors='replace') as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith('#') or line.startswith('//'):
                continue
            key_type, sep, value = line.partition('=')
            if sep:
                entries[key_type.split(':', 1)[0]] = value
    return entries


def _find_cmake_cache(build_dir: Path) -> Optional[Path]:
    '''
        <build_dir>/CMakeCache.txt, or, for per-config layouts such as Qt Creator's
        qt-build/Debug, the first <build_dir>/*/CMakeCache.txt.
    '''
    if (build_dir / 'CMakeCache.txt').exists():
        return build_dir / 'CMakeCache.txt'
    found = sorted(build_dir.glob('*/CMakeCache.txt'))
    if len(found) > 1:
        print(f'*** Warning *** Multiple CMakeCache.txt under {build_dir}, using {found[0]}: {[str(f) for f in found]}')
    return found[0] if found else None


def _cmake_src_version(src_dir: Path) -> Tuple[Optional[str], str]:
    '''
        Find the version of a CMake source tree:
        1. project(<name> VERSION x.y.z ...) in its top-level CMakeLists.txt;
        2. fallback: the nearest git tag.
        Only strings matching version.VERSION_PATTERN are returned, so check_verstr() won't raise.

        :return: (version or None, where it came from, plus the git commit if available)
    '''
    import re
    import subprocess
    from anson.io.odysz.version import VERSION_PATTERN

    ver_regex = re.compile(r'\s*(' + VERSION_PATTERN + r')\s*', re.VERBOSE | re.IGNORECASE)
    version: Optional[str] = None
    origin = ''

    top = src_dir / 'CMakeLists.txt'
    if top.exists():
        text = re.sub(r'#[^\n]*', '', top.read_text(encoding='utf-8', errors='replace'))
        m = re.search(r'\bproject\s*\(([^)]*)\)', text, re.IGNORECASE | re.DOTALL)
        if m:
            v = re.search(r'\bVERSION\s+(\S+)', m.group(1), re.IGNORECASE)
            if v and ver_regex.fullmatch(v.group(1)):
                version, origin = v.group(1), 'project(VERSION)'

    commit = ''
    git = shutil.which('git')
    if git and (src_dir / '.git').exists():
        try:
            r = subprocess.run([git, '-C', str(src_dir), 'rev-parse', '--short', 'HEAD'],
                               capture_output=True, text=True, timeout=30)
            if r.returncode == 0:
                commit = r.stdout.strip()
            if version is None:
                r = subprocess.run([git, '-C', str(src_dir), 'describe', '--tags', '--abbrev=0'],
                                   capture_output=True, text=True, timeout=30)
                if r.returncode == 0 and ver_regex.fullmatch(r.stdout.strip()):
                    version, origin = r.stdout.strip(), 'git tag'
        except (OSError, subprocess.TimeoutExpired) as e:
            print(f'*** Warning *** git query failed in {src_dir}: {e}')

    if commit:
        origin = f'{origin}, commit {commit}' if origin else f'commit {commit}'
    return version, origin


def requir_cmake_fetchcontent(build_dir: Union[str, Path], content_name: str,
                              require_ver: Optional[Union[str, List[str]]] = None,
                              tolerate: bool = False) -> None:
    '''
        Trace which source tree a CMake FetchContent dependency actually resolves to,
        the same way FetchContent_MakeAvailable() does, and check its version:

        1. FETCHCONTENT_SOURCE_DIR_<CONTENT_NAME> in CMakeCache.txt, if set -- a local
           override; nothing is fetched and the downloaded copy is never used;
        2. otherwise <FETCHCONTENT_BASE_DIR>/<content_name>-src, where FETCHCONTENT_BASE_DIR
           is read from CMakeCache.txt (CMake's default is <binary dir>/_deps).

        The version is read from project(... VERSION x.y.z) in the dependency's top-level
        CMakeLists.txt, falling back to its nearest git tag.

        Must be called after configuring, as both paths come from CMakeCache.txt.

        :param build_dir: the cmake binary dir (-B), or its parent for per-config
                          layouts, e.g. 'qt-build' with qt-build/Debug/CMakeCache.txt.
                          Required, no default: a relative path resolves against cwd,
                          which is usually another project's folder.
        :param content_name: the name given to FetchContent_Declare(), e.g. 'anson.cmake'
        :param require_ver: required version, str for minimum version,
                            list for exact version or version range [min, max]
        :param tolerate: if True, report problems but don't sys.exit(1)
        @since 0.6.9
    '''
    if not build_dir or not content_name:
        # Caller error, not a dependency problem -- fail regardless of tolerate.
        print(f'*** Error *** requir_cmake_fetchcontent(): build_dir and content_name are required, '
              f'got build_dir={build_dir!r}, content_name={content_name!r}.')
        sys.exit(1)

    build = Path(build_dir).resolve()
    cache_file = _find_cmake_cache(build)
    if cache_file is None:
        print(f'{content_name}: no CMakeCache.txt in {build} or its sub-folders -- configure the project first.')
        if not tolerate:
            sys.exit(1)
        return

    binary_dir = cache_file.parent
    cache = _read_cmake_cache(cache_file)
    upper, lower = content_name.upper(), content_name.lower()
    override_key = f'FETCHCONTENT_SOURCE_DIR_{upper}'

    base_dir = Path(cache.get('FETCHCONTENT_BASE_DIR') or (binary_dir / '_deps'))
    if not base_dir.is_absolute():
        base_dir = binary_dir / base_dir

    # Resolution order, the first existing one wins -- same as FetchContent.
    paths: List[Tuple[str, Path]] = []
    if cache.get(override_key):
        paths.append((override_key, Path(cache[override_key])))
    paths.append(('FETCHCONTENT_BASE_DIR' if cache.get('FETCHCONTENT_BASE_DIR') else 'default _deps',
                  base_dir / f'{lower}-src'))

    candidates = []  # (label, src_dir, version, origin)
    for label, p in paths:
        if p.is_dir():
            v, origin = _cmake_src_version(p)
            candidates.append((label, p, v, origin))
        elif label == override_key:
            print(f'*** Warning *** {override_key} is set to {p}, which does not exist.')

    if not candidates:
        print(f'{content_name}: no source tree found, using {cache_file}; '
              f'looked at {[str(p) for _, p in paths]}')
        strays = [str(p) for p in build.rglob(f'{lower}-src') if p.is_dir()]
        if strays:
            print(f'    Found by search, but not referenced by the cache: {strays}')
        if not tolerate:
            sys.exit(1)
        return

    print(f'Resolution trace for FetchContent {content_name} ({cache_file}):')
    for i, (label, p, v, origin) in enumerate(candidates):
        tag = '-> RESOLVED (this is what the build actually uses)' if i == 0 else '   (shadowed, never used)'
        print(f'  [{i}] {p}  [{label}]  version={v} ({origin})  {tag}')

    _label, resolved_dir, resolved_version, _origin = candidates[0]

    print(f"{content_name}: {resolved_version} (resolved from {resolved_dir})")

    if require_ver is None or require_ver == []:
        return

    if not resolved_version:
        print(f'*** Error *** {content_name}: cannot determine the version of {resolved_dir}. '
              f'Declare project(<name> VERSION x.y.z) in its top-level CMakeLists.txt, or tag the repo.')
        if not tolerate:
            sys.exit(1)
        return

    if check_verstr(resolved_version, require_ver):
        print(f'{content_name} {require_ver}: Positive.')
    else:
        print(f'*** Error *** {content_name} {require_ver}: resolved version is '
              f'{resolved_version!r} from {resolved_dir}, which does not satisfy the requirement.')
        # If a satisfying version exists in the downloaded copy, it's being shadowed by the override.
        for _label, src_dir, version, _origin in candidates[1:]:
            if version and check_verstr(version, require_ver):
                print(f'    Note: a satisfying version ({version}) exists at {src_dir}, '
                      f'but it is shadowed by {override_key} and will not be used.')
                break
        if not tolerate:
            sys.exit(1)


class mvn:
    '''
        Maven helpers, e.g.

            mvn.requir_installed('io.github.odys-z:semantic.jserv', '[1.5.0,2.0)')
        
        By Claude.ai, needing review
    '''

    qualifiers = {'alpha': 0, 'a': 0, 'beta': 1, 'b': 1, 'milestone': 2, 'm': 2,
                  'rc': 3, 'cr': 3, 'snapshot': 4, '': 5, 'ga': 5, 'final': 5, 'release': 5, 'sp': 6}
    _release = (1, 5, '')
    _local_repo: Optional[Path] = None

    @staticmethod
    def version_key(v: str) -> tuple:
        '''
            Simplified Maven ComparableVersion ordering:
            1.0-alpha < 1.0-beta < 1.0-milestone < 1.0-rc < 1.0-SNAPSHOT < 1.0 = 1.0.0 = 1.0-ga < 1.0-sp < 1.0.1
        '''
        import re
        toks = re.findall(r'\d+|[a-z]+', v.lower())
        items = [(2, int(t), '') if t.isdigit()
                 else (1, mvn.qualifiers[t], '') if t in mvn.qualifiers
                 else (1, 7, t) for t in toks]

        # 1.0.0-rc1 -> 1-rc-1, 1.0.0 -> 1: drop zeros before a qualifier / at the end, and trailing release qualifiers
        norm = []
        for it in reversed(items):
            nxt = norm[-1] if norm else None
            if (it == (2, 0, '') or it == mvn._release) and (nxt is None or nxt[0] == 1):
                continue
            norm.append(it)
        return tuple(reversed(norm))

    @staticmethod
    def compare(a: str, b: str) -> int:
        ka, kb = mvn.version_key(a), mvn.version_key(b)
        n = max(len(ka), len(kb))
        ka += (mvn._release,) * (n - len(ka))
        kb += (mvn._release,) * (n - len(kb))
        return (ka > kb) - (ka < kb)

    @staticmethod
    def parse_range(spec: str) -> List[Tuple[Optional[str], bool, Optional[str], bool]]:
        '''
            Maven version range syntax -> [(low, low_inclusive, high, high_inclusive), ...]

            "1.0"            -> x >= 1.0 (soft requirement, taken as minimum, like requir_pkg)
            "[1.0]"          -> x == 1.0
            "[1.0,2.0)"      -> 1.0 <= x < 2.0
            "(,1.0],[1.2,)"  -> x <= 1.0 or x >= 1.2
        '''
        import re
        spec = spec.strip()
        if not spec.startswith(('[', '(')):
            return [(spec, True, None, False)]

        sets = re.findall(r'([\[(])([^\])]*)([\])])', spec)
        if not sets:
            raise ValueError(f'Invalid maven version range: {spec}')

        ranges = []
        for lb, body, rb in sets:
            parts = [p.strip() for p in body.split(',')]
            if len(parts) == 1:
                if lb != '[' or rb != ']' or not parts[0]:
                    raise ValueError(f'Invalid maven version range: {spec}')
                ranges.append((parts[0], True, parts[0], True))
            elif len(parts) == 2:
                ranges.append((parts[0] or None, lb == '[', parts[1] or None, rb == ']'))
            else:
                raise ValueError(f'Invalid maven version range: {spec}')
        return ranges

    @staticmethod
    def in_range(v: str, ranges: List[Tuple[Optional[str], bool, Optional[str], bool]]) -> bool:
        for lo, lo_inc, hi, hi_inc in ranges:
            if lo is not None:
                c = mvn.compare(v, lo)
                if c < 0 or c == 0 and not lo_inc:
                    continue
            if hi is not None:
                c = mvn.compare(v, hi)
                if c > 0 or c == 0 and not hi_inc:
                    continue
            return True
        return False

    @staticmethod
    def local_repository() -> Path:
        '''
            Resolve the local repository as maven sees it (settings.xml, -Dmaven.repo.local in MAVEN_OPTS, ...),
            falling back to ~/.m2/repository if mvn is unavailable or the evaluation fails. Cached.
        '''
        if mvn._local_repo is not None:
            return mvn._local_repo

        import subprocess
        repo = Path.home() / '.m2' / 'repository'
        exe = shutil.which('mvn')  # mvn.cmd on Windows; full path so CreateProcess can launch the batch file
        if exe:
            try:
                r = subprocess.run(
                    [exe, '-B', '-q', '-DforceStdout', '-Dexpression=settings.localRepository',
                     'org.apache.maven.plugins:maven-help-plugin:3.4.0:evaluate'],
                    capture_output=True, text=True, timeout=120, cwd=Path.home())
                out = r.stdout.strip().splitlines()
                if r.returncode == 0 and out and Path(out[-1].strip()).is_dir():
                    repo = Path(out[-1].strip())
                else:
                    print('*** Warning *** mvn help:evaluate failed, falling back to ~/.m2/repository.', r.stderr.strip())
            except (OSError, subprocess.TimeoutExpired) as e:
                print('*** Warning *** mvn help:evaluate failed, falling back to ~/.m2/repository.', e)

        mvn._local_repo = repo
        return repo

    @staticmethod
    def requir_installed(coordinate: str, ver_range: Optional[str] = None,
                         tolerate: bool = False, local_repo: Optional[Union[str, Path]] = None) -> Optional[str]:
        '''
            Check the local maven repository has an artifact installed with version in the range.

            :param coordinate: 'group-id:artifact-id', e.g. 'io.github.odys-z:semantic.jserv'
            :param ver_range: maven version range, e.g. '1.5.0', '[1.5.0]', '[1.5.0,2.0)', '(,1.0],[1.2,)';
                              None for any version. A bare version is taken as minimum (like requir_pkg).
            :param tolerate: if True, report problems but don't sys.exit(1)
            :param local_repo: override the local repository path (skips calling mvn).
            :return: the highest matching version installed, or None (only if tolerate).
        '''
        from functools import cmp_to_key

        try:
            group, artifact = [s.strip() for s in coordinate.split(':')[:2]]
        except ValueError:
            raise ValueError(f'Invalid maven coordinate, expecting group:artifact, got: {coordinate}')

        repo = Path(local_repo) if local_repo is not None else mvn.local_repository()
        art_dir = repo.joinpath(*group.split('.'), artifact)

        installed = []
        if art_dir.is_dir():
            for d in art_dir.iterdir():
                # a version folder may hold only *.lastUpdated / _remote.repositories after a failed download
                if d.is_dir() and any(f.suffix in ('.jar', '.pom') for f in d.iterdir() if f.is_file()):
                    installed.append(d.name)
        installed.sort(key=cmp_to_key(mvn.compare))

        ranges = mvn.parse_range(ver_range) if ver_range else None
        matched = [v for v in installed if ranges is None or mvn.in_range(v, ranges)]

        print(f'{coordinate}: {", ".join(installed) or "(not installed)"}')

        if not matched:
            print(f'\n[ERROR] {coordinate} {ver_range or ""} is not installed in {repo}.')
            print(f'-> How to fix: mvn install the artifact, or mvn dependency:get -Dartifact={group}:{artifact}:<version>\n')
            if not tolerate:
                sys.exit(1)
            return None

        print(f'{coordinate} {ver_range or ""}: Positive. {matched[-1]}')
        return matched[-1]


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
    def isblank(s:Optional[Any], regex=None):
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
        try: return LangExt.len(s) == 0
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
    def to_str(obj):
        '''
        :param obj:
        :return:
        {obj.k: obj.v, ...} if obj is dict;
        [0, 1, ...] if obj is list;
        obj.toAnson if obj is Anson;
        else str(obj)
        '''
        def quot(v) -> str:
            return f'"{v}"' if type(v) == str else f'"{v.toBlock()}"' if isinstance(v, Anson) else LangExt.to_str(v)
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
    def trunc_right(s: str, byte_nums: int, encoding='utf-8') -> str:
        if byte_nums <= 0:
            return ''
        b = s.encode(encoding)
        truncated = b[-byte_nums:]
        # decode, ignoring any partial multi-byte char left at the start
        return truncated.decode(encoding, errors='ignore')

    @staticmethod
    def musteqs(a: str, b: str, msg = None):
        if a != b:
            from .anson import AnsonException
            raise AnsonException(0, f'{a} != {b}' if msg == None else msg)

    @staticmethod
    def only_wordextlen(likely: str, ext='', minlen = 0, maxlen = -1):
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


    @classmethod
    def str(cls, obj):
        '''
        @deprecated
        This function name is not a good choice. Uset str_() instead.
        :param obj:
        :return:
        '''
        return LangExt.to_str(obj)

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
    def update_patterns(file, patterns: dict, replaced_vals: Optional[dict]=None):
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

        with open(file, 'r', encoding='utf-8', newline='\n') as f:
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

        with open(file, 'w', encoding='utf-8', newline='\n') as f:
            f.writelines(lines)

        print(f'[{cnt / len(patterns)}] lines updated. Patterns updating finsied.', file)

        return replaced_vals

    @classmethod
    def writeline_nl(cls, file: str, lines: list[str]):
        with open(file, 'w+', encoding='utf-8', newline='\n') as f:
            for l in lines:
                f.write(l)
                f.write('\n')

    @classmethod
    def rm_any(cls, res: Union[str, Path, List[Union[str, Path]]], verbose=True):
        if isinstance(res, list):
            for r in res:
                cls.rm_any(r)
        try:
            if os.path.isfile(res):
                if verbose: print("Removing file: ", res)
                os.remove(res)
            else:
                if verbose: print("Removing tree: ", res)
                shutil.rmtree(res, ignore_errors=False)
            print(f"Successfully removed {res}")
        except FileNotFoundError:
            if verbose: print("FileNotFoundError:", res)
            pass
        except PermissionError:
            print(f"Permission denied: Unable to remove {res}")
        except OSError as e:
            if e.errno != errno.ENOENT:  # Ignore "No such file or directory" errors
                if verbose: print("OSError:", res)
                pass
            else:
                print(f"Path {res} does not exist")

    @classmethod
    def copy_anyway(cls, src: Union[str, Path, List[Path]], dest: Union[Path, str], log: bool = False) -> Union[Path, List]:
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
                     log: bool = False) -> Union[Path, List]:
        '''
        Credits to Claude.
        :param src:
        :param dest:
        :param overwrite:
        :param log:
        :return: final destination path
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
