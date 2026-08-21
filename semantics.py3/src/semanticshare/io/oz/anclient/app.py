from dataclasses import dataclass
from anson.io.odysz.anson import Anson
from semanticshare.io.odysz.jclient import AnclientSettings


@dataclass
class DesktopSettings (AnclientSettings):
    # class DesktopSettings(AnclientSettings):
    market: str
    org: str
    market_name: str
    org_name: str
    java_path: str
    # doctier_jar: str
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

