from __future__ import annotations

import fnmatch
import os
import re
import shutil
import zipfile
from pathlib import Path

from pycparser.c_ast import Union

from typing import List, Optional, Sequence, Union

def zip2(distzip, resources, exclude_patterns=[]):
    """
    example: zip2('registry-zsu.zip', {"zsu": "registry-deploy/*"}, ['*.zip'])
    :param distzip:
    :param resources:
    :param exclude_patterns:
    :return: None
    """
    def matches_patterns(filename, patterns):
        """
        Check if a filename matches any of the given patterns.

        Args:
            filename (str): The filename to check (e.g., "data.logs").
            patterns (list): List of patterns (e.g., ["*.logs", "*.dat"]).

        Returns:
            bool: True if the filename matches any pattern, False otherwise.
        """
        return any(fnmatch.fnmatch(os.path.basename(filename), pattern) for pattern in patterns)

    with zipfile.ZipFile(distzip, 'w', zipfile.ZIP_DEFLATED) as zipf:
        err = False
        # resources
        for rk, rv in resources.items():
            if "*" in rv:
                count = 0
                srcroot = re.sub('\\*$', '', rv.replace('\\', '/'))
                for pth, _dir, fs in os.walk(srcroot):
                    for file in fs:
                        if not matches_patterns(file, exclude_patterns):
                            file_path = os.path.join(pth, file)
                            relative_path = os.path.relpath(file_path, srcroot)

                            visited = set()
                            while os.path.islink(file_path):
                                if file_path in visited:
                                    raise ValueError(f"Cycle detected in symbolic links at {relative_path}")
                                visited.add(file_path)
                                print(file_path, '->', os.path.realpath(file_path))
                                file_path = os.path.realpath(file_path)

                            relative_path = os.path.relpath(relative_path)
                            arcname = os.path.join(rk, relative_path)
                            zipf.write(file_path, arcname)
                            count += 1
                            print(f"Added to ZIP: {relative_path} as {arcname}")
                if count == 0:
                    err = True
                    raise FileNotFoundError(f'[ERROR] No files found in {rv}.')
            else:  # Handle single files (jserv.jar and exiftool.exe)
                file = rk if rv == '.' else rv
                if os.path.exists(file):
                    zipf.write(file, rk)
                    print(f"Added to ZIP: {file} as {rk}")
                else:
                    err = True
                    raise FileNotFoundError(f"[ERROR]: Resource '{rk}': '{file}' not found.")

    print(f'Created ZIP file successfully: {distzip}' \
          if not err else 'Errors while making target (creaded zip file)')

def copy_anyway(src: Union(str, Path), dest: Path) -> Path:
    src = Path(src)
    if not src.is_file():
        raise FileNotFoundError(f'source path not found: {src}')

    dest.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dest)
    return dest

