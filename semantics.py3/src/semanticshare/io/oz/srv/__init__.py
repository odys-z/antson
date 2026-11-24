"""
html-service's types of io.oz.srv (github odys-z/html-service): WebAssets.

WebConfig, StaticResource and DynaResource are semantic.jserv's since package 0.8.3, see
semanticshare.io.odysz.semantic.jserv; the classes here, of the same names, only read a
html-service.json of an earlier version, its types io.oz.srv.* (an upgrade takes the new package's
file, with the user's values picked, see io.oz.syn.pkgresource.ConfigPicks).
"""
from dataclasses import dataclass
from typing import Optional

from anson.io.odysz.anson import Anson
from semanticshare.io.odysz.semantic import jserv


@dataclass
class StaticResource(jserv.StaticResource):
    '''
    @deprecated package 0.8.3, io.odysz.semantic.jserv.StaticResource. For reading an earlier version's file.
    '''
    def __init__(self, **kwargs):
        super().__init__(**kwargs)


@dataclass
class ResPath(jserv.StaticResource):
    '''
    @deprecated html-web 0.2.1 renamed it StaticResource. Kept for reading a html-service.json of an older
    version (an upgrade takes the new package's paths, see io.oz.syn.pkgresource.ConfigPicks).
    '''
    def __init__(self, **kwargs):
        super().__init__(**kwargs)


@dataclass
class DynaResource(jserv.DynaResource):
    '''
    @deprecated package 0.8.3, io.odysz.semantic.jserv.DynaResource. For reading an earlier version's file.
    '''
    def __init__(self):
        super().__init__()


@dataclass
class WebConfig(jserv.WebConfig):
    '''
    @deprecated package 0.8.3, io.odysz.semantic.jserv.WebConfig. For reading an earlier version's file.
    '''
    def __init__(self):
        super().__init__()


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
