from dataclasses import dataclass
from enum import Enum
from typing import Optional

from anson.io.odysz.common import LangExt
from typing_extensions import Self

from anson.io.odysz.anson import JsonOpt, Anson


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

    def Header(self, h: AnsonHeader) -> Self:
        self.header = h
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


class JProtocol:
    urlroot: str = 'must call JProtocol.setup()'

    @staticmethod
    def setup(urlpath: str, p: Port = None):
        JProtocol.urlroot = urlpath
        # And understand p

@dataclass
class JServUrl(Anson):
    https: bool
    ip: str
    port: int
    subpaths: list[str]
    jservtime: str

    def __init__(self, https: bool=False, ip: str=None, port: int=80, subpaths: list[str]=[]):
        super().__init__()
        self.https = https
        self.ip = ip
        self.port = port
        self.subpaths = subpaths
        self.jservtime = '1911-10-10'

