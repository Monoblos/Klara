package org.de.htwg.klara.transformers.variable;

import org.de.htwg.klara.utils.TransformUtils;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LocalVariableNode;

/**
 * Local variable of a method. Can be a parameter or just a declared variable within the method.
 * @author mrs
 *
 */
public class LocalVariable extends AbstractVariable {
	private final LocalVariableNode lvn;
	
	public LocalVariable(final LocalVariableNode lvn) {
		this.lvn = lvn;
	}

	@Override
	public InsnList load() {
		InsnList result = new InsnList();
		result.add(TransformUtils.load(lvn));
		return result;
	}

	@Override
	public String getName() {
		return lvn.name;
	}

	@Override
	public String getDescription() {
		return lvn.desc;
	}

	/**
	 * Get the original node this was created from.
	 * @return	The encapsulated node.
	 */
	public LocalVariableNode getNode() {
		return lvn;
	}
}
