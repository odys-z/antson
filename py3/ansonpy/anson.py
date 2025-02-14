'''
Created on 25 Oct 2019

@author: odys-z@github.com
'''

import inspect
from enum import Enum
from ansonpy.JSONListener import JSONListener
# from abc import abstractmethod
from odysz.common import LangExt, Utils
from ansonpy.x import AnsonException
import abc
import decimal
from typing import List
import re
from importlib import import_module

class AnsonFlags():
    parser = True

################################# Anson ##################################
class IJsonable(abc.ABC):
    '''
    Java interface (protocol) can be deserailized into json.
    For python protocol and ABC, see
    http://masnun.rocks/2017/04/15/interfaces-in-python-protocols-and-abcs/
    '''
    @abc.abstractmethod
    def toBlock(self, outstream):
        pass

def writeVal(outstream, v):
    if (isinstance(v, str)):
        outstream.write("\"")
        outstream.write(v)
        outstream.write("\"")
    else:
        outstream.write(str(v))

@dataclass
class Anson(IJsonable):
    to_del: str = "some vale"
    to_del_int: int = 5

    def toBlock(self, outstream, opts):
        quotK = opts == None or opts.length == 0 or opts[0] == None or opts[0].quotKey();
        if (quotK == True):
            outstream.write("{\"type\": \"");
            outstream.write(self.getClass());
            outstream.write('\"');
        else :
            outstream.write("{type: ");
            outstream.write(self.getClass());

        for (n, v) in self.getFields():
            outstream.write(", ");
            if (quotK == True):
                outstream.write("\"%s\": " % n);
            else :
                outstream.write("%s: " % n);
            writeVal(outstream, v);

        outstream.write("}")
        return "";

    def getClass(self):
        return "io.odysz.anson.Anson"

    def getFields(self):
        env_dict = []
        for (name, att) in inspect.getmembers(self, lambda attr: not callable(attr) ):
            if (not name.startswith("__")):
                env_dict.append((name, att))
        return env_dict

################################# From Json ##############################
class Reflectn(object):
    moduclss = dict()

    ''' Reflection helper
    '''
    @staticmethod
    def isArray(typename):
        return re.match(typename, 'list')

    @staticmethod
    def parseElemType(subTypes: str) -> List[str]:
        ''' Java equivalent:
            private static String[] parseElemType(String subTypes)
        '''
        if (LangExt.isblank(subTypes)):
            return None;
        return subTypes.split("/", 2);

    @staticmethod
    def isAnson(modclss) -> bool:
        ''' Is the class declared as a subclass of Anson?
            This verification also prevent injection attack?
            see discussion about eval():
            https://stackoverflow.com/questions/3451779/how-to-dynamically-create-an-instance-of-a-class-in-python

            This class will also automatically verifyClss user's module.class

            Also DP verify module class name.
            Parameters:
            -----------
            modclss: str
                name of module.path.ClassName
        '''
        modu, clss = modclss.rsplit('.', 1)
        if modclss not in Reflectn.moduclss:
            Reflectn.moduclss.update({modclss: modu})
            return clss, modclss
        else:
            return None, None

    @staticmethod
    def creatByName(class_str: str = 'tutorial.foo.foo.Class1'):
        try:
            module_path, class_name = class_str.rsplit('.', 1)
            module = import_module(module_path)
            return getattr(module, class_name), eval(class_str)()
        except (ImportError, AttributeError) as e: # @UnusedVariable
            raise ImportError(class_str)

    @staticmethod
    def allSubcls(cls):
        allSubs = []

        for sub in cls.__subclasses__():
            allSubs.append(sub.__name__)
            allSubs.extend(Reflectn.allSubcls(sub))

        return allSubs


# class AnInst:
#     ''' Anson Type
#         Equivolent of java Type
#     '''
#     def __init__(self, ftype):
#         self.ftype = ftype
#
#     def getClass(self):
#         return self.antype

