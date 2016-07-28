package org.de.htwg.klara.transformers.variable;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.VarInsnNode;

public class ClassVariable implements AbstractVariable {
	private final String owner;
	private final String name;
	private final String description;
	private final boolean isStatic;
	private VarType type = null;
	
	public ClassVariable(final String owner, final String name, final String description, final boolean isStatic) {
		this.owner = owner;
		this.name = name;
		this.description = description;
		this.isStatic = isStatic;
	}

	@Override
	public InsnList load() {
		InsnList result = new InsnList();
		if (isStatic) {
			result.add(new FieldInsnNode(Opcodes.GETSTATIC, owner, name, description));
		} else {
			result.add(new VarInsnNode(Opcodes.ALOAD, 0));
			result.add(new FieldInsnNode(Opcodes.GETFIELD, owner, name, description));
		}
		return result;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isArray() {
		return description.startsWith("[");
	}

	@Override
	public VarType getType() {
		if (type == null) {
			if (description.startsWith("[")) {
				type = VarType.OTHER;
			} else if (description.equals("Ljava/lang/String;")) {
				type = VarType.STRING;
			} else if (description.equals("C")) {
				type = VarType.CHAR;
			} else {
				type = VarType.OBJECT;
			}
		}
		return type;
	}

	@Override
	public String getDescription() {
		return description;
	}

}
