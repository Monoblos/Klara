package org.de.htwg.klara.linespec;

public class EndState implements ParsingState {
	private ParseEventListener listener;
	
	public EndState(ParseEventListener listener) {
		this.listener = listener;
	}

	@Override
	public ParsingState parse(char chr) {
		if(chr == ';' || chr == Character.MIN_VALUE) {
			return this;
		}
		if(Character.isDigit(chr)) {
			return new NumberState(chr - '0', listener);
		}
		return null;
	}

}
