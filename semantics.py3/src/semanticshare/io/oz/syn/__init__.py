from dataclasses import dataclass
from anson.io.odysz.anson import Anson
from enum import Enum


@dataclass
class SynodeMode(Enum):
    """
    @since synode.py3 0.7.6
    """

    nonsyn = 0
    """
    None-synode mode
    """

    peer = 1
    """
    Jserv node mode: cloud hub or peers, accepting application from others.
    """

    hub = 2
    """
    hub node, in passive service mode, accepting application from others.
    """


@dataclass
class Synode(Anson):
    org: str
    synid: str
    mac: str
    domain: str
    nyq: int
    syn_uid: str
    jserv: str
    remarks: str
    nyquence: int
    stat: str

    oper: str
    optime: str

    '''
    CynodeStats.create | installed, ...
    '''

    def __init__(self):
        super().__init__()
        self.jserv = None
        self.org = None
        self.synid = None
        self.mac = None
        self.domain = None
        self.nyq = None
        self.syn_uid = None
        self.jserv = None
        self.remarks = None
        self.nyquence = None
        self.stat = 'c' # not CynodeStats.create for circular import


@dataclass()
class SyncUser(Anson):
    userId: str
    userName: str
    pswd: str
    iv: str
    domain: str
    org: str

    def __init__(self, userId=None, userName=None, pswd=None):
        super().__init__()
        self.userId = userId
        self.userName = userName
        self.pswd = pswd
