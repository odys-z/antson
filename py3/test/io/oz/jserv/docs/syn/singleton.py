# This Python file uses the following encoding: utf-8
from dataclasses import dataclass
from typing import overload, Self

from src.anson.io.odysz.ansons import Anson

jserv_sep = ' '
synode_sep = ':'


# def parseJservs(jservstr: str) -> dict:
#     jss = split(jserv_sep, jservstr)
#     jservs = {}
#     for jserv in jss:
#         n_j = split(synode_sep, jserv)
#         n, j = n_j[0], re.sub("^{0}\s*{1}\s*".format(n_j[0], synode_sep), "", jserv)
#         jservs[n] = j
#
#     print(jservs)
#     return jservs


@dataclass
class AppSettings(Anson):
    envars: dict
    installkey: str
    rootkey: str

    volume: str
    vol_name: str
    port: int
    jservs: dict

    def __init__(self):
        super().__init__()
        self.envars = {}

    def load(self, data: dict):
        self.vol_name = data['vol_name']
        self.volume = data['volume']
        self.port = data['port']
        self.envars = data['envars']
        self.rootkey = data['rootkey']
        self.installkey = data['installkey']
        self.jservs = data['jservs']

        if isinstance(self.jservs, str):
            raise Exception("Jservs is a string, while expecting a dict. This is a guarded error to prevent old version's convention.")

        return self

    @overload
    def Jservs(self, jservs: str) -> Self:
        ...
        # self.jservs = parseJservs(jservs)
        # return self

    @overload
    def Jservs(self) -> str:
        ...
        # return Utils.join(self.jservs)

    def Jservs(self, urldict: dict = None):
        '''
        :param urldict:
            E. g. {x: 'http://127.0.0.1:8964/jserv-album'}
        :return: self when setting, jservs lines, [['x', 'http://127.0.0.1:8964/jserv-album']], when getting.
        '''
        if urldict is None:
            # return self.formatJservLines(self.jservs)
            return [[k, self.jservs[k]] for k in self.jservs]
        else:
            self.jservs = urldict
            return self

    def jservLines(self):
        return [':\t'.join([k, self.jservs[k]]) for k in self.jservs]

    def movekey(self):
        self.json.rootkey, self.json.installkey = self.json.installkey, None

    def toBlock(self) -> str:
        return f"{{ jservs: {self.jservs} }}"

