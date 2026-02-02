#include <gtest/gtest.h>
#include <entt/meta/meta.hpp>
#include <entt/meta/factory.hpp>
#include <nlohmann/json.hpp>
#include <iostream>
#include <io/odysz/jprotocol.h>
#include "io/odysz/json.h"

using json = nlohmann::json;
using namespace anson;

template<typename T>
void load_json(const std::string& raw_json, T& out_obj) {
    EnTTSaxParser handler;

    // Wrap our existing C++ instance so the parser can fill it
    handler.set_root(entt::forward_as_meta(out_obj));

    // Execute SAX parse
    if (nlohmann::json::sax_parse(raw_json, &handler)) {
        // Success: out_obj is now populated
    }
}

// 2. Generic Convert
json convert(entt::meta_any instance) {
    json j;
    auto type = instance.type();
    for(auto [id, data] : type.data()) {
        auto value = data.get(instance);
        // if(value.type() == entt::resolve<int>()) {
        //     j[id] = value.cast<int>();
        // } else if(value.type() == entt::resolve<std::string>()) {
        //     j[id] = value.cast<std::string>();
        // }
        if (auto* f = value.try_cast<string>())
            j[id] = value.cast<string>();
    }
    return j;
}

TEST(HELLO, ENTT_META) {
    register_meta();

    AnsonMsg<EchoReq> msg{Port::echo};

    cout << "Port: " << msg.port;
    EchoReq echobd{"echo..."};
    msg.Body(echobd);

    cout << "Echo: " << msg.body.back()->echo << NL;

    cout << serialize_json(msg) << NL;
    serialize_recursive(msg, cout) << NL;

    EXPECT_EQ(R"({"type": "io.odysz.jprotocol.AnsonMsg", )"
              R"("port": 2, "body": [{"a": "r/query", "echo": "echo..."}]})",
              serialize_json(msg))
        << "Obviously lack of port name, TODO ...";

    // 1. Create EchoReq via reflection
    auto echo_type = entt::resolve("EchoReq"_hs);
    auto req_instance = echo_type.construct();
    std::cout << "Actual Type Name: " << req_instance.type().info().name() << std::endl;
    EchoReq* echoreq = req_instance.try_cast<EchoReq>();
    cout << "EchoReq Reflected: " << echoreq->a << NL;

    // Set the 'echo' field
    if (auto data = echo_type.data("echo"_hs)) {
        data.set(req_instance, std::string("Reflection Hello"));
    }

    // 2. Create AnsonMsg<EchoReq> via reflection
    auto msg_rfl = entt::resolve("AnsonMsgEcho"_hs).construct(Port::echo);

    // Use this to check what EnTT actually thinks the type is:
    std::cout << "Actual Type Name: " << msg_rfl.type().info().name() << std::endl;

    // Try to get the reference first, then take the address
    if (auto* msg_rpt = msg_rfl.try_cast<AnsonMsg<EchoReq>>()) {
        string t = msg_rpt->type;
        ASSERT_EQ(AnsonMsg<EchoReq>::_type_, t);
    } else {
        // If that fails, msg_rfl might be holding a pointer.
        // Try casting to the pointer type directly:
        auto** ptr_to_ptr = msg_rfl.try_cast<AnsonMsg<EchoReq>*>();
        if (ptr_to_ptr) {
            AnsonMsg<EchoReq>* msg_rpt_actual = *ptr_to_ptr;
            ASSERT_EQ(AnsonMsg<EchoReq>::_type_, msg_rpt_actual->type);
        } else {
            FAIL() << "Could not cast meta_any to AnsonMsg<EchoReq>";
        }
    }

    // string t = msg_rpt->type;
    // ASSERT_EQ(AnsonMsg<EchoReq>::_type_, msg_rpt->type);
}
