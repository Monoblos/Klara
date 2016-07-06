package org.de.htwg.klara;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
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

public class VariableLister extends ClassNode {
	protected ClassVisitor cv = null;
	public VariableLister(int api, ClassVisitor cv) {
		super(api);
		this.cv = cv;
	}
	
	@Override
	public void visitEnd() {
		super.visitEnd();
		
		if (cv != null) {
			transform();
			accept(cv);
		}
		
		System.out.println("Transformer finished.");
		System.out.println();
	}
	
	@SuppressWarnings("unchecked")
	private void transform() {
		for (MethodNode mn : (List<MethodNode>) methods) {
			if ("<init>".equals(mn.name) || "<clinit>".equals(mn.name)) {
				continue;
			}
			if (mn.instructions.size() == 0) {
				continue;
			}
			methodTransform(mn);
			System.out.println();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void methodTransform(MethodNode mn) {
		Map<Integer, LocalVariableNode> currentScope = new HashMap<>();
		List<LocalVariableNode> futureVariables = new LinkedList<>();
		
		for (LocalVariableNode lvn : (List<LocalVariableNode>)mn.localVariables) {
			futureVariables.add(lvn);
		}
		
		InsnList insns = mn.instructions;
		Iterator<AbstractInsnNode> j = insns.iterator();
		AbstractInsnNode lastNewLine;
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
						System.out.println("Reached end of scope of " + tmpNode.name);
						currentScope.remove(scopeKeys[i]);
					}
				}
				for (int i = 0; i < futureVariables.size(); ++i) {
					tmpNode = futureVariables.get(i);
					if (label.equals(tmpNode.start)) {
						System.out.println("Reached start of scope of " + tmpNode.name);
						currentScope.put(tmpNode.index, tmpNode);
						futureVariables.remove(tmpNode);
						--i;
						insns.insert(in, generateVariablePrint(tmpNode));
					}
				}
			} else if (in.getType() == AbstractInsnNode.LINE) {
				LineNumberNode lnn = (LineNumberNode)in;
				insns.insert(in, generatePrint("Now at line " + lnn.line));
				lastNewLine = in;
			}
		}
	}
	
	private static int calculateDescSize(String desc, int access) {
		int paramCount = 0;
		if ((access & Opcodes.ACC_STATIC) == 0)
			paramCount++;
		String params = desc.split("\\)")[0];
		params = params.replaceFirst("\\(", "");
		for (String s : BytecodeUtils.typeListToJava(params)) {
			paramCount++;
			if (s.equals("long") || s.equals("double"))
				paramCount++;
		}
		return paramCount;
	}
	
	private static InsnList generateTimePrint(int variableIndex, String methodName) {
		InsnList il = new InsnList();
		il.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
		il.add(new TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"));
		il.add(new InsnNode(Opcodes.DUP));
		il.add(new LdcInsnNode("Executed method " + methodName + " in "));
		il.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false));
		il.add(new VarInsnNode(Opcodes.LLOAD, variableIndex));
		il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false));
		il.add(new InsnNode(Opcodes.LADD));
		il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false));
		il.add(new LdcInsnNode("ms."));
		il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false));
		il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false));
		il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
		return il;
	}
	
	private static InsnList generateVariablePrint(LocalVariableNode var) {
		boolean isArray = false;
		
		if (var.desc.startsWith("[")) {
			int[] test = new int[4];
			Arrays.toString(test);
			isArray = true;
		}

		String builderMethod = "Ljava/lang/Object;";
		if (var.desc.length() == 1)
			builderMethod = var.desc;
		else if (var.desc.equals("Ljava/lang/String;"))
			builderMethod = "Ljava/lang/String;";

		InsnList il = new InsnList();
		il.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
		il.add(new TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"));
		il.add(new InsnNode(Opcodes.DUP));
		il.add(new LdcInsnNode("Value of variable " + var.name + " now at: "));
		il.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false));
		il.add(load(var));
		if (isArray) {
			
		}
		il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(" + builderMethod + ")Ljava/lang/StringBuilder;", false));
		il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false));
		il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
		return il;
	}
	
	private static InsnList generatePrint(String text) {
		InsnList il = new InsnList();
		il.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
		il.add(new LdcInsnNode(text));
		il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
		return il;
	}
	
	private static VarInsnNode load(LocalVariableNode var) {
		switch (var.desc) {
		case "Z":
		case "C":
		case "B":
		case "S":
		case "I":
			return new VarInsnNode(Opcodes.ILOAD, var.index);
		case "F":
			return new VarInsnNode(Opcodes.FLOAD, var.index);
		case "J":
			return new VarInsnNode(Opcodes.LLOAD, var.index);
		case "D":
			return new VarInsnNode(Opcodes.DLOAD, var.index);

		default:
			return new VarInsnNode(Opcodes.ALOAD, var.index);
		}
	}
}
