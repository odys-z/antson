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
    synodesetups: dict
    '''
    E.g "synodesetups": {
            "inforise": [
                "http://127.0.0.1:8964/synodes/synode-0.7.8-x64_windows-alpha-inforise.zip"
                ]}

    In synode.py3 0.7.8, the synode setup zip url is always usable for all the runtime platforms,
    so the actual zip names in the array should be only one, and is actually entirely overwirten
    by the build process.
    See tasks.py:updateApkRes().
    '''

    def __init__(self):
        super().__init__()
        self.localip = None
        self.syndomx = dict()
        self.resources = dict()
