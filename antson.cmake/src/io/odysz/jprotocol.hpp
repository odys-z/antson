#pragma once

#include "anson.hpp"

namespace anson {

class AnsonBody : public anson::Anson {
public:
    string a;

    AnsonBody(string a) : Anson("io.odysz.jprotocol.AnsonBody") , a(a) {}

    AnsonBody(string a, string type) : Anson(type), a(a) {}
    RTTR_ENABLE(Anson)
};

class EchoReq: public AnsonBody {
public:
    string echo;

    EchoReq() : AnsonBody("r/query", "io.odysz.jprotocol.EchoReq") {}

    EchoReq(string echo) : AnsonBody("r/query", "io.odysz.jprotocol.EchoReq"), echo(echo) {}

    RTTR_ENABLE()
};

enum class Port {
    query,
    update,
    echo
};

std::ostream& operator<<(std::ostream& os, const Port& p) {
    rttr::enumeration e = rttr::type::get<Port>().get_enumeration();
    std::string name = e.value_to_name(p).to_string();

    if (name.empty())  os << static_cast<int>(p);
    else os << name;
    return os;
}

bool operator==(const Port& p, const std::string& s) {
    rttr::enumeration e = rttr::type::get<Port>().get_enumeration();
    return e.value_to_name(p).to_string() == s;
}

bool operator==(const std::string& s, const Port& p) {
    return p == s;
}

// c20 template<std::derived_from<AnsonBody> T = AnsonBody>
template <
    typename T,
    typename = std::enable_if_t<std::is_base_of_v<AnsonBody, T>>
    >
class AnsonMsg: public Anson {
public:
    vector<shared_ptr<T>> body;

    Port port;

    AnsonMsg(Port port) : Anson("io.odysz.jprotocol.AnsonMsg"), port(port) {}

    RTTR_ENABLE(Anson)
};

}

