import sys
import unittest

from anson.io.odysz.utils import Regexs

from src.semanticshare.io.odysz.semantic.jprotocol import MsgCode, JServUrl

from anson.io.odysz.anson import Anson

"""
Python port of the Java `testValidJserv()` unit test.

Each row is: (ok, url, https?, port_range, root_path, ipv6?)
- ok: expected overall validity
- https?: expected `parts.https`
- port_range: (min, max); max == -1 means unbounded
- root_path: expected path segments, or None
- ipv6?: expected `is_ipv6(url)`
"""

# from jserv_url import get_http_parts, is_ipv6, valid_paths, valid_url_port

URLS = [
    # 0
    (True, "https://odys-z.github.io:443/notes/index.html?v=1&w=2#rave", True, (80, 443), ["notes", "index.html"], False),
    (False, "https://odys-z.github.io/notes/index.html#rave?v=1&w=2", True, (1024, -1), ["notes", "index.html"], False),
    (True, "//odys-z.github.io/notes/index.html#rave?v=1&w=2", False, (80, 1024), ["notes", "index.html"], False),
    (True, "//odys-z.github.io/notes/index.html#rave?v=1&w=2", False, (80, 1024), ["notes", "index.html"], False),
    # 4
    (True, "//odys-z.github.io/notes/", False, (80, 1024), ["notes"], False),
    (True, "//odys-z.github.io/notes%20/", False, (80, 1024), ["notes%20"], False),
    (False, "//odys-z.github.io/notes /", False, (80, 1024), ["notes "], False),
    (True, "//odys-z.github.io/notes%20", False, (80, 1024), ["notes%20"], False),
    # 8
    (True, "//odys-z.github.io/", False, (80, 1024), None, False),
    (True, "//odys-z.github.io", False, (80, 1024), None, False),
    (False, "//odys-z.github.io%20", False, (80, 1024), None, False),
    (False, "//odys-z.github.io%20/notes%20", False, (80, 1024), ["notes%20"], False),
    # 12
    (True, "odys-z.github.io/notes/index.html#rave?v=1&w=2", False, (80, 1024), ["notes", "index.html"], False),
    (False, "odys-z.github.io/notes/index.html#rave?v=1&w=2", False, (81, 1024), ["notes", "index.html"], False),
    (True, "https://odys-z.github.io/notes/index.html", True, (443, 1024), ["notes", "index.html"], False),
    (False, "https://odys-z.github.io/notes/index.html", True, (1024, -1), ["notes", "index.html"], False),
    # 16
    (False, "https://127.0.0.1/jserv-album", True, (1024, -1), ["jserv-album"], False),
    (True, "https://127.0.0.1:8964/jserv-album", True, (1024, -1), ["jserv-album"], False),
    (False, "//127.0.0.1/jserv-album", True, (1024, -1), ["jserv-album"], False),
    (True, "127.0.0.1:8964/jserv-album", False, (1024, -1), ["jserv-album"], False),
    # 20
    (False, "https://::1/jserv-album", True, (1024, -1), ["jserv-album"], False),
    (True, "https://[::3]:8964/jserv-album", True, (1024, -1), ["jserv-album"], True),
    (False, "//2604:9cc0:14:b140:5706:4ab0:6cb8:d348/jserv-album", True, (80, -1), ["jserv-album"], False),
    (True, "https://[2604:9cc0:14:b140:5706:4ab0:6cb8:d348]/jserv-album", True, (443, -1), ["jserv-album"], True),
    # 24
    (True, "[2604:9cc0:14:b140:5706:4ab0:6cb8:d348]:8964/jserv-album", False, (1024, -1), ["jserv-album"], True),
]


class ValidatorTest(unittest.TestCase):

    # @pytest.mark.parametrize("row", URLS, ids=[u[1] for u in URLS])
    @staticmethod
    def check_is_ipv6(row):
        ok, url, https, port_range, path, ipv6 = row
        assert Regexs.is_ipv6(url) == ipv6, url


    # @pytest.mark.parametrize("row", URLS, ids=[u[1] for u in URLS])
    @staticmethod
    def check_valid_jserv(row):
        ok, url, https, port_range, path, ipv6 = row
        parts = Regexs.get_http_parts(url)
        # parts layout: [0]=ipv6, [1]=https, [2]=host, [3]=port, [4]=path, [5]=query, [6]=fragment
        result = (
            Regexs.is_valid_jserv_url(url)
            and parts[1] == https
            and Regexs.valid_url_port(parts[3], *port_range)
            and Regexs.valid_paths(path, parts[4])
        )
        assert result == ok, url

    def test(self):
        Anson.java_src('semanticshare')
        for url in URLS:
            print(url[1])
            ValidatorTest.check_valid_jserv(url)
            ValidatorTest.check_is_ipv6(url)


class JservTest(unittest.TestCase):
    def test(self):
        Anson.java_src('semanticshare')
        def err_ctx (c: MsgCode, e: str, *args: str) -> None:
            print(c, e.format(args), file=sys.stderr)
            self.fail(e)

        jserv = JServUrl(https= False, iport ="youtub.com:1984", protocolroot = "registry-central")
        self.assertEquals('http://youtub.com:1984/registry-central', jserv.jserv())

        jserw = JServUrl(jservurl='12.34.56.78:901/central-alpha')
        self.assertEquals('http://12.34.56.78:901/central-alpha', jserw.jserv())

if __name__ == '__main__':
    unittest.main()
    # t = JservTest()
    # t.test()

