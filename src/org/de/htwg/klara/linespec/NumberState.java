package org.de.htwg.klara.linespec;

/**
 * State for parsing a single number or the lower number of a range. Semicolon or end of string will add it as a single line. Minus will switch to the range-state.
 * @author mrs
 *
 */
public class NumberState implements ParsingState {
	private ParseEventListener listener;
	private int number;
	
	public NumberState(int number, ParseEventListener listener) {
		this.listener = listener;
		this.number = number;
	}
	
	@Override
	public ParsingState parse(char chr) {
		if(Character.isDigit(chr)) {
			number = number * 10 + chr - '0';
			return this;
		}
		if (chr == ';' || chr == ',' || chr == Character.MIN_VALUE) {
			listener.add(number);
			return new EndState(listener);
		}
		if (chr == '-') {
			return new RangeState(number, listener);
		}
		return null;
	}
}
