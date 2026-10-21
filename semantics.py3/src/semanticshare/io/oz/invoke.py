'''
Configuration of invoke tasks. All the configuration here only change the built packages.
'''

from dataclasses import dataclass
import json
import re
import shutil
import sys
import os
from typing import Union, List, Optional
from pathlib import Path
from invoke import Context

from anson.io.odysz.anson import Anson
from anson.io.odysz.common import LangExt, Utils
from semanticshare.io.oz.register.central import CentralSettings
from semanticshare.io.oz.syn.pkgresource import PkgResources
from semanticshare.io.odysz.semantic.jsession import JUser


@dataclass
class DeployInfo(Anson):
    '''
    Synode Client for Deploying
    '''

    # synode.json
    mirror_path: dict
    '''
    task.json -> synodepy3.synode.json/{lang-id: {jre_mirror: "value to be replaced"}}
    '''
    central_iport: str
    '''
    task.json -> settings.json
    '''
    central_path: str
    centralUid: str
    '''
    Central (registry) user id, task.json -> desktop app-settings.json/centralUid,
    the same name as io.odysz.jclient.AnclientSettings.centralUid.
    Defaults to 'admin' for task jsons before semantics.py3 0.6.11.
    '''
    central_pswd: str
    web_port: int
    jserv_port: int
    ws_port: int
    market: str
    market_id: str
    orgid: str
    '''
    E.g. riped, for domain id generation like riped-1, and so on.
    '''

    dom_nodes: int
    '''
    The domain initial nodes
    '''

    domain: str
    '''
    The default domain of orgid, task.json -> registry/dictionary.json config.domain.
    Blank: the installers select the community's first domain in the central.
    @since 0.8.0
    '''

    syn_admin_pswd: str
    '''
    deprecated since 0.5.6
    '''
    admin: str
    domain_token: str

    ui: str
    '''
    Ui name for the language, say ui_form.en.py
    '''

    lang: str
    langs: dict
    root_key: str

    def __init__(self):
        super().__init__()
        self.ui = 'ui_form.py'
        self.lang = 'en'
        self.centralUid = 'github'
        self.domain = ''

@dataclass
class BashCmd(Anson):
    '''
    Bash command configuration
    '''
    remarks: str
    vars: dict
    cmd: str

    def __init__(self, cmd: str = ''):
        super().__init__()
        self.vars = {}
        self.cmd = cmd

@dataclass
class ScpCmd(Anson):
    '''
    SCP command configuration
    '''

    host: str
    user: str
    remote_dir: str
    pswd: Union[str, None]
    port: Union[int, None]

    def __init__(self):
        super().__init__()
        self.port = 22

@dataclass
class LandingSite(Anson):
    redirector: str
    re_links  : str
    remot_path: str
    dist_path : str
    post_scp  : ScpCmd

    def __init__(self):
        super().__init__()

@dataclass
class GhRelease(Anson):
    '''
    The GitHub release the built packages are uploaded to with gh (GitHub CLI), see SynodeTask.gh_upload().
    @since semantics.py3 0.6.17
    '''

    repo: str
    '''
    E.g. odys-z/semantic-jserv. Blank (the default): not to be uploaded. A package carries its task json's
    central password and root key (tasks.py config, settings.json), so only a task json made for a public
    release, e.g. tasks.closed-test.json, configures it.
    '''
    tag: str
    '''
    The release's tag, which is not mapped to the package version.
    Blank: the repository's latest release (the newest published, not a prerelease or draft).
    '''

    def __init__(self):
        super().__init__()
        self.repo = ''
        self.tag = ''


def sha256_digest(f: Path) -> str:
    '''
    :return: 'sha256:<hex>', the format of a GitHub release asset's digest
    '''
    import hashlib
    h = hashlib.sha256()
    with open(f, 'rb') as fi:
        for chunk in iter(lambda: fi.read(1 << 20), b''):
            h.update(chunk)
    return f'sha256:{h.hexdigest()}'


