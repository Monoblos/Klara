package org.de.htwg.klara.transformers.variable;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Class variable, static or dynamic.
 * Dynamic variables can only be loaded from non-static methods of the class.
 * @author mrs
 *
 */
public class ClassVariable extends AbstractVariable {
	private final String owner;
	private final String name;
	private final String description;
	private final boolean isStatic;
	
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
	public String getDescription() {
		return description;
	}

}
