from dataclasses import dataclass
from typing import TextIO, List


@dataclass
class AnsonAst():
    pass


@dataclass
class AnsonMsgAst(AnsonAst):
    """

    """
    base: str
    body: str
    A: dict
    fields: dict



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

def parse_classname(ast: AnsonAst):
    pass

def parse_cpptype(t: str) -> str:
    pass


def add_A_h(h: TextIO, f, v):
    h.writelines(h_lines[3].format(f, v))

def end_A_h(h: TextIO, ast: AnsonMsgAst):
    h.writelines(h_lines[4])

def end_req_h(h: TextIO, ast: AnsonMsgAst):
    for f, v in ast.fields.items():
        h.writelines(f'    {f} {parse_cpptype(v)};')
    else:
        h.writelines(h_lines[5])

def gen_cpp(h: TextIO, ast: AnsonAst):
    # start req h(h: TextIO, ast: AnsonMsgAst):
    h.writelines(h_lines[0])
    h.writelines(h_lines[1].format(parse_classname(ast)))
    h.writelines(h_lines[2])

    return ast

def gen_entt(entt: TextIO, regs: List[AnsonAst]):
    pass