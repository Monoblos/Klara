package org.de.htwg.klara.transformers.variable;

import org.objectweb.asm.tree.InsnList;

public interface AbstractVariable {
	public InsnList load();
}