class ParsingCtx():
    ''' Parsing AST node's context, for handling the node's value,
        the element class of parsing stack.

        Attributes
        ---------
        parsingProp: str
            The json prop (object key)
            java: protected String parsingProp;

        parsedVal: object
            The parsed native value

        valType: str
            field value types

        enclosing: object
            enclosing instance currently parsing

        # private HashMap<String, Field> fmap;

        subType: str;
            Annotation's sub types
    '''

    def __init__(self, fmap, enclosing):
        '''
        Parameters
        ----------
        fmap: map
            fields map

        enclosing: object
            enclosing object
        '''
        self.fmap = fmap;
        self.enclosing = enclosing;

    def isInList(self):
        '''
        Returns
        -------
        boolean
            is in a list
        '''
        return isinstance(self.enclosing, list)

    def isInMap(self):
        return isinstance(self.enclosing, dict);

    def elemType(self, tn):
        ''' Set list element's type

            Parameters
            ----------
                tn : list of str
                    type name list
            Returns
            -------
                ParsingCtx: this
        '''
        self.valType = None if tn == None or tn.length <= 0 else tn[0];
        self.subTypes = None if tn == None or tn.length <= 1 else tn[1];

#         if (not LangExt.isblank(self.valType)):
#             # change / replace array type
#             # e.g. lang.String[] to [Llang.String;
#             if (self.valType.matches(".*\\[\\]$")):
#                 self.valType = "[L" + self.valType.replaceAll("\\[\\]$", ";");
#                 self.valType = self.valType.replaceFirst("^\\[L\\[", "[[");
        return self;

#     def elemType(self):
#         ''' Get type annotation
#             Returns
#             -------
#                 value type, {@link AnsonField#valType()} annotation if possible
#         '''
#         return self.valType;

    def subTypes(self):
        return self.subTypes;


class AnsonListener(JSONListener):
    def __init__(self):
        self.stack = []

    factorys = None
    ''' static
        private static HashMap<Class<?>, JsonableFactory> factorys;
    '''

    # envetype = None;
    # ''' Envelope Type Name '''

    # stack = []
    # ''' Parsing Node Stack '''

    def toparent(self, anInst):
        if (self.stack.size() <= 1 or LangExt.isblank(anInst, "None")):
            # no enclosing, no parent
            return None;

        # trace back, guess with type for children could be in array or map
        # ParsingCtx
        p = self.stack.get(1);
        i = 2;
        while (p != None):
            if (anInst == p.enclosing.getClass()):
                return p.enclosing;
            p = self.stack.get(i);
            i = i + 1;
        return None;

    def push(self, enclosingClazz, elemType = None):
        ''' Push parsing node (a envelope, map, list).
            private void push(Class<?> enclosingClazz, String[] elemType)
        Parameters
        ----------
        enclosingClazz : str
            new parsing IJsonable object's class

        elemType : [type] Not used in python?
            annotation of enclosing list/array. 0: main type, 1: sub-types
            This parameter can't be None if is pushing a list node.

        Raises
        ------
        ReflectiveOperationException
        SecurityException
        AnsonException
        '''
        if (Reflectn.isArray(enclosingClazz)):
            # HashMap<String, Field>
            fmap = map();
            # ParsingCtx
            newCtx = ParsingCtx(fmap, list());
            self.stack.insert(0, newCtx.elemType(elemType));
        else:
            fmap = {}; # HashMap<String, Field>

            if (JSONListener.isArray(enclosingClazz)):
                enclosing = list();
                self.stack.insert(0, ParsingCtx(fmap, enclosing).elemType(elemType));
            else:
#                 Constructor<?> ctor = None;
#                 try:
#                     ctor = enclosingClazz.getConstructor();
#                 except NoSuchMethodException as e:
#                     throw new AnsonException(0, "To make json can be parsed to %s, the class must has a default constructor(0 parameter)\n"
#                             + "Also, inner class must be static."
#                             + "getConstructor error: %s %s",
#                             enclosingClazz.getName(), e.getClass().getName(), e.getMessage());
#                 if (ctor != None && IJsonable.class.isAssignableFrom(enclosingClazz)):
                if (Reflectn.isAnson(enclosingClazz)):
                    # fmap = mergeFields(enclosingClazz, fmap); # map merging is only needed by typed object
                    fmap = {}
                    try:
                        # IJsonable
                        # enclosing = newInstance();
                        enclosing = Anson();
                        self.stack.insert(0, ParsingCtx(fmap, enclosing));
                    except Exception as e:
                        raise AnsonException(0, "Failed to create instance of IJsonable with\nconstructor: %s\n"
                            + "class: %s\nerror: %s\nmessage: %s\n"
                            + "Make sure the object can be created with the constructor.",
                            enclosingClazz, enclosingClazz.getName(), e.getClass().getName(), e.getMessage());
                else:
                    enclosing = {};
                    # ParsingCtx
                    top = ParsingCtx(fmap, enclosing);
                    self.stack.insert(0, top);

    def pop(self):
        ''' private ParsingCtx pop() {
        Returns
        -------
            ParsingCtx
        '''
        top = self.stack.popleft();
        return top;


