'''
Configuration of invoke tasks. All the configuration here only change the built packages.
'''
from dataclasses import dataclass

from anson.io.odysz.anson import Anson

@dataclass
class DeployInfo(Anson):
    '''
    Synode Client for Deploying
    '''
    market: str
    orgid: str
    '''
    E.g. riped, for domain id like riped-1, and so on.
    '''

    dom_nodes: int
    '''
    The domain initial nodes
    '''

    default_pswd: str

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

    def __init__(self):
        super().__init__()
