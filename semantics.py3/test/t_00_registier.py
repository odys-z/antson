import sys
import unittest

from src.semanticshare.io.odysz.semantic.jprotocol import AnsonResp, MsgCode

from anson.io.odysz.anson import Anson
from anclient.io.odysz.jclient import Clients


class ClientierTest(unittest.TestCase):
    def testPing(self):
        Anson.java_src('semanticshare')
        def err_ctx (c: MsgCode, e: str, *args: str) -> None:
            print(c, e.format(args), file=sys.stderr)
            self.fail(e)

        Clients.servRt = 'http://127.0.0.1:8964/jserv-album'
        resp = Clients.pingLess('/registier/test', err_ctx)
        self.assertIsNotNone(resp)

        print(Clients.servRt, '<echo>', resp.toBlock())
        self.assertEqual(type(resp).__name__, 'AnsonResp')
        self.assertEqual('ok', resp.code.name, resp.msg())


if __name__ == '__main__':
    unittest.main()
    t = ClientierTest()
    t.testPing()

