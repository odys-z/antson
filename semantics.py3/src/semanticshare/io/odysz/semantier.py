"""
"""

from dataclasses import dataclass
from typing import List

from anson.io.odysz.anson import Anson

@dataclass
class PeerSettings(Anson):
    """
    cpp fields:
    ===========
        vector<string> ansons;
        vector<string> scopeEnums;
        vector<string> javaEnums;
        string ansonMsg;
        string ansonBody;
        vector<string> ansonMsgs;
        vector<string> anRequests;

    json example
    ============
        { "type"      : "io.odysz.semantier.PeerSettings",
          "ansonMsg"  : "io.odysz.semantic.jprotocol.AnsonMsg",
          "ansons"    : [],
          "scopeEnums": ["io.odysz.semantic.jprotocol.MsgCode"],
          "javaEnums" : ["io.odysz.semantic.jprotocol.Port"],
          "ansonBody" : "io.odysz.semantic.jprotocol.AnsonBody",
          "anRequests": ["ast/echo.ast.json", ... ]
        }
    """
    ansons: List[str]
    scopeEnums: List[str]
    javaEnums: List[str]
    ansonMsg: str
    ansonBody: str
    ansonMsgs: List[str]
    anRequests: List[str]
    cpp_gen: str

    def __init__(self):
        super().__init__()
        self.ansonMsg  = 'io.odysz.semantic.jprotocol.AnsonMsg'
        self.ansonBody = 'io.odysz.semantic.jprotocol.AnsonBody'
        self.scopeEnums= ['io.odysz.semantic.jprotocol.MsgCode']
        self.javaEnums = ['io.odysz.semantic.jprotocol.Port']
        self.ansonMsgs = []
        self.anRequests= []
        self.cpp_gen = 'semantier.gen.h'