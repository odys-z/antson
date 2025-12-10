'''
Configuration of invoke tasks. All the configuration here only change the built packages.
'''

from dataclasses import dataclass
import re
import shutil
import sys

from anson.io.odysz.anson import Anson
from anson.io.odysz.common import LangExt
from semanticshare.io.oz.register.central import CentralSettings
from semanticshare.io.odysz.semantic.jsession import JUser
import os
from typing import Union
from pathlib import Path


@dataclass
class DeployInfo(Anson):
    '''
    Synode Client for Deploying
    '''

    # synode.json
    mirror_path: str
    '''
    task.json -> synodepy3.synode.json/{lang-id: {jre_mirror: "value to be replaced"}}
    '''
    central_iport: str
    '''
    task.json -> settings.json
    '''
    central_path: str
    central_pswd: str
    web_port: str
    jserv_port: str
    root_key: str
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

    ui: str
    '''
    Ui name for the language, say ui_form.en.py
    '''

    lang: str

    langs: dict

    def __init__(self):
        super().__init__()
        self.ui = 'ui_form.py'
        self.lang = 'en'

@dataclass
class BashCmd(Anson):
    '''
    Bash command configuration
    '''
    remarks: str
    vars: dict
    cmds: str

    def __init__(self):
        super().__init__()
        vars = {}
        cmds = []

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
class SynodeTask(Anson):
    '''
    The Portifolio 0.7 invoke tasks' configuration
    '''

    version: str
    apk_ver: str
    html_jar_v: str
    web_ver: str
    web_inf_dir: str
    jre_release: str
    jre_name: str
    host_json: str
    vol_files: dict
    vol_resource: dict
    registry_dir: str
    android_dir: str
    central_dir: str
    dist_dir: str
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
    post_cmds: list[BashCmd]
    post_scps: list[ScpCmd]

    backings: dict
    '''
    An ignored json field for deserilization.
    '''

    def __init__(self):
        super().__init__()
        self.backings = {}

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
        dist_name = f'{self.jre_name if self.jre_name else "online"}-{self.deploy.market_id}-{self.deploy.orgid}'
        return f'synode-{self.version}-{dist_name}.zip'

    def get_distzip(self) -> Path:
        return os.path.join(self.dist_dir, self.zip_name())

    def run_postcmds(self, c):
        if hasattr(self, 'post_cmds') and LangExt.len(self.post_cmds) > 0:
            print('Executing post build commands...')
            for bashcmd in self.post_cmds:
                for cmd in bashcmd.cmds:
                    print('cmd-config:', cmd)
                    for vk, vv in bashcmd.vars.items():
                        cmd = cmd.replace(f'{{{vk}}}', str(vv))
                    cmd = cmd.format(built_zip=self.get_distzip(), build_dir=self.dist_dir, zip_name=self.zip_name())
                    print(f'Executing: {cmd}')
                    ret = c.run(cmd)
                print('OK:', ret.ok, ret.stderr)
        else: 
            print('No post commands in property [post_cmds] are configured.')
 
    def run_postscps(self):
        if not hasattr(self, 'post_scps'):
            print('No post SCPs configured.')
            return

        scplen = LangExt.len(self.post_scps)
        print('Executing post build SCPs, len = {scplen}...')
        if scplen > 0:
            try:
                from paramiko import SSHClient
                from scp import SCPClient
            except ImportError as e:
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

            for cmd in self.post_scps:
                if sys.version_info.major < 3 or sys.version_info.minor < 10:
                    print('SCP post command requires Python 3.10 or above. Use tasks.json/post_cmds for this task,\n'
                          f'"post_cmds"   : ["scp build-0.7.7/{{zip_name}} [s{cmd.user}@{cmd.host}:{cmd.remote_dir}]"]\n'
                          '# Google AI sys it is widely reported that Python 3.9 has issues with scp packages.')
                    continue

                print(f'[SCP] {self.get_distzip()} -> {cmd.user}@{cmd.host}:{cmd.remote_dir} ...')
                if cmd.pswd is None:
                    password = input(f'Enter password for {cmd.user}@{cmd.host}: ')
                else:
                    password = cmd.pswd

                with SSHClient() as ssh:
                    ssh.load_system_host_keys()
                    ssh.connect(cmd.host, username=cmd.user, password=password)

                    # Use SFTP to manage directories
                    sftp_client = ssh.open_sftp()
                    norm_dir = create_remote_dir_if_not_exists(sftp_client, cmd.remote_dir)

                    with SCPClient(ssh.get_transport(), progress=report_scporg) as scp:
                        scp.put(self.get_distzip(), remote_path=norm_dir)

    def backup(self, target_path: str):
        backed = os.path.join(os.getcwd(), target_path) # '../synode.py/src/synodepy3/synode.json')
        backing = os.path.join('backup', os.path.basename(target_path))
        print('Backing up:', backed, '\n    ->', backing)
        if not os.path.exists('backup'):
            os.makedirs('backup')

        shutil.copy2(backed, backing)
        self.backings[backed] = backing
        return backed
    
    def restore_backups(self):
        for backed, backing in self.backings.items():
            shutil.copy2(backing, backed)

@dataclass
class CentralTask(Anson):
    '''
    The Portifolio 0.7 invoke tasks' configuration for central server
    '''

    users: dict[str, JUser] # ISSUE/FIXME: Anson.py3 0.4.1 cannot handle types in dict.

    def __init__(self):
        super().__init__()
        users = {}

from importlib.metadata import version, PackageNotFoundError
from packaging.version import Version

# requir_pkg("anson.py3", "0.4.3")
# requir_pkg("semantics.py3", "0.4.5")

def requir_pkg(pkg_name: str, require_ver: Union[str, list[str]]):
    '''
    Docstring for requir_pkg
    
    :param pkg_name: package name, e.g. 'cryptography', 'anson.py3', 'semantics.py3', ...
    :type pkg_name: str
    :param require_ver: requred version, str for minimum version,
     list for exact version or version range [min, max]
    :type require_ver: Union[str, list[str]]
    '''
    try:
        pkg_version = version(pkg_name.replace('.', '_').replace('-', '_'))
    except PackageNotFoundError:
        pkg_version = "uninstalled" 
    print (f"{pkg_name}: ", pkg_version)

    if isinstance(require_ver, str):
        if Version(pkg_version) < Version(require_ver):
            print(f'Please upgrade {pkg_name} to version {require_ver} or above. Current version: {pkg_version}')
            import sys
            sys.exit(1)
    elif isinstance(require_ver, list):
        if len(require_ver) == 1:
            if Version(pkg_version) != Version(require_ver[0]):
                print(f'Please install {pkg_name} version {require_ver[0]}. Current version: {pkg_version}')
                import sys
                sys.exit(1)
        else:
            if Version(pkg_version) < Version(require_ver[0]) or Version(pkg_version) > Version(require_ver[1]):
                print(f'Please install {pkg_name} version between {require_ver[0]} and {require_ver[1]}. Current version: {pkg_version}')
                import sys
                sys.exit(1)
