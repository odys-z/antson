from dataclasses import dataclass
from anson.io.odysz.anson import Anson

@dataclass
class DesktopSettings (Anson):
    # class DesktopSettings(AnclientSettings):
    market: str
    org: str
    market_name: str
    org_name: str
    java_path: str
    doctier_jar: str
    wsagent_jar: str
    synode_id: str
    synode_vol: str
    synode_jserv: str
    album_web: str
    wshost: str
    wsport: int
    wstimeout: int

    def __init__(self):
        super().__init__()
