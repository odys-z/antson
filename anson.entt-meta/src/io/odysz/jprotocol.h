#pragma once

#include <entt/meta/factory.hpp>
#include <entt/meta/meta.hpp>
#include "anson.h"

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
};

class EchoReq: public AnsonBody {
public:
    inline static const std::string _type_ = "io.odysz.jprotocol.EchoReq";

    string echo;

    EchoReq() : AnsonBody("r/query", EchoReq::_type_) {}

    EchoReq(string echo) : AnsonBody("r/query", EchoReq::_type_), echo(echo) {}
};

class UserReq : public AnsonBody {
public:
    string data;
    UserReq(string a) : AnsonBody(a, "io.odysz.jprotocol.UserReq") {}
};

enum class Port { query, update, echo };

void register_port_meta() {
    using namespace entt::literals;

    entt::meta_factory<Port>()
        .type("Port"_hs, "io.odysz.anson.Port")
        .data<Port::query>("query"_hs, "query")
        .data<Port::update>("update"_hs, "update")
        .data<Port::echo>("echo"_hs, "echo")
        ;
}

inline std::ostream& operator<<(std::ostream& os, const Port& p) {
    using namespace entt::literals;

    auto type = entt::resolve<Port>();

    if (type) {
        for (auto [id, data] : type.data()) {
            if (data.get({}).cast<Port>() == p) {
                // os << "Port::" << id;
                return os << id;
            }
        }
    }

    return os << static_cast<int>(p); // Fallback to numeric value
}

template<typename E>
std::optional<E> from_enum_string(const std::string& s) {
    using namespace entt::literals;
    auto type = entt::resolve<E>();

    if (type) {
        for (auto [id, data] : type.data()) {
            // if (auto prop = data.prop("name"_hs)) {
            if (auto prop = data.name()) {
                // if (prop.value().template cast<const char*>() == s) {
                if (prop == s) {
                    return data.get({}).template cast<E>();
                }
            }
        }
    }
    return std::nullopt;
}

inline bool operator==(const Port& p, const std::string& s) {
    auto converted = from_enum_string<Port>(s);

    return converted.has_value() && (converted.value() == p);
}

inline bool operator==(const std::string& s, const Port& p) {
    return p == from_enum_string<Port>(s);
}

enum class MsgCode { ok, exSession, exSemantic, exIo, exTransct, exDA, exGeneral, ext };

class AnsonResp : public AnsonBody{
public:
    MsgCode code;
    AnsonResp() : AnsonBody("NA", "io.odysz.semantic.jprotocol.AnsonResp") {}

    AnsonResp(string a) : AnsonBody(a, "io.odysz.semantic.jprotocol.AnsonResp") {}
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
};

class OnError {
    // virtual void err(MsgCode c, string& e, string... args);
    virtual void err(MsgCode code, std::string_view msg,std::initializer_list<std::string_view> args);
};
}

