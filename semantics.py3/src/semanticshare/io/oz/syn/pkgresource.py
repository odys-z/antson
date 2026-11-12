'''
The manifest of a Portfolio synode package, bin/resource.json in the package.

Generated while packing (jserv-album/tasks.py package(), from SynodeTask.pkg_res()), and read by
the installer and the upgrade (synodepy3 commands.update_srv()), which compares the installed
package's manifest with the new one, and checks the new one accepts the installed version.

It holds nothing private: the build configuration (SynodeTask, tasks.*.json) stays with the build.

Since 0.6.20 the manifest is a source file of its own (jserv-album/src/main/resource.json): upgrade_from,
upgrader and upgrades (ConfigUpgrades) are written by hand, only files and versions come from the build.

@since semantics.py3 0.6.17
'''
import json
import os
import re
from dataclasses import dataclass
from typing import List, Optional, Tuple

from anson.io.odysz.anson import Anson


def ver_tuple(v: str) -> Tuple[int, ...]:
    '''
    :param v: a version, e.g. "0.8.1", or "0.8.1-alpha" (only the numbers count)
    :return: (0, 8, 1)
    '''
    return tuple(int(n) for n in re.findall(r'\d+', v or ''))


def ver_cmp(a: str, b: str) -> int:
    '''
    :return: < 0, 0, > 0 as a is older than, the same as, newer than b ("0.8" == "0.8.0")
    '''
    ta, tb = ver_tuple(a), ver_tuple(b)
    n = max(len(ta), len(tb))
    ta, tb = ta + (0,) * (n - len(ta)), tb + (0,) * (n - len(tb))
    return (ta > tb) - (ta < tb)


class ConfigUpgrades:
    '''
    Upgrades of an installation's configuration files, one entry of a manifest's "upgrades":

        {"since": "0.8.0",
         "files": {"WEB-INF/html-service.json": {
            "replace": {"io.oz.srv.ResPath": "io.oz.srv.StaticResource"},
            "add": {"dynaResource": {"type": "io.oz.srv.DynaResource",
                                     "synode": "http://127.0.0.1:{settings.port}/jserv-album"}}}}}

    An upgrade keeps the installed configuration files (settings.json, html-service.json), so what a
    new version needs in them is told here, and applied to the kept files (PkgResources.upgrade_configs()).

    since: the minimum installed version: the entry applies to installations of this version or newer.
    Entries accumulate, the oldest first, and are applied in that order, at every upgrade: applying one
    again changes nothing.

    files: {file path relative to the installation folder: {"replace": {...}, "add": {...}}}
    - replace {old: new}: a renamed value or key, e.g. a type; only whole json strings are replaced;
    - add: values a new version needs. A missing key is added, with its value; an existing one is kept,
      whatever its value, except: an object is completed the same way, key by key, and a list gets the
      items it doesn't have. A string value can refer the installation's settings, as {settings.port}.

    Plain json, not parsed as Anson: the values of "add" are other applications' types.

    @since semantics.py3 0.6.20
    '''
    since: str
    files: dict

    def __init__(self, since: str = None, files: dict = None):
        self.since = since
        self.files = files or {}

    def applies(self, installed: Optional[str]) -> bool:
        '''
        :param installed: the installed version (jar_ver); unknown (None): every entry applies
        '''
        return not self.since or not installed or ver_cmp(installed, self.since) >= 0

    def apply(self, basedir: str = '.', env: dict = None) -> List[str]:
        '''
        Apply to the files in basedir. A file is written only if changed, and not at all if any of
        its values can't be resolved; a missing file is skipped.
        :param env: values of the installation, for values like "{settings.port}": {"settings.port": 8964}
        :return: paths (as in files) of the files changed
        :raise ValueError: a file is not json, or a value refers something not in env
        '''
        changed = []
        for path, ops in self.files.items():
            fpath = os.path.join(basedir, path)
            if not os.path.isfile(fpath):
                continue
            with open(fpath, encoding='utf-8') as f:
                text0 = f.read()

            text = text0
            for old, new in (ops.get('replace') or {}).items():
                text = text.replace(json.dumps(old), json.dumps(new))

            add = ops.get('add')
            if add:
                try: cfg = json.loads(text)
                except ValueError as e:
                    raise ValueError(f'{path} is not json: {e}')
                if ConfigUpgrades.complete(cfg, ConfigUpgrades.resolve(add, env or {}, path)):
                    text = json.dumps(cfg, indent=2, ensure_ascii=False) + '\n'

            if text != text0:
                with open(fpath, 'w', encoding='utf-8') as f:
                    f.write(text)
                changed.append(path)
        return changed

    @staticmethod
    def resolve(v, env: dict, path: str = ''):
        '''
        :return: v with "{name}" in its strings replaced by env[name]
        :raise ValueError: a name is not in env
        '''
        if isinstance(v, dict):
            return {k: ConfigUpgrades.resolve(x, env, path) for k, x in v.items()}
        if isinstance(v, list):
            return [ConfigUpgrades.resolve(x, env, path) for x in v]
        if isinstance(v, str):
            def val(m):
                if m.group(1) not in env:
                    raise ValueError(f'{path}: "{v}" refers {m.group(1)}, which is not a known value of the installation')
                return str(env[m.group(1)])
            return re.sub(r'\{([A-Za-z_][\w.]*)\}', val, v)
        return v

    @staticmethod
    def complete(cfg: dict, add: dict) -> bool:
        '''
        Add what cfg doesn't have, see the class's doc.
        :return: True if cfg is changed
        '''
        changed = False
        for k, v in add.items():
            if k not in cfg or cfg[k] is None and v is not None:
                cfg[k] = v
                changed = True
            elif isinstance(v, dict) and isinstance(cfg[k], dict):
                changed = ConfigUpgrades.complete(cfg[k], v) or changed
            elif isinstance(v, list) and isinstance(cfg[k], list):
                for item in v:
                    if item not in cfg[k]:
                        cfg[k].append(item)
                        changed = True
        return changed


