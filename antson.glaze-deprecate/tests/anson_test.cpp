#include <gtest/gtest.h>
#include "io/odysz/anson.hpp"
#include "io/oz/anclient/ipcagent.hpp"
#include "io/oz/anclient/soketier2.h"

#include <iostream>

using namespace std;

#define NL '\n'

TEST(Anson, WSEchoReq)
{
    string msg = "test";
    WSEchoReq req(msg);
    EXPECT_FALSE(req.echo.empty());

    string s = req.toBlock<WSEchoReq>();
    cout << "serialized: " << s << NL; cout.flush();

    WSEchoReq* rep = anson::Anson::fromJson<WSEchoReq>(s);

    cout << "deserialized: " << rep->echo;

    EXPECT_FALSE(rep->echo.empty()) << " -- A --";

    cout << "rep->echo:";
    cout << rep->echo << NL;

    EXPECT_EQ(rep->echo, req.echo) << " -- B --";
}
