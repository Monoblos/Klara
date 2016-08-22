package org.de.htwg.klara.linespec;

/**
 * Interface for a class that can receive events from a {@linkplain ParsingState}
 * @author mrs
 *
 */
public interface ParseEventListener {
	/**
	 * Add a single line to list of matching lines.
	 * @param line	The line to add
	 */
	public void add(int line);
	/**
	 * Add a range of lines to the list of matching lines.
	 * @param start	The start of the range to add
	 * @param end	The end of the range to add
	 */
	public void addRange(int start, int end);
}
