package org.de.htwg.klara;

public class Foo {
	public static void main(String args[]) {
		System.out.println("Foo got run. Class loader: " + Foo.class.getClassLoader());

		String x = "bla";
		long counter = 0;
		
		for(int i = 0; i < 10; i++) {
			counter += 2;
		}
		
		if (counter > 2)
			x = "blub";
		
		boolean result = x.equals("a");
		System.out.println("Result of usless compuation: " + result);
	}
	
	protected void test(int a, int b, double x) {
		
	}
}
