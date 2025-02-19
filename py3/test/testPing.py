import sys
import unittest

from src.anson.io.odysz.ansons import Anson
from test.io.odysz.jclient import Clients, OnError
from test.io.odysz.semantic.jprotocol import AnsonResp


class AnclientTest(unittest.TestCase):
    def testPing(self):
        Anson.java_src('test')
        err = OnError(lambda c, e, args: print(c, e.format(args), file=sys.stderr))

        Clients.servRt = 'http://127.0.0.1:8964/jserv-album'
        resp = Clients.pingLess('Anson.py3/test', err)

        print(resp.toBlock())
        self.assertEqual(type(resp), AnsonResp)
        self.assertEqual('ok', AnsonResp.code)


if __name__ == '__main__':
    unittest.main()
    t = AnclientTest()
    t.testPing()

