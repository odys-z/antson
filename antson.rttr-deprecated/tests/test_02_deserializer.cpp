#include <gtest/gtest.h>
#include "io/odysz/rttr.hpp"

using namespace std;
using namespace rttr;
using namespace anson;

TEST(Anson, Base) {

    type t = type::get_by_name("anson::Anson");
    rttr::variant v = t.create({std::string("io.odysz.anson.Anson")});
    shared_ptr<anson::Anson> ans = v.convert<shared_ptr<anson::Anson>>();
    cout << "[1] ans.type: " << ans->type << NL;
    ASSERT_EQ("io.odysz.anson.Anson", ans->type);

    std::string json_input = R"({"type": "input"})";
    // 1. dereferencing the shared_ptr, which results in a reference to the anson::Anson object
    // 2. Passing *ans to rttr::instance creates an instance that refers to the original object (shallow copy)
    RttrSaxHandler handler(*ans);

    cout << "[2] " << json_input << NL;
    bool result = nlohmann::json::sax_parse(json_input, &handler);
    
    ASSERT_TRUE(result);
    ASSERT_EQ("input", ans->type);
}

TEST(Anson, AnsonBody) {

    type b = type::get_by_name("anson::AnsonBody");
    rttr::variant v = b.create({std::string("r/ds")});
    shared_ptr<anson::AnsonBody> anb = v.convert<shared_ptr<anson::AnsonBody>>();
    cout << "[1] anb.type: " << anb->type << NL;
    ASSERT_EQ("io.odysz.jprotocol.AnsonBody", anb->type);
    ASSERT_EQ("r/ds", anb->a);

    std::string json_input = R"({"type": "input", "a": "r/query"})";
    RttrSaxHandler handler(*anb);

    cout << "[2] " << json_input << NL;
    bool result = nlohmann::json::sax_parse(json_input, &handler);
    cout << "[3] ok: " << result << ", type: " << anb->type << ", a: " << anb->a << NL;
    ASSERT_TRUE(result);
    ASSERT_EQ("input", anb->type);
    ASSERT_EQ("r/query", anb->a);
}

TEST(Anson, AnsonMsg) {

    using Req = AnsonMsg<EchoReq>;

    type m = type::get<anson::AnsonMsg<EchoReq>>();
    // rttr::variant v = m.create({std::string("echo")});
    rttr::variant v = m.create({Port::echo});
    shared_ptr<Req> msg = v.convert<shared_ptr<Req>>();

    // create an instance and return the pointer
    msg->body.push_back(std::make_shared<EchoReq>("Hello"));

    cout << "[1] msg.port: " << msg->port << NL;
    ASSERT_EQ("io.odysz.jprotocol.AnsonMsg", msg->type);
    ASSERT_EQ("echo", msg->port);
    ASSERT_EQ("r/query", msg->body.at(0)->a);
    ASSERT_EQ("Hello", msg->body.at(0)->echo);

    std::string json_input = R"({"type": "input", "port": "query", "body": []})";
    RttrSaxHandler handler(*msg);
    cout << "[2] " << json_input << NL;
    bool result = nlohmann::json::sax_parse(json_input, &handler);
    cout << "[3] ok: " << result << ", type: " << msg->type << ", port: " << msg->port << NL;

    ASSERT_TRUE(result);
    ASSERT_EQ("input", msg->type);
    EXPECT_EQ("query", msg->port) << "TODO: How SAX for enum deserialization is to be learnt...";

    EchoReq* reqbd = msg->body.at(0).get();

    cout << "[4] body: " << msg->body.size() << ", type: " << reqbd->type << ", a: " << reqbd->a << NL;
    EXPECT_EQ("io.odysz.jprotocol.QueryReq", reqbd->type) << "TODO: nested parser is not for HelloWorld.";
    EXPECT_EQ("r", reqbd->a);
}

TEST(Anson, Servialize) {

    // ... inside your main or worker function ...

    // 1. Create your message
    using Req = AnsonMsg<EchoReq>;
    auto msg = std::make_shared<Req>(Port::query);
    msg->body.push_back(std::make_shared<EchoReq>("Hello World"));

    // 2. Use a stringstream as the buffer
    std::ostringstream oss;

    // 3. Call the helper (it writes directly into the oss buffer)
    serialize_anson(oss, msg);

    // 4. Get the result
    std::string json_result = oss.str();

    std::cout << "Serialized JSON: " << json_result << std::endl;
    ASSERT_EQ(R"({"type":"io.odysz.jprotocol.AnsonMsg","port":"query"})", json_result);
}
