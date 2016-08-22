package org.de.htwg.klara.linespec;

/**
 * State for parsing the upper end of a range. More digits will add to the upper end of the range, semicolon and end of String will finish the parsing of this range.
 * @author mrs
 *
 */
public class HighNumberState implements ParsingState {
	private ParseEventListener listener;
	private int lower;
	private int higher;

	public HighNumberState(int lower, int higher, ParseEventListener listener) {
		this.listener = listener;
		this.lower = lower;
		this.higher = higher;
	}

	@Override
	public ParsingState parse(char chr) {
		if(Character.isDigit(chr)) {
			higher = higher * 10 + chr - '0';
			return this;
		}
		if(chr == ';' || chr == Character.MIN_VALUE) {
			listener.addRange(lower, higher);
			return new EndState(listener);
		}
		return null;
	}
}
