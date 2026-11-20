'''
A db's picking list, the Python type of the same json as Java's io.oz.syn.dbpicks (doctree.jserv):
how an upgrade picks an earlier version's db into the current tables (picks), and the records the
current version needs (inits).

One list per db, a file of the app's package named after the db, beside bin/resource.json, e.g. the
doc-tree db's <install>/bin/doctree-picks.json (source: jserv-album/src/main/doctree-picks.json), read
by the synode upgrading the db (semantic-doc io.oz.sdoc.DoctreeDBUpgrade).

The items are generated (by Claude) and merged with the earlier versions' at each upgrade, never
edited by hand. Each item's mark, "DB-PICKING-<package version> ...", e.g. "DB-PICKING-0.8.2 s_docs.template",
is also in a comment of the source line that needs it, e.g. the meta's DDL column. So is each field
of the package manifest's config_picks (PkgResources, bin/resource.json), marked
"JSON-PICKING-<package version> <file>#<field>" at its configuration type's field.

The marks file, {"marks": {mark: source file, relative to the github folder}}, generated with them
and not packed (e.g. jserv-album/src/main/picking-marks.json), says where each mark is; DbPicks.check_marks()
checks, both ways:
  every db pick and every config_picks field has a mark in the marks file, and every mark there has one;
  every mark is in its source file, once.

Used by a package's build, e.g. jserv-album/tasks.py package():

    checked = DbPicks.load('src/main/doctree-picks.json').check_marks(
        taskcfg.github, 'src/main/picking-marks.json', PkgResources.load('src/main/resource.json').config_picks)

or, e.g. in a CI checking out one repository only (--lenient):

    python -m semanticshare.io.oz.syn.dbpicks --github ~/github \\
        --picks    jserv-album/src/main/doctree-picks.json \\
        --resource jserv-album/src/main/resource.json \\
        --marks    jserv-album/src/main/picking-marks.json [--lenient]

@since semantics.py3 0.6.24
'''
import argparse
import json
import os
import re
import sys
from dataclasses import dataclass, field
from typing import List, Optional, cast

from anson.io.odysz.anson import Anson

mark_prefix = 'DB-PICKING-'
'''the marks' prefix of a db picking list's items, followed by the package version'''

db_mark = re.compile(r'^DB-PICKING-\d+(\.\d+)*\s+\S+$')
json_mark = re.compile(r'^JSON-PICKING-\d+(\.\d+)*\s+(\S+#\S+)$')


@dataclass
class DbLogic(Anson):
    '''
    A predicate of a picking item, e.g. DbPick.when: op, a name of semantic.transact's Logic.op,
    with its operands, e.g. {"op": "isNotnull", "l": "builtAt"}, {"op": "eq", "l": "state", "r": "'ready'"}.
    '''
    op: str
    '''name of Logic.op, e.g. "eq", "isNotnull"'''
    l: str
    '''left operand, an expression, e.g. a column'''
    r: str
    '''right operand, an expression, e.g. "'ready'"; None for unary ops'''

    def __init__(self, op: str = None, l: str = None, r: str = None):
        super().__init__()
        self.op = op
        self.l = l
        self.r = r


@dataclass
class DbPick(Anson):
    '''
    A column of the current tables, picked from an earlier version's db: the old table's column of the
    first of names found; if none is found, i.e. the column is new, dflt where when holds, else the DDL's
    default. A column without a pick is picked by its own name.
    '''
    mark: str
    '''"DB-PICKING-<package version> <table>.<column>"'''
    tabl: str
    col: str
    names: list
    '''the names the column ever had in earlier versions, the newest first; None: its own name'''
    dflt: str
    '''default of a new column, a string constant; None: the DDL's'''
    when: DbLogic
    '''the condition on the old table's row for dflt; None: every row. Rows not matching take null.'''

    def __init__(self, mark: str = None, tabl: str = None, col: str = None, names: list = None,
                 dflt: str = None, when: DbLogic = None):
        super().__init__()
        self.mark = mark
        self.tabl = tabl
        self.col = col
        self.names = names
        self.dflt = dflt
        self.when = when


@dataclass
class DbInitRow(Anson):
    '''
    A record the current version needs in a db, inserted if missing, e.g. the sequence of a new auto key
    in oz_autoseq. A record is missing if no row of tabl has the values of the key columns.
    '''
    mark: str
    '''"DB-PICKING-<package version> <table>:<key values>"'''
    tabl: str
    key: list
    '''columns identifying the record, of nvs'''
    nvs: list
    '''[[column, string constant]]'''
    counts: list
    '''[[column, table]], the column's value is the count of the table's rows'''

    def __init__(self, mark: str = None, tabl: str = None, key: list = None, nvs: list = None,
                 counts: list = None):
        super().__init__()
        self.mark = mark
        self.tabl = tabl
        self.key = key
        self.nvs = nvs
        self.counts = counts


@dataclass
class Checked:
    errors: List[str] = field(default_factory=list)
    '''what's wrong'''
    skipped: List[str] = field(default_factory=list)
    '''source files not found, when not strict'''