#     def enterJson(self, ctx):
#         # print("Hello: %s" % ctx.envelope()[0].type_pair().TYPE())
#         self.an = Anson()

    def exitObj(self):
        top = self.pop();
        top().parsedVal = top.enclosing;
        top.enclosing = None;

    def enterObj(self, ctx):
        top = self.top();
        try:
            fmap = top.fmap if top != None else None;
            if (fmap == None or not fmap.containsKey(top.parsingProp)):
                # In a list, found object, if not type specified with annotation, must failed.
                # But this is confusing to user. Set some report here.
                if (top.isInList() or top.isInMap()):
                    Utils.warn("Type in list or map is complicate, but no annotation for type info can be found. "
                            + "field type: %s\njson: %s\n"
                            + "Example: @AnsonField(valType=\"io.your.type\")\n"
                            + "Anson instances don't need annotation, but objects in json array without type-pair can also trigger this error report.",
                            top.enclosing.getClass(), ctx.getText());
                raise AnsonException(0, "Obj type not found. property: %s", top.parsingProp);

            # Class<?>
            ft = fmap.get(top.parsingProp).getType();
            # if (Map.class.isAssignableFrom(ft)):
            if (isinstance(fmap.get(top.parsingProp), dict)):
                # entering a map
                self.push(ft, None);
                # append annotation
                # Field
                f = top.fmap.get(top.parsingProp);
#                 # AnsonField
#                 a = None if f == None else f.getAnnotation(AnsonField.class);
#                 String anno = a == None ? None : a.valType();
#
#                 if (anno != None):
#                     String[] tn = parseElemType(anno);
#                     top().elemType(tn);
                top().elemType("object")
            else:
                # entering an envelope
                # push(fmap.get(top.parsingProp).getType());
                self.push(ft, None);
        except (AnsonException) as e:
            e.printStackTrace();

    def parsedEnvelope(self) -> IJsonable :
        if self.stack:
            return self.stack[0].enclosing;
        else:
            raise AnsonException(0, "No envelope is avaliable.");

    def enterEnvelope(self, ctx) -> None:
        ''' Java equivalent:
            public void enterEnvelope(EnvelopeContext)
        '''
        if (self.stack == None):
            self.stack = [];
        self.envetype = None;

    def exitEnvelope(self, ctx) -> None:
        ''' Java equivalent:
            public void exitEnvelope(EnvelopeContext)
        '''
        super().exitEnvelope(ctx);
        if (len(self.stack) > 1):
            top = self.pop(); # ParsingCtx
            top().parsedVal = top.enclosing;
        # else keep last one (root) as return value

    def enterType_pair(self, ctx) -> None:
        ''' Semantics of entering a type pair is found and parsingVal an IJsonable object.<br>
            This is always happening on entering an object.
            The logic opposite to this is exit object.

            Java equivalent:
            public void enterType_pair(Type_pairContext)
            @see gen.antlr.json.JSONBaseListener#enterType_pair(gen.antlr.json.JSONParser.Type_pairContext)
        '''
        if (self.envetype != None):
            # ignore this type specification, keep consist with java type
            return;

        strType = ctx.qualifiedName().STRING(); # TerminalNode
        txt = ctx.qualifiedName().getText(); # String
        envetype = AnsonListener.getStringValRaw(
            strType if isinstance(strType, str) else strType.getText(),
            txt);

        try:
            self.push(envetype);
        except (AnsonException ) as e:
            e.printStackTrace();

    def enterPair(self, ctx) -> None:
        ''' Java equivalent:
            public void enterPair(PairContext ctx)
        '''
        super.enterPair(ctx);
        top = self.top(); # ParsingCtx
        top.parsingProp = self.getProp(ctx);
        top.parsedVal = None;

