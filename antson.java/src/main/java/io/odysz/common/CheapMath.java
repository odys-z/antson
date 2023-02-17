package io.odysz.common;

public class CheapMath {
	
	/**
	 * (gcd * n) / (gcd * m) =&gt; n / m,
	 * where gcd (n, m) = 1
	 * @param x n * gcd
	 * @param y m * gcd
	 * @return n / m
	 */
	public static int[] reduceFract(int x, int y) {
		int g = gcd(x, y);
		if (x > y)
			return new int[] { y / g, x / g };
		else
			return new int[] { x / g, y / g };
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
}
