"""
html-service's configuration, WEB-INF/html-service.json: the types of io.oz.srv (github odys-z/html-service).
"""
from dataclasses import dataclass
from typing import Optional

from anson.io.odysz.anson import Anson


@dataclass
class StaticResource(Anson):
    '''
    A static resource path, io.oz.srv.StaticResource (ResPath before html-web 0.2.1).
    @since semantics.py3 0.6.20
    '''
    path: str
    resource: str
    allowDir: bool

    def __init__(self, **kwargs):
        super().__init__()
        self.path, self.resource = kwargs.get('path', '/'), kwargs.get('resource', 'web-dist')
        self.allowDir = kwargs.get('allowDir', False)


@dataclass
class ResPath(StaticResource):
    '''
    @deprecated html-web 0.2.1 renamed it StaticResource. Kept for reading a html-service.json not yet
    upgraded (the manifest's upgrades rename the type, see io.oz.syn.pkgresource.ConfigUpgrades).
    '''
    def __init__(self, **kwargs):
        super().__init__(**kwargs)


@dataclass
class DynaResource(Anson):
    '''
    Dynamic resources, io.oz.srv.DynaResource: the docs published by the local synode.
    @since semantics.py3 0.6.20 (html-web 0.2.1)
    '''
    help: str
    handler: str
    syn_jserv: str
    resource: str
    pollInterval_snd: int
    allowDir: bool
    danyroot: str

    def __init__(self):
        super().__init__()
        self.help = None
        self.handler = 'io.oz.srv.ResPathsTier'
        self.syn_jserv = None
        self.resource = 'respaths'
        self.pollInterval_snd = 3600
        self.allowDir = False
        self.danyroot = 'pub'


@dataclass
class WebConfig(Anson):
    port: int
    paths: list[StaticResource]
    startHandler: Optional[list[str]]
    welcomepages: Optional[list[str]]
    dynaResource: Optional[DynaResource]

    def __init__(self):
        super().__init__()

        self.port = 8900
        self.paths = [StaticResource(path='/', resource='web-dist')]
        self.startHandler = None
        self.welcomepages = None
        self.dynaResource = None
