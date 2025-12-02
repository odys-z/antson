from dataclasses import dataclass

from semanticshare.io.odysz.semantics import SemanticObject


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

