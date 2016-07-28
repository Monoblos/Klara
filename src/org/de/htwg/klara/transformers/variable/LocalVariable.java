package org.de.htwg.klara.transformers.variable;

import org.de.htwg.klara.transformers.TransformUtils;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LocalVariableNode;

public class LocalVariable implements AbstractVariable {
	private final LocalVariableNode lvn;
	private VarType type = null;
	
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
	public boolean isArray() {
		return lvn.desc.startsWith("[");
	}

	@Override
	public VarType getType() {
		if (type == null) {
			if (lvn.desc.startsWith("[")) {
				type = VarType.OTHER;
			} else if (lvn.desc.equals("Ljava/lang/String;")) {
				type = VarType.STRING;
			} else if (lvn.desc.equals("C")) {
				type = VarType.CHAR;
			} else {
				type = VarType.OBJECT;
			}
		}
		return type;
	}

	@Override
	public String getDescription() {
		return lvn.desc;
	}

	public LocalVariableNode getNode() {
		return lvn;
	}
}
