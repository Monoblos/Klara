package org.de.htwg.klara.linespec;

/**
 * Interface for a class that is able to parse a part of a line specification.
 * @author mrs
 *
 */
public interface ParsingState {
	/**
	 * Parse a single character. Return new state or
	 * null if invalid character for current state.
	 * @param chr	The character to parse. Possibly valid characters (depending on state) are: [0-9;,\-\EOF]
	 * @return	The new state or null if invalid char for current state
	 */
	public ParsingState parse(char chr);
}
