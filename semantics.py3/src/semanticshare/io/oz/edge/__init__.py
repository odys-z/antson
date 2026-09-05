"""
Helpers for deploy, networking, mirroring, etc.
"""

from dataclasses import dataclass
from pathlib import Path
import platform
from typing import List

from anson.io.odysz.anson import Anson

@dataclass
class Proxy(Anson):
    http: str
    https: str

    def __init__(self):
        super().__init__()

@dataclass
class JRERelease(Anson):
    lazy_flag: str
    proxy: str

    def __init__(self):
        super().__init__()
        self.lazy_flag = 'wait:'
        self.proxy = None

@dataclass
class Temurin17Release(JRERelease):
    '''
    Resources type of https://github.com/adoptium/temurin17-binaries
    '''
    date: str
    '''
    Mirror upating data
    '''
    src: str
    path: str
    '''
    sub path.
    "https://github.com/{path}/{resources[i]}" should reach the jre/jdk item.
    "https://<mirror-ip>/deploy-path/{resources[i]}" should reach the jre/jdk item at the mirror site.
    '''
    resources: List[str]

    mirroring: List[str]

    backup: List[str]

    def __init__(self):
        super().__init__()
        self.resources = []
        self.mirroring = []
        self.backup = []

    def mirror(self):
        pass

    def get_resources(self):
        pass

    def set_jre(self):
        '''
        Find out what jre is needed, push into mirroring
        :return: expected-itme, is-in-resources, is-in-mirroring, extreacted-rootpath (e.g. 'jdk-17.0.17+10-jre')
        the jre item needed by current environment
        '''
        system = platform.system()
        machine = platform.machine()

        if system == "Windows":
            os_name = "windows"
            ext = "zip"
        elif system == "Darwin":
            os_name = "mac"
            ext = "tar.gz"
        elif system == "Linux":
            os_name = "linux"
            ext = "tar.gz"
        else:
            raise RuntimeError("Unsupported OS")

        if machine in ("AMD64", "x86_64"):
            arch = "x64"
        elif machine in ("aarch64", "arm64"):
            arch = "aarch64"
        else:
            raise RuntimeError(f"Unsupported arch: {machine}")

        release = "17.0.17_10"
        zip_gz = f"OpenJDK17U-jre_{arch}_{os_name}_hotspot_{release}.{ext}"

        self.extract_root = 'jdk-17.0.17+10-jre'

        if not hasattr(self, 'mirroring') or self.mirroring is None:
            self.mirroring = []
        inmirror = zip_gz in self.mirroring
        if not inmirror:
            self.mirroring.append(zip_gz)
        return zip_gz, zip_gz in self.resources, inmirror, self.extract_root

def extract_check_jretree(gzip_path, target_dir):
    '''
    Extract the zip/tar.gz file to target_dir, and check if a valid JRE tree is present.
    :param zip_path: path to the zip/tar.gz file
    :param target_dir: directory to extract to
    :return: root path of the extracted JRE tree, or None if not found.
    '''
    import os
    import shutil
    import tarfile
    import zipfile

    def guess_jretree(target_root):
        '''
        Find java bin in target root. (only verified against JRE 17 tree)
        :param target_root:
        :return: root path of the extracted JRE tree, or None if not found.
        '''
        for root, dirs, _ in os.walk(target_root):
            if "bin" in dirs and "lib" in dirs and "NOTICE" in _ and "release" in _:
                return Path(root)
        return None

    filename = Path(gzip_path).stem

    target_dir = Path.joinpath(target_dir, filename + '-extract')
    try: shutil.rmtree(target_dir)
    except: pass

    print(f"Extracting {filename} ...")
    if filename.endswith(".zip"):
        with zipfile.ZipFile(gzip_path, 'r') as z:
            z.extractall(target_dir)
    elif filename.endswith(".gz") or filename.endswith(".tgz"):
        import tarfile
        with tarfile.open(gzip_path, 'r:gz') as t:
            t.extractall(target_dir)

    ext_root = guess_jretree(target_dir)
    if ext_root is None and (filename.endswith(".zip") or filename.endswith(".gz")):
        raise RuntimeError("JRE extraction failed")
    return ext_root