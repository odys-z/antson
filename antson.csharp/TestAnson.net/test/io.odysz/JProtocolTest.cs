using io.odysz.semantic.jprotocol;
using io.odysz.semantic.jsession;
using io.odysz.semantics;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Collections;
using System.IO;
using static io.odysz.semantic.jprotocol.AnsonMsg;

namespace io.odysz.anson.jprotocol
{
    [TestClass]
    public class JProtocolTest
    {
		const string iv64 = "iv: I'm base64";
        const string tk64 = "tk: I'm base64";
        const string uid = "test-id";
        // const string ssid = "ssid-base64";
		const string respjson =
        @"{ ""type"": ""io.odysz.semantic.jprotocol.AnsonMsg"",
            ""code"": ""ok"", ""opts"": null, ""port"": ""update"", ""header"": null,
            ""body"": [{ ""type"": ""io.odysz.semantic.jprotocol.AnsonResp"",
                         ""rs"": null,
                         ""parent"": ""io.odysz.semantic.jprotocol.AnsonMsg"",
                         ""a"": null, ""conn"": null, ""m"": null,
                         ""map"": { ""resulved"": { ""type"": ""io.odysz.semantics.SemanticObject"",
                                                    ""props"": { ""a_attaches"": { ""type"": ""io.odysz.semantics.SemanticObject"",
                                                                                   ""props"": { ""attId"": ""00000D""}
                                                                                 }
                                                               }
                                                  },
                                    ""updated"": [1, 1, 1]
                                  }
                      }],
            ""version"": ""1.0"", ""seq"": 0
          }";

        [TestMethod]
        public void TestSessionReq()
        {
			// formatLogin: {a: "login", logid: logId, pswd: tokenB64, iv: ivB64};
			AnsonMsg reqv11 = AnSessionReq.formatLogin(uid, tk64, iv64);

            MemoryStream stream = new MemoryStream();
            reqv11.ToBlock(stream);
			string json = Utils.ToString(stream);

			// json:
			// {type: io.odysz.anson.jprotocol.AnsonMsg,
			//  code: null, opts: null,
			//  port: "session",
			//  header: null, vestion: "1.0", 
			//  body: [{type: io.odysz.anson.jprotocol.AnSessionReq,
			//          uid: "test-id", 
			//          parent: "io.odysz.anson.jprotocol.AnsonMsg",
			//          a: "login", conn: null,
			//          iv: "iv: I'm base64", mds: null,
			//          token: "tk: I'm base64"}], seq: 909}
			AnsonMsg msg = (AnsonMsg)Anson.FromJson(json);

			Assert.AreEqual(reqv11.code, msg.code);
			Assert.AreEqual(reqv11.port.name, msg.port.name);
			Assert.AreEqual(((AnSessionReq)reqv11.Body(0)).iv, ((AnSessionReq)msg.Body(0)).iv);
			Assert.AreEqual(((AnSessionReq)reqv11.Body(0)).token, ((AnSessionReq)msg.Body(0)).token);
		}

        [TestMethod]
        public void TestAnsonResp()
        {
            AnsonMsg respmsg = (AnsonMsg)Anson.FromJson(respjson);
            Assert.AreEqual("ok", respmsg.code.Name());
            Assert.AreEqual(Port.update, respmsg.port.port());

            AnsonResp resp = (AnsonResp)respmsg.Body(0);
            Assert.IsNotNull(resp);
            Assert.IsNotNull(resp.map);
            SemanticObject resulved = (SemanticObject)resp.map["resulved"];
            Assert.IsNotNull(resulved);

            IDictionary props = resulved.props;
            SemanticObject attach = (SemanticObject)props["a_attaches"]; 
            Assert.IsNotNull(attach.props);
            Assert.AreEqual("00000D", attach.props["attId"]);
        }
    }
}
