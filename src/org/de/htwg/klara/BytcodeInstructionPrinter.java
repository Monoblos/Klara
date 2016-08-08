package org.de.htwg.klara;

import org.de.htwg.klara.utils.BytecodeUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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
			sb.append(") {");
			
			System.out.println(sb.toString());
			return new BytcodeInstructionPrinter(api, super.visitMethod(access, name, desc, signature, exceptions));
		}
	}
	
	public static Class<? extends ClassVisitor> getClassVisitorForThis() {
		return Dummy.class;
	}
	
	private boolean ongoingLine = false;
	
	public BytcodeInstructionPrinter(int api, MethodVisitor mv) {
		super(api, mv);
	}
	
	private void pushLine(String line) {
		StringBuilder sb = new StringBuilder("");
		if (!ongoingLine)
			sb.append("     ");
		sb.append(line);
		System.out.print(sb);
		
		System.out.println(" (" + Thread.currentThread().getStackTrace()[2].getMethodName() + ")");
		
		ongoingLine = false;
	}
	
	private void pushLineNumber(int lineNumber) {
		StringBuilder sb = new StringBuilder();
		sb.append(lineNumber);
		if (lineNumber < 10)
			sb.append(" ");
		if (lineNumber < 100)
			sb.append(" ");
		if (lineNumber < 1000)
			sb.append(" ");
		sb.append(" ");
		System.out.print(sb);
		ongoingLine =  true;
	}
	
	@Override
	public void visitCode() {
		super.visitCode();
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {
		super.visitFieldInsn(opcode, owner, name, desc);
		pushLine(BytecodeUtils.opcodeToString(opcode) + " " + owner + " " + name + " " + desc);
	}
	
	@Override
	public void visitIincInsn(int var, int increment) {
		super.visitIincInsn(var, increment);
		pushLine("IINC " + var + " " + increment);
	}
	
	@Override
	public void visitInsn(int opcode) {
		super.visitInsn(opcode);
		pushLine(BytecodeUtils.opcodeToString(opcode));
	}
	
	@Override
	public void visitIntInsn(int opcode, int operand) {
		super.visitIntInsn(opcode, operand);
		if (opcode != Opcodes.NEWARRAY)
			pushLine(BytecodeUtils.opcodeToString(opcode) + " " + operand);
		else
			pushLine(BytecodeUtils.opcodeToString(opcode) + " " + BytecodeUtils.opcodeToType(operand));
	}
	
	@Override
	public void visitJumpInsn(int opcode, Label label) {
		super.visitJumpInsn(opcode, label);
		pushLine(BytecodeUtils.opcodeToString(opcode) + " " + label);
	}
	
	@Override
	public void visitLabel(Label label) {
		super.visitLabel(label);
		System.out.println(label + ":");
	}
	
	@Override
	public void visitLdcInsn(Object cst) {
		super.visitLdcInsn(cst);
		if (cst instanceof String)
			cst = "\"" + cst + "\"";
		pushLine("LDC " + cst);
	}
	
	@Override
	public void visitLineNumber(int line, Label start) {
		// TODO Auto-generated method stub
		super.visitLineNumber(line, start);
		pushLineNumber(line);
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {
		super.visitMethodInsn(opcode, owner, name, desc, itf);
		pushLine(BytecodeUtils.opcodeToString(opcode) + " " + owner + " " + name + desc);
	}
	
	@Override
	public void visitVarInsn(int opcode, int var) {
		super.visitVarInsn(opcode, var);
		pushLine(BytecodeUtils.opcodeToString(opcode) + " " + var);
	}
	
	@Override
	public void visitTypeInsn(int opcode, String type) {
		super.visitTypeInsn(opcode, type);
		pushLine(BytecodeUtils.opcodeToString(opcode) + " " + type);
	}
	
	@Override
	public void visitEnd() {
		super.visitEnd();
		System.out.println("}");
		System.out.println();
	}
}
