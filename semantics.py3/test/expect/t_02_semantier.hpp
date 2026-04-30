#pragma once

#include <entt/meta/factory.hpp>
#include <entt/meta/meta.hpp>
#include "entt_jserv.h"

namespace anson {#pragma once

#include <entt/meta/factory.hpp>
#include <entt/meta/meta.hpp>
#include "entt_jserv.h"

namespace anson {
class EchoReq : public anson::AnsonBody {
public:
    inline static const std::string _type_ = "io.odysz.semantic.jserv.echo.EchoReq";
    struct A {
        inline static const string echo = "echo";
        inline static const string inet = "inet";
	};

    inline static void load_echoreqAst(AstMap &asts, const string &ast_path) {
        specialize_msg_astpth<EchoReq>(asts, ast_path,
          [](meta_factory<EchoReq> &entf, AnsonBodyAst *ast) {
            entf.data<&EchoReq::echo>("echo");

            //
            ast->get_field_instance = [ast](const IJsonable& ans, const string& fieldname) -> meta_any {
                if (ast->fields.contains(fieldname)) {
                    auto& concrete = static_cast<const EchoReq&>(ans);
                    if ("echo" == fieldname)
                        return entt::forward_as_meta(concrete.echo);
                }

                if (IJsonable::contxt_ptr->has_ast(ast->dataBaseAst)) {
                    AnsonBodyAst *bast = IJsonable::contxt_ptr->ast<AnsonBodyAst>(ast->dataBaseAst);
                    return bast->get_field_instance(ans, fieldname);
                }

                anerror("get_field_instance<EchoReq>(): Failed to get entt instance (meta_any)");
                return {};
            };
          });
        }
    
};

}