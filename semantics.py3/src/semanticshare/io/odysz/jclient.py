from dataclasses import dataclass

from anson.io.odysz.anson import Anson


@dataclass
class AnclientSettings (Anson):
    market_id: str      # JSON-PICKING-0.8.2 desktop/settings/app-settings.json#market_id
    market_name: str
    sysuri: str
    synuri: str
    jserv: str
    org: str
    domain: str         # JSON-PICKING-0.8.2 desktop/settings/app-settings.json#domain
    device: str         # JSON-PICKING-0.8.2 desktop/settings/app-settings.json#device
    admin: str          # JSON-PICKING-0.8.2 desktop/settings/app-settings.json#admin
    domain_token: str   # JSON-PICKING-0.8.2 desktop/settings/app-settings.json#domain_token
    regiserv: str       # JSON-PICKING-0.8.2 desktop/settings/app-settings.json#regiserv
    centralUid: str
    centralPswd: str    # JSON-PICKING-0.8.2 desktop/settings/app-settings.json#centralPswd
    temp_dir: str

    def __init__(self):
        super().__init__()