@dataclass
class PkgResources(Anson):
    files: dict
    '''
    {item: file path relative to the installation folder}, items:
    "synode": bin/jserv-album-#.jar, "html-web": bin/html-web-#.jar, "synode.py": bin/synode_py3-#.whl
    '''

    versions: dict
    '''
    {"jar_ver": the Portfolio version, of jserv-album-#.jar; "web_ver": web-dist's;
     "html_srver": html-web-#.jar's; "synodepy3": synode.py3's}.
    The Windows service names are made of jar_ver and web_ver.
    '''

    upgrade_from: dict
    '''
    The installed versions (jar_ver) this package can upgrade, {"min": "0.8.0", "max": "0.8.9"}, both
    inclusive and optional. Empty: any version not newer than this one.
    '''

    upgrader: dict
    '''
    The upgrade tool (synodepy3) this package needs, {"min": "0.8.2"}: an older tool doesn't know what
    the package needs done, e.g. its upgrades. Empty: any.
    @since semantics.py3 0.6.20
    '''

    # upgrades: List[ConfigUpgrades], the upgrades of the installed configuration files, the oldest first.
    # Not a field of the dataclass: it's plain json, read by load(), never parsed or written as Anson.

    def __init__(self, files: dict = None, versions: dict = None, upgrade_from: dict = None,
                 upgrader: dict = None, upgrades: list = None):
        super().__init__()
        self.files = files or {}
        self.versions = versions or {}
        self.upgrade_from = upgrade_from or {}
        self.upgrader = upgrader or {}
        self.upgrades = upgrades or []

    @staticmethod
    def load(path: str) -> 'PkgResources':
        '''
        Load a manifest, bin/resource.json, with its upgrades (see ConfigUpgrades).
        Use this, not Anson.from_file(), which would take the upgrades' values for its own types.
        @since semantics.py3 0.6.20
        '''
        with open(path, encoding='utf-8') as f:
            obj = json.load(f)
        upgrades = obj.pop('upgrades', None) or []
        res = Anson.from_envelope(obj)
        res.upgrades = [ConfigUpgrades(u.get('since'), u.get('files')) for u in upgrades
                        if isinstance(u, dict)]
        return res

    def refuses_upgrader(self, tool_ver: str) -> Optional[str]:
        '''
        Whether an upgrade tool of version tool_ver (synodepy3) can install this package.
        :return: None if it can, else why not
        @since semantics.py3 0.6.20
        '''
        lo = (self.upgrader or {}).get('min')
        if lo and ver_cmp(tool_ver, lo) < 0:
            return f'this upgrade tool {tool_ver} is older than {lo}, the oldest that installs package {self.jar_ver()}'
        return None

    def upgrade_configs(self, installed: Optional[str], basedir: str = '.', env: dict = None) -> List[str]:
        '''
        Apply the upgrades of configuration files, to an installation of version `installed`, in
        basedir: every entry since a version not newer than it, in order (see ConfigUpgrades).
        :return: the files changed
        @since semantics.py3 0.6.20
        '''
        changed = []
        for u in getattr(self, 'upgrades', None) or []:
            if u.applies(installed):
                changed += [f for f in u.apply(basedir, env) if f not in changed]
        return changed

    def file(self, item: str) -> Optional[str]:
        return self.files.get(item)

    def jar_ver(self) -> Optional[str]:
        return self.versions.get('jar_ver')

    def refuses(self, installed: str, force: bool = False) -> Optional[str]:
        '''
        Whether this package can upgrade an installation of version `installed` (its jar_ver).
        :param force: accept anyway (support cases)
        :return: None if it can, else why not
        '''
        if force:
            return None
        lo, hi = self.upgrade_from.get('min'), self.upgrade_from.get('max')
        mine = self.jar_ver()
        if lo and ver_cmp(installed, lo) < 0:
            return f'the installed version {installed} is older than {lo}, the oldest this package {mine} upgrades'
        if hi and ver_cmp(installed, hi) > 0:
            return f'the installed version {installed} is newer than {hi}, the newest this package {mine} upgrades'
        if mine and ver_cmp(installed, mine) > 0:
            return f'the installed version {installed} is newer than this package {mine} (a downgrade)'
        return None
