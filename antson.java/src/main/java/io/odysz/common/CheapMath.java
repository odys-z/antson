package io.odysz.common;

public class CheapMath {
	
	/**
	 * (gcd * n) / (gcd * m) =&gt; n / m,
	 * where gcd (n, m) = 1
	 * @param x n * gcd
	 * @param y m * gcd
	 * @return n / m, 
	 * @since 0.9.48 n, m order is the same as x, y
	 */
	public static int[] reduceFract(int x, int y) {
		int g = gcd(x, y);
		return g == 0
			? new int[] {x, y}
			: new int[] { x / g, y / g };
	}

	public static int gcd (int a, int b) {
		while(b != 0)  
		{  
			if(a > b)  
				a = a - b;  
			else  
				b = b - a;  
		}  
		return a;  
	}
	
	/**
	 * Get total pages / blocks.
	 * @param vol
	 * @param size
	 * @return 1 + (int)(v - 1) / size;
	 */
	public static int blocks(int vol, int size) {
		return vol <= 0 || size <= 0 ? 0 : 1 + (int)(vol - 1) / size;
	}
}
