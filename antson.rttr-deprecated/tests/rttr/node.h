#pragma once
// #ifndef NODE_H
// #define NODE_H

#include <rttr/type>
#include <rttr/registration_friend>

namespace ns_3d{
class node
{
public:
    node(std::string name, node* parent = nullptr);
    virtual ~node();
    void set_name(const std::string& name);
    const std::string& get_name() const;
    std::vector<node*> get_children() const;
    void set_visible(bool visible, bool cascade = true);
    virtual void render();
private:
    node*               m_parent;
    std::string         m_name;
    std::vector<node*>  m_children;
public:
    RTTR_ENABLE()
    RTTR_REGISTRATION_FRIEND
};}

// #endif // NODE_H