#     private static String[] parseListElemType(Field f) throws AnsonException {
#     @staticmethod
#     def parseListElemType(f) -> list[str]:
#         # for more information, see
#         # https://stackoverflow.com/questions/1868333/how-can-i-determine-the-type-of-a-generic-field-in-java
#
#         # Type
#         typ = f.getGenericType();
#         if (isinstance(typ, ParameterizedType)):
#             # ParameterizedType
#             pType = typ;
#
#             # String[]
#             ptypess = pType.getActualTypeArguments()[0].getTypeName().split("<", 2);
#             if (ptypess.length > 1):
#                 ptypess[1] = ptypess[1].replaceFirst(">$", "");
#                 ptypess[1] = ptypess[1].replaceFirst("^L", "");
#             # figure out array element class
#             else :
#                 # Type
#                 argType = pType.getActualTypeArguments()[0];
#                 if (not isinstance(argType, TypeVariable) and not isinstance(argType, WildcardType)):
#                     # Class<? extends Object>
#                     eleClzz = argType;
#                     if (eleClzz.isArray()):
#                         ptypess = list[ptypess[0], eleClzz.getComponentType().getName()];
#                 # else nothing can do here for a type parameter, e.g. "T"
#                 elif (AnsonFlags.parser):
#                         Utils.warn("[AnsonFlags.parser] Element type <%s> for %s is a type parameter (%s) - ignored",
#                             pType.getActualTypeArguments()[0],
#                             f.getName(),
#                             pType.getActualTypeArguments()[0].getClass());
#             return ptypess;
#         elif (f.getType().isArray()):
#             # complex array may also has annotation
#             # AnsonField a = None if f == None else f.getAnnotation(AnsonField.class);
#             a = "object";
#             # String
#             tn = None if a == None else a.valType();
#             # String[]
#             valss = JSONListener.parseElemType(tn);
#
#             eleType = f.getType().getComponentType().getTypeName();
#             if (valss != None and not eleType.equals(valss[0])):
#                 Utils.warn("[JSONAnsonListener#parseListElemType()]: Field %s is not annotated correctly.\n"
#                         + "field parameter type: %s, annotated element type: %s, annotated sub-type: %s",
#                         f.getName(), eleType, valss[0], valss[1]);
#
#             if (valss != None and valss.length > 1):
#                 return list[eleType, valss[1]];
#             else:
#                 return list[eleType];
#         else :
#             # not a parameterized, not an array, try annotation
#             # AnsonField a = f == None ? None : f.getAnnotation(AnsonField.class);
#             a = "object"
#             tn = None if a == None else a.valType();
#             return JSONListener.parseElemType(tn);

#     /**Parse property name, tolerate enclosing quotes presenting or not.
#      * @param ctx
#      * @return
#      */
#     private static String getProp(PairContext ctx) {
    @staticmethod
    def getProp(ctx) -> str:
        # TerminalNode
        p = ctx.propname().IDENTIFIER();
        return ctx.propname().STRING().getText().replaceAll("(^\\s*\"\\s*)|(\\s*\"\\s*$)", "") if p == None else p.getText();

    @staticmethod
    def getStringVal(ctx) -> str:
        ''' Java equivalent:
            private static String getStringVal(PairContext ctx)

            Convert json value : STRING | NUMBER | 'true' | 'false' | 'None' to java.lang.String.<br>
            java: Can't handle NUMBER | obj | array.

            parameters:
            -----------
                ctx: PairContext
                    antlr parsing context
            returns:
            -----------
                value in string
        '''

        # TerminalNode
        stri = ctx.value().STRING();
        # String
        txt = ctx.value().getText();
        return AnsonListener.getStringValRaw(stri, txt);

    @staticmethod
    def getStringValRaw(stri, rawTxt) -> str:
        ''' Get string value.
            If stri is null, use rawTxt.
            Java equivalent:
            private static String getStringVal(TerminalNode str, String rawTxt) {
        '''
        if (stri == None):
            try :
                if (LangExt.isblank(rawTxt)):
                    return None;
                else:
                    if ("None".equals(rawTxt)):
                        return None;
            except Exception as e: { }  # @UnusedVariable
            return rawTxt;
        else:
            # stri.getText().replaceAll("(^\\s*\")|(\"\\s*$)", "")
            return re.sub(r'(^\s*")|("\s*$)', '', stri)

    @staticmethod
    def figureJsonVal(ctx) -> object:
        '''grammar:<pre>value
        : STRING
        | NUMBER
        | obj        // all array's obj value can't parsed as Anson, taken as HashMap
        | envelope
        | array
        | 'true'
        | 'false'
        | 'None'
        ;</pre>
        Parameters:
        -----------
            ctx: ValueContext
        Returns
        -------
            java simple value (STRING, NUMBER, 'true', 'false', None)

        java equivalent:
        private static Object figureJsonVal(ValueContext ctx
        '''
        txt = ctx.getText();
        if (txt == None):
            return None;
        elif (ctx.NUMBER() != None):
            try :
                return int(txt);
            except Exception as e:  # @UnusedVariable
                try :
                    return float(txt);
                except Exception as e1:  # @UnusedVariable
                    return decimal(txt);
        elif (ctx.STRING() != None):
                return AnsonListener.getStringVal(ctx.STRING(), txt);
        elif (txt != None and txt.toLowerCase().equals("true")):
            return True;
        elif (txt != None and txt.toLowerCase().equals("flase")):
            return False;
        return None;

