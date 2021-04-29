using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.IO;

namespace io.odysz.anson
{
    [TestClass]
    public class AnsonTest
    {
        [TestMethod]
        public void Test2Block()
        {
            Anson an = new Anson();

            MemoryStream stream = new MemoryStream();
            an.ToBlock(stream);
            string expect = "{\"type\": \"io.odysz.anson.Anson\", \"ver\": \"0.9.1\"}";

            stream.Seek(0, SeekOrigin.Begin);
            Assert.AreEqual(expect, Utils.ToString(stream));
        }

        [TestMethod]
		public void TestFromJson() {
            AnsT1 anson = (AnsT1)Anson.FromJson("{type:io.odysz.anson.AnsT1, ver: \"v0.1\", "
                    + "m: {type:io.odysz.anson.AnsT1$AnsM1, \"name\": \"x\"}}");
            Assert.AreEqual("x", anson.m.name);

            anson = (AnsT1) Anson.FromJson("{type: io.odysz.anson.AnsT1, ver: \"v0.1\"}");
            Assert.AreEqual("v0.1", anson.ver);
            Assert.AreEqual(null, anson.m);

            anson = (AnsT1) Anson.FromJson("{type: io.odysz.anson.AnsT1, ver: \"v0\\n.\\n1\", m: null}");
            Assert.AreEqual("v0\\n.\\n1", anson.ver);
            Assert.AreEqual(null, anson.m);

            AnsT2 anson2 = (AnsT2)Anson.FromJson("{type:io.odysz.anson.AnsT2, m: [\"e1\", \"e2\"]}");
            Assert.AreEqual("e1", anson2.m[0]);
            Assert.AreEqual("e2", anson2.m[1]);

            anson2 = (AnsT2) Anson.FromJson("{type:io.odysz.anson.AnsT2, m: ["
                    + "\"Cannot create PoolableConnectionFactory (ORA-28001: xxx\\n)\", "
                    + "\"Cannot create PoolableConnectionFactory (ORA-28001: xxx\\\\n)\"]}");
            Assert.AreEqual("Cannot create PoolableConnectionFactory (ORA-28001: xxx\\n)", anson2.m[0]);
            Assert.AreEqual("Cannot create PoolableConnectionFactory (ORA-28001: xxx\\\\n)", anson2.m[1]);
        }
}
}
