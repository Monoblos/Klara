package org.de.htwg.klara.linespec;

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
