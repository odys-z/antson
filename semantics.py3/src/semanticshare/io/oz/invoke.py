'''
Configuration of invoke tasks. All the configuration here only change the built packages.
'''

import platform

from dataclasses import dataclass

from anson.io.odysz.anson import Anson
from semanticshare.io.oz.register.central import CentralSettings

@dataclass
class Proxy(Anson):
    http: str
    https: str

    def __init__(self):
        super().__init__()

@dataclass
class JRERelease(Anson):
    lazy_flag: str
    
    def __init__(self):
        super().__init__()
        self.lazy_flag = 'wait:'

@dataclass
class Temurin17Release(JRERelease):
    '''
    Resources type of https://github.com/adoptium/temurin17-binaries
    '''
    date: str
    '''
    Mirror upating data
    '''
    src: str
    path: str
    '''
    sub path.
    "https://github.com/{path}/{resources[i]}" should reach the jre/jdk item.
    "https://<mirror-ip>/deploy-path/{resources[i]}" should reach the jre/jdk item at the mirror site.
    '''
    resources: list[str]

    mirroring: list[str]

    lazy: list[str]

    def __init__(self):
        super().__init__()

    def mirror(self):
        pass
    
    def get_resources(self):
        pass

    def jre(self):
        '''
        :return: the jre item needed by current environment
        '''
        system = platform.system()
        machine = platform.machine()

        if system == "Windows":
            os_name = "windows"
            ext = "zip"
        elif system == "Darwin":
            os_name = "mac"
            ext = "tar.gz"
        elif system == "Linux":
            os_name = "linux"
            ext = "tar.gz"
        else:
            raise RuntimeError("Unsupported OS")

        if machine in ("AMD64", "x86_64"):
            arch = "x64"
        elif machine in ("aarch64", "arm64"):
            arch = "aarch64"
        else:
            raise RuntimeError(f"Unsupported arch: {machine}")

        download_url = f'https://github.com/{self.path}'

        build, plus = "17.0.9", "9"
        zip_gz = f"OpenJDK17U-jre_{arch}_{os_name}_hotspot_{build}_{plus}.{ext}"
        return f"{download_url}/jdk-{build}%2B{plus}/{zip_gz}"




@dataclass
class DeployInfo(Anson):
    '''
    Synode Client for Deploying
    '''

    # synode.json
    central_iport: str
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
    host_json: str
    vol_files: dict
    vol_resource: dict
    registry_dir: str
    android_dir: str
    deploy: DeployInfo
    dist_dir: str
    '''
    Replacing dictionary.json/registry/synusers[0].pswd, pattern of tasks.synuser_pswd_pattern
    '''
    web_inf_dir: str

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
