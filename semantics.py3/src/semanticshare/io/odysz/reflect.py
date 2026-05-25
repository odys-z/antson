
from dataclasses import dataclass
from pathlib import Path
from typing import List, cast, Union, Final, ClassVar, Literal
from types import MappingProxyType

from anson.io.odysz.anson import Anson, AnsonField
from anson.io.odysz.common import LangExt


semantypes = {
    # Ast type: cpp type, python, ts
    'String': ['string', 'str', 'string'],
    'long': ['long', 'int', 'int'],
    'int': ['int', 'int', 'int'],
    'List': ['vector', 'List', '[]'],
    'Map': ['map', 'Map', '{}']
}

@dataclass
class Semantics(Anson):
    def __init__(self):
        pass

Semantype = Literal['()', '=', 'ini']

@dataclass
class SemanExpr(Semantics):
    stype: Semantype
    args: [str]
    semantics: ['SemanExpr']
    expect_result: str

    def __init__(self, **kwargs):
        super().__init__()
        self.stype = kwargs.get('stype', '')
        self.args = kwargs.get('args', [])
        self.semantics = kwargs.get('semantics', [])

    def cpp_arg_decl(self):
        if self.stype == '':
            return ' '.join(self.args) if len(self.args) <= 2 else ' '.join(self.args)
        else: #elif self.stype == 'ini':
            return ' '.join(self.args) if len(self.args) <= 2 else ' '.join(self.args[:-1])


    def cpp_arg_ini(self):
        # return None if self.stype != 'ini' else f'{self.args[-1]}({self.args[-2]})' if len(self.args) > 2 else ' '.join(self.args) + '?'
        return None if self.stype != 'ini' else f'{self.args[-1]}({self.args[-2]})'

    def cpp_expr(self, indent: str):
        if self.stype == '()':
            return f'{indent}{self.args[0]}({", ".join(self.args[1:])});'
        elif self.stype == '=':
            return f'{indent}{("" if self.args[-2] != self.args[-1] else "this->") + " ".join(self.args[:-1])} = {self.args[-1]};'
        # else no body lines
        return None

    def arg_name_types(self):
        if self.stype == '':
            return self.args[-1], ' '.join(self.args[:-1])
        elif self.stype == 'ini':
            return self.args[-2], ' '.join(self.args[:-2])
        else:
            Utils.warn("shouldn't reach here: " + ' '.join(self.args))
            return self.args[-1], ' '.join(self.args[:-1])
        # if LangExt.len(self.args) <= 1:
        #     return None, ' '.join(self.args)
        # elif LangExt.len(self.args) == 2:
        #     # e.g. UserReq uri
        #     return self.args[-1], self.args[0]
        # else:
        #     # e.g.   string m echo
        #     #  const string m echo
        #     #  const unsigned int x seq
        #     return self.args[-2], ' '.join(self.args[:-2])


@dataclass
class AnCtor(Semantics):
    base: SemanExpr
    args: [SemanExpr]
    body: [SemanExpr]

    def __init__(self):
        super().__init__()
        self.base = ''
        self.args = []
        self.body = []

    def as_default(self, ast: 'AnsonAst'):
        self.base = SemanExpr(stype = '()', args=[ast.c_base()])
        return self

    def cpp_arg_decl(self):
        return ', '.join([arg.cpp_arg_decl() for arg in self.args])

    def cpp_base_ini(self, ast: 'AnsonAst'):
        if self.base.stype != '()':
            return None

        basecls = self.base.args[0] if LangExt.len(self.base.args) > 0 else 'baseAnclass'
        basecls = ast.c_base() if basecls == 'baseAnclass' else basecls
        base_args = ", ".join(self.base.args[1:]) if LangExt.len(self.base.args) > 0 else ""
        return f'{basecls}({base_args})'

    def map_args_decls(self):
        m = {}
        for arg in self.args:
            arg_name, arg_type = arg.arg_name_types()
            if arg_name is not None:
                m.update({arg_name: arg_type})
        return m

    def cpp_arg_inis(self):
        return None if LangExt.len(self.args) == 0 else ', '.join(filter(lambda ini : not LangExt.isblank(ini), [arg.cpp_arg_ini() for arg in self.args]))

    def cpp_body_exprs(self, ast, indent: str) -> List[str]:
        withType_setter = len(self.base.args) == 0 or ast.c_class() != self.base.args[0]
        # ret = [' ' * 8 + 'Type(_type_);'] if withType_setter else []
        ret = [indent + 'Type(_type_);'] if withType_setter else []
        if len(self.body) > 0:
            ret.extend([exp.cpp_expr(indent) for exp in self.body])
        elif isinstance(self.body, SemanExpr): # tolerate config error?
            ret.append(self.body.cpp_expr(indent))
        return ret


@dataclass
class AnsonAst(Anson):
    '''
    ISSUE: should be the subcalss of SemanExpr or Semantics?
    '''

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

    ctorsemantics: List[AnCtor]

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

        self.ctorsemantics = []

    def c_class(self) -> str:
        return self.dataAnclass.split('.')[-1]

    def c_base(self) -> str:
        return self.baseAnclass.split('.')[-1] if len(self.baseAnclass) > 0 else ''


@dataclass
class AnsonBodyAst(AnsonAst):
    A: dict[str, str]

    def __init__(self):
        super().__init__()
        self.A = {}


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
    tier_name: str
    ast_folder: str
    ansons: List[str]
    scopeEnums: List[str]
    javaEnums: List[str]
    ansonMsg: str
    ansonBody: str
    ansonMsgs: List[str]
    anRequests: List[str]
    cpp_include: List[str]
    cpp_gen: str

    def __init__(self):
        super().__init__()
        self.tier_name = None
        self.ast_folder = 'ast'
        self.ansonMsg   = 'io.odysz.semantic.jprotocol.AnsonMsg'
        self.ansonBody  = 'io.odysz.semantic.jprotocol.AnsonBody'
        self.scopeEnums = ['io.odysz.semantic.jprotocol.MsgCode']
        self.javaEnums  = ['io.odysz.semantic.jprotocol.Port']
        self.ansonMsgs  = []
        self.anRequests = []
        self.cpp_include= []
        self.cpp_gen    = 'semantier.gen.h'


def init_asts(ast_folder: str = None):
    '''
	@deprecated

    inline static void register_jserv(AstMap &asts, JsonOpt &ctx_opt) {
        IJsonable::contxt_ptr = &ctx_opt;

        register_varctors();
        register_asts(asts);
        register_msgs(asts);
        register_enums<MsgCode>(asts);
        register_port(asts, "ast/port.ast.json");
        specialize_respmsg(asts);
        setup_jserv_crud(asts);
        register_peersettings(asts);
    }
    :param ast_folder:
    :return:
    '''
    asts = {}

    # Anson
    ast = AnsonAst()
    asts[Anson().__type__] = ast

    return asts
