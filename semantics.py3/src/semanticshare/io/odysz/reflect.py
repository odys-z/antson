
from dataclasses import dataclass
from typing import List, Union, Literal

from anson.io.odysz.anson import Anson, AnsonFieldMeta
from anson.io.odysz.common import LangExt
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
class Semantics(Anson):
    def __init__(self):
        pass

Semantype = Literal['()', '=', 'ini']

@dataclass
class SemanExpr(Semantics):
    stype: Semantype
    args: List[str]
    semantics: List['SemanExpr']
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
        else:
            Utils.warn(f"""Configure Error:
            Only '()' and '=' types of c++ expression can be handled.
            stype: {self.stype}, args = {LangExt.str(self.args)}
            """)
            print(self.args)
        return None

    def arg_name_types(self):
        if self.stype == '':
            return self.args[-1], ' '.join(self.args[:-1])
        elif self.stype == 'ini':
            return self.args[-2], ' '.join(self.args[:-2])
        else:
            Utils.warn("shouldn't reach here: " + ' '.join(self.args) + '. Supported stype: ' + ', '.join(["''", "'ini'"]))
            return self.args[-1], ' '.join(self.args[:-1])


@dataclass
class AnCtor(Semantics):
    base: SemanExpr
    args: List[SemanExpr]
    body: List[SemanExpr]

    def __init__(self):
        super().__init__()
        self.base = ''
        self.args = []
        self.body = []

    def as_default(self, ast: 'AnsonAst'):
        self.base = SemanExpr(stype = '()', args=[ast.c_base()])
        return self

    def cpp_arg_decl(self, ast: 'AnsonAst'):
        '''
        e.g.:
            ctx, _type_, "sentinel",
        for JavaEnum subclasses, also force a 'JsonOpt* ctx'.
            Centralport(const JsonOpt* ctx) : JavaEnum(ctx, _type_, "_sentinel_") { }
        :param ast:
        :return:
        '''
        arglist = [arg.cpp_arg_decl() for arg in self.args]
        if isinstance(ast, AnsonJavaEnumAst):
            arglist.insert(0, 'const JsonOpt* ctx')
        return ', '.join(arglist)

    def cpp_base_ini(self, ast: 'AnsonAst'):
        '''
        :param ast:
        :return:
        e.g.

        1.    Port(ctx, "_sentinel_") for  WSPort(const JsonOpt* ctx) : Port(ctx, "_sentinel_")
        '''
        if self.base.stype != '()':
            return None

        basecls = self.base.args[0] if LangExt.len(self.base.args) > 0 else 'baseAnclass'
        # basecls = ast.c_base() if basecls == 'baseAnclass' else basecls
        basecls = ast.c_base() if basecls == 'baseAnclass' or basecls == 'base' else basecls
        base_args = ", ".join(self.base.args[1:]) if LangExt.len(self.base.args) > 0 else None

        if isinstance(ast, AnsonJavaEnumAst):
            base_args = ", ".join(['ctx', base_args]) if base_args else 'ctx'
        return f"{basecls}({base_args if base_args else ''})"

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

        if isinstance(ast, AnsonJavaEnumAst):
            if ast.c_class() != 'JavaEnum':
                '''
                WSPort(const JsonOpt * ctx): Port(ctx, "_sentinel_") {
                    Anclass(_type_);
                }
                '''
                ret = [indent + 'Anclass(_type_);']
        else: # not isinstance(ast, AnsonJavaEnumAst):
            ret = [indent + 'Type(_type_);'] if withType_setter else []

        if len(self.body) > 0:
            results = [exp.cpp_expr(indent) for exp in self.body]
            ret.extend(r for r in results if r is not None)
        elif isinstance(self.body, SemanExpr): # tolerate config error?
            ret.append(self.body.cpp_expr(indent))
        return ret

@dataclass
class AnValue(Anson):
    '''
    A value specified in an annotation, a name-value pair.
    Values are strings, with the declared type in valType.
    Java peer: io.odysz.reflect.AnValue, since antson 1.2.0.
    '''
    name: str

    valType: str
    '''boolean, int, ..., java.lang.String, java.lang.Class, the enum or annotation type; with "[]" for arrays'''

    value: str
    '''a scalar; class literals and enum constants as qualified names'''

    array: List[str]
    '''elements of an array of scalars'''

    annotations: List['AnAnnotation']
    '''nested annotation(s)'''

    def __init__(self):
        super().__init__()
        self.name = None
        self.valType = None
        self.value = None
        self.array = None
        self.annotations = None


