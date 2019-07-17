//package io.odysz.antson;
//
//
//import org.junit.Before;
//import org.junit.Test;
//
//import com.google.gson.Gson;
//
//class GsonTest {
//
//	private Gson gson;
//
//	@Before
//	void setUp() throws Exception {
//		gson = new Gson();
//		Utils.printCaller(false);
//	}
//
//	@Test
//	void test() {
//		String s = gson.toJson(new AsonG2());
//		Utils.logi(s);
//
//		s = gson.toJson(new Bag());
//		Utils.logi(s);
//		
//		Bag bag = gson.fromJson(s, Bag.class);
//		Utils.logi(bag.toString());
//	}
//
//	class AsonG1 extends Ason {
//		String g1;
//
//		AsonG1(String g1){
//			this.g1 = g1;
//		}
//	}
//	
//	class AsonG2 extends AsonG1 {
//		String g2;
//
//		AsonG2() {
//			super("g2");
//			g2 = "G2";
//		}
//	}
//	
//	class Bag { 
//		AsonG2 g2;
//		String bag;
//		Bag() {
//			g2 = new AsonG2();
//			bag = "HHH bag";
//		}
//		
//		@Override
//		public String toString() {
//			return String.format("Bag: g2.g1 %s, g2.g2 %s, bag = %s", g2.g1, g2.g2, bag);
//		}
//	}
//	
//}
