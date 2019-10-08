// Generated from JSON.g4 by ANTLR 4.7.2
package gen.antlr.json;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link JSONParser}.
 */
public interface JSONListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link JSONParser#json}.
	 * @param ctx the parse tree
	 */
	void enterJson(JSONParser.JsonContext ctx);
	/**
	 * Exit a parse tree produced by {@link JSONParser#json}.
	 * @param ctx the parse tree
	 */
	void exitJson(JSONParser.JsonContext ctx);
	/**
	 * Enter a parse tree produced by {@link JSONParser#envelope}.
	 * @param ctx the parse tree
	 */
	void enterEnvelope(JSONParser.EnvelopeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JSONParser#envelope}.
	 * @param ctx the parse tree
	 */
	void exitEnvelope(JSONParser.EnvelopeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JSONParser#obj}.
	 * @param ctx the parse tree
	 */
	void enterObj(JSONParser.ObjContext ctx);
	/**
	 * Exit a parse tree produced by {@link JSONParser#obj}.
	 * @param ctx the parse tree
	 */
	void exitObj(JSONParser.ObjContext ctx);
	/**
	 * Enter a parse tree produced by {@link JSONParser#type_pair}.
	 * @param ctx the parse tree
	 */
	void enterType_pair(JSONParser.Type_pairContext ctx);
	/**
	 * Exit a parse tree produced by {@link JSONParser#type_pair}.
	 * @param ctx the parse tree
	 */
	void exitType_pair(JSONParser.Type_pairContext ctx);
	/**
	 * Enter a parse tree produced by {@link JSONParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedName(JSONParser.QualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link JSONParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedName(JSONParser.QualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link JSONParser#pair}.
	 * @param ctx the parse tree
	 */
	void enterPair(JSONParser.PairContext ctx);
	/**
	 * Exit a parse tree produced by {@link JSONParser#pair}.
	 * @param ctx the parse tree
	 */
	void exitPair(JSONParser.PairContext ctx);
	/**
	 * Enter a parse tree produced by {@link JSONParser#propname}.
	 * @param ctx the parse tree
	 */
	void enterPropname(JSONParser.PropnameContext ctx);
	/**
	 * Exit a parse tree produced by {@link JSONParser#propname}.
	 * @param ctx the parse tree
	 */
	void exitPropname(JSONParser.PropnameContext ctx);
	/**
	 * Enter a parse tree produced by {@link JSONParser#array}.
	 * @param ctx the parse tree
	 */
	void enterArray(JSONParser.ArrayContext ctx);
	/**
	 * Exit a parse tree produced by {@link JSONParser#array}.
	 * @param ctx the parse tree
	 */
	void exitArray(JSONParser.ArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link JSONParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(JSONParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link JSONParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(JSONParser.ValueContext ctx);
}