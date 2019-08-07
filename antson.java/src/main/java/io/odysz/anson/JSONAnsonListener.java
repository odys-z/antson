package io.odysz.anson;

import org.antlr.v4.runtime.tree.ParseTree;

import gen.antlr.json.JSONBaseListener;
import gen.antlr.json.JSONListener;
import gen.antlr.json.JSONParser.JsonContext;
import gen.antlr.json.JSONParser.ObjContext;
import gen.antlr.json.JSONParser.PairContext;
import gen.antlr.json.JSONParser.Type_pairContext;
import io.odysz.common.Utils;

public class JSONAnsonListener extends JSONBaseListener implements JSONListener {

	@Override
	public void enterObj(ObjContext ctx) {
		ParseTree f = ctx.getChild(0);
		f.getText();
	}

	@Override
	public void exitObj(ObjContext ctx) {
	}

	@Override
	public void enterType_pair(Type_pairContext ctx) {
		Utils.logi("Type: %s", ctx.getChild(0).getText());
	}

	@Override
	public void enterPair(PairContext ctx) {
		Utils.logi("Property-name: %s", ctx.getChild(0).getText());
		Utils.logi("Property-value: %s", ctx.getChild(2).getText());
	}


}
