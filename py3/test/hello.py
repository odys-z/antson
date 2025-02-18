# Window - Preferences - PyDev - Interpreters - Python Interpreter - Forced builtins - new... - antlr4
# https://stackoverflow.com/questions/2112715/how-do-i-fix-pydev-undefined-variable-from-import-errors
from antlr4 import * #@UnusedWildImport

from src.ansonpy_del.JSONListener import JSONListener


class JSONPrintListener(JSONListener):
    def enterJson(self, ctx):
        print("Hello: %s" % ctx.envelope()[0].type_pair().TYPE())

def main():
#     lexer = JSONLexer(StdinStream())
#     stream = CommonTokenStream(lexer)
#     parser = JSONParser(stream)
#     tree = parser.json()
#     printer = JSONPrintListener()
#     walker = ParseTreeWalker()
#     walker.walk(printer, tree)
    print(generalizedGCD(5, [2,3,4,5,6]))
    print(generalizedGCD(5, [2,4,6,8,10]))
    print(generalizedGCD(3, [9,27,18]))

def tryTypes():
    s = list()
    l = list()
    l.append('s')
    l.append('abc')
    l.append(1)
    s.append(l)

    t = list()
    t.append('t')
    t.append('xyz')
    t.append(t)
    s.append(t)

    print(s)
    
    m = {}
    m[1] = "1"
    print(m)

def generalizedGCD(num, arr):
    # WRITE YOUR CODE HERE
    '''
    // 23:56 + 15min
    
    86 24    86 - 24 = 62
    62 24    62 - 24 = 38
    38 24    38 - 24 = 14
    24 14    10
    14 10    4
    10 4     6
    6  4     2
    4  2     2
             0
    '''
    gcd = 1
    for i in reversed(range(num - 1)):
        if (arr[i] < arr[i + 1]):
            diff = arr[i + 1] - arr[i]
            arr[i + 1] = diff
            if diff == 0:
                return arr[i]
        else: 
            diff = arr[i] - arr[i + 1]
            arr[i] = diff
            if diff == 0:
                return arr[i + 1]
        if diff == 1:
            return 1
        else:
            gcd = diff
    return gcd
    
            
        
# if __name__ == '__main__':
#     main()
# else:
#     tryTypes()
main()
