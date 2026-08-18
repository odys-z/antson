import sys
import unittest

from semanticshare.io.odysz.semantic.jprotocol import JServUrl

from src.anson.io.odysz.anson import Anson
from test.io.odysz.jclient import Clients
from test.io.odysz.semantic.jprotocol import AnsonResp, MsgCode


class AnnotationTest(unittest.TestCase):
    def testPing(self):
        Anson.java_src('test')

        jserv: JServUrl = JServUrl(jservurl="http://io.github.odys-z:8888/central-alpha")
        self.assertEqual(jserv.jprotocol.protocolpath, 'central-alpha')
        self.assertEqual(jserv.toBlock(),
                        '{\n  "type": "io.odysz.semantic.jprotocol.JServUrl",\n'
                        + '  "https": false,\n'
                        + '  "ip": "io.github.odys-z",\n'
                        + '  "port": 8888,\n'
                        + '  "subpaths": ["central-alpha"],\n'
                        + '  "jservtime": "1911-10-10"\n'
                        + '}')


if __name__ == '__main__':
    unittest.main()
    t = AnnotationTest()
    t.testPing()

