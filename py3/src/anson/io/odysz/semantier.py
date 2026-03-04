"""
@deprecated Moved to anson.py3/src/semantier.py
"""

from dataclasses import dataclass
from idlelib.browser import file_open
from typing import List, cast, Union, TextIO

from anson.io.odysz.anson import Anson

@dataclass
class AnsonAst():
    isEnum: bool
    base  : Union[str, None]
    fields: dict
    enums: Union[dict, None]
    ctors: List[List[str]]

    def __init__(self):
        super().__init__()
        self.isEnum = False
        self.base   = None
        self.fields = {}
        self.enums = None
        self.ctors = []

@dataclass
class AnsonMsgAst(AnsonAst):
    """

    """
    body: str
    A: dict

    def __init__(self):
        super().__init__()
        self.A = {}


def gen_py(py: TextIO, ast: AnsonAst):
    pass

#################################################################################


def gen_ts(ts: TextIO, ast: AnsonAst):
    pass

#################################################################################
'''
class EchoReq: public AnsonBody {
public:
    inline static const std::string _type_ = "io.odysz.jprotocol.EchoReq";

    string echo;

    EchoReq() : AnsonBody("r/query", EchoReq::_type_) {}

    EchoReq(string echo) : AnsonBody("r/query", EchoReq::_type_), echo(echo) {}
};
'''

h_lines = [
    # 0
    '''#pragma once
    
    #include <entt/meta/factory.hpp>
    #include <entt/meta/meta.hpp>
    #include "anson.h"
    
    namespace anson {
    ''',
    # 1
    '''public class {} : public anson::{} {{
    public:
    ''',
    # 2
    '    struct A {',
    # 3 A.a ...
    '        inline static const string {} = "{}";',
    # 4 end struct A
    '    };',
    # 5 end class & namespace
    '''
    };
    }
    ''' ]
'''
[0] pragma once ...

[1] public class Req : public anson::AnsonBody { ...

[2] stuct A {

[3] inline static const string...

[4] };

[5] ... }; }
'''


def parse_classname(ast: AnsonAst):
    pass

def parse_cpptype(t: str) -> str:
    pass

def parse_cpptypes(t: List[str]) -> List[str]:
    pass

def end_req_h(h: TextIO, ast: AnsonMsgAst):
    for f, v in ast.fields.items():
        h.writelines(f'    {f} {parse_cpptype(v)};')
    else:
        h.writelines(h_lines[5])
    return ast

def gen_cpp(h: TextIO, ast: AnsonAst):
    # start req h(h: TextIO, ast: AnsonMsgAst):
    h.writelines(h_lines[0])
    h.writelines(h_lines[1].format(parse_classname(ast)))

    if isinstance(ast, AnsonMsgAst) and ast.A:
        h.writelines(h_lines[2])
        for act, const in ast.A.items():
            h.writelines(h_lines[3].format(act, const))
        else:
            h.writelines(h_lines[4])

    return end_req_h(h, ast)

@dataclass()
class EnttLines:
    head = '''#pragma once

#include <entt/meta/factory.hpp>
#include <entt/meta/meta.hpp>

#include "anson.h"
#include "jprotocol.h"

using namespace entt::literals;

namespace anson {
inline void register_meta() {
'''

    anson_start = '''
    entt::meta_factory<anson::{type_name}>()
        .type("{type_name}"_hs)
        .ctor<{paras}>()'''

    base = \
        '        .base<anson::{base}>()'

    ctor = \
        '        .ctor<{paras}>()'

    anfield = \
        '        .data<&anson::{type_name}::{filed}>("{field}"_hs, "{field}")'

    speclize_msg = '''
    entt::meta_factory<anson::AnsonMsg<{body_type}>>()
        .type("AnsonMsg{body_type}"_hs)
        .base<anson::Anson>()
        .ctor<>()
        .ctor<anson::IPort>()
        .ctor<anson::IPort, anson::Msgcode>()
        .data<&anson::AnsonMsg<{body_type}>::body>("body"_hs, "body")
        .data<&anson::AnsonMsg<{body_type}>::port>("port"_hs, "port")
        ;'''

    enum_start = '''
    entt::meta_factory<anson::{enum_type}>()
        .type("{enum_type}"_hs)
    '''

    enum_val = \
        '        .data<anson::{enum_val}::{enum_val}>("{enum_val}"_hs, "{enum_val}")'


def regist_entt_enum(entt: TextIO, ast: AnsonAst):
    entt.writelines(EnttLines.enum_start)

    for enum_val in ast.enums:
        entt.writelines(EnttLines.enum_val.format(enum_val = enum_val))


def regist_entt_anson(entt: TextIO, ast: AnsonAst):
    # inline void regist_meta() {...
    entt.writelines(EnttLines.anson_start)

    # .ctor<para1, para2, ...>()
    for para_list in ast.ctors:
        entt.writelines(EnttLines.ctor.format(paras = ', '.join(parse_cpptypes(para_list))))

    #  .base<anson::AnsonBody>()
    if ast.base:
        entt.writelines(EnttLines.base.format(parse_cpptype(ast.base)))

    # .data<&anson::UserReq::data>("data"_hs, "data")
    for field in ast.fields:
        entt.writelines(EnttLines.anfield.format(type_name = field))

    entt.writelines('        ;')

    if isinstance(ast, AnsonMsgAst):
        entt.writelines(EnttLines.speclize_msg.format(body_type = parse_cpptype(ast.body)))


def gen_entt(entt: TextIO, asts: List[AnsonAst]):
    entt.writelines(EnttLines.head)
    for ast in asts:
        if ast.isEnum:
            regist_entt_enum(entt, ast)
        else:
            regist_entt_anson(entt, ast)


@dataclass
class SemanticSettings(Anson):
    """
    Mapping for gen.cmake.CSettings
    """
    src: str

    headers: List[str]
    '''
    @deprecated: for tree-sitter only, is replaced by AstConfig
    '''

    ###########################################################
    header: str
    json_h: str
    py: str
    ts: str

    # ansonMsg: str
    requests: List[List[str]]
    '''
    ASTs generated by anson.java AstHelper.
    '''

    def __init__(self):
        super().__init__()
        self.src = "src"
        self.headers = []
        self.json_h = "json.hpp"


def gen_peers(settings: SemanticSettings):
    count = 0
    entt_regs = []
    for astjson in settings.requests:
        ast = None
        with file_open(astjson, 'r') as f:
            ast = cast(AnsonAst, Anson.from_file(f))
        if ast is None:
            continue

        if settings.py:
            with file_open(settings.py, 'a' if count == 0 else 'w') as py:
                gen_py(py, ast)

        if settings.header:
            # with file_open(settings.header, 'w') as header, file_open(settings.json_h, 'w') as entt:
            with file_open(settings.header, 'a' if count == 0 else 'w') as header:
                entt_regs.append(gen_cpp(header, ast))

        if settings.ts:
            with file_open(settings.ts, 'a' if count == 0 else 'w') as ts:
                gen_ts(ts, ast)

        count = count+1

    if settings.json_h and count > 0:
        with file_open(settings.json_h, 'w') as entt:
            gen_entt(entt, entt_regs)
