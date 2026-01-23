#pragma once

#include "anson.hpp"

namespace anson {

/**
 * @brief The AnsonBody class
 * java type: io.odysz.semantic.jprotocol
 */
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

    RTTR_ENABLE(AnsonBody)
};

class UserReq : public AnsonBody {
public:
    string data;

    RTTR_ENABLE(AnsonBody)
};

enum class Port { query, update, echo };

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

enum class MsgCode { ok, exSession, exSemantic, exIo, exTransct, exDA, exGeneral, ext };

class AnsonResp : public AnsonBody{
public:
    MsgCode code;
    AnsonResp() : AnsonBody("NA", "io.odysz.semantic.jprotocol.AnsonResp") {}

    AnsonResp(string a) : AnsonBody(a, "io.odysz.semantic.jprotocol.AnsonResp") {}

    RTTR_ENABLE(AnsonBody)
};

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

class OnError {
    // virtual void err(MsgCode c, string& e, string... args);
    virtual void err(MsgCode code, std::string_view msg,std::initializer_list<std::string_view> args);
};
}

