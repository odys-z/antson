import json
from dataclasses import dataclass
from enum import Enum
from pathlib import Path
from typing import Union, cast, Optional

from anson.io.odysz.anson import Anson
from anson.io.odysz.common import LangExt
from semanticshare.io.odysz.semantic.jprotocol import AnsonBody, AnsonMsg, AnsonResp

from . import Synode, SyncUser


@dataclass
class SynOrg(Anson):
    meta: str

    orgId: str
    orgName: str
    # edu | org | com | ...
    orgType: str
    ''' This is a tree table. '''
    parent: Optional[AnsonMsg]

    fullpath: str
    """
    Ignored by toJson / toBlock in java
    """

    market: str
    # web server url, configured in dictionary like: $WEB-ROOT:8888
    webroot: str
    # The home page url (landing page)
    homepage: str
    # The default resources collection, usually a group / tree of documents.
    album0: str

    def __init__(self):
        super().__init__()
        self.parent = None
        self.fullpath = ""
        self.orgType = ""
        self.webroot = ""
        self.homepage = ""
        self.album0 = ""


@dataclass
class SynodeConfig(Anson):
    synid: str
    domain: str
    mode: Union[str, None]

    admin: str

    sysconn: str
    synconn: str

    org: SynOrg
    ''' Market, organization or so? '''

    syncIns: float
    '''
     * Synchronization interval, initially, in seconds.
     * No worker thread started if less or equals 0.
    '''

    peers: list[Synode]

    https: bool

    def __init__(self):
        super().__init__()
        self.https = False


@dataclass
class AnRegistry(Anson):
    config: SynodeConfig
    synusers: list[SyncUser]

    def __init__(self):
        super().__init__()
        self.config = cast('SynodeConfig', None)
        self.synusers = []

    @staticmethod
    def load(path) -> 'AnRegistry':
        if Path(path).is_file():
            with open(path, 'r', encoding="utf-8") as file:
                obj = json.load(file)
                obj['__type__'] = AnRegistry().__type__
                return Anson.from_envelope(obj)
        else:
            raise FileNotFoundError(f"File doesn't exist: {path}")

    @classmethod
    def find_synode(cls, synodes: list[Synode], id):
        if synodes is not None:
            for peer in synodes:
                if peer.synid == id:
                    return peer
        return None

    @classmethod
    def find_synuser(cls, users: list[SyncUser], id):
        if users is not None:
            for u in users:
                if u.userId == id:
                    return u
        return None

class Centralport(Enum):
    heartbeat = "ping.serv"
    session   = "login.serv"
    register  = "regist.serv"
    menu      = "menu.serv"

@dataclass
class RegistReq(AnsonBody):
    diction: SynodeConfig
    
    class A:
        registDom = "c/domx"
        updateDom = "u/domx"


    def __init__(self, act: str):
        super().__init__()
        self.a = act
        self.diction = None
    
    def dictionary(self, d: SynodeConfig):
        self.diction = d
        return self
    
    def domain(self):
        return None if self.diction is None else \
               self.diction.domain


@dataclass
class RegistResp(AnsonResp):
    class R:
        ok = "ok"
        domexists = "domexists"
        invalid = "invalid"
        error = "error"

    r: str
    diction: SynodeConfig
    
    def __init__(self):
        super().__init__()
    
    def peer_ids(self):
        return self.diction.peers if self.diction is not None else None
    
    def next_installing(self):
        for p in self.diction.peers:
            if LangExt.isblank(p.domain):
                return p.synid
        return None

def loadYellowPages():
    path = ""
    registry = AnRegistry().load(path)
    return registry
