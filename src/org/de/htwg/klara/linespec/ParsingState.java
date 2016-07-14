package org.de.htwg.klara.linespec;

public interface ParsingState {
	/**
	 * Parse a single character. Return new state or
	 * null if invalid character for current state
	 * @param chr	The character to parse
	 * @return	The new state or null if invalid char
	 */
	public ParsingState parse(char chr);
}
