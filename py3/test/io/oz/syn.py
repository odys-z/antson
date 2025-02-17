import json
from dataclasses import dataclass
from pathlib import Path
from typing import Self, TypeVar

from ansons.anson import Anson

@dataclass
class SynOrg(Anson):
    meta: str

    orgId: str
    orgName: str
    # edu | org | com | ...
    orgType: str
    ''' This is a tree table. '''
    parent: str
    sort: str
    fullpath: str
    market: str
    # web server url, configured in dictionary like: $WEB-ROOT:8888
    webroot: str
    # The home page url (landing page)
    homepage: str
    # The default resources collection, usually a group / tree of documents.
    album0: str


@dataclass
class Synode(Anson):
    org: str
    synid: str
    mac: str
    domain: str
    nyquence: int
    syn_uid: str


@dataclass
class SynodeConfig(Anson):
    synid: str

    sysconn: str
    synconn: str

    org: SynOrg
    ''' Market, organization or so? '''

    peers: list[Synode]

    # mode: SynodeMode
    https: bool

    admin: str

    syncIns: float
    '''
     * Synchronization interval, initially, in secends.
     * No worker thread started if less or equals 0.
    '''

    def __init__(self):
        super().__init__()
        pass


@dataclass()
class SyncUser(Anson):
    userId: str
    userName: str
    pswd: str
    iv: str
    domain: str
    org: str


@dataclass()
class AnRegistry(Anson):
    config: SynodeConfig
    synusers: list[SyncUser]

    def __init__(self):
        super().__init__()
        self.config = None # SynodeConfig()
        self.synusers = []

    def load(self, path) -> Self:
        if Path(path).is_file():
            with open(path, 'r') as file:
                # registry = json.load(file)
                # self.config = SynodeConfig.from_obj(registry['config'])
                obj = json.load(file)
                obj['__type__'] = AnRegistry().__type__
                registry = Anson.from_obj(obj)

        else:
            raise Exception(f"File doesn't exist: {path}")
        return self


def loadYellowPages():
    path = ""
    registry = AnRegistry().load(path)
    return registry
