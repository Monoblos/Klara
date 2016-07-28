package org.de.htwg.klara.transformers.events;

import org.objectweb.asm.tree.IincInsnNode;

public final class IincInsnEvent implements TransformationEvent {
	private final IincInsnNode node;

	public IincInsnEvent(final IincInsnNode node) {
		this.node = node;
	}

	@Override
	public IincInsnNode getNode() {
		return node;
	}

	@Override
	public int getType() {
		return IINC_INSN;
	}
}
