package org.de.htwg.klara.transformers.events;

import org.objectweb.asm.tree.VarInsnNode;

/**
 * Event identifying that a local variable was assigned.
 * @author mrs
 *
 */
public final class VarInsnEvent implements TransformationEvent {
	private final VarInsnNode node;

	public VarInsnEvent(final VarInsnNode node) {
		this.node = node;
	}

	@Override
	public VarInsnNode getNode() {
		return node;
	}

	@Override
	public int getType() {
		return VAR_INSN;
	}
}
