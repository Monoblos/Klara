package org.de.htwg.klara.linespec;

public interface ParseEventListener {
	public void add(int line);
	public void addRange(int start, int end);
}
