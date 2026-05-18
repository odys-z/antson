import sys
import unittest

from src.anson.io.odysz.anson import Anson
from test.io.odysz.jclient import Clients
from test.io.odysz.semantic.jprotocol import AnsonResp, MsgCode


class AnclientTest(unittest.TestCase):
    def testPing(self):
        Anson.java_src('test')

        # only for 3.11
        # err = OnError(lambda c, e, args: print(c, e.format(args), file=sys.stderr) and self.fail(e))

        def err_ctx (c: MsgCode, e: str, *args: str) -> None:
            try: print(c, e.format(args), file=sys.stderr)
            except:
                try: print(e, args)
                except: "Errors unable to be printed"
            self.fail(e)

        Clients.servRt = 'http://192.168.0.###:8961/jserv-album'
        # Clients.servRt = 'http://192.168.0.###:1984/regist-central'
        resp = Clients.pingLess('Anson.py3/test', err_ctx)
        self.assertIsNotNone(resp)

        print(Clients.servRt, '<echo>', resp.toBlock())
        self.assertEqual(type(resp.body[0]), AnsonResp)
        self.assertEqual('ok', resp.code) # TODO MsgCode.ok


if __name__ == '__main__':
    unittest.main()
    t = AnclientTest()
    t.testPing()

