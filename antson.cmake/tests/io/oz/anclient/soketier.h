// #ifndef io_oz_anclient_socketier_hpp
// #define io_oz_anclient_socketier_hpp

// #include <string>
// #include "io/odysz/semantic/jprotocol.hpp"

// using namespace std;

// class WSEchoReq;

// // template <>
// // constexpr std::string_view get_expected_type_tag<WSEchoReq>() noexcept {
// //     return "io.oz.anclient.socketier.WSEchoReq";
// // }

// // using ReadFn  = void(*)(WSEchoReq&, glz::is_context auto&&, auto&&, auto&&) noexcept;


// // template <>
// // std::string_view write_message_type<WSEchoReq>(const WSEchoReq&) noexcept {
// //     return "io.oz.anclient.socketier.WSEchoReq";
// // }

// // template <class T>
// // void ignore_type_tag(T&, glz::is_context auto&& ctx, auto&& it, auto&& end) noexcept {
// //     std::string_view ignored{};
// //     // Use the generic op call to skip the next JSON value
// //     glz::parse<glz::JSON>::op<glz::opts{}>(ignored, ctx, it, end);
// // }

// class WSEchoReq : public anson::AnsonBody {
//     // string type;
// public:
//     ~WSEchoReq() = default;

//     string echo;

//     WSEchoReq() {}

//     WSEchoReq(string echo) : echo(echo) {
//     }

//     // struct glaze {
//     //     static constexpr auto value = glz::object(
//     //         // "type", [](auto&&) { return "io.oz.anclient.socketier.WSEchoReq"; },
//     //         // "type", _ANSON_TYPE_(WSEchoReq, "io.oz.anclient.socketier.WSEchoReq")},
//     //         // "type", glz::custom < &ignore_type_tag, [](auto&&) { return "io.oz.anclient.socketier.WSEchoReq"; }>,
//     //         "type",  glz::custom<
//     //             [](WSEchoReq& v, auto&& ctx, auto&& it, auto&& end) noexcept {
//     //                 ignore_type_tag(v, ctx, it, end);
//     //             },
//     //             [](auto&&) { return "io.oz.anclient.socketier.WSEchoReq"; }>,
//     //         "echo", &WSEchoReq::echo);
//     // };
//     // REGISTER_ANTYPE(WSEchoReq, "io.oz.anclient.socketier.WSEchoReq");

//     // static void read_type_WSEchoReq(WSEchoReq& self,
//     //                               glz::is_context auto&& ctx,
//     //                               auto&& it, auto&& end) noexcept {
//     static void read_type_WSEchoReq(WSEchoReq& self,
//                                         glz::is_context auto&& ctx,
//                                         auto&& it, auto&& end) noexcept {
// //             std::string_view tag{};
// //             glz::parse<glz::JSON>::op<glz::opts{}>(tag, ctx, it, end);
// // \
// //             if (it != end && *it == ',') ++it;

//             std::string_view tag{};
//             // Explicitly pass the JSON format and options
//             if (auto ec = glz::parse<glz::JSON>::op<glz::opts{}>(tag, ctx, it, end)) {
//                 // Handle error if necessary
//             }
//             if (it != end && *it == ',') ++it;
//     }
// \
//     static std::string_view write_type_WSEchoReq(const WSEchoReq&) noexcept {
//             return "io.oz.anclient.socketier.WSEchoReq";
//     }

//     struct read_type : glz::from<WSEchoReq>
//     {
//         template <auto Opts>
//         static void op(WSEchoReq& v, is_context auto&& ctx, auto&& it, auto&& end)
//         {
//             // Initialize a string_view with the appropriately lengthed buffer
//             // Alternatively, use a std::string for any size (but this will allocate)
//             std::string_view str = "";
//             parse<JSON>::op<Opts>(str, ctx, it, end);
//             v.echo = "";
//         }
//     };

// \
//     struct glaze {
//         static constexpr auto value = glz::object(
//             "type", glz::custom<
//                         // &WSEchoReq::read_type_WSEchoReq,
//                         // [](const WSEchoReq&) -> std::string_view{},
//                         &read_type
//                         &WSEchoReq::write_type_WSEchoReq>,
//             "echo", &WSEchoReq::echo );
//     };
// };

// #endif
