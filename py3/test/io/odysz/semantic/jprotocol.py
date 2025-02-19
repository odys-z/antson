from dataclasses import dataclass
from enum import Enum
from typing import Self, TypeVar

from src.anson.io.odysz.ansons import Anson

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
    echo = 'ping.serv'
    session = "login.serv"
    r = "r.serv"




@dataclass
class AnsonHeader(Anson):
    uid: str
    ssid: str
    iv64: str
    usrAct: [str]
    ssToken: str

    def __init__(self, ssid, uid, token):
        super().__init__()

TAnsonBody = TypeVar('TAnsonBody', bound='AnsonBody')

@dataclass
class AnsonMsg(Anson):
    body: [TAnsonBody]
    header: AnsonHeader
    port: Port
    code = MsgCode.ok

    def __init__(self, p: Port = None):
        super().__init__()
        self.port = p
        self.body = []

    def Header(self, h: AnsonHeader) -> Self:
        self.header = h
        return self

    def Body(self, bodyItem: TAnsonBody) -> Self:
        self.body.append(bodyItem)
        return self



@dataclass
class AnsonBody(Anson):
    uri: str
    a: str

    def __init__(self, parent: AnsonMsg = None):
        super().__init__()
        self.parent = parent
        Anson.enclosinguardtypes.add(AnsonMsg)

    def A(self, a: str) -> Self:
        self.a = a
        return self

@dataclass
class AnsonReq(AnsonBody):
    def __init__(self):
        super().__init__()
        self.a = None


@dataclass
class AnsonResp(AnsonBody):
    code: MsgCode

    def __init__(self):
        super().__init__()
        self.a = None
        self.code = MsgCode.ok

