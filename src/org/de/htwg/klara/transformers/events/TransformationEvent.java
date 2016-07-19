package org.de.htwg.klara.transformers.events;

import org.objectweb.asm.tree.AbstractInsnNode;

public interface TransformationEvent {
	public static final int VAR_INSN = 1;
	public static final int IINC_INSN = 2;
	public static final int SCOPE_REACHED = 3;
	public static final int SCOPE_ENDED = 4;
	
	public AbstractInsnNode getNode();
	public int getType();
}
