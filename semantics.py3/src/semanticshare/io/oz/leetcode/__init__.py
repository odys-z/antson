from dataclasses import dataclass
from typing import List, Union

from anson.io.odysz.anson import Anson

@dataclass
class TestDatum(Anson):
    '''
    @since 0.6.1
    '''
    name: str
    nums: List[int]
    i_v: List[int]
    str_v: List[str]
    expect: Union[str, int]

    def __init__(self, name: str=''):
        super().__init__()
        self.name = name

@dataclass
class TestData(Anson):
    '''
    @since 0.6.1
    '''
    title: str
    cases: List[TestDatum]

    def __init__(self):
        super().__init__()

