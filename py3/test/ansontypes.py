'''
Created on 28 Oct 2019

Data types for test cases.

@author: odys-z@github.com
'''
from src.anson.io.odysz.ansons import Anson


class AnsT1(Anson):
    ver = "1.0" # type: str
    
    m = None # type: AnsM1 
