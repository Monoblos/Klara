package org.de.htwg.klara;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class BytcodeInstructionPrinter extends MethodVisitor {
	private static class Dummy extends ClassVisitor {
		public Dummy(int api, ClassVisitor cv) {
			super(api, cv);
		}
		
		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {
			String returnVal = desc.split("\\)")[1];
			returnVal = BytecodeUtils.typeToJava(returnVal);
			String params = desc.split("\\)")[0];
			params = params.replaceFirst("\\(", "");
			String[] paramList = BytecodeUtils.typeListToJava(params);
			
			StringBuilder sb = new StringBuilder(BytecodeUtils.accessBytesToString(access));
			sb.append(returnVal).append(" ").append(name).append("(");
			for (int i = 0; i < paramList.length; i++) {
				if (i != 0)
					sb.append(", ");
				sb.append(paramList[i]);
			}
			sb.append(")");
			
			System.out.print(sb.toString());
			return new BytcodeInstructionPrinter(api, super.visitMethod(access, name, desc, signature, exceptions));
		}
	}
	
	public static Class<? extends ClassVisitor> getVisitorForThis() {
		return Dummy.class;
	}
	
	public BytcodeInstructionPrinter(int api, MethodVisitor mv) {
		super(api, mv);
	}
	
	@Override
	public void visitCode() {
		super.visitCode();
		System.out.println();
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {
		super.visitFieldInsn(opcode, owner, name, desc);
		System.out.println(BytecodeUtils.opcodeToString(opcode) + " " + owner + " " + name + " " + desc);
	}
	
	@Override
	public void visitIincInsn(int var, int increment) {
		super.visitIincInsn(var, increment);
		System.out.println("INC " + var + " " + increment);
	}
	
	@Override
	public void visitInsn(int opcode) {
		super.visitInsn(opcode);
		System.out.println(BytecodeUtils.opcodeToString(opcode));
	}
	
	@Override
	public void visitIntInsn(int opcode, int operand) {
		super.visitIntInsn(opcode, operand);
		System.out.println(BytecodeUtils.opcodeToString(opcode) + " " + operand);
	}
	
	@Override
	public void visitJumpInsn(int opcode, Label label) {
		super.visitJumpInsn(opcode, label);
		System.out.println(BytecodeUtils.opcodeToString(opcode) + " " + label);
	}
	
	@Override
	public void visitLabel(Label label) {
		super.visitLabel(label);
		System.out.print(label + " ");
	}
	
	@Override
	public void visitLdcInsn(Object cst) {
		super.visitLdcInsn(cst);
		if (cst instanceof String)
			cst = "\"" + cst + "\"";
		System.out.println("LDC " + cst);
	}
	
	@Override
	public void visitLineNumber(int line, Label start) {
		// TODO Auto-generated method stub
		super.visitLineNumber(line, start);
		System.out.print("Z" + line + " ");
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {
		super.visitMethodInsn(opcode, owner, name, desc, itf);
	    System.out.println(BytecodeUtils.opcodeToString(opcode) + " " + owner + " " + name + desc);
	}
	
	@Override
	public void visitVarInsn(int opcode, int var) {
		super.visitVarInsn(opcode, var);
	    System.out.println(BytecodeUtils.opcodeToString(opcode) + " " + var);
	}
	
	@Override
	public void visitTypeInsn(int opcode, String type) {
		super.visitTypeInsn(opcode, type);
		System.out.println(BytecodeUtils.opcodeToString(opcode) + " " + type);
	}
	
	@Override
	public void visitEnd() {
		super.visitEnd();
		System.out.println("}");
		System.out.println();
	}
}