@dataclass
class AnAnnotation(Anson):
    '''
    An annotation on a field, e.g. @AnsonField(ignoreFrom=true, ref=AnsonField.enclosing).
    Only the values specified are listed, not the defaults.
    Java peer: io.odysz.reflect.AnAnnotation, since antson 1.2.0.
    '''
    annotation: str
    '''Qualified name of the annotation type, e.g. io.odysz.anson.AnsonField'''

    runtime: bool
    '''RetentionPolicy.RUNTIME, otherwise CLASS'''

    elements: List[AnValue]
    '''element values specified, in order'''

    def __init__(self):
        super().__init__()
        self.annotation = None
        self.runtime = False
        self.elements = []

    def element(self, name: str) -> Union[AnValue, None]:
        '''
        :return: the element's value specified, None if not specified (default)
        '''
        return next((v for v in self.elements or [] if v.name == name), None)


@dataclass
class AnsonField(Anson):
    '''
    A field of AnsonAst, as generated by anson.reflect.
    Java peer: io.odysz.reflect.AnsonField; C++ peer: anson::AnsonField.

    Hand-written ASTs' fields have no type, and are dicts, which can have the generator's
    keys, e.g. javaType, default.
    '''
    fieldname: str

    dataAnclass: str
    '''In AST syntax, not closed, e.g. "map<string, list<io.oz.x.Doc.Kind"'''

    valType: str
    '''Element type of a list, value type of a map, in AST syntax'''

    modifiers: List[str]
    '''Java only, e.g. ["protected", "static", "final"]; package private: []'''

    annotations: List[AnAnnotation]
    '''Java only'''

    def __init__(self):
        super().__init__()
        self.fieldname = None
        self.dataAnclass = None
        self.valType = None
        self.modifiers = []
        self.annotations = []


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

    fields: dict[str, AnsonFieldMeta]
    '''
    {name: field}. Generated by anson.reflect, the fields are AnsonField, with types in the json.
    '''
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

    static_val: str

    # Java only, generated by anson.reflect
    kind: str
    '''class, interface, enum, record or annotation'''

    modifiers: List[str]

    interfaces: List[str]

    uses: List[str]
    '''Qualified names of the types in the non-private API'''

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
        self.javaClass = None

        self.static_val = None
        self.kind = None
        self.modifiers = []
        self.interfaces = []
        self.uses = []

    def c_class(self) -> str:
        return self.dataAnclass.split('.')[-1]

    def c_base(self) -> str:
        return self.baseAnclass.split('.')[-1] if len(self.baseAnclass) > 0 else ''


@dataclass
class AnsonJavaEnumAst(AnsonAst):
    decode: dict[str, str]
    encode: dict[str, str]

    def __init__(self):
        super().__init__()


@dataclass
class AnsonBodyAst(AnsonAst):
    A: dict[str, str]

    def __init__(self):
        super().__init__()
        self.A = {}


@dataclass
class AnsonMsgAst(AnsonAst):
    msg: str
    A: dict

    def __init__(self):
        super().__init__()
        self.A = {}

    def body(self):
        return self.anclass


@dataclass
class AnsonTypes(Anson):
    '''
    A module's anson-types.json, generated by the Maven plugin anson.reflect, packed into the jar.
    Java peer: io.odysz.reflect.AnsonTypes, since antson 1.2.0.
    '''
    module: str
    '''groupId:artifactId:version'''

    asts: dict[str, AnsonAst]
    '''{qualified name: ast}, the exported types'''

    dependencies: dict[str, str]
    '''{qualified name: groupId:artifactId:version}, where a used type, not of this module, is found'''

    def __init__(self):
        super().__init__()
        self.module = None
        self.asts = {}
        self.dependencies = {}

    def simple_names(self) -> dict[str, List[str]]:
        '''
        :return: {simple name: [qualified names]}, e.g. {'Port': ['io.odysz.semantic.jprotocol.AnsonMsg.Port']}
        '''
        names: dict[str, List[str]] = {}
        for q in self.asts:
            names.setdefault(q.rsplit('.', 1)[-1], []).append(q)
        return names


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
    java_gen: str
    '''
    Optional, the Java source root for generated data classes, e.g. "src/main/java".
    No Java peers are generated if blank.
    '''

    def __init__(self):
        super().__init__()
        self.tier_name = None
        self.ast_folder = 'ast'
        self.ansonMsg   = 'io.odysz.semantic.jprotocol.AnsonMsg'
        self.ansonBody  = 'io.odysz.semantic.jprotocol.AnsonBody'
        self.scopeEnums = []
        self.javaEnums  = []
        self.ansonMsgs  = []
        self.anRequests = []
        self.cpp_include= []
        self.cpp_gen    = 'semantier.gen.h'
        self.java_gen   = None


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