class Regexs():
    """
    [Credits to Claude]
    
    jserv_url.py

    A Python 3.9+ port of `io.odysz.common.Regex`'s Jserv URL parsing helpers.
    Unlike the character-scanning approach of an earlier draft, this version is
    a line-for-line translation of the actual Java regexes and reconstruction
    logic:

        reg3986        RFC 3986 Appendix B "generic syntax" splitter:
                        ^(scheme:)?(//authority)?(path)(?query)?(#fragment)?
        reg_isIPv6      loose bracket/8-hex-group IPv6 sniff test
        reg_hostportv6  "[ipv6]:port" extractor
        schemePrefix    "does this string already start with scheme://+//?"

    recognizing URLs in any of these shapes:

        scheme://host[:port][/path...][?query][#fragment]
        //host[:port][/path...][?query][#fragment]           (protocol-relative)
        host[:port][/path...][?query][#fragment]              (bare, no "//")

    `host` may be:
        - a DNS-style hostname / IPv4 literal written as plain dotted text
          (letters, digits, '.', '-'), e.g. "odys-z.github.io", "127.0.0.1"
        - a bracketed IPv6 literal, e.g. "[::1]", "[2604:9cc0:14:b140::1]"

    An IPv6 address is only recognized when 8 full hex groups (or the "::xxxx"
    short form) are found immediately before a "]" -- an unbracketed address is
    never routed down the IPv6 path, mirroring the quirk in the original Java
    `reg_isIPv6` pattern (its second alternative matches 8 hex groups followed
    by a literal "]" with no matching "[" required beforehand; a plain
    unbracketed multi-colon host therefore still falls through to the v4
    parser and is rejected there for containing ':').

    Public API
    ----------
    is_ipv6(url: str) -> bool
        Java: `Regex.isIPv6(String)`.

    get_http_parts(url: str) -> list
        Java: `Regex.getHttpParts(String)`. Returns
            [0] ipv6     bool   -- is this an IPv6-literal authority
            [1] https    bool   -- True: https, False: possibly http
            [2] host     str    -- bracket-inclusive for IPv6, e.g. "[::1]"
            [3] port     int
            [4] path     list[str] | None
            [5] query    str | None
            [6] fragment str | None
        or None if the URL has no authority at all (blank host).

    get_https_partsv4(url: str) -> list | None
    get_https_partsv6(url: str) -> list | None
        Java: `Regex.getHttpsPartsv4` / `getHttpsPartsv6`. Same 6-element
        layout as get_http_parts() minus the leading ipv6 flag.

    as_jserv(semi_jserv: str) -> str
        Java: `Regex.asJserv(String)`. Reconstructs a canonical
        scheme://host[:port][/path][?query][#fragment] string.

    valid_url_port(port: int | str, *range) -> bool
        Java: `Regex.validUrlPort(int|String, int...)`.

    valid_paths(expected, actual) -> bool
        Helper (not in Regex.java) for comparing parsed path segments;
        treats None == None as a match.

    is_valid_jserv_url(url: str) -> bool
        Approximates `urlValidator.isValid(asJserv(url))` from the Java test,
        where `urlValidator` is an external Apache Commons Validator
        `UrlValidator` not reproduced here. This checks: no raw whitespace,
        a non-blank authority, a syntactically sound host (hostname/IPv4
        pattern, or a bracketed literal that parses as IPv6), and a port in
        1..65535.
    """

    import ipaddress
    import re

    __all__ = [
        "is_ipv6",
        "get_http_parts",
        "get_https_partsv4",
        "get_https_partsv6",
        "as_jserv",
        "valid_url_port",
        "valid_paths",
        "is_valid_jserv_url",
    ]

    # ---------------------------------------------------------------------------
    # Verbatim translations of the java.util.regex.Pattern strings in Regex.java.
    # Java regex syntax for these patterns (character classes, quantifiers,
    # groups, alternation, anchors) is identical to Python's `re`, so no
    # rewriting was needed beyond the raw-string literal.
    # ---------------------------------------------------------------------------

    # static Regex reg_isIPv6
    _REG_IS_IPV6 = re.compile(
        r"^(([^:/?#]+):)?(//)?\[(::[0-9A-Fa-f]{1,4})"
        r"|([0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4}){7})\](:\d{1,8})?([/?#])?"
    )

    # static Regex reg_hostportv6
    _REG_HOSTPORTV6 = re.compile(
        r"\[((::[0-9A-Fa-f]{1,4})|([0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4}){7}))\](:(\d+))?"
    )

    # static Regex reg3986 -- RFC 3986 Appendix B
    #   group(2) scheme text        group(4) authority text (host[:port])
    #   group(5) path text          group(7) query text      group(9) fragment text
    _REG_3986 = re.compile(r"^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?")

    # static Regex schemePrefix
    _SCHEME_PREFIX = re.compile(r"^(\w+:)?//")

    # Hostname / IPv4-literal token: letters, digits, '.', '-' only.
    _HOSTNAME_RE = re.compile(r"^[A-Za-z0-9]([A-Za-z0-9.\-]*[A-Za-z0-9])?$")

    def _isblank(s: Optional[str], strip_pattern: Optional[str] = None) -> bool:
        """Port of `LangExt.isblank(String, String)`.

        True if `s` is None/empty, or -- when `strip_pattern` is given -- if
        removing every match of `strip_pattern` from `s` leaves nothing.
        """
        if s is None or s == "":
            return True
        if strip_pattern is not None:
            return re.sub(strip_pattern, "", s) == ""
        return False

    def _java_split(s: str, sep: str) -> List[str]:
        """Port of Java's `String.split(regex)` (implicit limit=0): splits on
        `sep`, then drops *trailing* empty strings (internal empty strings
        from doubled separators are kept).
        """
        parts = s.split(sep)
        while parts and parts[-1] == "":
            parts.pop()
        return parts

    def is_ipv6(url: str) -> bool:
        """Port of `Regex.isIPv6(String)`."""
        if url is None:
            return False
        return bool(Regexs._REG_IS_IPV6.search(url))

    def _find_groups(pattern: re.Pattern, s: str) -> Optional[List[Optional[str]]]:
        """Port of `Regex.findGroups(String)`: first match's capture groups
        1..N as a list, or None if the pattern doesn't match at all.
        """
        m = pattern.search(s)
        if not m:
            return None
        return [m.group(i) for i in range(1, pattern.groups + 1)]

    def _parse_path(path_text: Optional[str]) -> Optional[List[str]]:
        """Shared tail of getHttpsPartsv4/v6:
        `isblank(path, "/+") ? null : path.replaceAll("^/*", "").split("/")`
        """
        if Regexs._isblank(path_text, "/+"):
            return None
        stripped = re.sub(r"^/*", "", path_text)
        segments = Regexs._java_split(stripped, "/")
        return segments if segments else None

    def get_https_partsv4(url: str) -> Optional[list]:
        """Port of `Regex.getHttpsPartsv4(String)`."""
        if not Regexs._SCHEME_PREFIX.search(url):
            url = "http://" + url

        m = Regexs._REG_3986.match(url)
        scheme_text = m.group(2)
        authority = m.group(4)
        path_text = m.group(5)
        query = m.group(7)
        fragment = m.group(9)

        if Regexs._isblank(authority):
            return None

        https = scheme_text == "https"
        http = (scheme_text if scheme_text is not None else "http") == "http"
        port = 443 if https else (80 if http else 0)

        host = authority
        iportss = host.split(":")
        if len(iportss) == 2:
            host = iportss[0]
            try:
                port = int(iportss[1])
            except ValueError:
                pass

        path = Regexs._parse_path(path_text)
        return [https, host, port, path, query, fragment]

    def get_https_partsv6(url: str) -> Optional[list]:
        """Port of `Regex.getHttpsPartsv6(String)`."""
        if not Regexs._SCHEME_PREFIX.search(url):
            url = "http://" + url

        m = Regexs._REG_3986.match(url)
        scheme_text = m.group(2)
        authority = m.group(4)
        path_text = m.group(5)
        query = m.group(7)
        fragment = m.group(9)

        if Regexs._isblank(authority):
            return None

        https = scheme_text == "https"
        http = (scheme_text if scheme_text is not None else "http") == "http"
        port = 443 if https else (80 if http else 0)

        host = authority
        iportss = Regexs._find_groups(Regexs._REG_HOSTPORTV6, host)
        if iportss is not None and len(iportss) == 6:
            host = iportss[0]
            try:
                port = int(iportss[5])
            except (TypeError, ValueError):
                pass

        host = re.sub(r"(^\[)|(\]$)", "", host)
        host = "[" + host + "]"

        path = Regexs._parse_path(path_text)
        return [https, host, port, path, query, fragment]

    def get_http_parts(url: str) -> Optional[list]:
        """Port of `Regex.getHttpParts(String)`.

        [0] ipv6, [1] https, [2] host, [3] port, [4] path, [5] query, [6] fragment
        """
        if Regexs.is_ipv6(url):
            parts = Regexs.get_https_partsv6(url)
            return None if parts is None else [True] + parts
        else:
            parts = Regexs.get_https_partsv4(url)
            return None if parts is None else [False] + parts

    def as_jserv(semi_jserv: str) -> str:
        """Port of `Regex.asJserv(String)`: reconstruct a normalized
        scheme://host[:port][/path][?query][#fragment] URL.
        """
        parts = Regexs.get_http_parts(semi_jserv)
        https, host, port, path, query, fragment = parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]
        scheme = "https" if https else "http"
        port_str = "" if port in (80, 443) else f":{port}"
        path_str = "" if path is None else "/" + "/".join(path)
        query_str = "" if Regexs._isblank(query) else f"?{query}"
        fragment_str = "" if Regexs._isblank(fragment) else f"#{fragment}"
        return f"{scheme}://{host}{port_str}{path_str}{query_str}{fragment_str}"

    def valid_url_port(port: Union[int, str], *port_range: int) -> bool:
        """Port of `Regex.validUrlPort(int|String, int...)`."""
        if isinstance(port, str):
            try:
                port = int(port)
            except ValueError:
                return False
        if not port_range:
            return port > 0
        lo = port_range[0]
        ok = lo < 0 or port >= lo
        if len(port_range) >= 2:
            hi = port_range[1]
            ok = ok and (hi < 0 or port <= hi)
        return ok

    def valid_paths(expected: Optional[Sequence[str]], actual: Optional[Sequence[str]]) -> bool:
        """Structural equality for parsed path segments; None == None matches."""
        if expected is None and actual is None:
            return True
        if expected is None or actual is None:
            return False
        return list(expected) == list(actual)

    def is_valid_jserv_url(url: str) -> bool:
        """Approximation of `urlValidator.isValid(asJserv(url))` from the Java
        test. The real Apache Commons `UrlValidator` isn't ported here; this
        checks the properties it's exercised on: no raw whitespace, a present
        host that's either a valid bracketed IPv6 literal or a plain
        hostname/IPv4 token (letters/digits/'.'/'-' only -- notably no '%' or
        ':'), and a port in 1..65535.
        """
        if re.search(r"\s", url):
            return False

        parts = Regexs.get_http_parts(url)
        if parts is None:
            return False
        ipv6, _https, host, port, *_ = parts

        if not (1 <= port <= 65535):
            return False

        if ipv6:
            inner = host[1:-1] if host.startswith("[") and host.endswith("]") else host
            try:
                Regexs.ipaddress.IPv6Address(inner)
            except ValueError:
                return False
            return True

        return bool(host) and bool(Regexs._HOSTNAME_RE.match(host))
