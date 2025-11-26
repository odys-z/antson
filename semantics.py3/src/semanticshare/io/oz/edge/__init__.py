"""
Helpers for deploy, networking, mirroring, etc.
"""

from dataclasses import dataclass
import platform

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
    resources: list[str]

    mirroring: list[str]

    backup: list[str]

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
        :return: expected-itme, is-in-resources, is-in-mirroring
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


        # build, plus = "17.0.9", "9"
        release = "17.0.17_10"
        zip_gz = f"OpenJDK17U-jre_{arch}_{os_name}_hotspot_{release}.{ext}"
        # download_url = f'https://github.com/{self.path}'
        # exp_item = f"{download_url}/jdk-{build}%2B{plus}/{zip_gz}"
        # exp_item = f"{self.path}/{zip_gz}"

        if not hasattr(self, 'mirroring') or self.mirroring is None:
            self.mirroring = []
        inmirror = zip_gz in self.mirroring
        if not inmirror:
            self.mirroring.append(zip_gz)
        return zip_gz, zip_gz in self.resources, inmirror
