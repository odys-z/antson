#ifndef io_oz_anclient_socketier_hpp
#define io_oz_anclient_socketier_hpp

#include <string>
#include "io/odysz/semantic/jprotocol.hpp"

using namespace std;

class WSEchoReq : public anson::AnsonBody {
    string type;
public:
    string echo;

    WSEchoReq() {}

    WSEchoReq(string echo) : echo(echo) {
    }

    struct glaze {
        static constexpr auto value = glz::object(
            "type", [](auto&&) { return "io.oz.anclient.socketier.WSEchoReq"; },
            "echo", &WSEchoReq::echo);
    };
};

#endif
