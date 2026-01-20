#ifndef io_oz_anclient_socketier_hpp
#define io_oz_anclient_socketier_hpp

#include <string>
#include "io/odysz/semantic/jprotocol.hpp"

using namespace std;

class WSEchoReq;

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
