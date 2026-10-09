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
    apk_ver: str
    html_jar_v: str
    web_ver: str
    desktop_ver: str
    ipcagent_ver: str

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

    backings: dict
    '''
    An ignored json field for deserilization.
    '''

    since: str = '0.5.6 2026-08-11'

    def __init__(self):
        super().__init__()
        self.backings = {}
        self.github = '../..'
        self.gitprjs = {}
        self.web_root_dir = '../../anclient/examples/example.js/album'
        self.desktop_dist_dir = 'qt-build/dist'
        self.package_dir = f'build-{self.version if hasattr(self, "version") and not LangExt.isblank(self.version) else "1.0.0"}'

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

