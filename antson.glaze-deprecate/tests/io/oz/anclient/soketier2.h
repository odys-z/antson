#ifndef io_oz_anclient_socketier_hpp
#define io_oz_anclient_socketier_hpp

#include <string>
#include "io/odysz/semantic/jprotocol.hpp"

using namespace std;

class WSEchoReq;

// template <>
// constexpr std::string_view get_expected_type_tag<WSEchoReq>() noexcept {
//     return "io.oz.anclient.socketier.WSEchoReq";
// }

// using ReadFn  = void(*)(WSEchoReq&, glz::is_context auto&&, auto&&, auto&&) noexcept;


// template <>
// std::string_view write_message_type<WSEchoReq>(const WSEchoReq&) noexcept {
//     return "io.oz.anclient.socketier.WSEchoReq";
// }

// template <class T>
// void ignore_type_tag(T&, glz::is_context auto&& ctx, auto&& it, auto&& end) noexcept {
//     std::string_view ignored{};
//     // Use the generic op call to skip the next JSON value
//     glz::parse<glz::JSON>::op<glz::opts{}>(ignored, ctx, it, end);
// }

class WSEchoReq : public anson::AnsonBody {
    // string type;
public:
    ~WSEchoReq() = default;

    string echo;

    WSEchoReq() {}

    WSEchoReq(string echo) : echo(echo) {
    }

    // Use a concrete signature to avoid template deduction errors
    void read_type(const glz::generic& value) {
        // 'glz::generic' lets you inspect the JSON value (string, number, etc.)
        // without manual iterator management.
        (void)value; // Ignore the 'type' field value
    }

    // Static helper for writing the constant string
    static constexpr auto write_type(auto&&) {
        return "io.oz.anclient.socketier.WSEchoReq";
    }

    struct glaze {
        static constexpr auto value = glz::object(
            "type", glz::custom<&WSEchoReq::read_type,  [](auto&&) { return "io.oz.anclient.socketier.WSEchoReq"; }>,
            "echo", &WSEchoReq::echo
            );
    };
};

#endif
