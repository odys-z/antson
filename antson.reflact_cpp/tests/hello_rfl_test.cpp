#include <gtest/gtest.h>

#include <iostream>
#include <rfl/json.hpp>
#include <rfl.hpp>

using namespace std;

#define NL '\n'

struct BaseData {
    std::string type = "Generic";
};

struct User {
    rfl::Flatten<BaseData> base; // "Inherits" fields for reflection
    std::string name;
    int age;
    string tags;
};

struct Rebel {
    rfl::Flatten<User> who;
    User target;
};

TEST(RFL, HelloWorld) {
    // 1. Serialization
    User my_user{.name = "Alice", .age = 3, .tags = R"({"cpp", "reflection"})"};
    std::string user_json = rfl::json::write(my_user);
    
    std::cout << "Serialized User: " << user_json << std::endl;

    // 2. Deserialization

    std::string zs = R"({"type": "Generic", "name": "ody", "age": 29, "tags": "8964"})";
    auto z = rfl::json::read<User>(zs);
    ASSERT_TRUE(z) << z.error().what();
    ASSERT_EQ(29, z->age) << "age";
    cout << "z ID: " << z->name << " type: " << z->base.value_.type << " age: " << z->age << " tags: " << z->tags << std::endl;

    zs = R"({"type":"Generic","name":"ody","age":29.9,"tags":"8964"})";
    auto x = rfl::json::read<User>(zs);
    ASSERT_FALSE(x) << " assert z: " << x.error().what();
    cerr << x.error().what() << NL;

    Rebel r{.who = *z, .target=*z};
    ASSERT_EQ(29, r.target.age) << "target age 1";

    std::string self = rfl::json::write(r);
    auto rz = rfl::json::read<Rebel>(self);
    ASSERT_EQ(29, rz->who.value_.age) << "age";
    ASSERT_EQ(29, rz->target.age) << "target age 2";

    rz->target.age = 150;
    ASSERT_EQ(29, rz->who.value_.age) << "target age 3";
}


struct Anson {
    std::string type = "io.odysz.anson.Anson";

    // Anson(string t) : type(t) {}
    string Type() { return this->type; }
};

struct AnsonBody {
    rfl::Flatten<Anson> anson;
    std::string a;

    // AnsonBody(string t) {anson.value_.type = t;}
};

template<derived_from<AnsonBody> T = AnsonBody>
struct AnsonMsg {
    string port;
    string header;
    rfl::Flatten<AnsonBody> body;
    T& Body();
};

struct EchoReq : public AnsonBody {
    rfl::Flatten<AnsonBody> base;
    string echo;


    // EchoReq() { this->base.value_.anson.value_.type = "io.oz.echo.EchoReq"; }

    // EchoReq(string& echo) : EchoReq() {
    //     this->echo = echo;
    // }
};

// struct LoginReq : public AnsonBody {
//     rfl::Flatten<AnsonBody> base;
//     string uid;
// };

TEST(RFL, Template) {
    EchoReq echobd;
    echobd.echo = "echo";

    string json = rfl::json::write(echobd);
    auto res = rfl::json::read<EchoReq>(json);
    ASSERT_TRUE(res);
    EchoReq echorep = res.value();
    ASSERT_EQ("echo", echorep.echo);

    AnsonMsg<EchoReq> msg{.header = "header", .body = echobd};
    // string json = rfl::json::write(msg);
    // cout << json << NL;

    // auto res = rfl::json::read<AnsonMsg<EchoReq>>(json);
    // AnsonMsg<EchoReq> msg_ = res.value();

    // ASSERT_EQ(echobd.echo, msg_.Body().echo);
}
