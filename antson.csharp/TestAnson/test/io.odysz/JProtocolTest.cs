using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.IO;

namespace io.odysz.anson.jprotocol
{
    [TestClass]
    public class JProtocolTest
    {
		const string iv64 = "iv: I'm base64";
        const string tk64 = "tk: I'm base64";
        const string uid = "test-id";
        const string ssid = "ssid-base64";

        [TestMethod]
        public void TestSessionReq()
        {
			// formatLogin: {a: "login", logid: logId, pswd: tokenB64, iv: ivB64};
			AnsonMsg reqv11 = AnSessionReq.FormatLogin(uid, tk64, iv64);

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

			Assert.AreEqual(reqv11.Code(), msg.Code());
			Assert.AreEqual(((AnSessionReq)reqv11.Body(0)).Iv(), ((AnSessionReq)msg.Body(0)).Iv());
			Assert.AreEqual(((AnSessionReq)reqv11.Body(0)).Token(), ((AnSessionReq)msg.Body(0)).Token());
			Assert.AreEqual(msg, msg.Body(0).Msg());
		}
    }
}
