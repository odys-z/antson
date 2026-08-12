#pragma once

#include <entt/meta/factory.hpp>
#include <entt/meta/meta.hpp>

#include <io/odysz/anson.h>
#include <io/odysz/jprotocol.h>
#include <io/odysz/entt_jserv.h>
#include <io/odysz/module/rs.h>



namespace anson {

class EchoReq : public anson::AnsonBody {
public:
    inline static const std::string _type_ = "io.odysz.semantic.jserv.echo.EchoReq";

    struct A {
        inline static const string echo = "echo";
        inline static const string inet = "inet";
    };
    string echo;

    EchoReq() : AnsonBody() {
        Type(_type_);
    }
};

inline static void load_echoreqAst(JsonOpt* ctx, const string &ast_path) {
    specialize_msg_astpth<EchoReq, AnsonBody>(ctx, ast_path,
      [ctx](meta_factory<EchoReq> &entf, AnsonBodyAst *ast) {
        entf.data<&EchoReq::echo>("echo");
        entf.ctor<>();
        entf.ctor<>();

        //
        ast->get_field_instance = [ast, ctx](const IJsonable& ans, const string& fieldname) -> meta_any {
            if (ast->fields.contains(fieldname)) {
                auto& concrete = static_cast<const EchoReq&>(ans);
                if ("echo" == fieldname)
                    return entt::forward_as_meta(concrete.echo);
            }

            if (ctx->has_ast(ast->baseAnclass)) {
                AnsonBodyAst *bast = ctx->ast<AnsonBodyAst>(ast->baseAnclass);
                return bast->get_field_instance(ans, fieldname);
            }

            anerror("get_field_instance<EchoReq>(): Failed to get entt instance (meta_any)");
            return { };
        };
    });
}

}