#     @Override
#     public void enterArray(ArrayContext ctx) {
    def enterArray(self, ctx) -> None:
        try:
            # ParsingCtx
            top = self.top();

            # if in a list or a map, parse top's sub-type as the new node's value type
            if (top.isInList() or top.isInMap()):
                # pushing ArrayList.class because entering array, isInMap() == true means needing to figure out value type
                # String[]
                # tn = JSONListener.parseElemType(top.subTypes());
                # ctx:        [{type:io.odysz.anson.AnsT2,s:4},{type:io.odysz.anson.AnsT1,ver:"x"}]
                # subtype:    io.odysz.anson.Anson
                # tn :        [io.odysz.anson.Anson]
                # push(ArrayList.class, tn);
                self.push(list, 'object');
            # if field available, parse field's value type as the new node's value type
            else:
                # Class<?>
                ft = top.fmap.get(top.parsingProp).getType();
                # Field
                f = top.fmap.get(top.parsingProp);
                # AnsT3 { ArrayList<Anson[]> ms; }
                # ctx: [[{type:io.odysz.anson.AnsT2,s:4},{type:io.odysz.anson.AnsT1,ver:"x"}]]
                # [0]: io.odysz.anson.Anson[],
                # [1]: io.odysz.anson.Anson
                # String[]
                tn = AnsonListener.parseListElemType(f);
                self.push(ft, tn);

            # now top is the enclosing list, it's component type is elem-type

        except (AnsonException) as e:
            e.printStackTrace();

