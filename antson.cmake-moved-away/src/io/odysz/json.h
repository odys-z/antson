/**
 * The eqivalent of gen.antlr.json + JSONAnsonListener
 */
#pragma once
#include <nlohmann/json.hpp>
#include <entt/meta/meta.hpp>
#include <entt/entt.hpp>
#include <entt/meta/container.hpp>
#include <vector>
#include <string>
#include "anson.h"
#include "jprotocol.h"

// using json = nlohmann::json;
// using namespace nlohmann;

namespace anson {

using namespace entt::literals;

inline void register_meta() {
    using namespace entt::literals;

    // Register Anson Base
    entt::meta_factory<anson::Anson>()
        .type("Anson"_hs)
        .ctor<>()
        .ctor<const std::string&>()
        .data<&anson::Anson::type>("type"_hs, "type");

    // Register SemanticObject
    entt::meta_factory<anson::SemanticObject>()
        .type("SemanticObject"_hs)
        .ctor<>()
        .base<anson::Anson>();

    // Register AnsonBody
    entt::meta_factory<anson::AnsonBody>()
        .type("AnsonBody"_hs)
        .ctor<const std::string&>()
        .ctor<const std::string&, const std::string&>()
        .base<anson::Anson>()
        .data<&anson::AnsonBody::a>("a"_hs, "a");

    // Register UserReq
    entt::meta_factory<anson::UserReq>()
        .type("UserReq"_hs)
        .ctor<const std::string&>()
        .base<anson::AnsonBody>()
        .data<&anson::UserReq::data>("data"_hs, "data");

    // Register EchoReq
    entt::meta_factory<anson::EchoReq>()
        .type("EchoReq"_hs)
        .ctor<>()
        .ctor<const std::string&>()
        .base<anson::AnsonBody>()
        .data<&anson::EchoReq::echo>("echo"_hs, "echo");

    // Register AnsonResp
    entt::meta_factory<anson::AnsonResp>()
        .type("AnsonResp"_hs)
        .ctor<>()
        .ctor<const std::string&>()
        .base<anson::AnsonBody>();

    // Register AnsonMsg template (example for EchoReq)
    entt::meta_factory<anson::AnsonMsg<anson::EchoReq>>()
        .type("AnsonMsgEcho"_hs)
        .ctor<anson::Port>()
        .base<anson::Anson>()
        .data<&anson::AnsonMsg<anson::EchoReq>::port>("port"_hs, "port")
        .data<&anson::AnsonMsg<anson::EchoReq>::body>("body"_hs, "body");

    // Register Port enum
    entt::meta_factory<anson::Port>()
        .type("Port"_hs)
        .data<anson::Port::query>("query"_hs)
        .data<anson::Port::update>("update"_hs)
        .data<anson::Port::echo>("echo"_hs);

    // Register MsgCode enum
    entt::meta_factory<anson::MsgCode>()
        .type("MsgCode"_hs)
        .data<anson::MsgCode::ok>("ok"_hs)
        .data<anson::MsgCode::exSession>("exSession"_hs)
        .data<anson::MsgCode::exSemantic>("exSemantic"_hs)
        .data<anson::MsgCode::exIo>("exIo"_hs)
        .data<anson::MsgCode::exTransct>("exTransct"_hs)
        .data<anson::MsgCode::exDA>("exDA"_hs)
        .data<anson::MsgCode::exGeneral>("exGeneral"_hs)
        .data<anson::MsgCode::ext>("ext"_hs);
}

inline ostream& serialize_recursive(const entt::meta_any &instance, std::ostream &os);

inline ostream& serialize_kvs(const entt::meta_type &type, const entt::meta_any &instance, std::ostream &os, bool &first) {
    // // 1. First, handle base classes (Recursive)
    // for (auto [id, base] : type.base()) {
    //     serialize_object_fields(base.type(), instance, os, first);
    // }

    // 2. Then, handle fields of the current class
    for (auto [id, data] : type.data()) {
        if (!first) os << ", ";

        // Use .name() from your meta_factory registration
        os << "\"" << data.name() << "\": ";

        // Pass the instance to data.get() to extract the value
        serialize_recursive(data.get(instance), os);
        first = false;
    }
    return os;
}

inline ostream& serialize_object(const entt::meta_type &type, const entt::meta_any &instance, std::ostream &os) {
    // 1. First, handle base classes (Recursive)
    bool first{true};
    os << "{";

    for (auto [id, base] : type.base())
        serialize_kvs(base, instance, os, first);

    serialize_kvs(type, instance, os, first);

    os << "}";
    return os;
}

inline ostream& serialize_recursive(const entt::meta_any &instance, std::ostream &os) {
    if (!instance) return os;

    auto type = instance.type();

    // 1. Dereference pointers (shared_ptr<EchoReq> -> EchoReq)
    if (type.is_pointer() || type.is_pointer_like()) {
        auto deref = *instance;
        serialize_recursive(deref, os);
        return os;
    }

    // 1. Strings (Specific check before sequence)
    if (auto s = instance.try_cast<std::string>()) {
        os << "\"" << *s << "\"";
        return os;
    }

    // 2. Enums
    if (type.is_enum()) {
        // This will use your overloaded operator<< for anson::Port
        if (auto p = instance.try_cast<anson::Port>()) os << *p;
        return os;
    }

    // 3. Sequence Containers (std::vector, etc.)
    // We use the meta_type to get the sequence view
    if (type.is_sequence_container()) {
        auto view = instance.as_sequence_container();
        // If as_sequence() still fails here, use the explicit version:
        // auto view = entt::meta_sequence_view{instance};
        os << "[";
        bool first = true;
        for (auto element : view) {
            if (!first) os << ", ";
            serialize_recursive(element, os);
            first = false;
        }
        os << "]";
        return os;
    }

    // 4. Associative Containers (std::map)
    if (type.is_associative_container()) {
        auto view = instance.as_associative_container();
        os << "{";
        bool first = true;
        for (auto [key, value] : view) {
            if (!first) os << ", ";
            serialize_recursive(key, os);
            os << ": ";
            serialize_recursive(value, os);
            first = false;
        }
        os << "}";
        return os;
    }

    // 5. Handling std::any for UserReq::data
    if (auto a = instance.try_cast<std::any>()) {
        // Check for shared_ptr<Anson> as requested
        if (a->has_value() && a->type() == typeid(std::shared_ptr<anson::Anson>)) {
            serialize_object(type, instance, os);
        }
        return os;
    }

    // 6. General Objects (Reflection)
    return serialize_object(type, instance, os);
}

inline string serialize_json(const entt::meta_any &instance) {
    if (!instance)  return string(nullptr);

    std::stringstream ss;
    serialize_recursive(instance, ss);
    return ss.str();
}


class EnTTSaxParser : public nlohmann::json_sax<nlohmann::json> {
private:
    std::vector<entt::meta_any> stack;
    entt::id_type active_key{0};

