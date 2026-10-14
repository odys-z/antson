'''
The manifest of a Portfolio synode package, bin/resource.json in the package.

Generated while packing (jserv-album/tasks.py package(), from SynodeTask.pkg_res()), and read by
the installer and the upgrade (synodepy3 commands.update_srv()), which compares the installed
package's manifest with the new one, and checks the new one accepts the installed version.

It holds nothing private: the build configuration (SynodeTask, tasks.*.json) stays with the build.

@since semantics.py3 0.6.17
'''
import re
from dataclasses import dataclass
from typing import Optional, Tuple

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

    def __init__(self, files: dict = None, versions: dict = None, upgrade_from: dict = None):
        super().__init__()
        self.files = files or {}
        self.versions = versions or {}
        self.upgrade_from = upgrade_from or {}

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
