from dataclasses import dataclass
from anson.io.odysz.anson import Anson
from semanticshare.io.odysz.jclient import AnclientSettings


@dataclass
class DesktopSettings (AnclientSettings):

    org: str          # JSON-PICKING-0.8.2 desktop/settings/app-settings.json#org
    market_name: str  # JSON-PICKING-0.8.2 desktop/settings/app-settings.json#market_name
    org_name: str
    java_path: str    # JSON-PICKING-0.8.2 desktop/settings/app-settings.json#java_path
    wsagent_jar: str
    synode_id: str    # JSON-PICKING-0.8.2 desktop/settings/app-settings.json#synode_id
    synode_vol: str   # JSON-PICKING-0.8.2 desktop/settings/app-settings.json#synode_vol
    synode_jserv: str # JSON-PICKING-0.8.2 desktop/settings/app-settings.json#synode_jserv
    album_web: str    # JSON-PICKING-0.8.2 desktop/settings/app-settings.json#album_web
    wshost: str
    wsport: int
    wstimeout: int

    def __init__(self):
        super().__init__()


@dataclass
class UIResources(Anson):
    iso: str
    lang: str
    langs: dict
    ui: str

    credits: str

    def __init__(self):
        super().__init__()
        self.ui = 'ui_form.py'
        self.langs = dict()

    def signup_prompt(self, defl = None):
        from antson.py3.src.anson.io.odysz.common import LangExt
        return LangExt.ifblank(self.langs[self.lang].signup_prompt, defl)

    def langstr(self, res):
        """
        Get a string resource for the language, self.langs[self.lang][res].
        Return the en version if not found
        :param res:
        :return: self.langs[self.lang][res].
        """
        print(res, self.langs['en'])
        return self.langs[self.lang][res] \
            if self.lang in self.langs and res in self.langs[self.lang] \
            else self.langs['en'][res]

    def langstrf(self, res, **args):
        return self.langstr(res).format(**args)


@dataclass
class DesknownPaths(Anson):
    '''
    User's home path, etc.
    @since 0.6.26
    '''
    home: str             # need JSON-PIKING-0.8.3 ?
    desktop: str          # need JSON-PIKING-0.8.3 ?
    desktop_settings: str # need JSON-PIKING-0.8.3 ?
    download: str         # need JSON-PIKING-0.8.3 ?
    documents: str        # need JSON-PIKING-0.8.3 ?
    pictures: str         # need JSON-PIKING-0.8.3 ?
    music: str            # need JSON-PIKING-0.8.3 ?
    video: str            # need JSON-PIKING-0.8.3 ?

    def __init__(self):
        super().__init__()