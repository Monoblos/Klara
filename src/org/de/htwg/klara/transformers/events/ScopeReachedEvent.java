package org.de.htwg.klara.transformers.events;

import org.de.htwg.klara.transformers.variable.LocalVariable;
import org.objectweb.asm.tree.LabelNode;

/**
 * Event identifying that the scope of a variable was reached.
 * The node returned by {@link #getNode()} is the label that marks the beginning of the variable scope.
 * @author mrs
 *
 */
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

	/**
	 * Get the variable of which the scope was reached
	 * @return	The variable of which the scope was reached
	 */
	public LocalVariable getVar() {
		return var;
	}

	@Override
	public int getType() {
		return SCOPE_REACHED;
	}

}
