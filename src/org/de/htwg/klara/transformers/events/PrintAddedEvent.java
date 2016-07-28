package org.de.htwg.klara.transformers.events;

import org.objectweb.asm.tree.AbstractInsnNode;

public final class PrintAddedEvent implements TransformationEvent {
	private final AbstractInsnNode node;

	public PrintAddedEvent(AbstractInsnNode node) {
		this.node = node;
	}

	@Override
	public AbstractInsnNode getNode() {
		return node;
	}

	@Override
	public int getType() {
		return PRINT_ADDED;
	}
}