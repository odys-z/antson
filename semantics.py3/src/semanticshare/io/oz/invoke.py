'''
Configuration of invoke tasks. All the configuration here only change the built packages.
'''

from dataclasses import dataclass

from anson.io.odysz.anson import Anson
from semanticshare.io.oz.register.central import CentralSettings
from semanticshare.io.odysz.semantic.jsession import JUser


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
    post_cmds: list[str]

    def __init__(self):
        super().__init__()

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

def requir_pkg(pkg_name: str, require_ver: str):
    try:
        pkg_version = version(pkg_name.replace('.', '_'))
    except PackageNotFoundError:
        pkg_version = "uninstalled" 
    print (f"{pkg_name}: ", pkg_version)

    if Version(pkg_version) < Version(require_ver):
        print(f'Please upgrade {pkg_name} to version {require_ver} or above. Current version: {pkg_version}')
        import sys
        sys.exit(1)
