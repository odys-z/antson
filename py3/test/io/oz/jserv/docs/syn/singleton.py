# This Python file uses the following encoding: utf-8
import os
from dataclasses import dataclass
from typing import overload, Optional

from src.anson.io.odysz.anson import Anson

jserv_sep = ' '
synode_sep = ':'


class PortfolioException(Exception):
    """
    As upto Portfolio 0.7, there is no such equivalent in Java.
    date
    Thu 20 Feb 2025 11:52:06 AM AWST
    mvn dependency:tree
    [INFO] io.github.odys-z:jserv-album:jar:0.7.0
    [INFO] +- io.github.odys-z:docsync.jserv:jar:0.2.2-SNAPSHOT:compile
    [INFO] |  +- io.github.odys-z:semantic.DA:jar:1.5.18-SNAPSHOT:compile (version selected from constraint [1.5.18-SNAPSHOT,2.0.0-SNAPSHOT))
    [INFO] |  |  +- io.github.odys-z:semantics.transact:jar:1.5.58:compile (version selected from constraint [1.5.58,))
    [INFO] |  |  |  \\- io.github.odys-z:antson:jar:0.9.114:compile (version selected from constraint [0.9.111,))
    [INFO] |  +- io.github.odys-z:anclient.java:jar:0.5.16:compile (version selected from constraint [0.5.16,))
    [INFO] |  \\- io.github.odys-z:synodict-jclient:jar:0.1.6:compile (version selected from constraint [0.1.6,))
    [INFO] +- io.github.odys-z:syndoc-lib:jar:0.5.18-SNAPSHOT:compile
    [INFO] |  \\- io.github.odys-z:semantic.jserv:jar:1.5.16-SNAPSHOT:compile (version selected from constraint [1.5.16-SNAPSHOT,2.0.0-SNAPSHOT))
    [INFO] +- io.github.odys-z:albumtier:jar:0.5.0-SNAPSHOT:test
    """
    msg: str
    cause: object

    def __init__(self, msg: str, *args: object):
        super().__init__(args)
        self.msg = msg
        self.cause = None if args is None or len(args) == 0 else args[0]


@dataclass
class AppSettings(Anson):
    envars: dict
    rootkey: str    # | None # test 3.12
    installkey: Optional[str] # test 3.9

    volume: str
    vol_name: str
    port: int
    jservs: dict

    def __init__(self):
        super().__init__()
        self.envars = {}

    @overload
    def Volume(self):
        """
        return self.volume
        """

    @overload
    def Volume(self, v: str):
        """
        set volume
        return self
        """

    def Volume(self, v: str = None):
        if v is None:
            return self.volume
        else:
            self.volume = os.path.normpath(v).replace("\\", "/")
            return self

    @overload
    def Jservs(self, jservs: dict):
        ...
        # self.jservs = parseJservs(jservs)
        # return self

    @overload
    def Jservs(self) -> str:
        ...

    def Jservs(self, urldict: dict = None):
        '''
        :param urldict:
            E.g. {x: 'http://127.0.0.1:8964/jserv-album'}
        :return: self when setting, jservs lines, [['x', 'http://127.0.0.1:8964/jserv-album']], when getting.
        '''
        if urldict is None:
            return [[k, self.jservs[k]] for k in self.jservs]
        else:
            self.jservs = urldict
            return self

    def jservLines(self):
        return [':\t'.join([k, self.jservs[k]]) for k in self.jservs]

    # def movekey(self):
    #     if LangExt.len(self.rootkey) == 0 and LangExt.len(self.installkey) > 0:
    #         self.rootkey, self.installkey = self.installkey, None
