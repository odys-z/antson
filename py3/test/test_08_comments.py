import contextlib
import io
import unittest
from dataclasses import dataclass

from src.anson.io.odysz.anson import Anson


@dataclass
class Cmd(Anson):
    cmd: str
    vars: dict

    def __init__(self):
        super().__init__()
        self.cmd = ''
        self.vars = {}


@dataclass
class Cfg(Anson):
    name: str
    prjs: dict
    cmds: list[Cmd]
    notes: list[str]

    def __init__(self):
        super().__init__()
        self.name = ''
        self.prjs = {}
        self.cmds = []
        self.notes = []


class CommentsTest(unittest.TestCase):
    def testIgnoreComments(self):
        cfg = Anson.from_json('''{
            "type": "test.test_08_comments.Cfg",
            "//": "an object comment",
            "// name": "a commented-out field",
            "name": "x",
            "prjs": {"//": "a dict comment", "a": "{github}/a"},
            "cmds": ["// example: scp {built_zip} ...",
                     {"type": "test.test_08_comments.Cmd", "//": "cmd comment", "cmd": "ls"},
                     "  // auto args: built_zip"],
            "notes": ["// skipped", "kept", "http://not-a-comment"]
        }''')

        self.assertEqual('x', cfg.name)
        self.assertEqual({'a': '{github}/a'}, cfg.prjs)
        self.assertEqual(1, len(cfg.cmds))
        self.assertEqual(Cmd, type(cfg.cmds[0]))
        self.assertEqual('ls', cfg.cmds[0].cmd)
        self.assertFalse(hasattr(cfg.cmds[0], '//'))
        self.assertFalse(hasattr(cfg, '//'))
        self.assertEqual(['kept', 'http://not-a-comment'], cfg.notes)

    def testUntypedNested(self):
        '''
        Comments in untyped values (unknown keys, nested dicts / lists) are removed recursively.
        '''
        cfg = Anson.from_json('''{
            "type": "test.test_08_comments.Cfg",
            "prjs": {"a": {"// x": 1, "y": ["// c", 2]}},
            "extra": {"// commants -1": "content-1", "// comment-2": "line2", "//": "line 3",
                      "arry": ["// comment"], "obj": {"//": "n", "k": ["v", "// c"]}}
        }''')

        self.assertEqual({'a': {'y': [2]}}, cfg.prjs)
        self.assertEqual({'arry': [], 'obj': {'k': ['v']}}, cfg.extra)

    def testWarnDroppedStrings(self):
        '''
        Each dropped string in a list is warned, with the string; dropped keys are not.
        '''
        err = io.StringIO()
        with contextlib.redirect_stderr(err):
            Anson.from_json('''{
                "type": "test.test_08_comments.Cfg",
                "//": "key comment, not warned",
                "notes": ["// scp {built_zip} user@host", "kept"],
                "extra": {"arry": ["// nested"]}
            }''')
        warns = [l for l in err.getvalue().splitlines() if l.startswith('Comment string in list dropped')]
        self.assertEqual(['Comment string in list dropped: // scp {built_zip} user@host',
                          'Comment string in list dropped: // nested'], warns)
        self.assertNotIn('key comment', err.getvalue())


if __name__ == '__main__':
    unittest.main()
