package org.de.htwg.klara.transformers;

import org.de.htwg.klara.transformers.events.ScopeReachedEvent;
import org.de.htwg.klara.transformers.events.TransformationEvent;
import org.de.htwg.klara.transformers.events.TransformationEventListener;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class VariableChangePrinter implements TransformationEventListener {
	private static enum VarType {
		OBJECT,
		STRING,
		CHAR,
		OTHER
	}
	private final Transformer trans;
	
	public VariableChangePrinter(Transformer trans) {
		this.trans = trans;
		trans.addListener(this);
	}
	
	@Override
	public void handle(TransformationEvent event) {
		if (event.getType() == TransformationEvent.VAR_INSN) {
			VarInsnNode varIn = (VarInsnNode)event.getNode();
			if (Opcodes.ISTORE <= varIn.getOpcode() && varIn.getOpcode() <= Opcodes.ASTORE) {
				LocalVariableNode lvn = trans.getVar(varIn.var);
				if (lvn != null)
					trans.printLine(varIn, generateVariablePrint(lvn));
			}
		} else if (event.getType() == TransformationEvent.IINC_INSN) {
			IincInsnNode iincIn = (IincInsnNode)event.getNode();
			LocalVariableNode lvn = trans.getVar(iincIn.var);
			if (lvn != null)
				trans.printLine(iincIn, generateVariablePrint(lvn));
		} else if (event.getType() == TransformationEvent.SCOPE_REACHED) {
			ScopeReachedEvent sre = (ScopeReachedEvent)event;
			trans.printLine(sre.getNode(), generateVariablePrint(sre.getVar()));
		}
	}
	
	private InsnList generateVariablePrint(LocalVariableNode var) {
		boolean isArray = false;
		//0 = Object, 1 = String, 2 = Char, 10 = Other
		VarType type = VarType.OBJECT;
		
		if (var.desc.startsWith("[")) {
			isArray = true;
		} else if (var.desc.equals("Ljava/lang/String;")) {
			type = VarType.STRING;
		} else if (var.desc.equals("C")) {
			type = VarType.CHAR;
		}

		String builderMethod = "Ljava/lang/Object;";
		if (var.desc.length() == 1) {
			builderMethod = var.desc;
			type = VarType.OTHER;
		} else if (isArray || type == VarType.STRING)
			builderMethod = "Ljava/lang/String;";

		InsnList il = new InsnList();
		il.add(new LdcInsnNode("New value of " + var.name + ": "));
		il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
		if (type == VarType.STRING || type == VarType.CHAR) {
			if (type == VarType.STRING)
				il.add(new LdcInsnNode("\""));
			else if (type == VarType.CHAR)
				il.add(new LdcInsnNode("'"));
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
		}
		il.add(TransformUtils.load(var));
		if (isArray) {
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Arrays", "deepToString", "([Ljava/lang/Object;)Ljava/lang/String;", false));
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
			il.add(new LdcInsnNode(" ("));
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
			il.add(TransformUtils.load(var));
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false));
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false));
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
			il.add(new LdcInsnNode("@"));
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
			il.add(TransformUtils.load(var));
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false));
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toHexString", "(I)Ljava/lang/String;", false));
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
			il.add(new LdcInsnNode(")"));
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
		}
		return il;
	}
}
