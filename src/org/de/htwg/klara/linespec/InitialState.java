package org.de.htwg.klara.linespec;

/**
 * The initial state for Parsing a line-specification. Only digits are valid in this state.
 * @author mrs
 *
 */
public class InitialState implements ParsingState {
	private ParseEventListener listener;
	
	public InitialState(ParseEventListener listener) {
		this.listener = listener;
	}

	@Override
	public ParsingState parse(char chr) {
		if(Character.isDigit(chr)) {
			return new NumberState(chr - '0', listener);
		}
		return null;
	}

}
