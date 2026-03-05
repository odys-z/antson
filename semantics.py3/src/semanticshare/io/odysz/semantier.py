"""
@deprecated Moved to anson.py3/src/peers.py
"""

from dataclasses import dataclass
from pathlib import Path
from typing import List, cast, Union, TextIO
from warnings import catch_warnings

from anson.io.odysz.anson import Anson

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
    base: Union[str, None]
    anclass: str
    fields: dict
    enums: Union[dict, None]
    ctors: List[List[str]]

    def __init__(self):
        super().__init__()
        self.isEnum = False
        self.base = None
        self.anclass = 'must set'
        self.fields = {}
        self.enums = None
        self.ctors = []


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

@dataclass
class HeaderLines:
    start_cpp = '''#pragma once

#include <entt/meta/factory.hpp>
#include <entt/meta/meta.hpp>
#include "anson.h"

namespace anson {'''
    '''
    [0] pragma once ...
    '''

    class_decl = '''
public class {} : public anson::{} {{
public:'''
    '''
    [1] public class Req : public anson::AnsonBody { ...
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

    end_class = '''
};
'''

    end_cpp = '''
}'''


def parse_cpptype(t: str) -> str:
    try:
        astype = t.split('.')[-1]
        return astype if astype not in semantypes else semantypes[astype][0]
    except:
        return t


def parse_cpp_class(ast: AnsonAst):
    return parse_cpptype(ast.anclass)


def parse_cpp_base(ast: AnsonAst):
    return parse_cpptype(ast.base)


def parse_cpptypes(ts: List[str]) -> List[str]:
    ret = []
    for t in ts:
        ret.append(parse_cpptype(t))
    return ret


def end_class(h: TextIO, ast: AnsonMsgAst):
    for f, v in ast.fields.items():
        h.writelines(f'\n    {parse_cpptype(v)} {f};')
    else:
        h.writelines(HeaderLines.end_class)
    return ast


def gen_cpp_class(h: TextIO, ast: AnsonAst):
    # h.writelines(HeaderLines.class_start)
    h.writelines(HeaderLines.class_decl.format(parse_cpp_class(ast), parse_cpp_base(ast)))

    if isinstance(ast, AnsonMsgAst) and ast.A:
        h.writelines(HeaderLines.struct_A)
        for act, const in ast.A.items():
            h.writelines(HeaderLines.act_enum.format(act, const))
        else:
            h.writelines(HeaderLines.end_A)

    return end_class(h, ast)


@dataclass()
class EnttLines:
    start_cpp = '''#pragma once

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
        .type("{type_name}"_hs)'''

    base = '''
        .base<anson::{}>()'''

    ctor = '''
        .ctor<{paras}>()'''

    anfield = '''
        .data<&anson::{type_name}::{field}>("{field}"_hs, "{field}")'''

    speclize_msg = '''
    entt::meta_factory<anson::AnsonMsg<{body_type}>>()
        .type("AnsonMsg{body_type}"_hs)
        .base<anson::Anson>()
        .ctor<>()
        .ctor<anson::IPort>()
        .ctor<anson::IPort, anson::MsgCode>()
        .data<&anson::AnsonMsg<{body_type}>::body>("body"_hs, "body")
        .data<&anson::AnsonMsg<{body_type}>::port>("port"_hs, "port")
        ;'''

    enum_start = '''
    entt::meta_factory<anson::{enum_type}>()
        .type("{enum_type}"_hs)'''

    enum_val = '''
        .data<anson::{enum_val}::{enum_val}>("{enum_val}"_hs, "{enum_val}")'''

    end_anson = '''
}
}
    '''


def regist_entt_enum(entt: TextIO, ast: AnsonAst):
    entt.writelines(EnttLines.enum_start)

    for enum_val in ast.enums:
        entt.writelines(EnttLines.enum_val.format(enum_val=enum_val))


def regist_entt_anson(entt: TextIO, ast: AnsonAst):
    # inline void regist_meta() {...
    entt.writelines(EnttLines.anson_start.format(type_name=parse_cpptype(ast.anclass)))

    # .ctor<para1, para2, ...>()
    for para_list in ast.ctors:
        entt.writelines(EnttLines.ctor.format(paras=', '.join(parse_cpptypes(para_list))))

    # .base<anson::AnsonBody>()
    if ast.base:
        entt.writelines(EnttLines.base.format(parse_cpptype(ast.base)))

    # .data<&anson::UserReq::data>("data"_hs, "data")
    for field in ast.fields:
        entt.writelines(EnttLines.anfield.format(type_name=ast.anclass, field=field))

    entt.writelines('\n        ;\n')

    if isinstance(ast, AnsonMsgAst):
        entt.writelines(EnttLines.speclize_msg.format(body_type=parse_cpptype(ast.body())))
        entt.writelines('\n')


@dataclass
class PeerSettings(Anson):
    """
    TODO there must be a similar / equivalent in @anclient/semantier?
    """
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
        # self.src = "src"
        # self.headers = []
        self.header = None
        self.py = None
        self.ts = None
        self.json_h = "json.hpp"


def gen_cpp_peer(settings: PeerSettings, config_path: Path):
    h_pth = config_path / settings.header
    h_pth.parent.mkdir(parents=True, exist_ok=True)

    entt_pth = config_path / settings.json_h
    entt_pth.parent.mkdir(parents=True, exist_ok=True)

    with open(h_pth, 'w') as hpp, open(entt_pth, 'w') as entt:
        hpp.writelines(HeaderLines.start_cpp)
        entt.writelines(EnttLines.start_cpp)

        for [astjson, timestamp] in settings.requests:
            ast = None
            ast = cast(AnsonAst, Anson.from_file(config_path / astjson))
            if ast is None:
                continue

            gen_cpp_class(hpp, ast)
            if ast.isEnum:
                regist_entt_enum(entt, ast)
            else:
                regist_entt_anson(entt, ast)

        else:
            hpp.writelines(HeaderLines.end_cpp)
            entt.writelines(EnttLines.end_anson)


def gen_peers(settings: PeerSettings, config_path: Path) -> None:
    # gen_ts_peer(settings)
    # gen_py_peer(settings)
    gen_cpp_peer(settings, config_path)
