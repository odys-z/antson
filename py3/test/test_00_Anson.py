'''
A temporary solution without LL* parser.

For testing another cheap way of deserialize JSON in python, without Antlr4,
unless the stream mode is critical.

- Semantics consistence with Java is to be verified.

- No need for generating Python3 source code from JSON ?

'''
import unittest
from dataclasses import dataclass
from typing import Any

from src.anson.io.odysz.anson import Anson, _fields
from test.testier.extra import ExtraData


# https://colab.research.google.com/drive/1pqeZGfqdEl_kOlJQ76SCeuKTtD3NGlev


@dataclass
class MyDataClass(Anson):
    name: str
    age: int
    incumbent: bool
    extra: ExtraData
    items: list[Any]  # = field(default_factory=list)

    def __init__(self, name: str = '', age: int = '0'):
        super().__init__()
        self.extra = ExtraData()
        self.name = name
        self.age = age
        self.incumbent = False
        self.items = ['']  # field(default_factory=list)

class DataClassTest(unittest.TestCase):
    def testStr(self):
        foo = MyDataClass('Trump', 78)
        foo.extra.l = ['']
        # print(f'{foo.extra.__module__}.{foo.__class__.__name__}')
        self.assertEqual('test.testier.extra.MyDataClass',
                         f'{foo.extra.__module__}.{foo.__class__.__name__}')

        # __type__: Anson.Trumpfield(name='__type__', fieldtype=<class 'str'>, ...)
        # name: 'name': Anson.Trumpfield(name='name', ...)

        my = MyDataClass('zz', 12)
        my.incumbent = True
        mytype = type(my)
        print(my.toBlock())
        self.assertEqual(MyDataClass, mytype)
        self.assertEqual('''{
  "type": "test.test_00_Anson.MyDataClass",
  "extra": {
    "type": "test.testier.extra.ExtraData",
    "s": null,
    "i": 0,
    "l": null,
    "d": null
  },
  "name": "zz",
  "age": 12,
  "incumbent": true,
  "items": [""]
}''', my.toBlock(beautify=True))

        your = mytype('yy', 13)
        self.assertEqual('''{
  "type": "test.test_00_Anson.MyDataClass",
  "extra": {
    "type": "test.testier.extra.ExtraData",
    "s": null,
    "i": 0,
    "l": null,
    "d": null
  },
  "name": "yy",
  "age": 13,
  "incumbent": false,
  "items": [""]
}''', your.toBlock(beautify=True))

        jsonstr = '{"type": "test.test_00_Anson.MyDataClass", "name": "Trump", "age": 78, "incumbent": true, "extra": {"s": "sss", "i": 1, "l": 2, "d": {"u": "uuu"}}}'
        his = Anson.from_json(jsonstr)
        # print(his.name)
        self.assertEqual('Trump', his.name)
        self.assertTrue(his.incumbent)
        print(his)

        jsonstr = '{\
          "type": "test.test_00_Anson.MyDataClass",\
          "extra": {\
            "s": null,\
            "i": 0,\
            "l": ["a", 2],\
            "d": {}\
          },\
          "name": "zz",\
          "age": 12,\
          "items": ['']\
        }'
        her = Anson.from_json(jsonstr)
        # print(her.name, type(her))
        self.assertEqual('zz', her.name)
        self.assertEqual(MyDataClass, type(her))
        self.assertFalse(her.incumbent)
        # print(her.toBlock())
        self.assertEqual('''{
  "type": "test.test_00_Anson.MyDataClass",
  "extra": {
    "type": "test.testier.extra.ExtraData",
    "s": null,
    "i": 0,
    "l": ["a", 2],
    "d": {}
  },
  "name": "zz",
  "age": 12,
  "incumbent": false,
  "items": []
}''', her.toBlock(beautify=True))


if __name__ == '__main__':
    unittest.main()
    t = DataClassTest()
    t.testStr()
