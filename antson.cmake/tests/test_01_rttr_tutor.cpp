#include <gtest/gtest.h>
#include <rttr/type>
#include <rttr/registration>
#include <iostream>


TEST(Hello, RTTR) {
    using namespace rttr;
    type t = type::get_by_name("ns_3d::node");
    // will create an instance of ns_3d::node as std::shared_ptr<ns_3d::node>
    variant var = t.create({std::string("MyNode")});
    std::cout << "[1] type " << var.get_type().get_name() << "\n";
    ASSERT_EQ("classstd::shared_ptr<classns_3d::node>", var.get_type().get_name());

    // sets/gets a property
    property prop = t.get_property("name");
    // remark: you can also set a member, although the instance is of type: 'std::shared_ptr<T>'
    prop.set_value(var, std::string("A New Name"));
    std::cout << "[2] name " << prop.get_value(var).to_string() << "\n";
    ASSERT_EQ("A New Name", prop.get_value(var).to_string());

    // retrieve the stored meta data of the property
    std::cout << "[3] MetaData TOOL_TIP: " << prop.get_metadata("TOOL_TIP").to_string() << "\n";
    // invoke a method
    method meth = t.get_method("set_visible");
    // remark: the 2nd argument will be provided automatically, because it has a default argument
    variant ret = meth.invoke(var, true);
    // a valid return value indicates a successful invoke
    std::cout << std::boolalpha << "[4] invoke of method 'set_visible' was successfully: " << ret.is_valid() << "\n\n";
    // retrieve all properties
    std::cout << "[5] 'node' properties:" << "\n";
    for (auto& prop : t.get_properties())
    {
        std::cout << "  name: " << prop.get_name() << "\n";
        std::cout << "    type: " << prop.get_type().get_name() << "\n";
    }
    std::cout << "\n";
    // retrieve all methods
    std::cout << "[6] 'node' methods:" << "\n";
    for (auto& meth : t.get_methods())
    {
        std::cout << "  name: " << meth.get_name();
        std::cout << "  signature: " << meth.get_signature() << "\n";
        for (auto& info : meth.get_parameter_infos())
        {
            std::cout << "    param " << info.get_index() << ": name: "<< info.get_name() << "\n";
        }
        std::cout << "\n";
    }
}
