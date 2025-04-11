import sys
import unittest

from src.anson.io.odysz.ansons import Anson
from test.io.odysz.jclient import OnError, Clients
from test.io.odysz.semantic.jprotocol import AnsonResp


class AnclientTest(unittest.TestCase):
    def testPing(self):
        Anson.java_src('test')
        err = OnError(lambda c, e, args: print(c, e.format(args), file=sys.stderr) and self.fail(e))

        Clients.servRt = 'http://127.0.0.1:8964/jserv-album'
        resp = Clients.pingLess('Anson.py3/test', err)
        self.assertIsNotNone(resp)

        print(Clients.servRt, '<echo>', resp.toBlock())
        self.assertEqual(type(resp.body[0]), AnsonResp)
        self.assertEqual('ok', resp.code) # TODO MsgCode.ok


if __name__ == '__main__':
    unittest.main()
    t = AnclientTest()
    t.testPing()

