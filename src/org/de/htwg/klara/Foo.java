package org.de.htwg.klara;

public class Foo {
	public static void main(String args[]) throws InterruptedException {
		System.out.println("Foo got run. Class loader: " + Foo.class.getClassLoader());

		String x = "bla";
		long counter = 0;
		
		for(int i = 0; i < 10; i++) {
			counter += 2;
		}
		
		Thread.sleep(200);
		
		if (counter > 2)
			x = "blub";
		
		boolean result = x.equals("a");
		System.out.println("Result of usless compuation: " + result);
	}
	
	protected boolean test(int a, int b, double x) {
		int c = (a << 2) + b;
		if (c < 20)
			return false;
		
		if (Math.pow(x, 3) < 123)
			return false;
		
		return true;
	}
}
