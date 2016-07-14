package org.de.htwg.klara.linespec;

public class RangeState implements ParsingState {
	private ParseEventListener listener;
	private int lower;

	public RangeState(int lower, ParseEventListener listener) {
		this.listener = listener;
		this.lower = lower;
	}

	@Override
	public ParsingState parse(char chr) {
		if(Character.isDigit(chr)) {
			return new HighNumberState(lower, chr - '0', listener);
		}
		return null;
	}
}