#     @Override
#     public void exitArray(ArrayContext ctx) {
#         if (!top().isInList())
#             throw new NullPointerException("existing not from an eclosing list. txt:\n" + ctx.getText());
#
#         ParsingCtx top = pop();
#         List<?> arr = (List<?>) top.enclosing;
#
#         top = top();
#         top.parsedVal = arr;
#
#         // figure the type if possible - convert to array
#         String et = top.elemType();
#         if (!LangExt.isblank(et, "\\?.*")) // TODO debug: where did this type comes from?
#             try {
#                 Class<?> arrClzz = Class.forName(et);
#                 if (arrClzz.isArray())
#                     top.parsedVal = toPrimitiveArray(arr, arrClzz);
#             } catch (AnsonException | IllegalArgumentException | ClassNotFoundException e) {
#                 Utils.warn("Trying convert array to annotated type failed.\ntype: %s\njson: %s\nerror: %s",
#                         et, ctx.getText(), e.getMessage());
#             }
#         // No annotation, for 2d list, parsed value is still a list.
#         // If enclosed element of array is also an array, it can not been handled here
#         // Because there is no clue for sub array's type if annotation is empty
#     }
#
#     def toPrimitiveArray(list, arrType):
#         '''
#         private static <P> P toPrimitiveArray(List<?> list, Class<P> arrType) throws AnsonException {
#          * Unboxes a List in to a primitive array.
#          * reference:
#          * https://stackoverflow.com/questions/25149412/how-to-convert-listt-to-array-t-for-primitive-types-using-generic-method
#          *
#          * @param  list      the List to convert to a primitive array
#          * @param  arrType the primitive array type to convert to
#          * @param  <P>       the primitive array type to convert to
#          * @return an array of P with the elements of the specified List
#          * @throws AnsonException list element class doesn't equal array element type - not enough annotation?
#          * @throws NullPointerException
#          *         if either of the arguments are None, or if any of the elements
#          *         of the List are None
#          * @throws IllegalArgumentException
#          *         if the specified Class does not represent an array type, if
#          *         the component type of the specified Class is not a primitive
#          *         type, or if the elements of the specified List can not be
#          *         stored in an array of type P
#         '''
#         if (!arrType.isArray()):
#             throw new IllegalArgumentException(arrType.toString());
#
#         if (list == None):
#             return None;
#
#         Class<?> eleType = arrType.getComponentType();
#
#         P array = arrType.cast(Array.newInstance(eleType, list.size()));
#
#         for (int i = 0; i < list.size(); i++):
#             Object lstItem = list.get(i);
#             if (lstItem == None)
#                 continue;
#
#             # this guess is error prone, let's tell user why. May be more annotation is needed
#             if (!eleType.isAssignableFrom(lstItem.getClass()))
#                 throw new AnsonException(1, "Set element (v: %s, type %s) to array of type of \"%s[]\" failed.\n"
#                         + "Array element's type not annotated?",
#                         lstItem, lstItem.getClass(), eleType);
#
#             Array.set(array, i, list.get(i));
#
#         return array;
#
#     public void exitValue(ValueContext ctx) {
#         '''
#         grammar:<pre>value
#         : STRING
#         | NUMBER
#         | obj        // all array's obj value can't parsed as Anson, taken as HashMap - TODO doc: known issue
#         | envelope
#         | array
#         | 'true'
#         | 'false'
#         | 'None'
#         ;</pre>
#          * @see gen.antlr.json.JSONBaseListener#exitValue(gen.antlr.json.JSONParser.ValueContext)
#         '''
#         ParsingCtx top = top();
#         if (top.isInList() || top.isInMap()) {
#             # if in a map, parsingProp is the map key,
#             # element type can only been handled with a guess,
#             # or according to annotation
#             # String txt = ctx.getText();
#             if (top.isInList()) {
#                 List<?> enclosLst = (List<?>) top.enclosing;
#                 # for List, ft is not None
#                 if (top.parsedVal == None) {
#                     # simple value like String or number
#                     ((List<Object>)enclosLst).add(figureJsonVal(ctx));
#                 }
#                 else {
#                     # try figure out is element also an array if enclosing object is an array
#                     # e.g. convert elements of List<String> to String[]
#                     # FIXME issue: if the first element is 0 length, it will failed to convert the array
#                     Class<?> parsedClzz = top.parsedVal.getClass();
#                     if (List.class.isAssignableFrom(parsedClzz)) {
#                         if (LangExt.isblank(top.elemType(), "\\?.*")) {
#                             // change list to array
#                             List<?> lst = (List<?>)top.parsedVal;
#                             if (lst != None && lst.size() > 0) {
#                                 // search first non-None element's type
#                                 Class<? extends Object> eleClz = None;
#                                 int ix = 0;
#                                 while (ix < lst.size() && lst.get(ix) == None)
#                                     ix++;
#                                 if (ix < lst.size())
#                                     eleClz = lst.get(ix).getClass();
#
#                                 if (eleClz != None) {
#                                     try {
#                                         ((List<Object>)enclosLst).add(toPrimitiveArray(lst,
#                                                 Array.newInstance(eleClz, 0).getClass()));
#                                     } catch (AnsonException e) {
#                                         Utils.warn("Trying convert array to annotated type failed.\nenclosing: %s\njson: %s\nerror: %s",
#                                             top.enclosing, ctx.getText(), e.getMessage());
#                                     }
#
#                                     # remember elem type for later None element
#                                     top.elemType(new String[] {eleClz.getName()});
#                                 }
#                                 # all elements are None, ignore the list is the only way
#                             }
#                             else
#                                 # FIXME this will broken when first element's length is 0.
#                                 ((List<Object>)enclosLst).add(lst.toArray());
#                         }
#                         # branch: with annotation or type name already figured out from 1st element
#                         else {
#                             try {
#                                 List<?> parsedLst = (List<?>)top.parsedVal;
#                                 String eleType = top.elemType();
#                                 Class<?> eleClz = Class.forName(eleType);
#                                 if (eleClz.isAssignableFrom(parsedClzz)) {
#                                     # annotated element can be this branch
#                                     ((List<Object>)enclosLst).add(parsedLst);
#                                 }
#                                 else {
#                                     # type is figured out from the previous element,
#                                     # needing conversion to array
#                                     #
#                                     # Bug: object value can't been set into string array
#                                     # lst.getClass().getTypeName() = java.lang.ArrayList
#                                     # ["val",88.91669145042222]
#
#
#                                     # Test case:    AnsT3 { ArrayList<Anson[]> ms; }
#                                     # ctx:         [{type:io.odysz.anson.AnsT2,s:4},{type:io.odysz.anson.AnsT1,ver:"x"}]
#                                     # parsedLst:    [{type: io.odysz.anson.AnsT2, s: 4, m: None}, {type: io.odysz.anson.AnsT1, ver: "x", m: None}]
#                                     # parsedClzz:    java.util.ArrayList
#                                     # eleType:        [Lio.odysz.anson.Anson;
#                                     # eleClz:        class [Lio.odysz.anson.Anson;
#                                     # action - change parsedLst to array, add to enclosLst
#                                     ((List<Object>)enclosLst).add(toPrimitiveArray(parsedLst,
#                                                     Array.newInstance(eleClz, 0).getClass()));
#                             except Exception as e:
#                                 Utils.warn(envelopName());
#                                 Utils.warn(ctx.getText());
#                                 e.printStackTrace();
#                     else:
#                         ((List<Object>)enclosLst).add(top.parsedVal);
#                 top.parsedVal = None;
#             else if (top.isInMap()):
#                 # parsed Value can already got when exit array
#                 if (top.parsedVal == None)
#                     top.parsedVal = getStringVal(ctx.STRING(), ctx.getText());
#
#     public void exitPair(PairContext ctx) {
#         super.exitPair(ctx);
#         if (AnsonFlags.parser) {
#             Utils.logi("[AnsonFlags.parser] Property-name: %s", ctx.getChild(0).getText());
#             Utils.logi("[AnsonFlags.parser] Property-value: %s", ctx.getChild(2).getText());
#         }
#
#         try {
#             // String fn = getProp(ctx);
#             ParsingCtx top = top();
#             String fn = top.parsingProp;
#
#             // map's pairs also exits here - map helper
#             if (top.isInMap()) {
#                 ((HashMap<String, Object>)top.enclosing).put(top.parsingProp, top.parsedVal);
#                 top.parsedVal = None;
#                 top.parsingProp = None;
#                 return;
#             }
#             # not map ...
#
#             Object enclosing = top().enclosing;
#             Field f = top.fmap.get(fn);
#             if (f == None)
#                 throw new AnsonException(0, "Field ignored: field: %s, value: %s", fn, ctx.getText());
#
#             f.setAccessible(true);
#             AnsonField af = f.getAnnotation(AnsonField.class);
#             if (af != None && af.ignoreFrom()) {
#                 if (AnsonFlags.parser)
#                     Utils.logi("[AnsonFlags.parser] %s ignored", fn);
#                 return;
#             }
#             else if (af != None && af.ref() == AnsonField.enclosing) {
#                 Object parent = toparent(f.getType());
#                 if (parent == None)
#                     Utils.warn("parent %s is ignored: reference is None", fn);
#
#                 f.set(enclosing, parent);
#                 return;
#             }
#
#             Class<?> ft = f.getType();
#
#             if (ft == String.class) {
#                 String v = getStringVal(ctx);
#                 f.set(enclosing, v);
#             }
#             else if (ft.isPrimitive()) {
#                 # construct primitive value
#                 v = ctx.getChild(2).getText();
#                 setPrimitive((IJsonable) enclosing, f, v);
#             else if (ft.isEnum()):
#                 String v = getStringVal(ctx);
#                 if (!LangExt.isblank(v)):
#                     f.set(enclosing, Enum.valueOf((Class<Enum>) ft, v));
#             else if (ft.isArray())
#                 f.set(enclosing, toPrimitiveArray((List<?>)top.parsedVal, ft));
#             else if (List.class.isAssignableFrom(ft)
#                     or AbstractCollection.class.isAssignableFrom(ft)
#                     or Map.class.isAssignableFrom(ft)):
#                 f.set(enclosing, top.parsedVal);
#             else if (IJsonable.class.isAssignableFrom(ft)):
#                 if (Anson.class.isAssignableFrom(ft))
#                     f.set(enclosing, top.parsedVal);
#                 else:
#                     # Subclass of IJsonable must registered
#                     String v = getStringVal(ctx);
#                     if (!LangExt.isblank(v, "None"))
#                         f.set(enclosing, invokeFactory(f, v));
#             else if (Object.class.isAssignableFrom(ft)):
#                 Utils.warn("\nDeserializing unsupported type, field: %s, type: %s, enclosing type: %s",
#                         fn, ft.getName(), enclosing == None ? None : enclosing.getClass().getName());
#                 String v = ctx.getChild(2).getText();
#
#                 if (!LangExt.isblank(v, "None"))
#                     f.set(enclosing, v);
#             else throw new AnsonException(0, "sholdn't happen");
#
#             # not necessary, top is dropped
#             top.parsedVal = None;
#         except (ReflectiveOperationException, RuntimeException) as e:
#             e.printStackTrace();
#         except AnsonException e:
#             Utils.warn(e.getMessage());

    def invokeFactory(self, f, v):
        '''
        private IJsonable invokeFactory(Field f, String v) throws AnsonException {
        '''
        if (self.factorys == None or not self.factorys.containsKey(f.getType())):
            raise AnsonException(0,
                    "Subclass of IJsonable (%s) must registered.\n - See javadoc of IJsonable.JsonFacotry\n"
                    + "Or don't declare the field as %1$s, use a subclass of Anson",
                    f.getType());

        factory = self.factorys.get(f.getType());
        try:
            return factory.fromJson(v);
        except Exception as t:
            raise AnsonException(0,
                    "Subclass of IJsonable (%s) must registered.\n - See javadoc of IJsonable.JsonFacotry\n"
                    + "Or don't declare the field as %1$s, use a subclass of Anson",
                    f.getType(), t.getMessage());

    @staticmethod
    def setPrimitive(self, obj, f, v):
        '''
        private static void setPrimitive(IJsonable obj, Field f, String v)
            throws RuntimeException, ReflectiveOperationException, AnsonException {
        '''
