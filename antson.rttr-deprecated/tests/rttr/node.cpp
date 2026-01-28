#include "node.h"
#include <iostream>
#include <rttr/registration>


namespace ns_3d {
// This is the specific symbol the linker is complaining about
node::node(std::string name, node* parent) : m_name(name), m_parent(parent) {}

node::~node() {}

void node::set_name(const std::string& name) { m_name = name; }
const std::string& node::get_name() const { return m_name; }

std::vector<node*> node::get_children() const { return m_children; }

void node::set_visible(bool visible, bool cascade) {
    std::cout << "Node visibility set to: " << visible << std::endl;
}

void node::render() { std::cout << "Rendering node..." << std::endl; }
}

RTTR_REGISTRATION
{
    using namespace rttr;
    using namespace ns_3d;

    // Use Release configuration because RTTR is built as the Release version.
    registration::class_<node>("ns_3d::node")
        .constructor<std::string, node*>()
        ( policy::ctor::as_std_shared_ptr, // should create an instance of the class as shared_ptr<ns_3d::node>
          default_arguments(nullptr) )     // second argument is optional, so we provide the default value for it

        .property("name", &node::get_name, &node::set_name)
         (metadata("TOOL_TIP", "Set the name of node."))  // stores metadata associated with this property

        // register directly a member object pointer; mark it as 'private'
        .property("parent", &ns_3d::node::m_parent, registration::private_access)

        .property_readonly("children", &node::get_children) // a read-only property; so not set possible

        .method("set_visible", &node::set_visible)
         ( default_arguments(true),              // the default value for 'cascade'
           parameter_names("visible", "cascade")) // provide the names of the parameter; optional, but might be useful for clients
        .method("render", &node::render)
        ;
}
