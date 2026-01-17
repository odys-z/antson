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

};

#endif
