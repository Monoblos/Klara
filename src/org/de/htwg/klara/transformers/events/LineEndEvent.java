package org.de.htwg.klara.transformers.events;

import org.objectweb.asm.tree.LineNumberNode;

public final class LineEndEvent implements TransformationEvent {
	private final LineNumberNode node;

	public LineEndEvent(final LineNumberNode node) {
		this.node = node;
	}

	@Override
	public LineNumberNode getNode() {
		return node;
	}

	@Override
	public int getType() {
		return LINE_END;
	}

}
