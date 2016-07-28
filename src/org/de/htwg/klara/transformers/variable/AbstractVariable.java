package org.de.htwg.klara.transformers.variable;

import org.objectweb.asm.tree.InsnList;

public interface AbstractVariable {
	public static enum VarType {
		OBJECT,
		STRING,
		CHAR,
		OTHER
	}
	
	public InsnList load();
	public String getName();
	public boolean isArray();
	public VarType getType();
	public String getDescription();
}
