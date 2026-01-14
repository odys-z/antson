#include <gtest/gtest.h>
#include "io/odysz/anson.hpp"
#include "io/oz/anclient/ipcagent.hpp"
#include "io/oz/anclient/soketier.h"

TEST(Anson, SmokeTest)
{
    string msg = "test";
    WSEchoReq req(msg);
    EXPECT_FALSE(req.echo.empty());

    string s = req.toBlock<WSEchoReq>();

    WSEchoReq* rep = anson::Anson::fromJson<WSEchoReq>(s);

    EXPECT_EQ(rep->echo, req.echo);
}
