from pathlib import Path
from typing import cast
from unittest import TestCase

from anson.io.odysz.anson import Anson
from semanticshare.io.odysz.reflect import PeerSettings


class GenEchomsgTest(TestCase):

    def test_(self):
        testpath = Path('test')
        settings = cast(PeerSettings, Anson.from_file('test/t_02-settings.json'))

        from semantier_gen.io.oz.semanticpeer.generator2 import gen_peers
        gen_peers(settings, testpath)

        with (open(testpath / 'expect/t_02_semantier.hpp', 'r') as e,
              open(settings.cpp_gen, 'r') as f):
            self.assertEqual(e.readlines(), f.readlines())
