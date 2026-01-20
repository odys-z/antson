
#ifndef io_odysz_semantics
#define io_odysz_semantics

#include <string>

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
};

#endif
