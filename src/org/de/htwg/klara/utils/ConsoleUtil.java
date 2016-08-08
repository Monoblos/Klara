package org.de.htwg.klara.utils;

import java.util.Scanner;

import org.de.htwg.klara.linespec.LineSpecification;

/**
 * Set of methods to simplify printing to and reading from the console.
 * @author mrs
 */
public final class ConsoleUtil {
	private static final int DEFAULT_WIDTH = 80;
	private static final int FRONT_SPACES = 4;
	
	private ConsoleUtil() { }

	/**
	 * Asks a simple yes/no question. Question has a (y/n) attached and will be repeated until either a 'y' or a 'n' was entered. 
	 * @param question	The question to ask
	 * @param s	The scanner to use for reading the input from the user.
	 * @return	True if the answer was answered with 'y', false otherwise. 
	 */
	public static boolean ask(final String question, final Scanner s) {
		String choice = "";
		while (!choice.equalsIgnoreCase("y") && !choice.equalsIgnoreCase("n")) {
			System.out.println(question + " (y/n)");
			choice = s.nextLine();
		}
		return choice.equalsIgnoreCase("y");
	}

	/**
	 * Prints text cropped to a fixed with. Default with is {@value #DEFAULT_WIDTH}.
	 * If the text is longer than that, additional lines will have {@value #FRONT_SPACES} spaces in front.<br>
	 * Works like calling {@link #fixwidthPrint(String, int) fixwidthPrint(text, default_width)}
	 * @param text	The text to be printed.
	 */
	public static void fixwidthPrint(final String text) {
		ConsoleUtil.fixwidthPrint(text, DEFAULT_WIDTH);
	}

	/**
	 * Prints text cropped to a fixed with.
	 * If the text is longer than that, additional lines will have {@value #FRONT_SPACES} spaces in front.
	 * @param text	The text to be printed.
	 * @param lineLength	The maximum length of a line.
	 */
	public static void fixwidthPrint(String text, final int lineLength) {
		if (text.length() < lineLength) {
			System.out.println(text);
			return;
		}
		String outBlock = text.substring(0, lineLength);
		text = text.substring(lineLength);
		System.out.println(outBlock);
		while(text.length() > lineLength - FRONT_SPACES) {
			outBlock = text.substring(0, lineLength - FRONT_SPACES);
			text = text.substring(lineLength - FRONT_SPACES);
			for (int i = 0; i < FRONT_SPACES; i++) {
				outBlock = " " + outBlock;
			}
			System.out.println(outBlock);
		}
		for (int i = 0; i < FRONT_SPACES; i++) {
			System.out.print(" ");
		}
		System.out.println(text);
	}

	/**
	 * Prompt and read a line specification from the console. Can also create a wildcard specification if "exit" is entered.
	 * @param s	The scanner to read the user input from.
	 * @return	The Line Specification read.
	 */
	public static LineSpecification readLineSpec(final Scanner s) {
		LineSpecification result = new LineSpecification();
		String input = "";
		while (true) {
			System.out.println("Please enter line specification. Use - to specify ranges. Use , or ; to split blocks. Use \"exit\" to allow all. Example: 5-13,19;60-70;");
			input = s.nextLine();
			if (input.equalsIgnoreCase("exit"))
				break;
			try {
				result = new LineSpecification(input);
				break;
			} catch (IllegalArgumentException e) {
				System.err.println("Invalid format.");
			}
		}
		return result;
	}
}
