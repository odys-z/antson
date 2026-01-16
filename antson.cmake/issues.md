<ol reversed>
<li>glz::skip_read() for Anson.type</li>
  There is no glz::skip_read() for deserialize the type field, and
  this line will result in segament fault:
  <pre><code>
  class WSEchoReq : public anson::AnsonBody {
  public:
    ...
    struct glaze {
        static constexpr auto value = glz::object(
            "type", [](auto&&) { return "io.oz.anclient.socketier.WSEchoReq"; });
    };
  };

  ----------------------------------------------------------------------------------------------
  1 FAILED TEST unknown file: error: SEH exception with code 0xc0000005 thrown in the test body.
  Stack trace:

  </code></pre>

  Solution by Grok:
  <pre><code>
  #define DEFINE_GLAZE_MESSAGE_TYPE(ClassName, TypeStr) \
    namespace glz { namespace detail { \
        inline void write_type_tag_##ClassName(const ClassName&, auto&&... args) noexcept { \
            detail::to_json<std::string_view>::op<Opts>(TypeStr, args...); \
        } \
        inline void read_type_tag_##ClassName(ClassName&, is_context auto&& ctx, auto&& it, auto&& end) noexcept { \
            std::string_view tag; \
            parse<JSON>::op<Opts>(tag, ctx, it, end); \
            if (tag != TypeStr) { \
                ctx.error = "type tag mismatch"; \
                return; \
            } \
        } \
    }} \
    struct ClassName::glaze { \
        static constexpr auto value = glz::object( \
            "type", glz::custom< \
                &::glz::detail::read_type_tag_##ClassName, \
                &::glz::detail::write_type_tag_##ClassName \
            >, \
            /* your fields here */ \
        ); \
    };
  
  DEFINE_GLAZE_MESSAGE_WITH_TYPE(WSEchoReq,
    "io.oz.anclient.socketier.WSEchoReq",
    "echo", &WSEchoReq::echo
  )
  </code></pre>
  This will fail as the glz::custom<auto from, auto to>() is not accepted by MSVC 2022 compiler.

  Google AI
  <pre>
  In C++, a non-type template parameter declared as auto (like template <auto From>) requires a single, unambiguous value. Even if you provide the template argument <WSEchoReq>, the resulting instantiation can sometimes be viewed as an "overloaded function type" if there are other potential overloads with the same name, or if the compiler cannot reconcile the function pointer type with the auto parameter without an explicit cast.
  </pre>
</ol>


