
from dataclasses import dataclass
from anson.io.odysz.anson import Anson

@dataclass
class CentralSettings(Anson): 
    market: str #      : "my""
    vol_name: str #    : "VOLUME_HOME
    volume: str #      : "../regist-vol
    port: str #        : 1990"
    conn: str #		: "sys-sqlite
    startHandler: list #: []"
    rootkey: str #     : "0123456789ABCDEF"
    
    def __init__(self):
        super().__init__()
