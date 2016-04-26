package org.de.htwg.klara;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class Decompiler extends ClassVisitor {
	private static class MethodDecompiler extends MethodVisitor {
		private String indent = "";
		private int methodParams;

		public MethodDecompiler(int api, MethodVisitor mv, int indent, int methodParams) {
			super(api, mv);
			while (indent-- > 0)
				this.indent += "    ";
			this.methodParams = methodParams;
			if (methodParams == 0)
				System.out.println(") {");
		}
		
		@Override
		public void visitParameter(String name, int access) {
			super.visitParameter(name, access);
			System.out.println("Visiting param " + name);
		}
		
		@Override
		public void visitLdcInsn(Object cst) {
			if (cst instanceof Integer) {
			    System.out.println("Loading int " + cst);
			} else if (cst instanceof Float) {
			    System.out.println("Loading float " + cst);
			} else if (cst instanceof Long) {
			    System.out.println("Loading long " + cst);
			} else if (cst instanceof Double) {
			    System.out.println("Loading double " + cst);
			} else if (cst instanceof String) {
			    System.out.println("Loading String \"" + cst + "\"");
			    if (cst.equals("a")) {
					super.visitLdcInsn("blub");
					return;
			    }
			} else if (cst instanceof Type) {
			    int sort = ((Type) cst).getSort();
			    if (sort == Type.OBJECT) {
				    System.out.println("Loading class " + BytecodeUtils.typeToJava(cst.toString()));
				} else if (sort == Type.ARRAY) {
				    System.out.println("Loading array " + cst);
				} else if (sort == Type.METHOD) {
				    System.out.println("Loading method " + cst);
				} else {
				    // throw an exception
			    }
			} else if (cst instanceof Handle) {
			    System.out.println("Loading handle " + cst);
			    // ...
			} else {
			    System.out.println("Loading Error");
			    // throw an exception
			}

			super.visitLdcInsn(cst);
		}
		
		@Override
		public void visitMethodInsn(int opcode, String owner, String name,
				String desc, boolean itf) {
			super.visitMethodInsn(opcode, owner, name, desc, itf);
		    System.out.println("Using " + BytecodeUtils.opcodeToString(opcode) + " to call method " + BytecodeUtils.typeToJava(owner, false) + "." + name + desc);
		}
		
		@Override
		public void visitCode() {
			super.visitCode();
			System.out.println(") {");
		}
		
		@Override
		public void visitLocalVariable(String name, String desc,
				String signature, Label start, Label end, int index) {
			super.visitLocalVariable(name, desc, signature, start, end, index);
			if (index < methodParams - 1) {
				System.out.print(BytecodeUtils.typeToJava(desc) + " " + name + ", ");
				if (desc.equals("J") || desc.equals("D"))
					methodParams++;		//Long and Double block 2 index.
			} else if (index < methodParams) {
				System.out.println(BytecodeUtils.typeToJava(desc) + " " + name + ") {");
			} else {
				System.out.println(indent + BytecodeUtils.typeToJava(desc) + " " + name + "; (Index: " + index + ")");
			}
		}
		
		@Override
		public void visitEnd() {
			System.out.println(indent.substring(4) + "}");
			super.visitEnd();
		}
	}
	
	public Decompiler(int api) {
		super(api);
	}
	public Decompiler(int api, ClassVisitor cw) {
		super(api, cw);
	}
	
	@Override
	public void visit(int version, int access, String name,
			String signature, String superName, String[] interfaces) {
		String[] classpath = name.split("/");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < classpath.length - 1; i++) {
			if (i != 0)
				sb.append(".");
			sb.append(classpath[i]);
		}
		System.out.println("package " + sb.toString() + ";");
		System.out.println();
		sb = new StringBuilder("");
		if ((access & Opcodes.ACC_PUBLIC) > 0)
			sb.append("public ");
		System.out.println(sb.toString() + "class " + classpath[classpath.length - 1] + " {");

		super.visit(version, access, name, signature, superName, interfaces);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		int containsThis = 0;
		if ((access & Opcodes.ACC_STATIC) == 0)
			containsThis++;
		String accessRight = BytecodeUtils.accessBytesToString(access);
		String returnVal = desc.split("\\)")[1];
		returnVal = BytecodeUtils.typeToJava(returnVal);
		String params = desc.split("\\)")[0];
		params = params.replaceFirst("\\(", "");
		String[] paramList = BytecodeUtils.typeListToJava(params);
		
		System.out.print("    " + accessRight + returnVal + " " + name + "(");
		
		return new MethodDecompiler(api, super.visitMethod(access, name, desc, signature, exceptions), 2, paramList.length + containsThis);
	}
	
	@Override
	public void visitEnd() {
		System.out.println("}");
		super.visitEnd();
	}
}
