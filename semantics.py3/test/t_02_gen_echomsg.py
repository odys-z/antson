from pathlib import Path
from typing import cast
from unittest import TestCase

from anson.io.odysz.anson import Anson

from src.semanticshare.io.odysz.reflect import gen_peers
from src.semanticshare.io.odysz.semantier import PeerSettings


class GenEchomsgTest(TestCase):

    def test_(self):
        testpath = Path('test')
        settings = cast(PeerSettings, Anson.from_file('test/t_02-settings.json'))

        gen_peers(settings, testpath)

        with open(testpath / 'gen/cpp/t02-echomsg.expect.hpp', 'r') as e, open(testpath / settings.cpp_gen, 'r') as f:
            self.assertEqual(e.readlines(), f.readlines())
