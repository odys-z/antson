import json
from dataclasses import dataclass
from enum import Enum
from pathlib import Path
from typing import Union, cast, Optional

from anson.io.odysz.anson import Anson
from anson.io.odysz.common import LangExt
from semanticshare.io.odysz.semantic.jprotocol import AnsonBody, AnsonMsg, AnsonResp, JServUrl, JProtocol

from . import Synode, SyncUser, SynodeMode


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

    # market: str

    # web server url, configured in dictionary like: $WEB-ROOT:8888
    webroot: str
    # The home page url (landing page)
    homepage: str
    # The default resources collection, usually a group / tree of documents.
    album0: str

    def __init__(self, orgtype:str='', orgid:str='', orgname:str='', webroot:str='', homepage:str='', albumid=''):
        super().__init__()
        self.parent = None
        self.fullpath = ""
        self.orgId = orgid
        self.orgName = orgname
        self.orgType = orgtype
        self.webroot = webroot
        self.homepage = homepage
        self.album0 = albumid


@dataclass
class SynodeConfig(Anson):
    debug: bool
    '''
    Java but not used
    '''
    chsize: int
    '''
    Java but not used
    '''

    sysconn: str
    synconn: str

    synid: str
    domain: str
    mode: Union[str, None]
    admin: str
    org: SynOrg
    ''' Market, organization or so? '''
    syncIns: float
    '''
     * Synchronization interval, initially, in seconds.
     * No worker thread started if less or equals 0.
    '''

    peers: list[Synode]

    https: bool

    def __init__(self, domain:str=None, synode:str=None,
                 synconn:str=None, sysconn:str=None, admin:str='admin',
                 org:SynOrg=None, peers:list=None, mode:str=None):
        super().__init__()
        self.synconn = synconn
        self.sysconn = sysconn
        self.domain  = domain
        self.org   = org
        self.https = False
        self.synid = synode
        self.admin = admin
        self.peers = peers
        self.mode  = mode

    def overlay(self, by):
        ''' self.org |= by.org '''
        self.chsize  = by.chsize if by.chsize >= 0 else self.chsize
        self.synconn = by.synconn if not LangExt.isblank(by.synconn) else self.synconn
        self.sysconn = by.sysconn if not LangExt.isblank(by.sysconn) else self.sysconn
        self.domain  = by.domain if not LangExt.isblank(by.domain) else self.domain
        self.org   = by.org if not LangExt.isblank(by.org) else self.org
        self.https = by.https if by.https is not None else self.https
        self.synid = by.synid if not LangExt.isblank(by.synid) else self.synid
        self.admin = by.admin if not LangExt.isblank(by.admin) else self.admin
        self.peers = by.peers if LangExt.len(by.peers) > 0 else self.peers
        self.mode  = by.mode if not LangExt.isblank(by.mode) else self.mode

    def set_domain(self, domid: str):
        self.domain = domid
        if LangExt.len(self.peers) > 0:
            for p in self.peers:
                p.domain = domid


@dataclass
class AnRegistry(Anson):
    json: str
    '''
    Null java equivolent
    '''
    config: SynodeConfig
    synusers: list[SyncUser]

    def __init__(self):
        super().__init__()
        self.config = cast('SynodeConfig', None)
        self.synusers = []
        # self.json = cast(str, None)

    @staticmethod
    def load(path: str) -> 'AnRegistry':
        return cast('AnRegistry', Anson.from_file(path))
    #     if Path(path).is_file():
    #         with open(path, 'r', encoding="utf-8") as file:
    #             obj = json.load(file)
    #             obj['__type__'] = AnRegistry().__type__
    #             an =  Anson.from_envelope(obj)
    #             an.json = path
    #             return an
    #     else:
    #         raise FileNotFoundError(f"File doesn't exist: {path}")
        
    def find_peer(self, peerid: str):
        return None if LangExt.isblank(peerid) or self.config.peers is None \
            else AnRegistry.find_synode(self.config.peers, peerid) \
    
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

    def find_hubpeer(self):
        if LangExt.len(self.config.peers) > 0:
            for p in self.config.peers:
                if p.remarks == SynodeMode.hub.name:
                    return p
            else:
                p0 = self.config.peers[0]
                return p0 if LangExt.isblank(p0.stat) and LangExt.isblank(p0.remarks) else None
        return None

    # def save(self):
    #     self.toFile(self.json)


@dataclass
class Centralport(Enum):
    heartbeat:str = "ping.serv"
    session  :str = "login.serv"
    register :str = "regist.serv"
    menu     :str = "menu.serv"


@dataclass
class CynodeStats:
    create :str = "c"
    asHub  :str = "h"
    asPeer :str = "p"
    die    :str = "d"


@dataclass
class RegistReq(AnsonBody):
    
    class A:
        queryDomx      = "r/domx"
        queryDomConfig = "r/dom-config"
        registDom      = "c/domx"
        updateDom      = "u/domx"
        submitSettings = "u/settings"

    market: str
    diction: Optional[SynodeConfig]
    myjserv: Optional[JServUrl]
    protocolPath: Optional[str]
    mystate: Optional[str]

    def __init__(self, act: str=None, market:str=None):
        super().__init__()
        self.market = market
        self.a = act
        self.diction = None

    def dictionary(self, d: SynodeConfig):
        self.diction = d
        return self
    
    def domain(self):
        return None if self.diction is None else \
               self.diction.domain

    def Jservtime(self, utc:str):
        if self.myjserv is None:
            self.myjserv = JServUrl()
        self.myjserv.jservtime = utc
        return self

    def jserurl(self, https: bool, iport: tuple[str, int]):
        self.myjserv = JServUrl(
            https=https,
            ip=iport[0],
            port=iport[1])
            # 2025-09-29 Java version doesn't have this line:
            # subpaths=[JProtocol.urlroot],
        return self

    def mystate(self, stat: CynodeStats=None):
        self.mystate = stat
        return self

    def protocol_path(self, urlroot):
        self.protocolPath = urlroot
        return self


@dataclass
class RegistResp(AnsonResp):
    class R:
        ok = "ok"
        domexists = "domexists"
        invalid = "invalid"
        error = "error"

    r: str
    orgDomains: list[str]
    
    diction: SynodeConfig
    
    def __init__(self):
        super().__init__()
        self.r = ''
        self.orgDomains = []
        self.diction = SynodeConfig()
    
    # def peer_ids(self) -> list[Synode]:
    #     return self.diction.peers if self.diction is not None else None
    
    def next_installing(self):
        for p in self.diction.peers:
            if p is not None and p.stat == CynodeStats.create:
                return p.synid
        return None

    def domains(self):
        return self.orgDomains if self.orgDomains is not None else []


def loadYellowPages():
    path = ""
    registry = AnRegistry().load(path)
    return registry
