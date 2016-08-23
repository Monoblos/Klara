package org.de.htwg.klara;

import java.awt.Point;
import java.util.Random;

/**
 * Test class for loading and transforming
 * @author mrs
 *
 */
public class Foo {
	private static String clasVar = "abc123";
	@SuppressWarnings("unused")
	private final Random r;
	private Point initValue = new Point();
	
	public Foo() {
		r = new Random();
		initValue.x = 3;
		int k = 'k';
		System.out.println("Well than... " + k);
	}
	
	public static void main(String args[]) throws InterruptedException {
		System.out.println("Foo got run. Class loader: " + Foo.class.getClassLoader());

		long counter = 0;
		
		for(int i = 0; i < 10; i++) {
			counter += 2;
		}
		
		@SuppressWarnings("unused")
		Integer k = new Integer(17);
		
		Thread.sleep(200);
		
		if (counter > 2)
			clasVar = "blub";
		
		boolean result = clasVar.equals("a");
		System.out.println("Result of usless compuation: " + result);
		new Foo();
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
