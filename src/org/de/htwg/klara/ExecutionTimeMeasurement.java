package org.de.htwg.klara;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ExecutionTimeMeasurement extends MethodVisitor {
	private static class Dummy extends ClassVisitor {
		public Dummy(int api, ClassVisitor cv) {
			super(api, cv);
		}
		
		private boolean isInterface;
		
		@Override
		public void visit(int version, int access, String name,
				String signature, String superName, String[] interfaces) {
			super.visit(version, access, name, signature, superName, interfaces);
			isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
		}
		
		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {
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

			MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
			if (mv != null && !isInterface && !name.equals("<init>"))
				return new ExecutionTimeMeasurement(api, mv, paramCount, name);
			return mv;
		}
	}
	
	public static Class<? extends ClassVisitor> getClassVisitorForThis() {
		return Dummy.class;
	}
	
	private int paramCount;
	private String methodName;
	
	public ExecutionTimeMeasurement(int api, MethodVisitor mv, int paramCount, String methodName) {
		super(api, mv);
		this.paramCount = paramCount;
		this.methodName = methodName;
	}
	
	@Override
	public void visitCode() {
		super.visitCode();
		mv.visitInsn(Opcodes.LCONST_0);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
		mv.visitInsn(Opcodes.LSUB);
		mv.visitVarInsn(Opcodes.LSTORE, paramCount);
	}
	
	@Override
	public void visitIincInsn(int var, int increment) {
		if (var >= paramCount)
			var += 2;
		
		super.visitIincInsn(var, increment);
	}
	
	@Override
	public void visitVarInsn(int opcode, int var) {
		if (var >= paramCount)
			var += 2;

		super.visitVarInsn(opcode, var);
	}
	
	@Override
	public void visitInsn(int opcode) {
		if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW) {
			// Adds the printing of the execution time before any return or throw.
			// Line added is:
			// System.out.println("Executed method <methodName> in " + (startTime - System.currentTimeMillis()) + "ms.");
			
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
			mv.visitInsn(Opcodes.DUP);
			mv.visitLdcInsn("Executed method " + methodName + " in ");
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitVarInsn(Opcodes.LLOAD, paramCount);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
			mv.visitInsn(Opcodes.LADD);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false);
			mv.visitLdcInsn("ms.");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
		}
		super.visitInsn(opcode);
	}
	
	@Override
	public void visitEnd() {
		super.visitEnd();
	}
}