def gh_upload_asset(upload_url: str, token: str, f: Path, name: str, proxy: str = '') -> dict:
    '''
    POST f to a GitHub release's upload_url as the asset name, printing the percentage (every 0.1%), size and speed.
    :param upload_url: the release's upload_url, without the {?name,label} template
    :param token: e.g. from gh auth token
    :param proxy: e.g. http://127.0.0.1:7890, see SynodeTask.gh_proxy(); blank: no proxy (not the system's)
    :return: the asset created (json)
    @since semantics.py3 0.6.17
    '''
    import time
    import urllib.parse
    import urllib.request

    size = f.stat().st_size

    class Progress:
        ''' The request body, read by http.client chunk by chunk while sending. '''
        def __init__(self, fi):
            self.fi, self.sent, self.shown, self.t0 = fi, 0, -1, time.monotonic()

        def read(self, n: int = -1) -> bytes:
            b = self.fi.read(n if n and n > 0 else 1 << 16)
            self.sent += len(b)
            permille = self.sent * 1000 // size if size else 1000  # updated every 0.1%
            if permille != self.shown:
                self.shown = permille
                mib, secs = 1 << 20, max(time.monotonic() - self.t0, 1e-3)
                print(f'\r  {permille / 10:5.1f}%  {self.sent / mib:7.1f} / {size / mib:.1f} MiB  {self.sent / mib / secs:5.1f} MiB/s',
                      end='', flush=True)
            return b

    with open(f, 'rb') as fi:
        req = urllib.request.Request(
            f'{upload_url}?{urllib.parse.urlencode({"name": name})}', data=Progress(fi), method='POST',
            headers={'Authorization': f'Bearer {token}', 'Accept': 'application/vnd.github+json',
                     'Content-Type': 'application/octet-stream', 'Content-Length': str(size)})
        opener = urllib.request.build_opener(urllib.request.ProxyHandler({'https': proxy} if proxy else {}))
        try:
            with opener.open(req) as resp:
                return json.loads(resp.read().decode('utf-8'))
        finally:
            print()


class TaskCredentials():
    credentials: dict
    '''
        {host: {user: pswd}}
    '''
    def __init__(self):
        super().__init__()

        cred_path = f'{Path.home()}/.tasksrc.json'
        if os.path.isfile(cred_path):
            print('Global Tasks Credentials:', cred_path)
            with open(cred_path, 'r') as file:
                self.credentials = json.load(file)
        else:
            print('Task Credentials not found:', cred_path)
            self.credentials = {}

    
    def find_pswd(self, scpcmd: Optional[ScpCmd] = None):
        
        if scpcmd is not None and scpcmd.pswd is not None:
            return scpcmd.pswd

        if self.credentials is not None and scpcmd.host in self.credentials:
            if scpcmd.user in self.credentials[scpcmd.host]:
                return self.credentials[scpcmd.host][scpcmd.user]

        print(f'File {Path.home()}/.taskrs.json can be used for configuring remote password.')
        print('''Example: { "host": { "user": "pswd" } }''')
        return input(f'Enter password for {scpcmd.user}@{scpcmd.host}: ')

task_credentials: TaskCredentials = TaskCredentials()

_temp_ = 'temp'

link_json = 'link-json'
'''
gitprjs key of a linked json file, whose gitprjs are included, e.g. {"link-json": "./tasks.0.8.0.json"}.
'''

_linked_gitprjs = {}
'''
Cache of linked json files' gitprjs, {abs-path: resolved gitprjs}.
'''


def resolve_gitprjs(gitprjs: dict, base: str = '.', _linking: tuple = ()) -> dict:
    '''
    Resolve gitprjs[link_json]: the linked json's gitprjs (which can link further), overridden by
    the entries here. Keys of comments, starting with "//", are ignored.
    :param gitprjs: SynodeTask.gitprjs, or a linked json's
    :param base: folder the link path is relative to; '.' (the working folder) for the task json,
                 or the linking json's folder for a linked one
    :return: {project: path}, without link_json
    '''
    gitprjs = gitprjs or {}
    resolved = {}
    if link_json in gitprjs:
        linked = os.path.abspath(os.path.join(base, gitprjs[link_json]))
        if linked in _linking:
            Utils.warn('Circular gitprjs[{}]: {}', link_json, ' -> '.join(_linking + (linked,)))
            sys.exit(-1)
        if linked not in _linked_gitprjs:
            if not os.path.isfile(linked):
                Utils.warn('gitprjs[{}] not found: {}', link_json, linked)
                sys.exit(-1)
            with open(linked, 'r', encoding='utf-8') as jf:
                _linked_gitprjs[linked] = resolve_gitprjs(
                    json.load(jf).get('gitprjs', {}), os.path.dirname(linked), _linking + (linked,))
        resolved.update(_linked_gitprjs[linked])

    resolved.update({k: v for k, v in gitprjs.items() if k != link_json and not k.lstrip().startswith('//')})
    return resolved


