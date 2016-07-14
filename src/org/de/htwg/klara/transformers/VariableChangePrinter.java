package org.de.htwg.klara.transformers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class VariableChangePrinter extends Transformer {
	private int currentLine = 0;
	
	public VariableChangePrinter(int api, ClassVisitor cv) {
		super(api, cv);
	}
	
	@SuppressWarnings("unchecked")
	protected void methodTransform(MethodNode mn) {
		Map<Integer, LocalVariableNode> currentScope = new HashMap<>();
		List<LocalVariableNode> futureVariables = new LinkedList<>();
		
		for (LocalVariableNode lvn : (List<LocalVariableNode>)mn.localVariables) {
			futureVariables.add(lvn);
		}
		
		InsnList insns = mn.instructions;
		Iterator<AbstractInsnNode> j = insns.iterator();
		while (j.hasNext()) {
			AbstractInsnNode in = j.next();
			if (in.getType() == AbstractInsnNode.VAR_INSN) {
				VarInsnNode varIn = (VarInsnNode)in;
				if (Opcodes.ISTORE <= varIn.getOpcode() && varIn.getOpcode() <= Opcodes.ASTORE) {
					LocalVariableNode lvn = currentScope.get(varIn.var);
					if (lvn != null)
						insns.insert(in, generateVariablePrint(lvn));
				}
			} else if (in.getType() == AbstractInsnNode.IINC_INSN) {
				IincInsnNode iincIn = (IincInsnNode)in;
				LocalVariableNode lvn = currentScope.get(iincIn.var);
				if (lvn != null)
					insns.insert(in, generateVariablePrint(lvn));
			} else if (in.getType() == AbstractInsnNode.LABEL) {
				LabelNode label = (LabelNode)in;
				LocalVariableNode tmpNode;
				Integer[] scopeKeys = currentScope.keySet().toArray(new Integer[0]);
				for (int i = 0; i < scopeKeys.length; ++i) {
					tmpNode = currentScope.get(scopeKeys[i]);
					if (label.equals(tmpNode.end)) {
						currentScope.remove(scopeKeys[i]);
					}
				}
				for (int i = 0; i < futureVariables.size(); ++i) {
					tmpNode = futureVariables.get(i);
					if (label.equals(tmpNode.start)) {
						currentScope.put(tmpNode.index, tmpNode);
						futureVariables.remove(tmpNode);
						--i;
						insns.insert(in, generateVariablePrint(tmpNode));
					}
				}
			} else if (in.getType() == AbstractInsnNode.LINE) {
				LineNumberNode lnn = (LineNumberNode)in;
				currentLine = lnn.line;
			}
		}
	}
	
	private InsnList generateVariablePrint(LocalVariableNode var) {
		boolean isArray = false;
		
		if (var.desc.startsWith("[")) {
			int[] test = new int[4];
			Arrays.toString(test);
			isArray = true;
		}

		String builderMethod = "Ljava/lang/Object;";
		if (var.desc.length() == 1)
			builderMethod = var.desc;
		else if (isArray || var.desc.equals("Ljava/lang/String;"))
			builderMethod = "Ljava/lang/String;";

		InsnList il = new InsnList();
		il.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
		il.add(new TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"));
		il.add(new InsnNode(Opcodes.DUP));
		il.add(new LdcInsnNode(TransformUtils.formatLocation(getClassName(), currentLine) + ": Value of variable " + var.name + " now at: "));
		il.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false));
		il.add(TransformUtils.load(var));
		if (isArray) {
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Arrays", "deepToString", "([Ljava/lang/Object;)Ljava/lang/String;", false));
		}
		il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(" + builderMethod + ")Ljava/lang/StringBuilder;", false));
		il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false));
		il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
		return il;
	}

}
