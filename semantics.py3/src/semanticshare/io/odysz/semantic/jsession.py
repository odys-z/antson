from dataclasses import dataclass
from typing import Optional

from anson.io.odysz.anson import Anson
from semanticshare.io.odysz.semantic.jprotocol import AnsonBody, AnsonResp
from semanticshare.io.odysz.semantics import SemanticObject, SessionInf


@dataclass
class AnSessionReq(AnsonBody):
    """
    Java io.odysz.semantic.jsession.AnSessionReq, anson.cmake's AnSessionReq (gen/semantier.hpp).
    @since semantics.py3 0.6.18
    """
    class A:
        login  = "login"
        logout = "logout"
        pswd   = "pswd"
        init   = "init"
        touch  = "touch"
        ping   = "ping"

    uid: str
    token: str
    '''
    Login: encrypt(uid, pswd, iv) in base 64.
    '''
    iv: str
    '''
    iv in base 64.
    '''
    deviceId: str

    def __init__(self):
        super().__init__()
        self.uid = None
        self.token = None
        self.iv = None
        self.deviceId = None

    @staticmethod
    def formatLogin(uid: str, tk64: str, iv64: str, device: str = None) -> 'AnSessionReq':
        """
        Java AnSessionReq.formatLogin(), the body only.
        """
        req = AnSessionReq()
        req.a = AnSessionReq.A.login
        req.uid = uid
        req.token = tk64
        req.iv = iv64
        req.deviceId = device
        return req


@dataclass
class JProfile(Anson):
    """
    Java io.odysz.semantic.jsession.JProfile, the common profile of a session user, replied on login
    in AnSessionResp.profile (IUser.profile()).
    @since semantics.py3 0.6.18
    """
    home: Optional[str]
    '''
    The page to land on, decided by the server: the client router's path, e.g. "/home".
    '''

    orgId: Optional[str]
    '''
    The user's org, e.g. a_users.orgId; None for a user of no org, e.g. the central's super user (r00).
    '''

    customExt: Optional[str]
    '''
    A serialized Anson string, only a particular system / business understands or cares about.
    '''

    def __init__(self, home: str = None, orgId: str = None):
        super().__init__()
        self.home = home
        self.orgId = orgId
        self.customExt = None


@dataclass
class AnSessionResp(AnsonResp):
    """
    Java io.odysz.semantic.jsession.AnSessionResp, anson.cmake's AnSessionResp (gen/semantier.hpp).
    @since semantics.py3 0.6.18
    """
    ssInf: SessionInf

    profile: Optional[Anson]
    '''
    IUser.profile() replied on login, a JProfile, which all clients understand.
    '''

    def __init__(self, ssInf: SessionInf = None):
        super().__init__()
        self.ssInf = ssInf
        self.profile = None


@dataclass
class JUser(SemanticObject):
    """
    protected String ssid;
	protected String uid;
	protected String org;
	protected String role;
	private String pswd;

    """
    ssid: str
    uid: str
    org: str
    role: str
    pswd: str
    iv: str
    uname: str

    def __init__(self, uid: str, pswd: str, username: str=None):
        super().__init__()
        self.uid = uid
        self.pswd = pswd
        self.uname = username

