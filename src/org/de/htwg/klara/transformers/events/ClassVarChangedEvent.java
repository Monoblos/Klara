package org.de.htwg.klara.transformers.events;

import org.objectweb.asm.tree.FieldInsnNode;

/**
 * Event identifying that a class variable was assigned.
 * @author mrs
 *
 */
public class ClassVarChangedEvent implements TransformationEvent {
	private final FieldInsnNode node;

	public ClassVarChangedEvent(final FieldInsnNode node) {
		this.node = node;
	}

	@Override
	public FieldInsnNode getNode() {
		return node;
	}

	@Override
	public int getType() {
		return CLS_VAR_CHANGED;
	}

}
