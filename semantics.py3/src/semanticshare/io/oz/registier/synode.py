"""
Namespace for client side of registry tier.
"""

from anson.io.odysz.anson import Anson
from semanticshare.io.odysz.semantic.jprotocol import AnsonMsg


class RegistierSettings(Anson):
    '''
    Merge with AppSettings?
    '''

    def __init__(self, parent: AnsonMsg = None):
        super().__init__()
        self.parent = parent

