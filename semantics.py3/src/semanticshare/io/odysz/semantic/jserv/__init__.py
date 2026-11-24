"""
html-service's configuration, WEB-INF/html-service.json, the types of semantic.jserv's io.odysz.semantic.jserv
(github odys-z/semantic-jserv), since package 0.8.3: WebConfig, StaticResource and DynaResource, moved
from html-service's io.oz.srv, where its handler of dynaResource, io.oz.sdoc.htmlsrv.ResPathsTier
(doctree.jserv), is configured.
"""
from dataclasses import dataclass
from typing import Optional

from anson.io.odysz.anson import Anson


@dataclass
class StaticResource(Anson):
    '''
    A static resource path, io.odysz.semantic.jserv.StaticResource (io.oz.srv.StaticResource before
    package 0.8.3, ResPath before html-web 0.2.1).
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
class DynaResource(Anson):
    '''
    Dynamic resources, io.odysz.semantic.jserv.DynaResource (io.oz.srv.DynaResource before package 0.8.3):
    the docs published by the local synode.
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
        self.handler = 'io.oz.sdoc.htmlsrv.ResPathsTier'
        self.syn_jserv = None
        self.resource = 'respaths'
        self.pollInterval_snd = 3600
        self.allowDir = False
        self.danyroot = 'pub'


@dataclass
class WebConfig(Anson):
    '''
    html-service.json, io.odysz.semantic.jserv.WebConfig (io.oz.srv.WebConfig before package 0.8.3).
    '''
    port: int
    paths: list[StaticResource]
    startHandler: Optional[list[str]]
    startHandlers: Optional[list[list[str]]]
    '''
    The handlers run when the service starts, in this order, each [class name, args...], e.g.
    [["io.oz.srv.WebAssetsHandler"], ["io.oz.srv.JservLocalHandler", "private/host.json"]].
    startHandler, the only one before, is still run, first. html-web 0.2.2, semantics.py3 0.6.21.
    '''
    welcomepages: Optional[list[str]]
    dynaResource: Optional[DynaResource]
    notfound: Optional[str]
    '''
    The page of every 404, a path in the server, e.g. "/404.html"; None: Jetty's. html-web 0.2.1.
    '''

    def __init__(self):
        super().__init__()

        self.port = 8900
        self.paths = [StaticResource(path='/', resource='web-dist')]
        self.startHandler = None
        self.startHandlers = None
        self.welcomepages = None
        self.dynaResource = None
        self.notfound = None
