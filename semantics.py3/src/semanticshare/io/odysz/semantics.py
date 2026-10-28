from dataclasses import dataclass

from anson.io.odysz.anson import Anson


@dataclass
class SessionInf(Anson):
    """
    Since semantics.py3 0.6.18, no pswd.
    """
    ssid: str
    uid: str
    roleId: str
    userName: str
    roleName: str

    ssToken: str
    device: str

    def __init__(self, ssid: str = None, uid: str = None, roleId: str = None):
        super().__init__()
        self.ssid = ssid
        self.uid = uid
        self.roleId = roleId
        self.userName = None
        self.roleName = None
        self.ssToken = None
        self.device = None

@dataclass
class SemanticObject(Anson):

    props: dict

    def __init__(self):
        super().__init__()
        props = {}
