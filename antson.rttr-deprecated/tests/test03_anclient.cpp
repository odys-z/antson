#include <gtest/gtest.h>
#include "io/odysz/rttr.hpp"

using namespace std;
using namespace rttr;
using namespace anson;

class DocsReq : public UserReq {
public:
    string synuri;
    string docTabl;

    RTTR_ENABLE(UserReq)
};

class InsecureClient {
public:
    AnsonResp& commit_ws(AnsonResp &resp);
};

AnsonResp& InsecureClient::commit_ws(AnsonResp &resp) {
    resp.code = MsgCode::ok;
    return resp;
}

TEST(Anclient, Less) {
    AnsonResp exp;
    InsecureClient c;
    AnsonResp &resp = c.commit_ws(exp);

    std::ostringstream oss;
    serialize_anson(oss, resp);
    std::string json_result = oss.str();

    std::cout << "Response: " << json_result << std::endl;
    string exps = R"({"type":"io.odysz.semantic.jprotocol.AnsonResp","a":"NA","code":"ok"})";
    ASSERT_EQ(exps, json_result) << "Expect: " + exps;
    ASSERT_EQ(MsgCode::ok, resp.code) << "code: ok";
}
