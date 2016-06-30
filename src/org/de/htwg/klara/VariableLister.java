package org.de.htwg.klara;

import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

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
		}
	}
	
	@SuppressWarnings("unchecked")
	private void methodTransform(MethodNode mn) {
		int paramCount = 0;
		if ((access & Opcodes.ACC_STATIC) == 0)
			paramCount++;
		String params = mn.desc.split("\\)")[0];
		params = params.replaceFirst("\\(", "");
		for (String s : BytecodeUtils.typeListToJava(params)) {
			paramCount++;
			if (s.equals("long") || s.equals("double"))
				paramCount++;
		}
		
		System.out.println("Local variables:");
		for (LocalVariableNode lvn : (List<LocalVariableNode>)mn.localVariables) {
			System.out.println("Found local variable " + lvn.name + " of type " + BytecodeUtils.typeToJava(lvn.desc));
		}
		System.out.println();
		
		InsnList insns = mn.instructions;
		Iterator<AbstractInsnNode> j = insns.iterator();
		while (j.hasNext()) {
			AbstractInsnNode in = j.next();
			int opcode = in.getOpcode();
			if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW) {
				InsnList il = new InsnList();
				il.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
				il.add(new TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"));
				il.add(new InsnNode(Opcodes.DUP));
				il.add(new LdcInsnNode("Executed method " + mn.name + " in "));
				il.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false));
				il.add(new VarInsnNode(Opcodes.LLOAD, paramCount));
				il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false));
				il.add(new InsnNode(Opcodes.LADD));
				il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false));
				il.add(new LdcInsnNode("ms."));
				il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false));
				il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false));
				il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
				insns.insert(in.getPrevious(), il);
			} else if (in.getType() == AbstractInsnNode.VAR_INSN) {
				VarInsnNode varIn = (VarInsnNode)in;
				if (varIn.var >= paramCount)
					varIn.var += 2;
			} else if (in.getType() == AbstractInsnNode.IINC_INSN) {
				IincInsnNode iincIn = (IincInsnNode)in;
				if (iincIn.var >= paramCount)
					iincIn.var += 2;
			}
		}
		InsnList il = new InsnList();
		il.add(new InsnNode(Opcodes.LCONST_0));
		il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false));
		il.add(new InsnNode(Opcodes.LSUB));
		il.add(new VarInsnNode(Opcodes.LSTORE, paramCount));
		insns.insert(il);
	}
}
