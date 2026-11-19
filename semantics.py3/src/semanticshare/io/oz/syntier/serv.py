from dataclasses import dataclass

from anson.io.odysz.anson import Anson


@dataclass
class ExternalHosts(Anson):
    marketid: str
    '''
    Don't configure this field in host.json, it is set from taskcfg.deploy.market_id.
    '''
    host: str
    localip: str
    syndomx: dict
    resources: dict
    '''
    TODO rename as clients
    '''

    def __init__(self):
        super().__init__()
        self.localip = None
        self.syndomx = dict()
        self.resources = dict()