@dataclass
class SynodeTask(Anson):
    '''
    The Portfolio 0.8 invoke tasks' configuration
    @since Portfolio 0.7
    TODO rename to PortfolioTask
    '''

    version: str
    '''
    The Portfolio (synode package) version, also of jserv-album-#.#.#.jar.
    '''
    ver_synodepy3: str
    '''
    The version of synode.py3 (package synodepy3, wheel synode_py3-#.#.#-py3-none-any.whl), which can be
    released on its own, e.g. as the upgrade tool. If not configured, it's the version, see synodepy3_ver().
    @since semantics.py3 0.6.16
    '''
    apk_ver: str
    html_jar_v: str
    web_ver: str
    desktop_ver: str
    ipcagent_ver: str

    upgrade_from: dict
    '''
    The installed versions the package can upgrade, {"min": "0.8.0", "max": "0.8.9"}, both inclusive
    and optional, written into the package's manifest (PkgResources.upgrade_from, bin/resource.json).
    Not configured: any version not newer than this package's.
    @since semantics.py3 0.6.17
    '''

    web_inf_dir: str
    '''
        Synode 'WEB-INF': 'src/main/webapp/WEB-INF-0.7/*', 
    '''
    jre_release: str
    jre_name: str
    java_home: str
    host_json: str
    vol_files: dict
    vol_resource: dict
    registry_dir: str
    web_root_dir: str
    '''
    @deprecated since semantics.py3 0.6.14, replaced by gitprjs['album-web']
    '''
    android_dir: str
    '''
    @deprecated since semantics.py3 0.6.14, replaced by gitprjs['album-android']
    '''
    ipcagent_dir: str
    '''
    @deprecated since semantics.py3 0.6.14, replaced by gitprjs['album-wsagent']
    '''
    desktop_dir: str
    '''
    @deprecated since semantics.py3 0.6.14, replaced by gitprjs['album-desktop']
    '''
    desktop_dist_dir: str
    central_dir: str
    '''
    @deprecated since semantics.py3 0.6.14, replaced by gitprjs['registry-central']
    '''
    package_dir: str

    github: str
    '''
    The local github root, the folder where all source projects are cloned, e.g. "../..".
    Relative to the task json's folder, which is also the working folder of the invoke tasks.
    '''
    gitprjs: dict
    '''
    Source projects, {project: path}, where "{github}" in path is replaced with github, e.g.
    {"semantic-DA": "{github}/semantic-DA/semantic.DA", "album-web": "{github}/anclient/examples/example.js/album"}.
    A path is where the project's building file is, e.g. pom.xml, build.gradle, CMakeLists.txt, pyproject.toml.
    {"link-json": "./tasks.0.8.0.json"} includes another json's gitprjs, see resolve_gitprjs().
    Use prjs() for all the projects, and git_prj() to get a project's (sub-)path.
    '''

    deploy: DeployInfo
    '''
    E.g. x64_windows, used in final zip name for distinguished packages of different runtime.
    '''

    build_zip: str
    '''
    The final zip (relativ-)path.file-name.zip. This is a runtime value and not configurable.
    '''
    download_root: str
    '''
    Used for compose the download url of the built zip in host.json:
    {synodesetups: {
        "orgid": [
        "download 0, {download_root}/zip_name, e.g. http://127.0.0.1/html-service-synodes/synode-0.7.8-x64-windows-alpha-zsu.zip",
        ...]} } 
    
    TODO: move 'resources.apk' in host.json/resources to clients.apk?
    '''
    deploy_cmds: List[BashCmd]
    deploy_scps: List[ScpCmd]

    landings: List[LandingSite]

    gh_release: GhRelease
    '''
    The GitHub release gh_upload() uploads dist_assets() to.
    @since semantics.py3 0.6.17
    '''

    backings: dict
    '''
    An ignored json field for deserilization.
    '''

    since: str = '0.5.6 2026-08-11'

    def __init__(self):
        super().__init__()
        self.backings = {}
        self.upgrade_from = {}
        self.github = '../..'
        self.gitprjs = {}
        self.web_root_dir = '../../anclient/examples/example.js/album'
        self.desktop_dist_dir = 'qt-build/dist'
        self.package_dir = f'build-{self.version if hasattr(self, "version") and not LangExt.isblank(self.version) else "1.0.0"}'
        self.gh_release = GhRelease()

    def synodepy3_ver(self) -> str:
        '''
        :return: ver_synodepy3, or version for task jsons without it.
        '''
        v = getattr(self, 'ver_synodepy3', None)
        return self.version if LangExt.isblank(v) else v

    def synodepy3_whl(self) -> str:
        '''
        :return: the synode.py3 wheel's file name, synode_py3-{synodepy3_ver()}-py3-none-any.whl.
                 pip reads the version from it, so it must be kept when the wheel is copied.
        '''
        return f'synode_py3-{self.synodepy3_ver()}-py3-none-any.whl'

    def synode_jar(self) -> str:
        '''
        :return: the synode's (jserv-album) jar file name, jserv-album-{version}.jar
        '''
        return f'jserv-album-{self.version}.jar'

    def htmlweb_jar(self) -> str:
        '''
        :return: the web service's (html-service) jar file name, html-web-{html_jar_v}.jar
        '''
        return f'html-web-{self.html_jar_v}.jar'

    def pkg_res(self) -> PkgResources:
        '''
        The package's manifest, written to bin/resource.json while packing: the files' paths in the
        package (the same names they are packed with), the versions, and upgrade_from.
        @since semantics.py3 0.6.17
        '''
        return PkgResources(
            files={'synode': f'bin/{self.synode_jar()}',
                   'html-web': f'bin/{self.htmlweb_jar()}',
                   'synode.py': f'bin/{self.synodepy3_whl()}'},
            versions={'jar_ver': self.version, 'web_ver': self.web_ver,
                      'html_srver': self.html_jar_v, 'synodepy3': self.synodepy3_ver()},
            upgrade_from=dict(getattr(self, 'upgrade_from', None) or {}))

    def git_prj(self, prj: str, *subpaths: str) -> str:
        '''
        :param prj: key in gitprjs
        :param subpaths: optional sub-paths in the project
        :return: e.g. with gitprjs = {"anclient.py3": "{github}/anclient/py3"},
                 git_prj('anclient.py3', 'dist') -> '../../anclient/py3/dist'
        '''
        prjs = self.prjs()
        if prj not in prjs:
            Utils.warn('Source project "{}" is not configured in gitprjs: {}', prj, prjs)
            sys.exit(-1)
        return os.path.join(prjs[prj].replace('{github}', self.github), *subpaths)

    def prjs(self) -> dict:
        '''
        :return: gitprjs with link-json resolved, {project: path (with "{github}" not replaced)}
        '''
        return resolve_gitprjs(self.gitprjs)

    def check_local_resource(self, local_path: Path) -> Path:
        """
        Check if the resource exists locally, if not, call sys.exit(-1).
        Args:
            local_path (str): Local path of the resource to check.
        """
        if not os.path.exists(local_path):
            Utils.warn(f"Resource not found locally: {local_path}. Needing download to{local_path}...")
            sys.exit(-1)
        return local_path

    def config_central(self, central_settings: CentralSettings):
        print(central_settings.market)
        # MEMO set central_path to config.xml/c[k=regist-central]/v
        pass
        '''
        Configure central settings from task configuration
        central_settings.market = self.deploy.market
        central_settings.vol_name = f'VOLUME_{self.deploy.market.upper()}'
        central_settings.volume = f'../{self.registry_dir}/{central_settings.vol_name}'
        central_settings.port = '1990'
        central_settings.conn = 'sys-sqlite'
        central_settings.startHandler = []
        central_settings.rootkey = self.deploy.root_key
        '''
    
    def zip_name(self) -> str:
        '''
        :return: e.g. synode-0.8.0-jre17-alpha-pmking.zip or tar.gz
        '''
        dist_name = f'{self.jre_name if self.jre_name else "online"}-{self.deploy.market_id}-{self.deploy.orgid}'
        return f'synode-{self.version}-{dist_name}.{"zip" if os.name == "nt" else "tar.gz"}'

    def get_apk_name(self):
        '''
        :return: e.g. portfolio-0.5.4.apk
        '''
        return f'portfolio-{self.apk_ver}.apk'

    def deskzip_name(self) -> str:
        '''
        :return: e.g. desktop-0.8.0-alpha-pmking.zip
        '''
        market_org = f'{self.deploy.market_id}-{self.deploy.orgid}'
        return f'desktop-{self.version}-{market_org}.{"zip" if os.name == "nt" else "tar.gz"}'

    def get_distzip(self) -> str:
        return os.path.join(self.package_dir, self.zip_name())

    def get_deskapp_zip(self) -> str:
        '''
        @return: e.g. .../example.slint/app/build-0.8.0/desktop-0.8.0-alpha-pmking.zip
                      .../example.slint/app/build-0.8.0/desktop-0.8.0-alpha-pmking.tar.gz
        '''
        return os.path.join(self.git_prj('album-desktop'), self.package_dir, self.deskzip_name())

    def get_gradleprj_apk(self) -> Path:
        '''
        :return: Path(gitprjs['album-android']) / 'app' / 'build' / 'outputs' / 'apk' / 'release' / 'app-release.apk'
        (must keep consist with gradle project settings)
        '''
        return Path(self.git_prj('album-android')) / 'app' / 'build' / 'outputs' / 'apk' / 'release' / 'app-release.apk'

    def dist_assets(self) -> List[Path]:
        '''
        The packages package() makes in package_dir, the folder of the final results (the clean task
        runs first, so only one version is there), pushed by tasks.py run_scps() and uploaded by gh_upload().
        @since semantics.py3 0.6.17
        '''
        files = [Path(self.get_distzip()), Path(self.package_dir) / self.get_apk_name()]
        if os.name == 'nt':  # not posix 0.8.0
            files.append(Path(self.package_dir) / self.deskzip_name())  # package() copies get_deskapp_zip() here
        return files

    @staticmethod
    def gh_proxy() -> tuple:
        '''
        The proxy for gh and the uploading, the first configured of
        "https_proxy" in ~/.tasksrc.json, $HTTPS_PROXY, git config https.proxy / http.proxy.
        The Windows system proxy is not used (gh doesn't either).
        :return: (proxy, where it's configured), ('', 'none') for no proxy
        @since semantics.py3 0.6.17
        '''
        import subprocess

        rc = task_credentials.credentials.get('https_proxy')
        if not LangExt.isblank(rc):
            return rc, f'{Path.home()}/.tasksrc.json'
        for v in ('HTTPS_PROXY', 'https_proxy'):
            if not LangExt.isblank(os.environ.get(v)):
                return os.environ[v], f'${v}'
        for k in ('https.proxy', 'http.proxy'):
            r = subprocess.run(['git', 'config', '--get', k], capture_output=True, text=True)
            if r.returncode == 0 and r.stdout.strip():
                return r.stdout.strip(), f'git config {k}'
        return '', 'none'

    def gh_upload(self, dry_run: bool = False, no_proxy: bool = False) -> int:
        '''
        Upload dist_assets() to the GitHub release gh_release, with gh (GitHub CLI, logged in with gh auth login).
        An asset of the same name and sha256 digest is skipped. A changed one is replaced: the new file is
        uploaded as "<name>.uploading" first (gh_upload_asset(), with the progress), and only then is the old
        one deleted and the new one renamed, so a failed upload leaves the old asset in place.
        Each uploaded asset is labelled "<name> @ <commit>", the git head of the working folder.
        Assets of other versions in the release are not touched.
        :param dry_run: only print the release and the plan
        :param no_proxy: connect directly, not looking for a proxy (gh_proxy()), and not passing $HTTPS_PROXY to gh
        :return: 0 if ok
        @since semantics.py3 0.6.17
        '''
        rel = self.gh_release
        if LangExt.isblank(rel.repo):
            Utils.warn('gh_release.repo is not configured: the packages of this task json are not to be uploaded.\n'
                       'They carry its central password and root key; configure gh_release only for a public release, e.g.\n'
                       '    "gh_release": {"type": "io.oz.invoke.GhRelease", "repo": "odys-z/semantic-jserv", "tag": ""}')
            return -1

        import subprocess

        proxy, proxy_from = ('', 'no-proxy') if no_proxy else self.gh_proxy()
        if proxy.lower().startswith('socks'):
            Utils.warn('Proxy {} ({}): a socks proxy cannot be used for uploading, configure an http(s) one, e.g. in {}:\n'
                       '    {{"https_proxy": "http://127.0.0.1:7890"}}', proxy, proxy_from, f'{Path.home()}/.tasksrc.json')
            return -1
        env = {k: v for k, v in os.environ.items() if k.lower() != 'https_proxy'}
        if proxy:
            env['HTTPS_PROXY'] = proxy

        def gh(*args: str) -> subprocess.CompletedProcess:
            return subprocess.run(['gh', *args], capture_output=True, text=True, encoding='utf-8', env=env)

        try:
            r = gh('--version')
        except OSError as e:
            r = subprocess.CompletedProcess(['gh', '--version'], -1, '', str(e))
        if r.returncode != 0:
            Utils.warn('gh (GitHub CLI) is needed for uploading the packages, but "gh --version" failed: {}\n'
                       'Install it, then log in once with: gh auth login\n'
                       '    Windows: winget install --id GitHub.cli\n'
                       '    Ubuntu : sudo apt install gh\n'
                       '    Others : https://cli.github.com',
                       r.stderr.strip())
            return -1
        print(r.stdout.splitlines()[0])  # e.g. gh version 2.81.0 (2026-09-30)
        print(f'Proxy: {proxy or "none"} ({proxy_from})')

        api = f'repos/{rel.repo}/releases/' + ('latest' if LangExt.isblank(rel.tag) else f'tags/{rel.tag}')
        r = gh('api', api)
        if r.returncode != 0:
            Utils.warn('Release not found: {}\n{}', api, r.stderr)
            return -1

        release = json.loads(r.stdout)
        tag = release['tag_name']
        remote = {a['name']: a for a in release['assets']}
        print(f'Release: {tag} {release["html_url"]}')

        commit = subprocess.run(['git', 'rev-parse', '--short', 'HEAD'],
                                capture_output=True, text=True).stdout.strip()

        plan = []
        for f in self.dist_assets():
            digest = sha256_digest(f)  # also fails loudly if package() didn't make it
            a = remote.get(f.name)
            action = 'same' if a and a.get('digest') == digest else 'replace' if a else 'new'
            plan.append((f, action))
            print(f'{action:<8} {f}')

        if dry_run:
            return 0

        r = gh('auth', 'token')
        if r.returncode != 0:
            Utils.warn('gh is not logged in, run: gh auth login\n{}', r.stderr)
            return -1
        token = r.stdout.strip()
        upload_url = release['upload_url'].split('{')[0]
        assets = f'repos/{rel.repo}/releases/assets'

        def gh_ok(*args: str) -> bool:
            r = gh(*args)
            if r.returncode != 0:
                Utils.warn('gh {} failed:\n{}', ' '.join(args), r.stderr)
            return r.returncode == 0

        for f, action in plan:
            if action == 'same':
                continue
            temp = f'{f.name}.uploading'
            if temp in remote and not gh_ok('api', '-X', 'DELETE', f'{assets}/{remote[temp]["id"]}'):
                return -1  # left by an aborted upload

            print(f'Uploading {f} ...')
            try:
                asset = gh_upload_asset(upload_url, token, f, temp, proxy)
            except Exception as e:
                Utils.warn('Uploading {} failed: {}\n{}', f, e, e.read().decode('utf-8', 'replace') if hasattr(e, 'read') else '')
                return -1

            if action == 'replace' and not gh_ok('api', '-X', 'DELETE', f'{assets}/{remote[f.name]["id"]}'):
                return -1
            if not gh_ok('api', '-X', 'PATCH', f'{assets}/{asset["id"]}',
                         '-f', f'name={f.name}', '-f', f'label={f.name} @ {commit}'):
                return -1

        print(f'Uploaded to {release["html_url"]}')
        return 0

    def run_deploycmds(self, c: Context, verbose=True):
        '''
        Run self.deploy_cmds.
        deploy_cmds: [{cmd: "cmd template", vars{k: v}}, ...]
            auto args:
                built_zip  : self.get_distzip()
                package_dir: self.package_dir
                zip_name   : self.zip_name()
            
            c.run(formated cmd)
        '''

        ok, err = 0, 0
        if hasattr(self, 'deploy_cmds') and LangExt.len(self.deploy_cmds) > 0:
            print('Executing post build commands...')
            if verbose:
                print(f"""\t\tcommands format:
                [{{cmd: "string template", vars{{k: v}}, ...]
                with default / named args include: 
                    built_zip  : {self.get_distzip()}
                    package_dir: {self.package_dir}
                    zip_name   : {self.zip_name()}""")
            for bashcmd in self.deploy_cmds:
                if verbose: print('cmd-template:', bashcmd.cmd)

                ccli = bashcmd.cmd.format(built_zip=self.get_distzip(),
                                          package_dir=self.package_dir,
                                          zip_name=self.zip_name(),
                                          **bashcmd.vars)

                print(f'Executing: {ccli}')
                if os.name != 'nt':
                    ret = c.run(ccli, pty=True)
                else:
                    ret = c.run(ccli)
                print('OK:', ret.ok, ret.stderr)
                ok = ok + 1
        else: 
            print('No post commands in property [deploy_cmds] are configured.')
            err = err + 1
        return ok, err
 
    def run_deployscps(self, dist_zip: str):
        if LangExt.isblank(dist_zip):
            Utils.warn("*** ERROR *** dest_zip is empty. Nothing to push with scp.")
            return
            
        if not hasattr(self, 'deploy_scps'):
            print('No post SCPs configured.')
            return

        scplen = LangExt.len(self.deploy_scps)
        print(f'Executing post build SCPs, len = {scplen}...')
        if scplen > 0:
            self.scp_pushs(local_path=dist_zip)

    def scp_pushs(self, local_path):
        for cmd in self.deploy_scps:
            self.scp_push(local_path=local_path, cmd=cmd)
    
    def scp_push(self, local_path: str, cmd: ScpCmd):
        try:
            from paramiko import SSHClient, SSHException, WarningPolicy
            from scp import SCPClient
        except ImportError as e:
            print("Only Tested on Python 3.12.9 or later")
            print('ERROR', e)
            print('Please install paramiko and scp packages to enable SCP post build:')
            print('pip install paramiko scp')
            return

        def report_scporg(filename, size, sent):
            percent_complete = float(sent) / float(size) * 100
            print(f'Transferring {filename}: {percent_complete:.2f}% complete', end='\r')
        
        def create_remote_dir_if_not_exists(sftp, remote_dir):
            try:
                sftp.chdir()
                remote_dir = re.sub(r'(^\$HOME/?)|(^~/?)', '', remote_dir)
                print("chdir:", remote_dir)
                sftp.chdir(remote_dir)
                return remote_dir
            except FileNotFoundError as fe:
                print(fe)
                print("Remote directory does not exist. Creating:", remote_dir)
                full_path = '/' if remote_dir.startswith('/') else ''
                for ch_dir in remote_dir.split('/'):
                    if ch_dir:
                        full_path += f'{ch_dir}/'
                        print('ch_dir =', full_path)
                        try:
                            sftp.chdir(ch_dir)
                        except IOError as e:
                            print(e)
                            print("creating remote directory:", full_path)
                            sftp.mkdir(ch_dir)
                            sftp.chdir(ch_dir)
                else:
                    return remote_dir

        if sys.version_info.major < 3 or sys.version_info.minor < 10:
            print('SCP post command requires Python 3.10 or above. Use tasks.json/deploy_cmds for this task,\n'
                    f'"deploy_cmds"   : ["scp build-0.7.7/{{zip_name}} [s{cmd.user}@{cmd.host}:{cmd.remote_dir}]"]\n'
                    '# Google AI sys it is widely reported that Python 3.9 has issues with scp packages.')
            return None

        print(f'[SCP] {local_path} -> {cmd.user}@{cmd.host}:{cmd.remote_dir} ...')

        password = None
        try: 
            password = task_credentials.find_pswd(cmd)
        except Exception as e:
            Utils.warn("find_pswd failed: {}", e)

        with SSHClient() as ssh:
            ssh.load_system_host_keys()
            ssh.set_missing_host_key_policy(WarningPolicy())
            try:
                ssh.connect(cmd.host, port=cmd.port, username=cmd.user, password=password)
            except SSHException as e:
                Utils.warn("SSHClient connection failed with auto accept host is false." +
                           "\n\tHad you ever tried ssh connection to {} : {} ?",
                           cmd.host, cmd.port)
                Utils.warn(e)
                sys.exit(-1)

            # Use SFTP to manage directories
            sftp_client = ssh.open_sftp()
            norm_dir = create_remote_dir_if_not_exists(sftp_client, cmd.remote_dir)

            with SCPClient(ssh.get_transport(), progress=report_scporg) as scp:
                scp.put(local_path, remote_path=norm_dir)

    def scp_pull(self, target: str, cmd: ScpCmd) -> Union[dict, None]:
        try:
            from paramiko import SSHClient
            from scp import SCPClient, SCPException
        except ImportError as e:
            print('ERROR', e)
            print('Please install paramiko and scp packages to enable SCP post build:')
            print('pip install paramiko scp')
            return None

        def report_scporg(filename, size, sent):
            percent_complete = float(sent) / float(size) * 100
            print(f'Downloading {filename}: {percent_complete:.2f}%', end='\r')
        
        if sys.version_info.major < 3 or sys.version_info.minor < 10:
            print('SCP post command requires Python 3.10 or above. Use tasks.json/deploy_cmds for this task,\n'
                    f'"deploy_cmds"   : ["scp build-0.7.7/{{zip_name}} [s{cmd.user}@{cmd.host}:{cmd.remote_dir}]"]\n'
                    '# Google AI sys it is widely reported that Python 3.9 has issues with scp packages.')
            return None

        print(f'[SCP] {self.get_distzip()} <- {cmd.user}@{cmd.host}:{cmd.remote_dir} ...')
        password = task_credentials.find_pswd(cmd)

        with SSHClient() as ssh:
            ssh.load_system_host_keys()
            ssh.connect(cmd.host, port=cmd.port, username=cmd.user, password=password)

            with SCPClient(ssh.get_transport(), progress=report_scporg) as scp:
                if not os.path.isdir(_temp_):
                    os.mkdir(_temp_)

                local_path = f'{_temp_}/{target}'
                try:
                    scp.get(local_path=local_path, remote_path=f'{cmd.remote_dir}/{target}')
                except SCPException as e: 
                    print(e)
                    return None

                with open(local_path, 'r') as file:
                    return json.load(file)
                
        # return None

    def restore_backups(self):
        for backed, backing in self.backings.items():
            shutil.copy2(backing, backed)
    
    def publish_landings(self):
        import json

        if not hasattr(self, 'landings') or LangExt.len(self.landings) == 0:
            return

        if not os.path.exists(_temp_):
            os.mkdir(_temp_)

        for landing in self.landings:
            # generate redirctor json
            links_path = f'links-{self.deploy.market_id}-{self.deploy.orgid}.json'
            redirector = {'redirect': links_path}

            local_redir_p = f'{_temp_}/{landing.redirector}'
            with open(local_redir_p, 'w') as f:
                json.dump(redirector, f)

            # download links.json
            _images_ = 'images'
            landing.post_scp.remote_dir = f'{landing.remot_path}/{landing.dist_path}'
            links = self.scp_pull(links_path, landing.post_scp)
            if links is not None:
                imgs = links[_images_] if hasattr(links, _images_) else {}
                imgs[self.jre_name] = f'{landing.dist_path}/{self.zip_name()}' # x64_windows = 'res/dist/synode-0.7.8-x64_windows-alpha-qqhome.zip'
                links[_images_] = imgs 
            else:
                links = {}
                links[_images_] = {self.jre_name: f'{landing.dist_path}/{self.zip_name()}'}

            local_links_p = f'{_temp_}/{links_path}'
            with open(local_links_p, 'w') as f:
                json.dump(links, f)

            # push lings.json, redirector.json
            scpcmd = landing.post_scp
            scpcmd.remote_dir = landing.remot_path
            self.scp_push(local_path=local_redir_p, cmd=landing.post_scp)
            self.scp_push(local_path=local_links_p, cmd=landing.post_scp)
            
            scpcmd.remote_dir = f'{landing.remot_path}/{landing.dist_path}'
            self.scp_push(local_path=self.get_distzip(), cmd=landing.post_scp)

@dataclass
class CentralTask(Anson):
    '''
    The Portifolio 0.7 invoke tasks' configuration for central server

    @deprecated since semantics.py3 0.6.11, central users are managed online.
    '''

    users: dict[str, JUser] # ISSUE/FIXME: Anson.py3 0.4.1 cannot handle types in dict.

    def __init__(self):
        super().__init__()
        self.users = {}

