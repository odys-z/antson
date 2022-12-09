package io.odysz.common;

import static org.junit.jupiter.api.Assertions.*;
import static io.odysz.common.CheapMath.*;

import org.junit.jupiter.api.Test;

class CheapMathTest {

	@Test
	void testReduceFract() {
		int[] uv = reduceFract( 1024, 768 );

		assertEquals(3, uv[0]);
		assertEquals(4, uv[1]);
		
		uv = reduceFract( 1920, 1080 );

		assertEquals(9, uv[0]);
		assertEquals(16, uv[1]);
	}

}
