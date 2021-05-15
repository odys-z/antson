using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Collections;
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
            string expect = "{\"type\": \"io.odysz.anson.Anson\", \"ver\": \"0.9.xx\"}";

            Assert.AreEqual(expect, Utils.ToString(stream));
        }

        [TestMethod]
        public void TestFromJson()
        {
            Anson ans = (Anson)Anson.FromJson("{type:io.odysz.anson.Anson, ver: \"v0.1\"}");
            Assert.AreEqual("v0.1", ans.ver);

            AnsT1 anson = (AnsT1)Anson.FromJson("{type:io.odysz.anson.AnsT1, ver: \"v0.1\", "
                    // our C# listener repaced $ -> +
                    + "m: {type:io.odysz.anson.AnsT1$AnsM1, \"name\": \"x\"}}");
            Assert.AreEqual("x", anson.m.name);

            anson = (AnsT1)Anson.FromJson("{type: io.odysz.anson.AnsT1, ver: \"v0.1\"}");
            Assert.AreEqual("v0.1", anson.ver);
            Assert.AreEqual(null, anson.m);

            anson = (AnsT1)Anson.FromJson("{type: io.odysz.anson.AnsT1, ver: \"v0\\n.\\n1\", m: null}");
            Assert.AreEqual("v0\\n.\\n1", anson.ver);
            Assert.AreEqual(null, anson.m);

            AnsT2 anson2 = (AnsT2)Anson.FromJson("{type:io.odysz.anson.AnsT2, m: [\"e1\", \"e2\"]}");
            Assert.AreEqual("e1", anson2.m[0]);
            Assert.AreEqual("e2", anson2.m[1]);

            anson2 = (AnsT2)Anson.FromJson("{type:io.odysz.anson.AnsT2, m: ["
                    + "\"Cannot create PoolableConnectionFactory (ORA-28001: xxx\\n)\", "
                    + "\"Cannot create PoolableConnectionFactory (ORA-28001: xxx\\\\n)\"]}");
            Assert.AreEqual("Cannot create PoolableConnectionFactory (ORA-28001: xxx\\n)", anson2.m[0]);
            Assert.AreEqual("Cannot create PoolableConnectionFactory (ORA-28001: xxx\\\\n)", anson2.m[1]);
        }

        [TestMethod]
        public void Test2dArr()
        {
            Ans2dArr a2d = new Ans2dArr();
            a2d.strs = new string[][] {
                new string[] {"1.0", "1.1", "1.2"},
                new string[] { "2.0" },
                new string[] { } };

            MemoryStream stream = new MemoryStream();
            a2d.ToBlock(stream);
            string s = Utils.ToString(stream);
            string expect = "{\"type\": \"io.odysz.anson.Ans2dArr\", \"strs\": [[\"1.0\", \"1.1\", \"1.2\"], [\"2.0\"], []], \"ver\": \"0.9.xx\"}";
            Assert.AreEqual(expect, s);

            a2d = (Ans2dArr)Anson.FromJson(expect);
            Assert.AreEqual("1.0", a2d.strs[0][0]);
            Assert.AreEqual("1.1", a2d.strs[0][1]);
            Assert.AreEqual("2.0", a2d.strs[1][0]);
            Assert.AreEqual(0, a2d.strs[2].Length);
        }

        [TestMethod]
        public void Test2Json_PC()
        {
            AnsT3 parent = new AnsT3();

            AnsT3Child c = new AnsT3Child(parent);
            // should trigger parent: io.odysz.anson.AnsT3
            AnsT3son son = new AnsT3son(parent);

            MemoryStream stream = new MemoryStream();
            parent.ToBlock(stream);
            string s = Utils.ToString(stream);
            string expect = "{\"type\": \"io.odysz.anson.AnsT3\", \"ms\": null, "
                            + "\"m\": [{\"type\": \"io.odysz.anson.AnsT3Child\", \"ver\": \"0.9.xx\"}, "
                            + "{\"type\": \"io.odysz.anson.AnsT3son\", \"gendre\": \"male\", \"parent\": \"io.odysz.anson.AnsT3\", \"ver\": \"0.9.xx\"}], \"ver\": \"0.9.xx\"}";

            // in .net framwork 4.72, fields and properties are not always the same order
            Assert.AreEqual(expect.Length, s.Length);

            // should resolve parent ref with a type guess
            AnsT3 p = (AnsT3)Anson.FromJson(s);
            Assert.AreEqual(((AnsT3son)p.m[1]).gendre, "male");
            Assert.AreEqual(null, ((AnsT3Child)p.m[0]).parent);
            Assert.AreEqual(p, ((AnsT3son)p.m[1]).parent);

            AnsT3 p0 = (AnsT3)Anson.FromJson(expect);
            Assert.AreEqual(((AnsT3son)p0.m[1]).gendre, ((AnsT3son)p.m[1]).gendre);
            Assert.AreEqual(p0.ver, p.ver);
        }

        [TestMethod]
        public void TestFromJson_map()
        {
            AnsTMap m = (AnsTMap)Anson.FromJson("{type: io.odysz.anson.AnsTMap, ver: null, map: {\"A\": \"B\"}}");
            Assert.AreEqual("B", m.map["A"]);

            m = (AnsTMap)Anson.FromJson("{type: io.odysz.anson.AnsTMap, map: {\"A\": \"B\"}, mapArr: {a: [1, \"s\"]}}");
            Assert.AreEqual("B", m.map["A"]);
            Assert.AreEqual(2, ((object[])m.mapArr["a"]).Length);
            Assert.AreEqual(1, ((object[])m.mapArr["a"])[0]);
            Assert.AreEqual("s", ((object[])m.mapArr["a"])[1]);
        }

        [TestMethod]
        public void testFromJson_rs() {
            AnsTRs rs = (AnsTRs)Anson.FromJson("{type: io.odysz.anson.AnsTRs, rs: "
                    + "{type: io.odysz.module.rs.AnResultset, stringFormats: null, total: 0, rowCnt: 3, colCnt: 4,"
                    + " colnames: {\"1\": [1, \"1\"], \"2\": [2, \"2\"], \"3\": [3, \"3\"], \"4\": [4, \"4\"]},"
                    + " rowIdx: 0, results: [[\"0 1\", \"0 2\", \"0 3\", \"0 4\"], [\"1 1\", \"1 2\", \"1 3\", \"1 4\"], [\"2 1\", \"2 2\", \"2 3\", \"2 4\"]]"
                    + "}}");

            int a = rs.rs.GetRowCount();
            Assert.AreEqual(3, rs.rs.GetRowCount());
            Assert.AreEqual("0 1", ((IList)rs.rs.results[0])[0]);

            rs = (AnsTRs) Anson.FromJson("{type: io.odysz.anson.AnsTRs, rs: "
                    + "{type: io.odysz.module.rs.AnResultset, stringFormats: null, total: 0, rowCnt: 3, colCnt: 4,"
                    + " colnames: {\"1\": [1, \"1\"], \"2\": [2, \"2\"], \"3\": [3, \"3\"], \"4\": [4, \"4\"]},"
                    + " rowIdx: 0, results: [[\"0, 1\", \"0, 2\", \"0, 3\", \"0, 4\"], [\"1, 1\", \"1, 2\", \"1, 3\", \"1, 4\"], [\"2, 1\", \"2, 2\", \"2, 3\", \"2, 4\"]]"
                    + "}}");

            Assert.AreEqual(3, rs.rs.GetRowCount());
            Assert.AreEqual("0, 2", rs.rs.GetString(0, "1"));
            Assert.AreEqual("0, 3", rs.rs.GetString(0, "2"));
        }

}
}