@dataclass
class DbPicks(Anson):
    '''
    A db's picking list, e.g. bin/doctree-picks.json.
    '''
    picks: List[DbPick]
    inits: List[DbInitRow]

    def __init__(self, picks: List[DbPick] = None, inits: List[DbInitRow] = None):
        super().__init__()
        self.picks = picks or []
        self.inits = inits or []

    @staticmethod
    def load(path: str) -> 'DbPicks':
        '''
        Load a picking list, e.g. src/main/doctree-picks.json.
        '''
        p = cast(DbPicks, Anson.from_file(path))
        if p.picks is None: p.picks = []
        if p.inits is None: p.inits = []
        return p

    def pick(self, tabl: str, col: str) -> Optional[DbPick]:
        '''the pick of a column, None if none'''
        for p in self.picks:
            if p.tabl.lower() == tabl.lower() and p.col.lower() == col.lower():
                return p
        return None

    def check_marks(self, github: str, marks: str, config_picks: dict = None, strict: bool = True) -> Checked:
        '''
        Check the items' marks, and the config_picks fields' marks, against the marks file and the sources.

        :param github: the folder of the repositories, e.g. ~/github, where the marks' source files are
        :param marks: the marks file, e.g. jserv-album/src/main/picking-marks.json
        :param config_picks: the package manifest's config_picks (PkgResources.config_picks), {file: {field: [names]}};
                             None: the marks file has no JSON-PICKING marks
        :param strict: a source file not found is an error, else it's skipped
        :return: the errors, and the files skipped
        '''
        r = Checked()

        with open(marks, encoding='utf-8') as f:
            mk = json.load(f).get('marks', {})

        # the items' marks
        items = set()
        for it in list(self.picks) + list(self.inits):
            m = it.mark
            if not m or not db_mark.match(m):
                r.errors.append(f'picking list: an item without a mark DB-PICKING-<version> ...: {it.tabl}, {getattr(it, "col", None) or getattr(it, "nvs", None)}')
            elif m in items:
                r.errors.append(f'picking list: duplicated mark: {m}')
            else:
                items.add(m)

        fields = {f'{file}#{fld}' for file, flds in (config_picks or {}).items() for fld in flds}

        # the marks file
        marked_db, marked_json = set(), {}
        for m in mk:
            jm = json_mark.match(m)
            if db_mark.match(m):
                marked_db.add(m)
            elif jm:
                if jm.group(2) in marked_json:
                    r.errors.append(f'{marks}: {jm.group(2)} is marked twice: {marked_json[jm.group(2)]}, {m}')
                marked_json[jm.group(2)] = m
            else:
                r.errors.append(f'{marks}: not a picking mark: {m}')

        for m in sorted(items - marked_db):
            r.errors.append(f'{marks}: no source of {m}')
        for m in sorted(marked_db - items):
            r.errors.append(f'{marks}: {m} is no item of the picking list')
        for k in sorted(fields - set(marked_json)):
            r.errors.append(f'{marks}: no mark of config_picks {k}')
        for k in sorted(set(marked_json) - fields):
            r.errors.append(f'{marks}: {marked_json[k]} is no field of config_picks')

        # the marks in the sources
        texts = {}
        for m, src in mk.items():
            path = os.path.join(os.path.expanduser(github), src)
            if not os.path.isfile(path):
                (r.errors if strict else r.skipped).append(f'{src} (of {m}) not found in {github}')
                continue
            if path not in texts:
                with open(path, encoding='utf-8') as f:
                    texts[path] = f.read()
            n = len(re.findall(re.escape(m) + r'(?![\w.#:-])', texts[path]))
            if n != 1:
                r.errors.append(f'{src}: {m} is marked {n} times, expecting once')

        return r


def main(argv: Optional[List[str]] = None) -> int:
    '''
    :return: 0 if the picking lists match the marks, else 1
    '''
    from semanticshare.io.oz.syn.pkgresource import PkgResources

    p = argparse.ArgumentParser(prog='python -m semanticshare.io.oz.syn.dbpicks',
                                description="Check a package's picking lists against the marks in the sources.")
    p.add_argument('--github', required=True, help='the folder of the repositories, e.g. ~/github')
    p.add_argument('--picks', required=True, help="a db's picking list, e.g. jserv-album/src/main/doctree-picks.json")
    p.add_argument('--resource', help="the package's manifest with config_picks, e.g. jserv-album/src/main/resource.json")
    p.add_argument('--marks', required=True, help='the marks file, e.g. jserv-album/src/main/picking-marks.json')
    p.add_argument('--lenient', action='store_true', help='skip the source files not found, e.g. in a CI with one repository')
    a = p.parse_args(argv)

    cfg = PkgResources.load(a.resource).config_picks if a.resource else None
    r = DbPicks.load(a.picks).check_marks(a.github, a.marks, cfg, strict=not a.lenient)
    for s in r.skipped:
        print('skipped:', s)
    for e in r.errors:
        print(e, file=sys.stderr)
    return 1 if r.errors else 0


if __name__ == '__main__':
    sys.exit(main())
