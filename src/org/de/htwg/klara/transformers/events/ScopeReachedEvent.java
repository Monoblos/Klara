package org.de.htwg.klara.transformers.events;

import org.de.htwg.klara.transformers.variable.LocalVariable;
import org.objectweb.asm.tree.LabelNode;

public final class ScopeReachedEvent implements TransformationEvent {
	private final LocalVariable var;
	private final LabelNode node;

	public ScopeReachedEvent(final LabelNode node, final LocalVariable var) {
		this.var = var;
		this.node = node;
	}

	@Override
	public LabelNode getNode() {
		return node;
	}

	public LocalVariable getVar() {
		return var;
	}

	@Override
	public int getType() {
		return SCOPE_REACHED;
	}

}
