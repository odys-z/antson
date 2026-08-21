from dataclasses import dataclass, field
from enum import Enum
from typing import Optional, List
from urllib.parse import urlparse
import re

from anson.io.odysz.common import LangExt
from anson.io.odysz.utils import Regexs
from typing_extensions import Self

from anson.io.odysz.anson import JsonOpt, Anson, AnsonField


class MsgCode(Enum):
    """
    public enum MsgCode {ok, exSession, exSemantic, exIo, exTransct, exDA, exGeneral, ext };
    """
    ok = 'ok'
    exSession = 'exSession'
    exSemantics = 'exSemantics'
    exIo = 'exIo'
    exTransc = 'exTransac'
    exDA = 'exDA'
    exGeneral = 'exGeneral'
    ext = 'ext'


class Port(Enum):
    echo = 'echo.less'
    singup = 'signup.less'
    session = 'login.serv'
    r = 'r.serv'


@dataclass
class AnsonHeader(Anson):
    uid: str
    ssid: str
    iv64: str
    usrAct: [str]
    ssToken: str

    def __init__(self, ssid = None, uid = None, token = None):
        super().__init__()
        self.ssid = ssid
        self.uid = uid
        self.ssToken = token


@dataclass
class AnsonMsg(Anson):
    body: ['AnsonBody']
    header: AnsonHeader

    port: Optional[Port]
    '''
    The semantic-serv port, optional only when deserializing by Anson.fromJson().
    '''

    code: MsgCode
    opts: JsonOpt
    addr: str
    seq: int
    version: str

    def __init__(self, p: Enum = None):
        super().__init__()
        self.port = p
        self.body = []

    def Header(self, h: AnsonHeader = None, ssinf: 'SessionInf' = None) -> Self:
        if h is not None:
            self.header = h
        if ssinf is not None:
            self.header = AnsonHeader()
            self.header.uid = ssinf.uid
            self.header.ssid = ssinf.ssid
            self.header.ssToken = ssinf.ssToken
        return self

    def Body(self, bodyItem: 'AnsonBody'=None) -> Self:
        if bodyItem is None:
            return None if LangExt.len(self.body) == 0 else self.body[0]
        else:
            self.body.append(bodyItem)
            return self


@dataclass
class AnsonBody(Anson):
    uri: str
    parent: Optional[AnsonMsg]
    a: str
    rs: dict
    m: str
    map: dict
    opts: JsonOpt
    addr: str
    version: str
    seq: int


    def __init__(self, parent: AnsonMsg = None):
        super().__init__()
        self.uri = None
        self.parent = parent
        Anson.enclosinguardtypes.add(AnsonMsg)

    def A(self, a: str) -> Self:
        self.a = a
        return self

    def Uri(self, func_uri):
        self.uri = func_uri
        return self


@dataclass
class UserReq(AnsonBody):
    
    def __init__(self):
        super().__init__()
        self.a = None


@dataclass
class AnsonResp(AnsonBody):
    code: MsgCode
    parent: str

    def __init__(self):
        super().__init__()
        self.a = None
        self.code = MsgCode.ok

    def msg(self) -> str:
        return self.m
    
    def Code(self, code: MsgCode):
        self.code = code
        return self


class JProtocol:
    urlroot: str = 'must call JProtocol.setup()'
    '''
    @deprecated
    replaced by protocolpath
    '''

    protocolpath: str

    @staticmethod
    def setup(urlpath: str, p: Port = None):
        '''
        @deprecated

        static usage of JProtocol is deprecated
        '''
        JProtocol.urlroot = urlpath
        # And understand p ?
    
    def __init__(self, protocol_id: str = ''):
        self.protocolpath = protocol_id

@dataclass
class JServUrl(Anson):
    https: bool
    ip: str
    port: int
    subpaths: List[str]
    jservtime: str


    jprotocol: JProtocol = field(metadata={'ignore': True})
    '''
    @since 0.5.7
    '''

    def __init__(self,
                 jservurl: str = None,
                 https: bool=False,
                 ip: str=None, port: int=80, iport: str = None,
                 protocolroot: str = '',
                 subpaths: List[str]=[]):
        super().__init__()
        self.jprotocol = JProtocol(protocol_id=protocolroot)
        
        self.https = https
        self.ip = ip
        self.port = port
        if iport is not None:
            host_port = iport.split(":")
            self.ip = host_port[0]
            self.port = int(host_port[1])
        self.subpaths = subpaths
        self.jservtime = '1911-10-10'

        if jservurl:
            parts = Regexs.get_http_parts(jservurl)
            self.https = parts[1]
            self.ip = parts[2]
            self.port = parts[3]
            self.subpaths = parts[4]
            # self.subpaths = None if LangExt.len(parts[4]) == 0 else \
            #                 re.sub('^/*', '', parts[4]).split('/')[1:]
        
        if not LangExt.isNull(self.subpaths):
            self.jprotocol = JProtocol(protocol_id=self.subpaths[0])

    def __str__(self):
        # return f"{'https' if self.https else 'http'}://{self.ip}:{self.port}/{
        # '/'.join(filter(lambda v: not LangExt.isblank(v), [
        # self.jprotocol.protocolpath, *self.subpaths]))}"

        scheme = "https" if self.https else "http"
        raw_paths = [self.jprotocol.protocolpath, *self.subpaths]
        path = "/".join(v for v in raw_paths if not LangExt.isblank(v))
        return f"{scheme}://{self.ip}:{self.port}/{path}"

    def jserv(self):
        return self.__str__()

    @staticmethod
    def asJserv(jserv: str):
        parts = urlparse(jserv)
        jurl = JServUrl(https=parts.scheme == 'https',
                        ip=parts.hostname, port=parts.port,
                        subpaths= None if LangExt.len(parts.path) == 0 else \
                            re.sub('^/*', '', parts.path).split('/')[1:])
        return jurl
    
    @staticmethod
    def valid(jserv: str, rootpath: str = None):
        '''
        @deprecated
        There are better version in anson.cmake
        :param jserv:
        :param rootpath: length template, not really used
        '''
        if rootpath is None:
            rootpath = JProtocol.urlroot

        if LangExt.len(jserv) < 8 + len(rootpath):
            return False

        parts = urlparse(jserv)
        urlroot = re.sub('^/*', '', parts.path.removeprefix("/")) if LangExt.len(parts.path) > 0 else ''
        return (parts.port is None or type(parts.port) == int and parts.port >= 1024) \
            and (parts.scheme == "http" or parts.scheme == "https") \
            and rootpath == urlroot.split('/')[0]

    def is_valid(self, target_jserv: str):
        '''
        @seince 0.5.8
        :param targe_jserv:
        :return: True valid agains my jprotocol.
        '''
        return JServUrl.valid(target_jserv, self.jprotocol.protocolpath)
