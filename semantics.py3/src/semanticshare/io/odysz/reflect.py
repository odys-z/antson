
from dataclasses import dataclass
from pathlib import Path
from typing import List, cast, Union

from anson.io.odysz.anson import Anson, AnsonField
from anson.io.odysz.common import Utils

from .semantier import PeerSettings

semantypes = {
    # Ast type: cpp type, python, ts
    'String': ['string', 'str', 'string'],
    'long': ['long', 'int', 'int'],
    'int': ['int', 'int', 'int'],
    'List': ['vector', 'List', '[]'],
    'Map': ['map', 'Map', '{}']
}

@dataclass
class AnsonAst(Anson):
    isEnum: bool
    anclass: str

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
        self.isEnum = False
        # self.base = None
        self.anclass = 'must set'
        self.fields = {}
        self.enums = None
        self.ctors = []

    def c_class(self) -> str:
        return self.dataAnclass.split('.')[-1]

    def c_base(self) -> str:
        return self.baseAnclass.split('.')[-1]


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
class MsgLines:
    start_header = '''#pragma once

#include <entt/meta/factory.hpp>
#include <entt/meta/meta.hpp>
#include "entt_jserv.h"

namespace anson {'''
    '''
    [0] pragma once ...
    '''

    class_decl = '''
public class {} : public anson::{} {{
public:
    inline static const std::string _type_ = "{}";'''
    '''
    [1] public class {Req} : public anson::{AnsonBody} { _type_={} ...
    '''

    struct_A = '''
    struct A {'''
    '''
    [2] stuct A {
    '''
    # A.a ...

    act_enum = '''
        inline static const string {} = "{}";'''
    '''
    [3] inline static const string...
    '''

    end_A = '''
    };'''

    inline_static = True

    # 0: echoreq, 1: EchoReq
    load_ast = '''void load_{0}Ast(AstMap &asts, const string &ast_path) {{
        specialize_msg_astpth<{1}>(asts, ast_path,
          [](meta_factory<{1}> &entf, AnsonBodyAst *ast) {{'''
    entt_data = 'entf.data<&{0}::{1}>("{1}");'
    field_getter0 = '''
            ast->get_field_instance = [ast](const IJsonable& ans, const string& fieldname) -> meta_any {{
                if (ast->fields.contains(fieldname)) {{
                    auto& concrete = static_cast<const {0}&>(ans);'''
    field_getif ='''if ("{0}" == fieldname)
                        return entt::forward_as_meta(concrete.{0});'''
    field_getter9 = '''
                }

                if (IJsonable::contxt_ptr->has_ast(ast->dataBaseAst)) {
                    AnsonBodyAst *bast = IJsonable::contxt_ptr->ast<AnsonBodyAst>(ast->dataBaseAst);
                    return bast->get_field_instance(ans, fieldname);
                }

                anerror("get_field_instance<EchoReq>(): Failed to get entt instance (meta_any)");
                return {};
            };
          });
        }
    '''

    end_req = '''
};
'''

    end_ns = '''
}'''

    def specialize_req(self, ast: AnsonBodyAst):
        '''
        Example
        =======
        class EchoReq: public AnsonBody {
        public:
            inline static const std::string _type_ = "io.odysz.semantic.jserv.echo.EchoReq";
            struct A {
                inline static const string echo = "echo";
                inline static const string inet = "inet";
            };

            string echo;
            EchoReq() : AnsonBody("r/query", EchoReq::_type_) {}
            EchoReq(string echo) : AnsonBody("r/query", EchoReq::_type_), echo(echo) {}
        };

        inline static void load_echoAst_expect(AstMap &asts, const string &ast_path) {
            specialize_msg_astpth<EchoReq>(asts, ast_path,
              [](meta_factory<EchoReq> &entf, AnsonBodyAst *ast) {

                entf.data<&EchoReq::echo>("echo");

                ast->get_field_instance = [ast](const IJsonable& ans, const string& fieldname) -> meta_any {
                    if (ast->fields.contains(fieldname)) {
                        auto& concrete = static_cast<const EchoReq&>(ans);
                        if ("echo" == fieldname)
                            return entt::forward_as_meta(concrete.echo);
                    }

                    if (IJsonable::contxt_ptr->has_ast(ast->dataBaseAst)) {
                        AnsonBodyAst *bast = IJsonable::contxt_ptr->ast<AnsonBodyAst>(ast->dataBaseAst);
                        return bast->get_field_instance(ans, fieldname);
                    }

                    anerror("get_field_instance<EchoReq>(): Failed to get entt instance (meta_any)");
                    return {};
                };
            });
        }
        :param ast:
        :return: formatted source header lines
        '''
        return [self.start_header, self.class_decl.format(ast.c_class(), ast.c_base(), ast.dataAnclass),
                self.struct_A,
                *[f'inline statci const string {k} = "{v}";' for k, v in ast.A.items()],
                '\t' + ('inline static ' if self.inline_static else '') + self.load_ast.format(
                    ast.c_class().lower(), ast.c_class()),
                *[self.entt_data.format(ast.c_class(), fn) for fn, _ in ast.fields.items()],
                self.field_getter0.format(ast.c_class()),
                *[self.field_getif.format(fn) for fn, _ in ast.fields.items()],
                self.field_getter9,
                self.end_req
                ]


def gen_cpp_peer(settings: PeerSettings, ast_folder: Path):
    '''
    :param settings:
    :param config_path:
    :return:
    '''

    msglines = MsgLines()

    gen_pth = Path(settings.cpp_gen)
    gen_pth.parent.mkdir(parents=True, exist_ok=True)

    with open(gen_pth, 'w') as gen:
        gen.writelines(msglines.start_header)

        for astjson in settings.anRequests:
            if Path(astjson).exists():
                ast = cast(AnsonBodyAst, Anson.from_file(str(ast_folder / astjson)))
                msglines.specialize_req(ast)
            else:
                Utils.warn('Cannot find file ' + astjson)

        gen.writelines(msglines.end_ns)


def gen_peers(settings: PeerSettings, config_path: Path) -> None:
    # gen_ts_peer(settings)
    # gen_py_peer(settings)
    gen_cpp_peer(settings, config_path)
