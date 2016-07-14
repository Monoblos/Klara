package org.de.htwg.klara.linespec;

import java.util.Scanner;

public class ParsingTester {

	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);
		String read = null;
		System.out.println("Please enter line-spec ");
		while (!(read = s.nextLine()).equals("exit")) {
			System.out.println("Got " + read);
			System.out.println(new LineSpecification(read));
			System.out.println("Please enter line-spec ");
		}
		s.close();
	}

}
