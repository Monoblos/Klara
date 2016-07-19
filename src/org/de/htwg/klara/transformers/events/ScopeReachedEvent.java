package org.de.htwg.klara.transformers.events;

import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;

public class ScopeReachedEvent implements TransformationEvent {
	private final LocalVariableNode var;
	private final LabelNode node;

	public ScopeReachedEvent(final LabelNode node, final LocalVariableNode var) {
		this.var = var;
		this.node = node;
	}

	@Override
	public LabelNode getNode() {
		return node;
	}

	public LocalVariableNode getVar() {
		return var;
	}

	@Override
	public int getType() {
		return SCOPE_REACHED;
	}

}