    // Helper to set values on the current object in the stack
    template<typename T>
    void set_value(T&& val) {
        if (!stack.empty() && active_key != 0) {
            auto data = stack.back().type().data(active_key);
            if (data) {
                data.set(stack.back(), std::forward<T>(val));
            }
        }
    }

public:
    EnTTSaxParser(Anson& obj) {
    }

    bool start_object(std::size_t size) override {
        if (active_key != 0 && !stack.empty()) {
            auto data = stack.back().type().data(active_key);
            if (data) {
                stack.push_back(data.get(stack.back()));
            }
        } else if (stack.empty()) {
            // This is the root object, we assume the caller set it up
            return true;
        }
        return true;
    }

    bool key(string_t& val) override {
        active_key = entt::hashed_string{val.c_str()};
        return true;
    }

    bool end_object() override {
        if (stack.size() > 1) stack.pop_back();
        active_key = 0;
        return true;
    }

    // 2. Data Type Handling
    bool number_float(number_float_t val, const string_t&) override {
        set_value(static_cast<float>(val));
        return true;
    }

    bool number_integer(number_integer_t val) override {
        set_value(static_cast<int>(val));
        return true;
    }

    bool string(string_t& val) override {
        set_value(val);
        return true;
    }

    bool boolean(bool val) override {
        set_value(val);
        return true;
    }

    // 3. Boilerplate requirements
    bool null() override { return true; }
    bool number_unsigned(number_unsigned_t val) override { set_value(static_cast<int>(val)); return true; }
    bool binary(binary_t&) override { return true; }
    bool start_array(std::size_t) override { return true; }
    bool end_array() override { return true; }
    bool parse_error(std::size_t, const std::string&, const nlohmann::detail::exception&) override { return false; }

    // Root Management
    void set_root(entt::meta_any instance) { stack.push_back(instance); }
};
}
