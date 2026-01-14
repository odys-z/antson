
#ifndef io_odysz_semantics
#define io_odysz_semantics

#include <string>
#include <glaze/glaze.hpp>

using namespace std;

struct SessionInf {
    string type = "io.odysz.semantics.SessionInf";

    string ssid;
	string uid;
	string roleId;
	string userName;
	string roleName;
	string ssToken;
	int seq;
    string device;

    struct glaze {
        using T = SessionInf;
        static constexpr auto value = glz::object(
            "type",     &T::type,
            "ssid",     &T::ssid,
            "uid",      &T::uid,
            "roleId",   &T::roleId,
            "userName", &T::userName,
            "roleName", &T::roleName,
            "ssToken",  &T::ssToken,
            "seq",      &T::seq,
            "device",   &T::device
            );
    };
};

#endif
