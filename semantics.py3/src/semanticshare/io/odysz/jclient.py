from dataclasses import dataclass

from anson.io.odysz.anson import Anson


@dataclass
class AnclientSettings (Anson):
    sysuri: str
    synuri: str
    jserv: str
    org: str
    domain: str
    device: str
    admin: str
    domain_token: str
    regiserv: str
    centralPswd: str
    temp_dir: str

    def __init__(self):
        super().__init__()