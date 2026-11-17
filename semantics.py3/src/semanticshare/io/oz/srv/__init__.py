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
    @deprecated html-web 0.2.1 renamed it StaticResource. Kept for reading a html-service.json of an older
    version (an upgrade takes the new package's paths, see io.oz.syn.pkgresource.ConfigPicks).
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


@dataclass
class WebAssets(Anson):
    '''
    The list of the files the web pages' build emits, [web root]/webpack-assets.json, io.oz.srv.WebAssets,
    written by the pages' webpack.config.js (AssetsListPlugin), one list for each build, e.g.
    { "type": "io.oz.srv.WebAssets",
      "development": ["login.min.js", "login.min.js.gz", ...], "production": [...] }

    A package carries a listed file with a listed .gz as the .gz only (gz_only(), used by jserv-album's
    tasks.py); html-service unzips these when it starts (io.oz.srv.WebAssetsHandler).
    @since semantics.py3 0.6.21 (html-web 0.2.2)
    '''
    development: Optional[list[str]]
    production: Optional[list[str]]

    json = 'webpack-assets.json'
    ''' the list's file name, in a web root '''

    def __init__(self):
        super().__init__()
        self.development = None
        self.production = None

    def names(self) -> set:
        '''
        :return: the files of all the builds, paths in the web root, e.g. fonts/a.ttf
        '''
        return {n for build in (self.development, self.production) if build is not None for n in build}

    def gz_only(self, webroot: Optional[str] = None) -> list:
        '''
        The files to be packed as their .gz only: the listed files, each with a listed .gz - the
        same files io.oz.srv.WebAssets.restore() unzips.
        :param webroot: the web root's path in the package, e.g. "web-dist"; None: paths in the web root
        :return: the files' paths, sorted, e.g. ["web-dist/fonts/a.ttf", "web-dist/portfolio.min.js"],
                 to be excluded by anson.py3's gzip2() (0.6.13, patterns with a '/')
        '''
        names = self.names()
        return sorted(n if webroot is None else f'{webroot}/{n}'
                      for n in names if not n.endswith('.gz') and f'{n}.gz' in names)
