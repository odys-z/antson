'''
The manifest of a Portfolio synode package, bin/resource.json in the package.

Generated while packing (jserv-album/tasks.py package(), from SynodeTask.pkg_res()), and read by
the installer and the upgrade (synodepy3 commands.update_srv()), which compares the installed
package's manifest with the new one, and checks the new one accepts the installed version.

It holds nothing private: the build configuration (SynodeTask, tasks.*.json) stays with the build.

Since 0.6.20 the manifest is a source file of its own (jserv-album/src/main/resource.json): upgrade_from,
upgrader and config_picks (ConfigPicks) are written by hand, only files and versions come from the build.

@since semantics.py3 0.6.17
'''
import copy
import json
import re
from dataclasses import dataclass
from typing import Any, List, Optional, Tuple

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


class ConfigPicks:
    '''
    How an upgrade makes a configuration file, e.g. WEB-INF/html-service.json, of a manifest's
    "config_picks": the new package's file, its template, with the installed file's values of the
    fields picked.

        "config_picks": {
          "WEB-INF/html-service.json": {
            "welcomepages":           ["welcomepages"],
            "dynaResource.syn_jserv": ["dynaResource.syn_jserv", "dynaResource.synode"]}}

    {file path relative to the installation folder: {field: the names it ever had, the newest first}}.
    A field is a path of keys, e.g. "dynaResource.syn_jserv". The first of its names found in the
    installed file gives the value, so a renamed field gets its old name appended, and no version
    of the installation is needed.

    So everything else is the new version's: new fields and their default values, renamed types,
    fields removed. A file not in config_picks is kept as installed.

    The installed file is read as plain json, never parsed as Anson: an old type name, or a field
    no version knows now, can't fail it. A picked value is not taken if the template's value is of
    another kind (object, list, text, number, bool), nor a null over the template's value.

    @since semantics.py3 0.6.20
    '''

    @staticmethod
    def check(picks: dict):
        '''
        :param picks: a manifest's config_picks
        :raise ValueError: not {path: {field: [names]}}
        '''
        if not isinstance(picks, dict):
            raise ValueError('config_picks must be an object, {file path: {field: [its names]}}')
        for path, fields in picks.items():
            if not isinstance(fields, dict) or not all(
                    isinstance(f, str) and isinstance(ns, list) and ns and all(isinstance(n, str) and n for n in ns)
                    for f, ns in fields.items()):
                raise ValueError(f'config_picks, {path}: must be an object, {{field: [its names, the newest first]}}')

    @staticmethod
    def get(obj, path: str) -> Tuple[bool, Any]:
        '''
        :return: (found, value) of a field, a path of keys like "dynaResource.syn_jserv"
        '''
        for k in path.split('.'):
            if not isinstance(obj, dict) or k not in obj:
                return False, None
            obj = obj[k]
        return True, obj

    @staticmethod
    def set(obj: dict, path: str, v):
        ''' Set a field, a path of keys; a missing object on the path is created. '''
        keys = path.split('.')
        for k in keys[:-1]:
            if not isinstance(obj.get(k), dict):
                obj[k] = {}
            obj = obj[k]
        obj[keys[-1]] = v

    @staticmethod
    def kind(v) -> str:
        return 'null' if v is None else 'object' if isinstance(v, dict) else 'list' if isinstance(v, list) \
            else 'bool' if isinstance(v, bool) else 'number' if isinstance(v, (int, float)) else 'text'

    @staticmethod
    def pick(template: dict, installed: dict, fields: dict) -> Tuple[dict, List[str], List[str]]:
        '''
        :param template: the new package's file
        :param installed: the installed file
        :param fields: {field: [its names]}
        :return: (the new file, the fields picked, the fields not picked, with why)
        '''
        cfg = copy.deepcopy(template)
        kept, skipped = [], []
        for field, names in fields.items():
            name, found, v = None, False, None
            for name in names:
                found, v = ConfigPicks.get(installed, name)
                if found:
                    break
            if not found:
                continue

            has, t = ConfigPicks.get(template, field)
            if has and t is not None and ConfigPicks.kind(v) != ConfigPicks.kind(t):
                skipped.append(f'{name} (yours is {ConfigPicks.kind(v)}, now {ConfigPicks.kind(t)})')
                continue

            ConfigPicks.set(cfg, field, copy.deepcopy(v))
            kept.append(field if name == field else f'{field} (was {name})')
        return cfg, kept, skipped


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
    the package needs done, e.g. its config_picks. Empty: any.
    @since semantics.py3 0.6.20
    '''

    # config_picks: {path: {field: [names]}}, the configuration files an upgrade makes from the package's
    # template, see ConfigPicks. Not a field of the dataclass: plain json, read by load(), never parsed as Anson.

    def __init__(self, files: dict = None, versions: dict = None, upgrade_from: dict = None,
                 upgrader: dict = None, config_picks: dict = None):
        super().__init__()
        self.files = files or {}
        self.versions = versions or {}
        self.upgrade_from = upgrade_from or {}
        self.upgrader = upgrader or {}
        self.config_picks = config_picks or {}

    @staticmethod
    def load(path: str) -> 'PkgResources':
        '''
        Load a manifest, bin/resource.json, with its config_picks (see ConfigPicks).
        :raise ValueError: config_picks is written wrongly
        @since semantics.py3 0.6.20
        '''
        with open(path, encoding='utf-8') as f:
            obj = json.load(f)
        picks = obj.pop('config_picks', None) or {}
        ConfigPicks.check(picks)
        res = Anson.from_envelope(obj)
        res.config_picks = {p: fs for p, fs in picks.items() if not p.lstrip().startswith('//')}
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

    def pick_config(self, path: str, template: str, installed: str) -> Tuple[List[str], List[str]]:
        '''
        Make a configuration file of config_picks: the template with the installed values picked,
        written over the template (see ConfigPicks).
        :param path: the file, a key of config_picks, e.g. "WEB-INF/html-service.json"
        :param template: the new package's file, written with the result
        :param installed: the installed file, e.g. its backup
        :return: (the fields picked, the fields not picked, with why); an installed file that is not
        a json object gives nothing
        :raise ValueError: path is not in config_picks, or the template is not a json object
        @since semantics.py3 0.6.20
        '''
        if path not in self.config_picks:
            raise ValueError(f'{path} is not in config_picks')
        with open(template, encoding='utf-8') as f:
            tmpl = json.load(f)
        if not isinstance(tmpl, dict):
            raise ValueError(f'the package\'s {path} is not a json object')
        try:
            with open(installed, encoding='utf-8') as f:
                mine = json.load(f)
        except ValueError:
            mine = None
        if not isinstance(mine, dict):
            return [], ['all (yours is not a json object)']

        cfg, kept, skipped = ConfigPicks.pick(tmpl, mine, self.config_picks[path])
        with open(template, 'w', encoding='utf-8') as f:
            json.dump(cfg, f, indent=2, ensure_ascii=False)
            f.write('\n')
        return kept, skipped

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
