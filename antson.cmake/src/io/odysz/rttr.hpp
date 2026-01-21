#pragma once

/**
 * What:
 * The RTTR_REGISTRATION block for all Anson types.
 *
 * Why:
 * Up to Jan 2026, RTTR is still can not separate RTTR_REGISTRATION blocks.
 * See Issue 106: https://github.com/rttrorg/rttr/issues/106
 */

#include <rttr/type.h>
#include <rttr/registration>
#include "anson.hpp"
#include "jprotocol.hpp"

#include <nlohmann/json.hpp>

#define NL '\n'

RTTR_REGISTRATION {
    using namespace rttr;
    using namespace anson;
    rttr::registration::class_<Anson>("anson::Anson")
        .constructor<std::string>()
         (policy::ctor::as_std_shared_ptr,
          default_arguments(string("-type-")) )
        .property("type", &Anson::type)
        ;

    rttr::registration::class_<AnsonBody>("anson::AnsonBody")
        .constructor<std::string>()
         (policy::ctor::as_std_shared_ptr,
          default_arguments(string("-a-")) )
        .property("a", &AnsonBody::a)
        ;

    rttr::registration::enumeration<Port>("PortType")(
        rttr::value("query", Port::query),
        rttr::value("update", Port::update),
        rttr::value("echo", Port::echo)
        );

    using Req = AnsonMsg<EchoReq>;
    rttr::registration::class_<Req>("anson::AnsonMsg<EchoReq>")
        .constructor<Port>()
        .property("port", &Req::port)
        ;
}

class RttrSaxHandler : public nlohmann::json_sax<nlohmann::json> {
    rttr::instance m_instance;
    std::string m_current_key;

public:
    RttrSaxHandler(rttr::instance inst) : m_instance(inst) {}

    bool key(string_t& val) override { m_current_key = val; return true; }

    bool string(string_t& val) override {
        auto prop = m_instance.get_type().get_property(m_current_key);
        std::cout << "on-string(): {" << m_current_key << ": " << val << "}\n";
        if (prop) prop.set_value(m_instance, val);
        else
            std::cout << "   " << m_instance.get_type().get_name().to_string() << "  prop not found: " << m_current_key << '\n';
        return true;
    }

    bool binary(binary_t& val) override {
        auto prop = m_instance.get_type().get_property(m_current_key);
        if (prop) prop.set_value(m_instance, val);
        return true;
    }

    bool number_integer(number_integer_t val) override {
        auto prop = m_instance.get_type().get_property(m_current_key);
        if (prop) prop.set_value(m_instance, static_cast<int>(val));
        return true;
    }

    // Boilerplate for remaining SAX events
    bool null() override { return true; }
    bool boolean(bool val) override { return true; }
    bool number_unsigned(number_unsigned_t val) override { return number_integer(static_cast<number_integer_t>(val)); }
    bool number_float(number_float_t val, const string_t& s) override { return true; }
    bool start_object(std::size_t elements) override { return true; }
    bool end_object() override { return true; }
    bool start_array(std::size_t elements) override { return true; }

    bool end_array() override {
        // auto prop = m_instance.get_type().get_property(m_current_key);
        std::cout << "end-array(): " << m_current_key << "\n";
        return true;
    }

    bool parse_error(std::size_t, const std::string&, const nlohmann::json::exception&) override { return false; }
};

using json = nlohmann::json;
using namespace rttr;

void serialize_anson(std::ostream& os, rttr::variant var) {
    if (!var.is_valid()) {
        os << "null";
        return;
    }

    type t = var.get_type();

    // 1. Handle Sequential Containers (vector<shared_ptr<T>>)
    if (t.is_sequential_container()) {
        variant_sequential_view view = var.create_sequential_view();
        os << "[";
        bool first = true;
        for (const auto& item : view) {
            if (!first) os << ",";
            serialize_anson(os, item);
            first = false;
        }
        os << "]";
        return;
    }

    // 2. Handle Wrappers (shared_ptr, unique_ptr)
    if (t.is_wrapper()) {
        serialize_anson(os, var.extract_wrapped_value());
        return;
    }

    // 3. Handle Primitives (int, float, bool)
    if (t.is_arithmetic()) {
        // Let nlohmann::json handle numeric formatting
        if (t == type::get<bool>()) {
            os << (var.get_value<bool>() ? "true" : "false");
        } else {
            os << var.to_string();
        }
        return;
    }

    // 3. NEW: Handle Enums
    if (t.is_enumeration()) {
        rttr::enumeration e = t.get_enumeration();
        std::string name = e.value_to_name(var).to_string();
        if (name.empty()) {
            // Fallback to integer if name is not registered
            os << var.to_string();
        } else {
            os << nlohmann::json(name).dump();
        }
        return;
    }

    // 4. Handle Strings (Properly escaped)
    if (t == type::get<std::string>()) {
        os << json(var.get_value<std::string>()).dump();
        return;
    }

    // 5. Handle Objects (Anson, AnsonBody, EchoReq)
    os << "{";
    bool first = true;
    for (auto& prop : t.get_properties()) {
        if (!first) os << ",";

        os << "\"" << prop.get_name() << "\":";
        serialize_anson(os, prop.get_value(var));

        first = false;
    }
    os << "}";
}
