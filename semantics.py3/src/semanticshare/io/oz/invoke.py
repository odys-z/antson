'''
Configuration of invoke tasks. All the configuration here only change the built packages.
'''

import platform
import urllib.request
import zipfile
import os
from pathlib import Path

from dataclasses import dataclass

from anson.io.odysz.anson import Anson
from semanticshare.io.oz.register.central import CentralSettings

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
