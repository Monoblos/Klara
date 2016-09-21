package org.de.htwg.klara.transformers;

import java.awt.Point;

import org.de.htwg.klara.transformers.events.ScopeReachedEvent;
import org.de.htwg.klara.transformers.events.TransformationEvent;
import org.de.htwg.klara.transformers.events.TransformationEventListener;
import org.de.htwg.klara.transformers.variable.AbstractVariable;
import org.de.htwg.klara.transformers.variable.AbstractVariable.VarType;
import org.de.htwg.klara.transformers.variable.ClassVariable;
import org.de.htwg.klara.transformers.variable.LocalVariable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * The variable change printer will print the new value of a variable whenever it is changed or a new is created.
 * Does not track changes of values within Objects, like the x value of a {@link Point}
 * @author mrs
 *
 */
public final class VariableChangePrinter implements TransformationEventListener {
	private final Transformer trans;
	
	public VariableChangePrinter(final Transformer trans) {
		this.trans = trans;
		trans.addListener(this);
	}
	
	@Override
	public void handle(TransformationEvent event) {
		if (event.getType() == TransformationEvent.VAR_INSN) {
			VarInsnNode varIn = (VarInsnNode)event.getNode();
			if (Opcodes.ISTORE <= varIn.getOpcode() && varIn.getOpcode() <= Opcodes.ASTORE) {
				LocalVariable lv = trans.getVar(varIn.var);
				if (lv != null)
					trans.printLine(varIn, generateVariablePrint(lv));
			}
		} else if (event.getType() == TransformationEvent.IINC_INSN) {
			IincInsnNode iincIn = (IincInsnNode)event.getNode();
			LocalVariable lv = trans.getVar(iincIn.var);
			if (lv != null)
				trans.printLine(iincIn, generateVariablePrint(lv));
		} else if (event.getType() == TransformationEvent.SCOPE_REACHED) {
			ScopeReachedEvent sre = (ScopeReachedEvent)event;
			trans.printLine(sre.getNode(), generateVariablePrint(sre.getVar()));
		} else if (event.getType() == TransformationEvent.CLS_VAR_CHANGED) {
			FieldInsnNode node = (FieldInsnNode)event.getNode();
			ClassVariable cv = new ClassVariable(node.owner, node.name, node.desc, node.getOpcode() == Opcodes.PUTSTATIC);
			trans.printLine(node, generateVariablePrint(cv));
		}
	}
	
	/**
	 * Builds the instruction list to generate a text to display a variable value. Expects a {@link StringBuilder} on the stack.
	 * @param var	The variable to generate the text for.
	 * @return	The instructions to append it to a {@link StringBuilder}
	 */
	private InsnList generateVariablePrint(AbstractVariable var) {
		VarType type = var.getType();

		String builderMethod = "Ljava/lang/Object;";
		if (var.isPrimitive()) {
			builderMethod = var.getDescription();
		} else if (var.isArray() || type == VarType.STRING)
			builderMethod = "Ljava/lang/String;";

		InsnList il = new InsnList();
		il.add(new LdcInsnNode("New value of " + var.getName() + ": "));
		il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
		if (type == VarType.STRING || type == VarType.CHAR) {
			if (type == VarType.STRING)
				il.add(new LdcInsnNode("\""));
			else if (type == VarType.CHAR)
				il.add(new LdcInsnNode("'"));
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
		}
		il.add(var.load());
		if (var.isArray()) {
			if (var.getArrayType() == VarType.OBJECT || var.getArrayType() == VarType.STRING)
				il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Arrays", "deepToString", "([Ljava/lang/Object;)Ljava/lang/String;", false));
			else
				il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Arrays", "toString", "(" + var.getDescription() + ")Ljava/lang/String;", false));
		}
		il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(" + builderMethod + ")Ljava/lang/StringBuilder;", false));
		if (type == VarType.STRING || type == VarType.CHAR) {
			if (type == VarType.STRING)
				il.add(new LdcInsnNode("\""));
			else if (type == VarType.CHAR)
				il.add(new LdcInsnNode("'"));
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
		}
		if (type == VarType.OBJECT) {
			//Add the typical Class@Hash
			LabelNode jumpNode = new LabelNode();
			il.add(var.load());
			il.add(new JumpInsnNode(Opcodes.IFNULL, jumpNode));
			il.add(new LdcInsnNode(" ("));
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
			il.add(var.load());
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false));
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false));
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
			il.add(new LdcInsnNode("@"));
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
			il.add(var.load());
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false));
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toHexString", "(I)Ljava/lang/String;", false));
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
			il.add(new LdcInsnNode(")"));
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
			il.add(jumpNode);
		}
		return il;
	}
}
