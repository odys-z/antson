from dataclasses import dataclass
from typing import List

from anson.io.odysz.anson import Anson


@dataclass
class CSettings(Anson):
    """
    Mapping for gen.cmake.CSettings
    """
    src: str
    headers: List[str]
    json_h: str

    def __init__(self):
        super().__init__()
        self.src = "src"
        self.headers = []
        self.json_h = "json.hpp"
