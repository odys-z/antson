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
	
	@Test
	void testBlocks() {
		assertEquals(0, blocks(0, 1));
		assertEquals(1, blocks(1, 1));
		assertEquals(1, blocks(1, 2));
		assertEquals(1, blocks(2, 2));
		assertEquals(2, blocks(3, 2));
		assertEquals(2, blocks(4, 2));
		assertEquals(3, blocks(5, 2));

		assertEquals(0, blocks(0, 10));
	}

}
