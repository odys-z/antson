#ifndef io_oz_anclient_ipcagent
#define io_oz_anclient_ipcagent

#include <string>
#include <format>
#include <filesystem>

#include "io/odysz/anson.hpp"
#include "io/odysz/semantics.hpp"

using namespace std;

class WSAgent {
public:
    static string ipc_path;
};
string WSAgent::ipc_path = "ipc";

struct TestSettings : public anson::Anson {
    string type;

    string agent_jar;
    string agent_json;
    string qtclient;
    int ipc_port;
    SessionInf ipc_session;

    string wsUri()
    {
        return format("ws://127.0.0.1:{0:d}/{1:s}", ipc_port, WSAgent::ipc_path);
    }

    filesystem::path agentJar(string prefix) {
        filesystem::path p0 = prefix;
        return p0 / this->agent_jar;
    }

    filesystem::path agentJson(string prefix) {
        filesystem::path p0 = prefix;
        return p0 / this->agent_json;
    }

    struct glaze {
        using T = TestSettings;
        static constexpr auto value = glz::object(
            "type",       [](auto&&) { return "io.oz.anclient.ipcagent.TestSettings"; },
            "agent_jar",  &T::agent_jar,
            "agent_json", &T::agent_json,
            "qtclient",   &T::qtclient,
            "ipc_port",   &T::ipc_port,
            "ipc_session",&T::ipc_session);
    };
};

template void anson::Anson::toPath<TestSettings>(const string&);

#endif
