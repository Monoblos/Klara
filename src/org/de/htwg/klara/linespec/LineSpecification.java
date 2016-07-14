package org.de.htwg.klara.linespec;

import java.util.HashSet;
import java.util.Set;

public class LineSpecification implements ParseEventListener {
	Set<Integer> includedLines = new HashSet<>();
	public LineSpecification(String spec) {
		parseSpec(spec);
	}
	
	public boolean contains(int line) {
		return includedLines.contains(line);
	}
	
	private final void parseSpec(String spec) {
		int currentPos = 0;
		ParsingState state = new InitialState(this);
		
		while (currentPos < spec.length()) {
			state = state.parse(spec.charAt(currentPos++));
			if(state == null) {
				throw new IllegalArgumentException("Line specification " + spec + " is invalid!");
			}
		}
		state = state.parse(Character.MIN_VALUE);
		if(state == null) {
			throw new IllegalArgumentException("Line specification " + spec + " is invalid!");
		}
	}

	@Override
	public void add(int line) {
		includedLines.add(line);
	}

	@Override
	public void addRange(int start, int end) {
		for(int i = Math.min(start, end); i <= Math.max(start, end); i++) {
			includedLines.add(i);
		}
	}
	
	@Override
	public String toString() {
		return includedLines.toString();
	}
}
