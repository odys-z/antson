
from dataclasses import dataclass
from pathlib import Path
from typing import List, cast, Union, Final, ClassVar
from types import MappingProxyType

from anson.io.odysz.anson import Anson, AnsonField
from anson.io.odysz.common import Utils


semantypes = {
    # Ast type: cpp type, python, ts
    'String': ['string', 'str', 'string'],
    'long': ['long', 'int', 'int'],
    'int': ['int', 'int', 'int'],
    'List': ['vector', 'List', '[]'],
    'Map': ['map', 'Map', '{}']
}

@dataclass
class primtypes:
    C20: ClassVar[MappingProxyType] = MappingProxyType({
        "String": "string", "string": "string", "java.lang.String": "string",
        "int": "int", "Integer": "int", "java.lang.Integer": "int",
        "short": "int", "Short": "int", "java.lang.Short": "int",
        "long": "long", "Long": "long", "java.lang.Long": "long",
        "float": "float", "Float": "float", "java.lang.Float": "float",
        "double": "double", "Double": "double", "java.lang.Double": "double",
        "boolean": "boolean", "Boolean": "boolean", "java.lang.Boolean": "boolean",
        "VarType": "VarType", "LangExt::VarType": "VarType", "anson::LangExt::VarType": "VarType"
    })

@dataclass
class AnsonAst(Anson):
    base: str

    isInt: bool
    isDouble: bool
    isEnum: bool
    isList: bool
    isMap: bool
    istring: bool
    isJsonable: bool
    isPortEnum: bool
    isVar: bool

    # anclass: str

    dataAnclass: str
    baseAnclass: str

    fields: dict[str, AnsonField]
    enums: Union[dict, None]

    ctors: List[List[List[str]]]
    '''
    E.g. if define (Design Cmake 0.1) 
     @AnsonCtor(initialist="echo(string m)", base={"r/query", "uri"}),
     
     // [0]: Initializer 0, ..., [-1] Base Initializer
     
     AnsonAst.cotrs[i] = [["echo": "string", "m"], ["r/query", "uri"]]
     
     // c++ constructor:
     
     EchReq::EchoReq(string m) : AnsonBody("r/query, "uri", EchoReq::_type_), echo(m) {}
     
    see Java io.odysz.anson.AnsonCtor
    '''

    def __init__(self):
        super().__init__()
        self.base = 'io.odysz.anson.Anson'

        self.isInt = bool
        self.isDouble = bool
        self.isEnum = bool
        self.isList = bool
        self.isMap = bool
        self.istring = bool
        self.isJsonable = bool
        self.isPortEnum = bool
        self.isVar = bool

        self.anclass = 'must set'
        self.fields = {}
        self.enums = None
        self.ctors = []
        self.baseAnclass = ''
        self.dataAnclass = ''

    def c_class(self) -> str:
        return self.dataAnclass.split('.')[-1]

    def c_base(self) -> str:
        return self.baseAnclass.split('.')[-1] if len(self.baseAnclass) > 0 else ''


@dataclass
class AnsonBodyAst(AnsonAst):
    A: dict[str, str]


    def __init__(self):
        super().__init__()


@dataclass
class AnsonMsgAst(AnsonAst):
    """

    """
    msg: str
    A: dict

    def __init__(self):
        super().__init__()
        self.A = {}

    def body(self):
        return self.anclass


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
