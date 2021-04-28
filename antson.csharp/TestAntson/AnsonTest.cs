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
            string expect = "{\"type\": \"io.odysz.anson.Anson\", \"version\": \"0.9.1\"}";

            stream.Seek(0, SeekOrigin.Begin);
            Assert.AreEqual(expect, Utils.ToString(stream));
        }

        [TestMethod]
		void TestFromJson() {
            AnsT1 anson = (AnsT1)Anson.FromJson("{type:io.odysz.anson.AnsT1, ver: \"v0.1\", "
                    + "m: {type:io.odysz.anson.AnsT1$AnsM1, \"name\": \"x\"}}");
            Assert.AreEqual("x", anson.m.name);

            anson = (AnsT1) Anson.fromJson("{type: io.odysz.anson.AnsT1, ver: \"v0.1\"}");
            assertEquals("v0.1", anson.ver);
            assertEquals(null, anson.m);

            anson = (AnsT1) Anson.fromJson("{type: io.odysz.anson.AnsT1, ver: \"v0\\n.\\n1\", m: null}");
            assertEquals("v0\\n.\\n1", anson.ver);
            assertEquals(null, anson.m);

            AnsT2 anson2 = (AnsT2)Anson.fromJson("{type:io.odysz.anson.AnsT2, m: [\"e1\", \"e2\"]}");
            assertEquals("e1", anson2.m[0]);
            assertEquals("e2", anson2.m[1]);

            anson2 = (AnsT2) Anson.fromJson("{type:io.odysz.anson.AnsT2, m: ["
                    + "\"Cannot create PoolableConnectionFactory (ORA-28001: xxx\\n)\", "
                    + "\"Cannot create PoolableConnectionFactory (ORA-28001: xxx\\\\n)\"]}");
            assertEquals("Cannot create PoolableConnectionFactory (ORA-28001: xxx\\n)", anson2.m[0]);
            assertEquals("Cannot create PoolableConnectionFactory (ORA-28001: xxx\\\\n)", anson2.m[1]);
        }
}
}