#         if (f.getType() == int.class || f.getType() == Integer.class)
#             f.set(obj, Integer.valueOf(v));
#         else if (f.getType() == float.class || f.getType() == Float.class)
#             f.set(obj, Float.valueOf(v));
#         else if (f.getType() == double.class || f.getType() == Double.class)
#             f.set(obj, Double.valueOf(v));
#         else if (f.getType() == long.class || f.getType() == Long.class)
#             f.set(obj, Long.valueOf(v));
#         else if (f.getType() == short.class || f.getType() == Short.class)
#             f.set(obj, Short.valueOf(v));
#         else if (f.getType() == byte.class || f.getType() == Byte.class)
#             f.set(obj, Byte.valueOf(v));
#         else
#             # what's else?
#             raise AnsonException(0, "Unsupported field type: %s (field %s)",
#                     f.getType().getName(), f.getName());

    @staticmethod
    def registFactory(jsonable, factory):
        '''
        Parameters
        ---------
        jsonable: Class<?>
        factory: JsonableFactory
        '''
        if (AnsonListener.factorys == None):
            # factorys = new HashMap<Class<?>, JsonableFactory>();
            factorys = {}
        factorys.put(jsonable, factory);

################################## To Json ###############################

class MsgCode(Enum):
    ok = "ok"
    exGeneral = "exGeneral"
    exSemantics = "exSemantics"
    exTransc = "exTransac"

class Port(Enum):
    session = "login.serv"
    r = "r.serv"

class AnsonMsg(Anson):
    # code = MsgCode.ok
    def __init__(self):
        self.port = None

    body = []

    def getClass(self):
        return "io.odysz.anson.AnsonMsg" # self.type;

class AnsonBody(Anson):
    pass

class AnsonReq(AnsonBody):
    def __init__(self):
        self.a = None

class AnsonResp(AnsonBody):
    def __init__(self):
        self.a = None
