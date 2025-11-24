from typing import cast
from urllib.parse import urlparse

from anson.io.odysz.anson import Anson

from src.semanticshare.io.oz.invoke import Temurin17Release, JRE17Installer


class Temurin17Test():

    def test_jre(self):
        Anson.java_src('semanticshare')
        res = cast(Temurin17Release, Anson.from_file('temurin17.json'))
        site = urlparse('https://github.com')
        mirror = JRE17Installer(res, site)
