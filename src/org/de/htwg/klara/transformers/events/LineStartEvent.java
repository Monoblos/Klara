package org.de.htwg.klara.transformers.events;

import org.objectweb.asm.tree.LineNumberNode;

/**
 * Event identifying that a new line started
 * @author mrs
 *
 */
public final class LineStartEvent implements TransformationEvent {
	private final LineNumberNode node;

	public LineStartEvent(final LineNumberNode node) {
		this.node = node;
	}

	@Override
	public LineNumberNode getNode() {
		return node;
	}

	@Override
	public int getType() {
		return LINE_START;
	}
}
